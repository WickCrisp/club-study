<template>
  <div class="app-page">
    <el-button link :icon="Back" class="back" @click="$router.back()">返回</el-button>

    <el-row v-if="club.id" :gutter="16">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never" class="panel">
          <div class="club-head">
            <div class="logo" :style="{ background: coverColor(club.id) }">{{ (club.name || '社').slice(0, 1) }}</div>
            <div class="head-text">
              <div class="title-row">
                <h1>{{ club.name }}</h1>
                <el-tag :type="club.status === 'ACTIVE' ? 'success' : 'info'" effect="light" round>
                  {{ club.status === 'ACTIVE' ? '活跃' : '已解散' }}
                </el-tag>
              </div>
              <p class="sub">
                {{ club.categoryName || '未分类' }} · 负责人 {{ club.leaderName || '未指定' }}
              </p>
            </div>
          </div>

          <p class="desc">{{ club.description || '这个社团还没有填写简介。' }}</p>

          <div class="actions">
            <el-button
              v-if="!myMember"
              type="primary"
              size="large"
              :loading="acting"
              @click="handleApply"
            >申请加入</el-button>

            <el-button
              v-else-if="myMember.status === 'APPROVED'"
              type="warning"
              size="large"
              @click="handleRequestQuit"
            >申请退出社团</el-button>

            <el-button v-else-if="myMember.status === 'PENDING'" size="large" disabled>入社申请审核中…</el-button>
            <el-button v-else-if="myMember.status === 'QUIT_PENDING'" size="large" disabled>退出申请审核中…</el-button>
            <el-button v-else-if="myMember.status === 'REJECTED'" type="primary" size="large" @click="handleApply">
              重新申请加入
            </el-button>
          </div>
        </el-card>

        <el-card shadow="never" class="panel">
          <template #header>
            <div class="side-head">
              <span class="side-title">社团活动</span>
              <span class="side-sub">{{ activities.length }} 个</span>
            </div>
          </template>

          <div v-if="activities.length" class="act-list">
            <div v-for="a in activities" :key="a.id" class="act-item" @click="$router.push('/activity/detail/' + a.id)">
              <div class="act-left">
                <div class="act-title">{{ a.title }}</div>
                <div class="act-meta">{{ formatTime(a.startTime) }} · {{ a.location || '地点待定' }}</div>
              </div>
              <el-tag :type="statusType(a.status)" size="small" effect="light" round>{{ statusText(a.status) }}</el-tag>
            </div>
          </div>
          <el-empty v-else description="这个社团还没有发布活动" :image-size="70" />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="panel">
          <template #header>
            <div class="side-head">
              <span class="side-title">成员列表</span>
              <span class="side-sub">{{ membersLocked ? '—' : approvedMembers.length + ' 人' }}</span>
            </div>
          </template>

          <div v-if="membersLocked" class="locked-box">
            <el-icon><Lock /></el-icon>
            <p>成员名单仅登录可见</p>
            <el-button type="primary" size="small" @click="$router.push('/login')">去登录</el-button>
          </div>
          <div v-else-if="approvedMembers.length" class="member-list">
            <div v-for="m in approvedMembers" :key="m.id" class="member-item">
              <div class="member-left">
                <span class="avatar">{{ (m.userName || '?').slice(0, 1) }}</span>
                <div>
                  <div class="member-name">{{ m.userName }}</div>
                  <div class="member-no">{{ m.studentNo }}</div>
                </div>
              </div>
              <el-tag :type="roleType(m.role)" size="small" effect="plain" round>{{ roleText(m.role) }}</el-tag>
            </div>
          </div>
          <el-empty v-else description="暂无成员" :image-size="70" />
        </el-card>
      </el-col>
    </el-row>

    <el-skeleton v-else :rows="6" animated />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Back, Lock } from '@element-plus/icons-vue'
import api from '../api'
import { getToken } from '../utils/auth'

const route = useRoute()
const router = useRouter()

const club = ref({})
const activities = ref([])
const members = ref([])
const membersLocked = ref(false)
const myMember = ref(null)
const acting = ref(false)

const palette = ['#1f2d3d', '#2c3e50', '#33415c', '#3d3b5e', '#2f4858', '#443c4a']
const coverColor = (id) => palette[(id || 0) % palette.length]

// 成员列表只展示审批通过的
const approvedMembers = computed(() => members.value.filter(m => m.status === 'APPROVED'))

const formatTime = (t) => (t ? t.substring(5, 16).replace('T', ' ') : '—')
const roleText = (r) => ({ LEADER: '负责人', VICE_LEADER: '副负责人', MEMBER: '成员' }[r] || r)
const roleType = (r) => ({ LEADER: 'danger', VICE_LEADER: 'warning', MEMBER: 'info' }[r] || 'info')
const statusText = (s) => ({ DRAFT: '草稿', PENDING: '待审核', REJECTED: '已驳回', ONGOING: '报名中', FINISHED: '已结束', CANCELLED: '已取消' }[s] || s)
const statusType = (s) => ({ PENDING: 'warning', REJECTED: 'danger', ONGOING: 'success', FINISHED: 'info', CANCELLED: 'info' }[s] || 'info')

const loadClub = async () => {
  try {
    const result = await api.get(`/club/${route.params.id}`)
    if (result.code === 200) club.value = result.data || {}
  } catch (e) {
    ElMessage.error('加载社团失败')
  }
}

const loadActivities = async () => {
  try {
    const result = await api.get(`/activity/club/${route.params.id}`)
    if (result.code === 200) activities.value = result.data || []
  } catch (e) {
    activities.value = []
  }
}

const loadMembers = async () => {
  // 成员名单含真实姓名与学号，匿名不发请求、直接显示占位
  if (!getToken()) {
    membersLocked.value = true
    return
  }
  try {
    const result = await api.get(`/club-member/club/${route.params.id}`, { skipAuthRedirect: true })
    if (result.code === 200) {
      members.value = result.data || []
      membersLocked.value = false
    }
  } catch (e) {
    // 令牌过期等情形按未登录处理，避免整页跳转打断匿名浏览
    membersLocked.value = true
    members.value = []
  }
}

const loadMyMember = async () => {
  try {
    const result = await api.get('/club-member/my', { skipAuthRedirect: true })
    if (result.code === 200) {
      myMember.value = (result.data || []).find(m => m.clubId === Number(route.params.id)) || null
    }
  } catch (e) {
    myMember.value = null
  }
}

const handleApply = async () => {
  acting.value = true
  try {
    const result = await api.post(`/club-member/apply?clubId=${route.params.id}`)
    if (result.code === 200) {
      ElMessage.success('申请已提交，等待负责人审批')
      await loadMyMember()
    } else {
      ElMessage.error(result.message || '申请失败')
    }
  } catch (e) {
    ElMessage.error('请先登录再申请')
  } finally {
    acting.value = false
  }
}

const handleRequestQuit = async () => {
  try {
    await ElMessageBox.confirm(
      `确定申请退出「${club.value.name}」吗？需要负责人或管理员审批。`,
      '申请退出社团',
      { type: 'warning', confirmButtonText: '提交申请', cancelButtonText: '再想想' }
    )
  } catch (e) {
    return
  }

  try {
    const result = await api.post(`/club-member/request-quit/${myMember.value.id}`)
    if (result.code === 200) {
      ElMessage.success('退出申请已提交')
      await loadMyMember()
    } else {
      ElMessage.error(result.message || '提交失败')
    }
  } catch (e) {
    ElMessage.error('提交失败')
  }
}

onMounted(() => {
  loadClub()
  loadActivities()
  loadMembers()
  loadMyMember()
})
</script>

<style scoped>
.back {
  margin-bottom: 12px;
  color: #4a5568;
}

.panel {
  border-radius: 10px;
  margin-bottom: 16px;
}

.club-head {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.logo {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: none;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-row h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #1a1a2e;
}

.sub {
  margin: 6px 0 0;
  font-size: 13px;
  color: #8a94a6;
}

.desc {
  margin: 0 0 22px;
  font-size: 13px;
  line-height: 1.9;
  color: #4a5568;
  white-space: pre-wrap;
}

.actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.side-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.side-title {
  font-size: 15px;
  font-weight: 500;
  color: #1f2d3d;
}

.side-sub {
  font-size: 12px;
  color: #8a94a6;
}

.act-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 6px;
  border-bottom: 1px solid #f2f4f7;
  cursor: pointer;
}

.act-item:last-child {
  border-bottom: none;
}

.act-item:hover {
  background: #fafbfc;
}

.act-title {
  font-size: 14px;
  color: #1f2d3d;
}

.act-meta {
  margin-top: 2px;
  font-size: 12px;
  color: #8a94a6;
}

.member-list {
  max-height: 460px;
  overflow-y: auto;
}

/* 未登录时的名单占位：说明原因并给出登录入口 */
.locked-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 34px 0 26px;
}

.locked-box .el-icon {
  font-size: 26px;
  color: #a8abb2;
}

.locked-box p {
  margin: 0;
  font-size: 13px;
  color: #8a94a6;
}

.member-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 9px 2px;
  border-bottom: 1px solid #f2f4f7;
}

.member-item:last-child {
  border-bottom: none;
}

.member-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #eef1f6;
  color: #1a1a2e;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: none;
}

.member-name {
  font-size: 13px;
  color: #1f2d3d;
}

.member-no {
  font-size: 11px;
  color: #a8abb2;
}
</style>

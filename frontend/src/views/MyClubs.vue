<template>
  <div class="my-clubs-page">
    <div class="page-head">
      <div>
        <h2>我参加的社团</h2>
        <p>查看已加入的社团与退出申请进度</p>
      </div>
      <el-tag v-if="myClubs.length" type="info" effect="plain" round>共 {{ myClubs.length }} 个</el-tag>
    </div>

    <el-row v-loading="loading" :gutter="16">
      <el-col v-for="m in myClubs" :key="m.id" :xs="24" :sm="12" :lg="8" class="col">
        <el-card shadow="hover" class="my-card">
          <div class="title-row">
            <h3>{{ m.clubName }}</h3>
            <el-tag :type="roleType(m.role)" size="small" effect="plain" round>{{ roleText(m.role) }}</el-tag>
          </div>

          <div class="meta-row">
            <el-tag :type="m.status === 'APPROVED' ? 'success' : 'warning'" size="small" effect="light">
              {{ m.status === 'APPROVED' ? '已加入' : '退出审核中' }}
            </el-tag>
            <span v-if="m.joinedAt" class="time">加入于 {{ formatDate(m.joinedAt) }}</span>
          </div>

          <div class="actions">
            <el-button @click="viewClub(m.clubId)">查看社团</el-button>
            <el-button v-if="m.status === 'APPROVED'" type="warning" plain @click="requestQuit(m)">
              申请退出
            </el-button>
            <el-button v-else disabled>等待审批</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && myClubs.length === 0" description="还没有加入任何社团">
      <el-button type="primary" @click="$router.push('/club/list')">去浏览社团</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const router = useRouter()
const myClubs = ref([])
const loading = ref(false)

const roleText = (r) => ({ LEADER: '负责人', VICE_LEADER: '副负责人', MEMBER: '成员' }[r] || r)
const roleType = (r) => ({ LEADER: 'danger', VICE_LEADER: 'warning', MEMBER: 'info' }[r] || 'info')
const formatDate = (t) => (t ? t.substring(0, 10) : '')

const load = async () => {
  loading.value = true
  try {
    const result = await api.get('/club-member/my')
    if (result.code === 200) {
      // 只展示已加入和正在退出审批的记录，被拒/已退出的不在这里出现
      myClubs.value = (result.data || []).filter(
        m => m.status === 'APPROVED' || m.status === 'QUIT_PENDING'
      )
    } else {
      ElMessage.error(result.message || '加载失败')
    }
  } catch (e) {
    ElMessage.error('加载我的社团失败')
  } finally {
    loading.value = false
  }
}

const viewClub = (id) => router.push(`/club/detail/${id}`)

const requestQuit = async (member) => {
  try {
    await ElMessageBox.confirm(
      `确定申请退出「${member.clubName}」吗？退出后需要负责人或管理员审批。`,
      '申请退出社团',
      { type: 'warning', confirmButtonText: '提交申请', cancelButtonText: '再想想' }
    )
  } catch (e) {
    return
  }

  try {
    const result = await api.post(`/club-member/request-quit/${member.id}`)
    if (result.code === 200) {
      ElMessage.success('退出申请已提交')
      load()
    } else {
      ElMessage.error(result.message || '提交失败')
    }
  } catch (e) {
    ElMessage.error('提交失败')
  }
}

onMounted(load)
</script>

<style scoped>
.my-clubs-page {
  max-width: 1180px;
  margin: 0 auto;
  padding: 24px 16px 40px;
}

.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.page-head h2 {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 600;
  color: #1a1a2e;
}

.page-head p {
  margin: 0;
  font-size: 13px;
  color: #8a94a6;
}

.col {
  margin-bottom: 16px;
}

.my-card {
  border-radius: 10px;
}

.title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;
}

.title-row h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #1f2d3d;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.time {
  font-size: 12px;
  color: #8a94a6;
}

.actions {
  display: flex;
  gap: 8px;
  border-top: 1px solid #f2f4f7;
  padding-top: 12px;
}

.actions .el-button {
  margin-left: 0;
}
</style>

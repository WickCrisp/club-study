<template>
  <div class="club-page">
    <div class="page-head">
      <div>
        <h2>社团浏览</h2>
        <p>共 {{ clubs.length }} 个社团，卡片上的图文就是他们最新的社团风采</p>
      </div>
      <div class="head-right">
        <el-input
          v-model="keyword"
          placeholder="搜索社团名称"
          clearable
          style="width: 240px"
          @keyup.enter="doSearch"
          @clear="loadClubs"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" :icon="Search" @click="doSearch">搜索</el-button>
      </div>
    </div>

    <el-row v-loading="loading" :gutter="16">
      <el-col v-for="club in clubs" :key="club.id" :xs="24" :sm="12" :lg="8" class="col">
        <el-card shadow="hover" :body-style="{ padding: '0' }" class="club-card" @click="viewClub(club.id)">
          <div class="cover" :style="{ background: coverColor(club.id) }">
            <img
              v-if="latest(club.id) && latest(club.id).coverUrl"
              :src="latest(club.id).coverUrl"
              class="cover-img"
              :alt="club.name"
            />
            <span v-else class="cover-char">{{ (club.name || '社').slice(0, 1) }}</span>

            <span class="status-dot" :class="club.status === 'ACTIVE' ? 'on' : 'off'">
              {{ club.status === 'ACTIVE' ? '活跃' : '已解散' }}
            </span>
            <span v-if="countOf(club.id)" class="showcase-count">风采 {{ countOf(club.id) }}</span>
          </div>

          <div class="card-body">
            <div class="title-row">
              <h3>{{ club.name }}</h3>
              <span v-if="club.categoryName" class="category">{{ club.categoryName }}</span>
            </div>

            <p class="desc">{{ club.description || '这个社团还没有填写简介' }}</p>

            <div v-if="latest(club.id)" class="showcase" @click.stop="openShowcase(club)">
              <el-icon class="sc-icon"><Picture /></el-icon>
              <span class="sc-title">{{ latest(club.id).title }}</span>
              <span class="sc-date">{{ formatDate(latest(club.id).createdAt) }}</span>
            </div>
            <div v-else class="showcase empty">
              <el-icon class="sc-icon"><Picture /></el-icon>
              <span class="sc-title">还没有发布风采</span>
            </div>

            <div class="card-footer">
              <span class="leader">负责人 {{ club.leaderName || '未指定' }}</span>
              <el-button link type="primary" :disabled="!countOf(club.id)" @click.stop="openShowcase(club)">
                查看风采
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && clubs.length === 0" description="没有找到匹配的社团" />

    <el-dialog
      v-model="showcaseVisible"
      :title="showcaseClub ? showcaseClub.name + ' · 社团风采' : '社团风采'"
      width="620px"
      align-center
    >
      <div class="sc-list">
        <div v-for="s in currentShowcases" :key="s.id" class="sc-item">
          <img v-if="s.coverUrl" :src="s.coverUrl" class="sc-img" :alt="s.title" />
          <div class="sc-body">
            <h4>{{ s.title }}</h4>
            <p>{{ s.content }}</p>
            <span class="sc-time">{{ formatDate(s.createdAt) }}</span>
          </div>
        </div>
        <el-empty v-if="currentShowcases.length === 0" description="这个社团还没有发布风采" :image-size="80" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Picture } from '@element-plus/icons-vue'
import api from '../api'

const router = useRouter()

const clubs = ref([])
const showcases = ref([])
const keyword = ref('')
const loading = ref(false)

const showcaseVisible = ref(false)
const showcaseClub = ref(null)

// 没封面时用固定色板兜底，保证一屏卡片不会花
const palette = ['#1f2d3d', '#2c3e50', '#33415c', '#3d3b5e', '#2f4858', '#443c4a']
const coverColor = (id) => palette[(id || 0) % palette.length]

const showcaseMap = computed(() => {
  const map = {}
  showcases.value.forEach(s => {
    if (!s.clubId) return
    if (!map[s.clubId]) map[s.clubId] = []
    map[s.clubId].push(s)
  })
  Object.keys(map).forEach(k => {
    map[k].sort((a, b) => (b.createdAt || '').localeCompare(a.createdAt || ''))
  })
  return map
})

const countOf = (clubId) => (showcaseMap.value[clubId] || []).length
const latest = (clubId) => (showcaseMap.value[clubId] || [])[0] || null
const currentShowcases = computed(() => (showcaseClub.value ? showcaseMap.value[showcaseClub.value.id] || [] : []))

const formatDate = (t) => (t ? t.substring(0, 10) : '')

const loadClubs = async () => {
  loading.value = true
  try {
    const result = await api.get('/club/list')
    if (result.code === 200) {
      clubs.value = result.data || []
      keyword.value = ''
    }
  } catch (e) {
    ElMessage.error('加载社团列表失败')
  } finally {
    loading.value = false
  }
}

const loadShowcases = async () => {
  try {
    const result = await api.get('/club-showcase/list')
    if (result.code === 200) showcases.value = result.data || []
  } catch (e) {
    showcases.value = []
  }
}

const doSearch = async () => {
  const kw = keyword.value.trim()
  if (!kw) return loadClubs()
  loading.value = true
  try {
    const result = await api.get(`/club/search?keyword=${encodeURIComponent(kw)}`)
    if (result.code === 200) clubs.value = result.data || []
    else ElMessage.error(result.message || '搜索失败')
  } catch (e) {
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

const viewClub = (id) => router.push(`/club/detail/${id}`)

const openShowcase = (club) => {
  if (!countOf(club.id)) return
  showcaseClub.value = club
  showcaseVisible.value = true
}

onMounted(() => {
  loadClubs()
  loadShowcases()
})
</script>

<style scoped>
.club-page {
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
  flex-wrap: wrap;
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

.head-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.col {
  margin-bottom: 16px;
}

.club-card {
  cursor: pointer;
  overflow: hidden;
  transition: transform 0.18s, box-shadow 0.18s;
}

.club-card:hover {
  transform: translateY(-2px);
}

.cover {
  position: relative;
  height: 132px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.cover-char {
  font-size: 40px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
  letter-spacing: 2px;
}

.status-dot {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 1px 8px;
  border-radius: 10px;
  font-size: 11px;
  color: #ffffff;
  background: rgba(0, 0, 0, 0.45);
}

.status-dot.off {
  background: rgba(0, 0, 0, 0.6);
}

.showcase-count {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 1px 8px;
  border-radius: 10px;
  font-size: 11px;
  color: #ffffff;
  background: rgba(0, 0, 0, 0.45);
}

.card-body {
  padding: 14px 16px 12px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-row h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 500;
  color: #1f2d3d;
}

.category {
  font-size: 11px;
  color: #8a94a6;
  background: #f2f4f7;
  border-radius: 4px;
  padding: 1px 6px;
}

.desc {
  margin: 8px 0 10px;
  font-size: 12px;
  line-height: 1.6;
  color: #8a94a6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 38px;
}

.showcase {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 8px;
  border-radius: 6px;
  background: #f7f9fc;
  font-size: 12px;
  color: #4a5568;
  margin-bottom: 10px;
}

.showcase:not(.empty):hover {
  background: #eef2f7;
}

.showcase.empty {
  color: #b6bfcc;
}

.sc-icon {
  color: #8a94a6;
  flex: none;
}

.sc-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sc-date {
  color: #b6bfcc;
  flex: none;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-top: 1px solid #f2f4f7;
  padding-top: 10px;
}

.leader {
  font-size: 12px;
  color: #8a94a6;
}

.sc-list {
  max-height: 460px;
  overflow-y: auto;
}

.sc-item {
  display: flex;
  gap: 14px;
  padding: 14px 0;
  border-bottom: 1px solid #f2f4f7;
}

.sc-item:last-child {
  border-bottom: none;
}

.sc-img {
  width: 140px;
  height: 96px;
  object-fit: cover;
  border-radius: 6px;
  flex: none;
}

.sc-body h4 {
  margin: 0 0 6px;
  font-size: 14px;
  font-weight: 500;
  color: #1f2d3d;
}

.sc-body p {
  margin: 0 0 8px;
  font-size: 12px;
  line-height: 1.6;
  color: #6b7688;
}

.sc-time {
  font-size: 12px;
  color: #b6bfcc;
}
</style>

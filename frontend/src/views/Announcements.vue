<template>
  <div class="app-page notice-page">
    <div class="page-head">
      <div>
        <h2>校园公告</h2>
        <p>全校性通知，由管理员发布</p>
      </div>
    </div>

    <div v-loading="loading">
      <el-card v-for="a in announcements" :key="a.id" shadow="never" class="notice-card">
        <div class="notice-head">
          <div class="notice-title">
            <el-tag v-if="a.pinned" type="danger" size="small" effect="plain" round>置顶</el-tag>
            <h3>{{ a.title }}</h3>
          </div>
          <span class="time">{{ formatTime(a.publishedAt || a.createdAt) }}</span>
        </div>

        <p
          :ref="el => setContentRef(el, a.id)"
          class="content"
          :class="{ clamped: !expanded[a.id] }"
        >{{ a.content }}</p>

        <div class="notice-foot">
          <el-button v-if="overflow[a.id]" link type="primary" @click="toggle(a.id)">
            {{ expanded[a.id] ? '收起' : '展开全文' }}
          </el-button>
          <span v-else class="spacer"></span>
          <span class="publisher">发布人：{{ a.publisherName || '管理员' }}</span>
        </div>
      </el-card>

      <el-empty v-if="!loading && announcements.length === 0" description="暂无公告" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted } from 'vue'
import api from '../api'

const announcements = ref([])
const loading = ref(false)
const expanded = reactive({})
const overflow = reactive({})
const contentRefs = {}

// 只有真正被截断的公告才显示「展开全文」，短公告不该出现这个按钮
const setContentRef = (el, id) => {
  if (el) contentRefs[id] = el
}

const measureOverflow = () => {
  announcements.value.forEach(a => {
    const el = contentRefs[a.id]
    if (el) overflow[a.id] = el.scrollHeight > el.clientHeight + 1
  })
}

const formatTime = (t) => (t ? t.substring(0, 16).replace('T', ' ') : '—')
const toggle = (id) => { expanded[id] = !expanded[id] }

onMounted(async () => {
  loading.value = true
  try {
    const result = await api.get('/announcement/list')
    if (result.code === 200) {
      announcements.value = result.data || []
      await nextTick()
      measureOverflow()
    }
  } catch (e) {
    announcements.value = []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.notice-page {
  max-width: 900px;
}

.notice-card {
  border-radius: 10px;
  margin-bottom: 14px;
}

.notice-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 10px;
}

.notice-title {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.notice-title h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #1f2d3d;
}

.time {
  flex: none;
  font-size: 12px;
  color: #a8abb2;
}

.content {
  margin: 0 0 10px;
  font-size: 13px;
  line-height: 1.9;
  color: #4a5568;
  white-space: pre-wrap;
}

.content.clamped {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notice-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 10px;
  border-top: 1px solid #f2f4f7;
}

.spacer {
  display: inline-block;
}

.publisher {
  font-size: 12px;
  color: #a8abb2;
}
</style>

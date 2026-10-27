<template>
  <div class="user-manage">
    <div class="page-title">
      <div>
        <h2>用户管理</h2>
        <p>管理系统内的全部账号与角色，删除用户会级联清理其成员、报名与发布的活动</p>
      </div>
    </div>

    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="card-title">账号列表</span>
            <el-tag type="info" effect="plain" round>共 {{ users.length }} 人</el-tag>
          </div>
          <div class="header-right">
            <el-input
              v-model="keyword"
              placeholder="按姓名 / 学号搜索"
              clearable
              style="width: 240px"
              @keyup.enter="search"
              @clear="load"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" :icon="Search" @click="search">搜索</el-button>
            <el-button :icon="Refresh" @click="reset">重置</el-button>
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="pageUsers"
        stripe
        border
        style="width: 100%"
        :header-cell-style="{ background: '#f7f9fc', color: '#4a5568', fontWeight: 600 }"
      >
        <el-table-column prop="studentNo" label="学号" width="170">
          <template #default="{ row }">
            <span class="mono">{{ row.studentNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" effect="light" round>
              {{ row.role === 'ADMIN' ? '管理员' : '学生' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="手机号" width="170">
          <template #default="{ row }">
            <span :class="{ muted: !row.phone }">{{ row.phone || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" min-width="150">
          <template #default="{ row }">
            <span :class="{ muted: !row.createdAt }">
              {{ row.createdAt ? row.createdAt.substring(0, 10) : '—' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" align="center">
          <template #default="{ row }">
            <el-button type="danger" link :icon="Delete" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="没有匹配的用户" :image-size="90" />
        </template>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="users.length"
          layout="total, sizes, prev, pager, next"
          background
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Delete } from '@element-plus/icons-vue'
import api from '../api'

const users = ref([])
const keyword = ref('')
const loading = ref(false)

const page = ref(1)
const pageSize = ref(10)

const pageUsers = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return users.value.slice(start, start + pageSize.value)
})

const load = async () => {
  loading.value = true
  try {
    const result = await api.get('/user/list')
    if (result.code === 200) {
      users.value = result.data || []
      page.value = 1
    } else {
      ElMessage.error(result.message || '加载用户失败')
    }
  } catch (e) {
    ElMessage.error('加载用户失败')
  } finally {
    loading.value = false
  }
}

const search = async () => {
  const kw = keyword.value.trim()
  if (!kw) return load()
  loading.value = true
  try {
    const result = await api.get(`/user/search?keyword=${encodeURIComponent(kw)}`)
    if (result.code === 200) {
      users.value = result.data || []
      page.value = 1
    } else {
      ElMessage.error(result.message || '搜索失败')
    }
  } catch (e) {
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

const reset = () => {
  keyword.value = ''
  load()
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除「${row.name}（${row.studentNo}）」吗？该操作会级联删除其成员、报名与发布的活动。`,
      '删除用户',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }
    )
  } catch (e) {
    return // 用户点了取消
  }

  try {
    const result = await api.delete(`/user/${row.id}`)
    if (result.code === 200) {
      ElMessage.success('删除成功')
      load()
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

onMounted(load)
</script>

<style scoped>
.user-manage {
  max-width: 1180px;
  margin: 0 auto;
  padding: 24px 16px 40px;
}

.page-title {
  margin-bottom: 18px;
}

.page-title h2 {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 600;
  color: #1a1a2e;
}

.page-title p {
  margin: 0;
  font-size: 13px;
  color: #8a94a6;
}

.table-card {
  border-radius: 10px;
  border-color: #e8ecf2;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2d3d;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.mono {
  font-family: Consolas, Monaco, monospace;
  color: #3a4252;
}

.muted {
  color: #b6bfcc;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>

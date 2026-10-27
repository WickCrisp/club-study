<template>
  <div class="manage-shell">
    <aside class="side">
      <div class="side-head">
        <div class="side-label">当前负责社团</div>
        <el-select
          v-if="managedClubs.length > 1"
          v-model="currentClubId"
          size="small"
          style="width: 100%"
        >
          <el-option v-for="c in managedClubs" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <div v-else class="club-name">{{ currentClub ? currentClub.name : '—' }}</div>
      </div>

      <el-menu :default-active="activePath" router class="side-menu">
        <el-menu-item-group title="概览">
          <el-menu-item index="/manage">
            <el-icon><Odometer /></el-icon><span>工作台</span>
          </el-menu-item>
        </el-menu-item-group>

        <el-menu-item-group title="成员">
          <el-menu-item index="/manage/approvals">
            <el-icon><Stamp /></el-icon><span>入社与退社审批</span>
          </el-menu-item>
        </el-menu-item-group>

        <el-menu-item-group title="活动">
          <el-menu-item index="/manage/activities">
            <el-icon><Calendar /></el-icon><span>活动管理</span>
          </el-menu-item>
          <el-menu-item index="/manage/activities/create">
            <el-icon><EditPen /></el-icon><span>发布活动</span>
          </el-menu-item>
          <el-menu-item index="/manage/registrations">
            <el-icon><Tickets /></el-icon><span>报名管理</span>
          </el-menu-item>
        </el-menu-item-group>

        <el-menu-item-group title="社团">
          <el-menu-item index="/manage/club">
            <el-icon><OfficeBuilding /></el-icon><span>社团信息</span>
          </el-menu-item>
          <el-menu-item index="/manage/analytics">
            <el-icon><DataAnalysis /></el-icon><span>数据统计</span>
          </el-menu-item>
        </el-menu-item-group>
      </el-menu>
    </aside>

    <section class="content">
      <router-view />
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import {
  Odometer, Stamp, Calendar, EditPen, Tickets, OfficeBuilding, DataAnalysis
} from '@element-plus/icons-vue'
import { useManagedClubs } from '../composables/useManagedClubs'

const route = useRoute()
const { managedClubs, currentClubId, currentClub, ensure } = useManagedClubs()

const activePath = computed(() => route.path)

onMounted(() => { ensure() })
</script>

<style scoped>
.manage-shell {
  max-width: 1240px;
  margin: 0 auto;
  padding: 24px 16px 48px;
  display: flex;
  align-items: flex-start;
  gap: 20px;
}

.side {
  width: 208px;
  flex: none;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 14px 0 14px;
  position: sticky;
  top: 76px;
}

.side-head {
  padding: 0 16px 12px;
  border-bottom: 1px solid #f2f4f7;
}

.side-label {
  margin-bottom: 6px;
  font-size: 11px;
  color: #a8abb2;
  letter-spacing: 0.5px;
}

.club-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2d3d;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.side-menu {
  border-right: none;
  background: transparent;
  --el-menu-item-height: 40px;
}

.side-menu :deep(.el-menu-item) {
  margin: 2px 8px;
  border-radius: 6px;
  font-size: 13px;
  color: #4a5568;
}

.side-menu :deep(.el-menu-item:hover) {
  background: #f4f6fa;
  color: #1a1a2e;
}

.side-menu :deep(.el-menu-item.is-active) {
  background: #eef1f6;
  color: #1a1a2e;
  font-weight: 500;
}

.side-menu :deep(.el-menu-item-group__title) {
  padding: 12px 16px 4px;
  font-size: 11px;
  color: #b6bfcc;
}

.content {
  flex: 1;
  min-width: 0;
}
</style>

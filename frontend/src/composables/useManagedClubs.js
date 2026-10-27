import { ref, computed } from 'vue'
import api from '../api'
import { getToken } from '../utils/auth'

/**
 * 当前用户负责的社团
 *
 * 负责人入口的所有页面都依赖"我是哪个社团的负责人"：
 * 顶栏入口是否显示、路由守卫是否放行、页面查哪个社团的数据，都用这里的结果。
 * 模块级单例，保证一次会话只请求一次。
 */
const managedClubs = ref([])
const loaded = ref(false)
const currentClubId = ref(null)
let inflight = null

const currentClub = computed(() =>
  managedClubs.value.find(c => c.id === currentClubId.value) || managedClubs.value[0] || null
)

const isLeader = computed(() => managedClubs.value.length > 0)

async function ensure(force = false) {
  if (!getToken()) {
    managedClubs.value = []
    loaded.value = true
    return []
  }
  if (loaded.value && !force) return managedClubs.value
  if (inflight) return inflight

  inflight = api.get('/club/my-managed', { skipAuthRedirect: true })
    .then(res => {
      managedClubs.value = res.code === 200 ? (res.data || []) : []
      loaded.value = true
      // 当前选中的社团失效时回到第一个
      if (!managedClubs.value.some(c => c.id === currentClubId.value)) {
        currentClubId.value = managedClubs.value[0] ? managedClubs.value[0].id : null
      }
      return managedClubs.value
    })
    .catch(() => {
      managedClubs.value = []
      loaded.value = true
      return []
    })
    .finally(() => { inflight = null })

  return inflight
}

function reset() {
  managedClubs.value = []
  loaded.value = false
  currentClubId.value = null
}

export function useManagedClubs() {
  return { managedClubs, loaded, currentClubId, currentClub, isLeader, ensure, reset }
}

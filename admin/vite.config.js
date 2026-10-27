import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
  // .env.production 里可设 VITE_ADMIN_BASE=/admin/（Docker 部署的子路径）；
  // 本地 npm run dev 走 development 模式，不加载该文件，仍是 '/'
  const env = loadEnv(mode, process.cwd(), '')

  return {
    base: env.VITE_ADMIN_BASE || '/',
    plugins: [vue()],
    server: {
      port: 5174,
      proxy: {
        '/api': {
          target: 'http://localhost:8081',
          changeOrigin: true
        }
      }
    }
  }
})

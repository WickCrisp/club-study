import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import './styles/element-theme.css'
import './styles/auth.css'
import './styles/page.css'
import './style.css'

const app = createApp(App)
app.use(router)
// 全量引入 Element Plus：样式排在 style.css 之前，便于项目样式覆盖
app.use(ElementPlus, { locale: zhCn })
app.mount('#app')

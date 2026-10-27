<template>
  <div class="app-page">
    <div class="page-head">
      <div>
        <h2>社团小助手</h2>
        <p>可以问活动、问名额、查自己的报名，也能帮你写一段活动宣传文案</p>
      </div>
      <el-tag type="info" effect="plain" round>AI 生成，仅供参考</el-tag>
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="14">
        <el-card shadow="never" class="panel chat-panel">
          <div class="chat">
            <div ref="scroller" class="chat-list">
              <div v-if="messages.length === 0" class="empty">
                <p class="empty-tip">试试这样问：</p>
                <div class="chips">
                  <el-button v-for="q in suggestions" :key="q" size="small" plain round @click="send(q)">
                    {{ q }}
                  </el-button>
                </div>
              </div>

              <div v-for="(m, i) in messages" :key="i" class="msg" :class="m.role">
                <div class="avatar" :class="m.role">{{ m.role === 'user' ? '我' : 'AI' }}</div>
                <div class="bubble">
                  <span v-if="m.content">{{ m.content }}</span>
                  <span v-else class="typing">正在思考…</span>
                </div>
              </div>
            </div>

            <div class="composer">
              <el-input
                v-model="draft"
                type="textarea"
                :rows="2"
                maxlength="500"
                resize="none"
                placeholder="输入问题，Enter 发送，Shift + Enter 换行"
                @keydown.enter.exact.prevent="send()"
              />
              <div class="composer-foot">
                <span class="tip">{{ draft.length }}/500 · 保留最近 5 轮上下文</span>
                <el-button type="primary" :disabled="sending || !draft.trim()" :loading="sending" @click="send()">
                  {{ sending ? '回答中' : '发送' }}
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="10">
        <el-card shadow="never" class="panel">
          <template #header><span class="panel-title">活动文案生成</span></template>

          <el-form label-position="top" size="default">
            <el-form-item label="活动名称" required>
              <el-input v-model="form.title" maxlength="60" placeholder="例如：新生篮球友谊赛" />
            </el-form-item>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="时间">
                  <el-input v-model="form.time" placeholder="10 月 30 日 15:00" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="地点">
                  <el-input v-model="form.place" placeholder="东区篮球场" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="面向对象">
                  <el-input v-model="form.audience" placeholder="全校学生" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="语气">
                  <el-select v-model="form.tone" style="width: 100%">
                    <el-option label="活泼" value="活泼" />
                    <el-option label="正式" value="正式" />
                    <el-option label="简洁" value="简洁" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-button
              type="primary"
              class="gen-btn"
              :disabled="copying || !form.title.trim()"
              :loading="copying"
              @click="generateCopy"
            >
              {{ copying ? '生成中' : '生成文案' }}
            </el-button>
          </el-form>

          <el-alert v-if="copyError" type="warning" :closable="false" show-icon class="copy-error" :title="copyError" />

          <div v-if="copy" class="result">
            <p class="intro">{{ copy.intro }}</p>

            <div v-if="copy.highlights && copy.highlights.length" class="block">
              <div class="block-title">活动亮点</div>
              <ul>
                <li v-for="(h, i) in copy.highlights" :key="'h' + i">{{ h }}</li>
              </ul>
            </div>

            <div v-if="copy.notes && copy.notes.length" class="block">
              <div class="block-title">报名须知</div>
              <ul>
                <li v-for="(n, i) in copy.notes" :key="'n' + i">{{ n }}</li>
              </ul>
            </div>

            <div class="result-foot">
              <el-button size="small" :icon="copied ? Check : CopyDocument" @click="copyText">
                {{ copied ? '已复制' : '复制全部' }}
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { nextTick, reactive, ref } from 'vue'
import { Check, CopyDocument } from '@element-plus/icons-vue'
import { getToken, clearToken } from '../utils/auth'

// SSE 与普通请求都绕过了 axios，需要手动带上令牌
const authHeaders = () => {
  const token = getToken()
  return token
    ? { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` }
    : { 'Content-Type': 'application/json' }
}

const messages = ref([])
const draft = ref('')
const sending = ref(false)
const scroller = ref(null)

const suggestions = [
  '最近有哪些活动可以报名？',
  '算法竞赛社是做什么的？',
  '我报名了哪些活动？'
]

const form = reactive({ title: '', time: '', place: '', audience: '', tone: '活泼' })
const copy = ref(null)
const copying = ref(false)
const copyError = ref('')
const copied = ref(false)

const scrollToBottom = async () => {
  await nextTick()
  if (scroller.value) scroller.value.scrollTop = scroller.value.scrollHeight
}

const history = () => messages.value
  .filter(m => m.content && (m.role === 'user' || m.role === 'assistant'))
  .slice(-10)
  .map(m => ({ role: m.role, content: m.content }))

const send = async (text) => {
  const question = (text || draft.value).trim()
  if (!question || sending.value) return

  const past = history()
  messages.value.push({ role: 'user', content: question })
  draft.value = ''
  const reply = reactive({ role: 'assistant', content: '' })
  messages.value.push(reply)
  sending.value = true
  await scrollToBottom()

  try {
    const res = await fetch('/api/assistant/chat', {
      method: 'POST',
      headers: authHeaders(),
      body: JSON.stringify({ message: question, history: past })
    })

    if (res.status === 401) {
      clearToken()
      window.location.href = '/login'
      return
    }
    if (!res.ok) {
      let msg = '助手暂时不可用，请稍后再试。'
      try {
        const body = await res.json()
        if (body && body.message) msg = body.message
      } catch (e) { /* 非 JSON 响应就用兜底文案 */ }
      reply.content = msg
      return
    }
    if (!res.body) {
      reply.content = '当前浏览器不支持流式输出，请换个浏览器试试。'
      return
    }

    const reader = res.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })

      const events = buffer.split('\n\n')
      buffer = events.pop() || ''
      for (const raw of events) {
        const data = raw.split('\n')
          .filter(line => line.startsWith('data:'))
          .map(line => line.slice(5).replace(/^ /, ''))
          .join('\n')
        if (data) {
          reply.content += data
          await scrollToBottom()
        }
      }
    }
  } catch (e) {
    reply.content = reply.content || '网络异常，请稍后再试。'
  } finally {
    if (!reply.content) reply.content = '这次没有拿到回答，请再问一次。'
    sending.value = false
    await scrollToBottom()
  }
}

const generateCopy = async () => {
  if (!form.title.trim() || copying.value) return
  copying.value = true
  copyError.value = ''
  copied.value = false
  try {
    const res = await fetch('/api/assistant/copy', {
      method: 'POST',
      headers: authHeaders(),
      body: JSON.stringify(form)
    })
    if (res.status === 401) {
      clearToken()
      window.location.href = '/login'
      return
    }
    const body = await res.json()
    if (body.code === 200) {
      copy.value = body.data
    } else {
      copyError.value = body.message || '生成失败，请稍后再试'
    }
  } catch (e) {
    copyError.value = '网络异常，请稍后再试'
  } finally {
    copying.value = false
  }
}

const copyText = async () => {
  if (!copy.value) return
  const parts = [copy.value.intro || '']
  if (copy.value.highlights?.length) parts.push('活动亮点：\n' + copy.value.highlights.map(h => '· ' + h).join('\n'))
  if (copy.value.notes?.length) parts.push('报名须知：\n' + copy.value.notes.map(n => '· ' + n).join('\n'))
  try {
    await navigator.clipboard.writeText(parts.join('\n\n'))
    copied.value = true
    setTimeout(() => { copied.value = false }, 2000)
  } catch (e) {
    copied.value = false
  }
}
</script>

<style scoped>
.panel {
  border-radius: 10px;
  margin-bottom: 16px;
}

.panel-title {
  font-size: 15px;
  font-weight: 500;
  color: #1f2d3d;
}

.chat-panel :deep(.el-card__body) {
  padding: 0;
}

.chat {
  display: flex;
  flex-direction: column;
  height: 560px;
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 18px 18px 6px;
}

.empty {
  padding: 24px 4px;
}

.empty-tip {
  margin: 0 0 12px;
  font-size: 13px;
  color: #8a94a6;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.msg {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.msg.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: none;
}

.avatar.user {
  background: #1a1a2e;
  color: #ffffff;
}

.avatar.assistant {
  background: #eef1f6;
  color: #1a1a2e;
}

.bubble {
  max-width: 78%;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.75;
  white-space: pre-wrap;
  word-break: break-word;
}

.msg.user .bubble {
  background: #1a1a2e;
  color: #ffffff;
  border-top-right-radius: 3px;
}

.msg.assistant .bubble {
  background: #f4f6fa;
  color: #1f2d3d;
  border-top-left-radius: 3px;
}

.typing {
  color: #8a94a6;
}

.composer {
  border-top: 1px solid #eef1f6;
  padding: 12px 16px 14px;
}

.composer-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
}

.tip {
  font-size: 12px;
  color: #a8abb2;
}

.gen-btn {
  width: 100%;
}

.copy-error {
  margin-top: 12px;
}

.result {
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px dashed #e5e7eb;
}

.intro {
  margin: 0 0 12px;
  font-size: 13px;
  line-height: 1.8;
  color: #1f2d3d;
}

.block {
  margin-bottom: 12px;
}

.block-title {
  margin-bottom: 4px;
  font-size: 12px;
  color: #8a94a6;
}

.block ul {
  margin: 0;
  padding-left: 18px;
  font-size: 13px;
  line-height: 1.8;
  color: #4a5568;
}

.result-foot {
  display: flex;
  justify-content: flex-end;
}
</style>

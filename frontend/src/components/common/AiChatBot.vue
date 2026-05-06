<template>
  <button class="ai-btn" @click="open = true">💬 AI助手</button>
  <div v-if="open" class="ai-panel">
    <div class="ai-top">
      <span>🤖 知行AI助手</span>
      <button @click="open = false">✕</button>
    </div>
    <div class="ai-body" ref="box">
      <div v-for="(m, i) in list" :key="i" :class="m.role">{{ m.text }}</div>
      <div v-if="wait" class="assistant">思考中...</div>
    </div>
    <div class="ai-bottom">
      <input v-model="txt" @keydown.enter="go" placeholder="输入问题..." />
      <button @click="go" :disabled="wait">发送</button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
const open = ref(false)
const txt = ref('')
const list = ref([])
const wait = ref(false)
const box = ref(null)

const scroll = async () => {
  await nextTick()
  if (box.value) box.value.scrollTop = box.value.scrollHeight
}

const go = async () => {
  const t = txt.value.trim()
  if (!t || wait.value) return
  list.value.push({ role: 'user', text: t })
  txt.value = ''
  wait.value = true
  await scroll()
  try {
    const res = await fetch('https://api.deepseek.com/chat/completions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer sk-0421479e732446ffb4aa6b4f4d6cb2f2'
      },
      body: JSON.stringify({
        model: 'deepseek-chat',
        messages: [
          { role: 'system', content: '你是知行创境平台的AI助手。' },
          ...list.value.map(m => ({ role: m.role, content: m.text }))
        ]
      })
    })
    const d = await res.json()
    list.value.push({ role: 'assistant', text: d.choices?.[0]?.message?.content || '无回复' })
  } catch {
    list.value.push({ role: 'assistant', text: '网络错误' })
  } finally {
    wait.value = false
    await scroll()
  }
}
</script>

<style scoped>
/* 浮动按钮 - 改成网站同款清爽蓝 */
.ai-btn {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 9999;
  background: #3b82f6;         /* 清爽蓝，和网站主色调一致 */
  color: white;
  border: none;
  border-radius: 50px;         /* 大圆角，和网站卡片风格统一 */
  padding: 12px 24px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3); /* 带点蓝色阴影更有质感 */
  transition: all 0.2s ease;
  letter-spacing: 0.5px;
}
.ai-btn:hover {
  background: #2563eb;         /* 深蓝色悬停效果 */
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
  transform: translateY(-1px); /* 悬停时微微上浮 */
}

/* 聊天窗口 - 更柔和的白色大圆角面板 */
.ai-panel {
  position: fixed;
  bottom: 140px;
  right: 20px;
  width: min(380px, calc(100vw - 40px));
  height: 460px;
  background: white;
  border-radius: 20px;         /* 加大圆角 */
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12); /* 更柔和的阴影 */
  display: flex;
  flex-direction: column;
  z-index: 9999;
  overflow: hidden;
  border: 1px solid #f1f5f9;   /* 加个极淡的边框增加层次 */
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  box-sizing: border-box;
}

/* 顶部标题栏 - 干净的白底黑字设计 */
.ai-top {
  background: white;
  color: #1e293b;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 15px;
  border-bottom: 1px solid #f1f5f9;
  border-radius: 20px 20px 0 0;
}
.ai-top span {
  display: flex;
  align-items: center;
  gap: 8px;
}
.ai-top button {
  flex-shrink: 0;
  min-width: 56px;
  padding: 10px 20px;
  background: none;
  border: none;
  color: #94a3b8;
  font-size: 18px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: all 0.2s;
}
.ai-top button:hover {
  background: #f1f5f9;
  color: #64748b;
}

/* 对话区 - 淡灰背景区分消息 */
.ai-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f8fafc;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.ai-body > div {
  padding: 10px 14px;
  border-radius: 14px;
  max-width: 85%;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
  margin-bottom: 2px;
}

/* 用户消息 - 蓝色气泡 */
.user {
  background: #3b82f6;
  color: white;
  align-self: flex-end;
  border-bottom-right-radius: 4px;
}

/* AI消息 - 白色气泡带边框 */
.assistant {
  background: white;
  color: #334155;
  align-self: flex-start;
  border: 1px solid #e2e8f0;
  border-bottom-left-radius: 4px;
}

/* 底部输入区 - 简洁设计 */
.ai-bottom {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  gap: 10px;
  border-top: 1px solid #f1f5f9;
  background: white;
  min-height: 52px;
}
.ai-bottom input {
  
  flex: 1 1 auto;
  min-width: 0;
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 20px;         /* 大圆角输入框 */
  font-size: 14px;
  outline: none;
  background: #f8fafc;
  transition: all 0.2s;
  box-sizing: border-box;
}
.ai-bottom input:focus {
  border-color: #3b82f6;
  background: white;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}
.ai-bottom button {
  flex-shrink: 0;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 20px;         /* 大圆角按钮 */
  padding: 10px 16px;
  min-width: 80px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  box-sizing: border-box;
}
.ai-bottom button:hover {
  background: #2563eb;
}
.ai-bottom button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: #94a3b8;
}
</style>

<template>
  <div class="fixed bottom-6 right-6 z-50" v-if="isClient">
    <!-- 浮动按钮 -->
    <Transition name="fade">
      <button
        v-if="!isOpen"
        @click="toggleChat"
        class="w-14 h-14 rounded-full bg-gradient-to-br from-blue-500 to-blue-600 shadow-lg hover:shadow-xl transform hover:scale-110 transition-all flex items-center justify-center text-white"
        aria-label="打开AI助手"
      >
        <svg class="w-7 h-7" fill="currentColor" viewBox="0 0 24 24">
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm3.5-9c.83 0 1.5-.67 1.5-1.5S16.33 8 15.5 8 14 8.67 14 9.5s.67 1.5 1.5 1.5zm-7 0c.83 0 1.5-.67 1.5-1.5S9.33 8 8.5 8 7 8.67 7 9.5 7.67 11 8.5 11zm3.5 6.5c2.33 0 4.31-1.46 5.11-3.5H6.89c.8 2.04 2.78 3.5 5.11 3.5z"></path>
        </svg>
      </button>
    </Transition>

    <!-- 聊天窗口 -->
    <Transition name="slide-up">
      <div
        v-if="isOpen"
        class="fixed bottom-6 right-6 w-96 h-600 bg-white rounded-lg shadow-2xl flex flex-col responsive-chat"
      >
        <!-- 头部 -->
        <div class="bg-gradient-to-r from-blue-500 to-blue-600 text-white px-6 py-4 rounded-t-lg flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="w-8 h-8 rounded-full bg-white bg-opacity-30 flex items-center justify-center">
              <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm3.5-9c.83 0 1.5-.67 1.5-1.5S16.33 8 15.5 8 14 8.67 14 9.5s.67 1.5 1.5 1.5zm-7 0c.83 0 1.5-.67 1.5-1.5S9.33 8 8.5 8 7 8.67 7 9.5 7.67 11 8.5 11zm3.5 6.5c2.33 0 4.31-1.46 5.11-3.5H6.89c.8 2.04 2.78 3.5 5.11 3.5z"></path>
              </svg>
            </div>
            <div>
              <h3 class="font-semibold text-lg">AI助手</h3>
              <p class="text-xs text-blue-100">Deepseek驱动</p>
            </div>
          </div>
          <button
            @click="toggleChat"
            class="text-white hover:bg-white hover:bg-opacity-20 rounded-full p-2 transition-colors"
            aria-label="关闭助手"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>

        <!-- 消息区域 -->
        <div class="flex-1 overflow-y-auto px-4 py-4 space-y-4 bg-gray-50" ref="messagesContainer">
          <div v-if="messages.length === 0" class="flex flex-col items-center justify-center h-full text-gray-400 py-8">
            <svg class="w-12 h-12 mb-3 opacity-50" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V5a2 2 0 012-2h14a2 2 0 012 2v9a2 2 0 01-2 2h-4l-4 4v-4z"></path>
            </svg>
            <p class="text-sm">有什么我可以帮你的吗？</p>
          </div>

          <div v-for="msg in messages" :key="`${msg.timestamp}-${msg.role}`" class="flex" :class="msg.role === 'user' ? 'justify-end' : 'justify-start'">
            <div
              class="max-w-xs lg:max-w-md px-4 py-2 rounded-lg"
              :class="msg.role === 'user'
                ? 'bg-blue-500 text-white rounded-br-none'
                : 'bg-gray-200 text-gray-800 rounded-bl-none'
              "
            >
              <p class="text-sm leading-relaxed break-words">{{ msg.content }}</p>
              <p class="text-xs mt-1" :class="msg.role === 'user' ? 'text-blue-100' : 'text-gray-500'">
                {{ formatTime(msg.timestamp) }}
              </p>
            </div>
          </div>

          <!-- 加载指示器 -->
          <div v-if="isLoading" class="flex items-center gap-2">
            <div class="w-8 h-8 rounded-lg bg-gray-200 flex items-center justify-center">
              <div class="flex gap-1">
                <div class="w-1.5 h-1.5 bg-gray-500 rounded-full animate-bounce"></div>
                <div class="w-1.5 h-1.5 bg-gray-500 rounded-full animate-bounce" style="animation-delay: 0.1s"></div>
                <div class="w-1.5 h-1.5 bg-gray-500 rounded-full animate-bounce" style="animation-delay: 0.2s"></div>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="border-t bg-white px-4 py-3 rounded-b-lg">
          <div class="flex gap-2">
            <input
              v-model="inputMessage"
              @keyup.enter="sendMessage"
              :disabled="isLoading"
              type="text"
              placeholder="输入你的问题..."
              class="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:opacity-50 disabled:cursor-not-allowed text-sm"
            />
            <button
              @click="sendMessage"
              :disabled="isLoading || !inputMessage.trim()"
              class="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              aria-label="发送消息"
            >
              <svg v-if="!isLoading" class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                <path d="M16.6915026,12.4744748 L3.50612381,13.2599618 C3.19218622,13.2599618 3.03521743,13.4170592 3.03521743,13.5741566 L1.15159189,20.0151496 C0.8376543,20.8006365 0.99,21.89 1.77946707,22.52 C2.41,22.99 3.50612381,23.1 4.13399899,22.8429026 L21.714504,14.0454487 C22.6563168,13.5741566 23.1272231,12.6315722 22.9702544,11.6889879 L4.13399899,1.16820535 C3.34915502,0.9111079 2.40734225,1.02638306 1.77946707,1.4976752 C0.994623095,2.13399899 0.837654326,3.22460564 1.15159189,3.95718654 L3.03521743,10.3981796 C3.03521743,10.5552769 3.19218622,10.7123744 3.50612381,10.7123744 L16.6915026,11.4978612 C16.6915026,11.4978612 17.1624089,11.4978612 17.1624089,11.0265691 L17.1624089,12.6315722 C17.1624089,12.6315722 17.1624089,12.4744748 16.6915026,12.4744748 Z"></path>
              </svg>
              <span v-else class="inline-block">
                <svg class="w-5 h-5 animate-spin" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
              </span>
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { sendChatMessage, createConversation } from '@/api/modules/chat'
import type { ChatMessage } from '@/api/modules/chat'

const isClient = ref(false)
const isOpen = ref(false)
const isLoading = ref(false)
const inputMessage = ref('')
const messages = ref<ChatMessage[]>([])
const conversationId = ref<string>('')
const messagesContainer = ref<HTMLElement>()

onMounted(() => {
  isClient.value = true
  // 创建新对话
  initializeConversation()
})

async function initializeConversation() {
  try {
    const response = await createConversation()
    if (response.data.data) {
      conversationId.value = response.data.data.conversationId
    }
  } catch (error) {
    console.error('Failed to create conversation:', error)
  }
}

async function sendMessage() {
  if (!inputMessage.value.trim() || isLoading.value) {
    return
  }

  const userMessage: ChatMessage = {
    content: inputMessage.value,
    role: 'user',
    timestamp: Date.now(),
  }

  messages.value.push(userMessage)
  inputMessage.value = ''
  isLoading.value = true

  await scrollToBottom()

  try {
    const response = await sendChatMessage({
      content: userMessage.content,
      conversationId: conversationId.value,
    })

    if (response.data.data?.message) {
      const assistantMessage = response.data.data.message
      assistantMessage.timestamp = Date.now()
      messages.value.push(assistantMessage)
    }
  } catch (error) {
    console.error('Failed to send message:', error)
    const errorMessage: ChatMessage = {
      content: '抱歉，发送失败，请重试。',
      role: 'assistant',
      timestamp: Date.now(),
    }
    messages.value.push(errorMessage)
  } finally {
    isLoading.value = false
    await scrollToBottom()
  }
}

async function scrollToBottom() {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

function toggleChat() {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    nextTick(() => scrollToBottom())
  }
}

function formatTime(timestamp?: number): string {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}
</script>

<style scoped>
.responsive-chat {
  @media (max-width: 640px) {
    position: fixed !important;
    bottom: 0 !important;
    right: 0 !important;
    left: 0 !important;
    width: 100% !important;
    height: 100% !important;
    max-height: 100vh !important;
    border-radius: 0 !important;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from {
  transform: translateY(20px);
  opacity: 0;
}

.slide-up-leave-to {
  transform: translateY(20px);
  opacity: 0;
}

h-600 {
  height: 600px;
}
</style>

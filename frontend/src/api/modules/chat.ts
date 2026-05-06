import { http } from '@/utils/request'
import type { ApiEnvelope, PageResponse } from '@/api/types'

/**
 * 聊天消息类型
 */
export interface ChatMessage {
  id?: string
  content: string
  role: 'user' | 'assistant'
  timestamp?: number
  conversationId?: string
}

/**
 * 聊天接口响应
 */
export interface ChatResponse {
  message: ChatMessage
  conversationId: string
}

/**
 * 发送聊天消息
 */
export const sendChatMessage = (data: {
  content: string
  conversationId?: string
}) => {
  return http.post<ApiEnvelope<ChatResponse>>('/api/chat/message', data)
}

/**
 * 获取对话历史
 */
export const getChatHistory = (conversationId: string) => {
  return http.get<ApiEnvelope<ChatMessage[]>>(`/api/chat/history/${conversationId}`)
}

/**
 * 创建新对话
 */
export const createConversation = () => {
  return http.post<ApiEnvelope<{ conversationId: string }>>('/api/chat/conversation', {})
}

/**
 * 删除对话
 */
export const deleteConversation = (conversationId: string) => {
  return http.delete<ApiEnvelope<void>>(`/api/chat/conversation/${conversationId}`)
}

/**
 * 获取对话列表
 */
export const getConversationList = () => {
  return http.get<ApiEnvelope<Array<{
    id: string
    title: string
    createdAt: number
    messageCount: number
  }>>>('/api/chat/conversations')
}

import { http } from '@/utils/request'
import type {
  CreateSubmissionRequest,
  CreateTaskRequest,
  ExhibitionSummary,
  PageResponse,
  SubmissionDetail,
  TaskDetail,
  TaskProgress,
  TaskSummary,
  UpdateTaskRequest,
} from '@/api/types'

export function createTask(payload: CreateTaskRequest) {
  return http.post<TaskDetail>('/tasks', payload)
}

export function updateTask(taskId: number, payload: UpdateTaskRequest) {
  return http.put<void>(`/tasks/${taskId}`, payload)
}

export function getTaskList(params?: Record<string, unknown>) {
  return http.get<PageResponse<TaskSummary>>('/tasks', { params })
}

export function getTaskDetail(taskId: number) {
  return http.get<TaskDetail>(`/tasks/${taskId}`)
}

export function getTaskExcellentExhibitions(taskId: number) {
  return http.get<ExhibitionSummary[]>(`/tasks/${taskId}/excellent-exhibitions`)
}

export function getTaskProgress(taskId: number) {
  return http.get<TaskProgress>(`/tasks/${taskId}/progress`)
}

export function submitTaskWork(taskId: number, payload: CreateSubmissionRequest) {
  return http.post<SubmissionDetail>(`/tasks/${taskId}/submit`, payload)
}

export function getTaskSubmissions(taskId: number) {
  return http.get<SubmissionDetail[]>(`/tasks/${taskId}/submissions`)
}

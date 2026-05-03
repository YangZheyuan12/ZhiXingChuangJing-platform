import { http } from '@/utils/request'
import type {
  ApproveSubmissionRequest,
  CreateSubmissionReviewRequest,
  ReturnSubmissionRequest,
  SubmissionDetail,
  SubmissionReview,
} from '@/api/types'

export function getSubmissionDetail(submissionId: number) {
  return http.get<SubmissionDetail>(`/submissions/${submissionId}`)
}

export function createSubmissionReview(submissionId: number, payload: CreateSubmissionReviewRequest) {
  return http.post<SubmissionReview>(`/submissions/${submissionId}/reviews`, payload)
}

export function approveSubmission(submissionId: number, payload: ApproveSubmissionRequest) {
  return http.post<void>(`/submissions/${submissionId}/approve`, payload)
}

export function returnSubmission(submissionId: number, payload: ReturnSubmissionRequest) {
  return http.post<void>(`/submissions/${submissionId}/return`, payload)
}

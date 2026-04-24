import { http } from '@/utils/request'
import type { CreateSubmissionReviewRequest, SubmissionDetail, SubmissionReview } from '@/api/types'

export function getSubmissionDetail(submissionId: number) {
  return http.get<SubmissionDetail>(`/submissions/${submissionId}`)
}

export function createSubmissionReview(submissionId: number, payload: CreateSubmissionReviewRequest) {
  return http.post<SubmissionReview>(`/submissions/${submissionId}/reviews`, payload)
}

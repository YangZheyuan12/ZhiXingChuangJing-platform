export interface ApiEnvelope<T> {
  code: number
  message: string
  data: T
  requestId: string
}

export interface PageResponse<T> {
  list: T[]
  page: number
  pageSize: number
  total: number
}

export interface LoginRequest {
  account: string
  password: string
}

export interface CaptchaResponse {
  captchaId: string
  imageData: string
  expiresInSeconds: number
}

export interface LoginUser {
  id: number
  role: string
  nickname: string
}

export interface LoginResponse {
  token: string
  refreshToken: string
  expiresIn: number
  user: LoginUser
}

export interface RegisterRequest {
  account: string
  password: string
  captchaId: string
  captchaCode: string
}

export interface RegisterResponse {
  userId: number
  account: string
  role: string
}

export interface SchoolInfo {
  id: number
  name: string
}

export interface ClassInfo {
  id: number
  name: string
  grade?: string | null
  academicYear?: string | null
}

export interface SimpleUser {
  id: number
  role: string
  name: string
  nickname: string
  avatarUrl?: string | null
}

export interface CurrentUser {
  id: number
  role: string
  realName: string
  nickname: string
  avatarUrl?: string | null
  schoolId?: number | null
}

export interface UserProfile {
  id: number
  role: string
  realName: string
  nickname: string
  avatarUrl?: string | null
  email?: string | null
  mobile?: string | null
  bio?: string | null
  school?: SchoolInfo | null
  classInfo?: ClassInfo | null
}

export interface UpdateProfileRequest {
  nickname?: string | null
  avatarUrl?: string | null
  bio?: string | null
}

export interface UpdatePasswordRequest {
  oldPassword: string
  newPassword: string
}

export interface Announcement {
  id: number
  classId: number
  title: string
  content: string
  pinned: boolean
  publisher: SimpleUser
  publishedAt: string
}

export interface CreateAnnouncementRequest {
  title: string
  content: string
  pinned?: boolean
}

export interface ClassDetail extends ClassInfo {
  school?: SchoolInfo | null
  headTeacher?: SimpleUser | null
  memberCount: number
}

export interface ClassMember {
  userId: number
  role: 'student' | 'teacher' | 'assistant'
  realName: string
  nickname: string
  avatarUrl?: string | null
  joinedAt: string
}

export interface TaskMaterial {
  id: number
  title: string
  materialType: string
  url?: string | null
  description?: string | null
}

export interface TaskMaterialInput {
  title: string
  materialType: string
  url?: string | null
  description?: string | null
}

export interface CreateTaskRequest {
  title: string
  coverUrl?: string | null
  description: string
  backgroundMaterials?: TaskMaterialInput[]
  evaluationCriteria?: string | null
  startTime?: string | null
  dueTime: string
  targetClassIds: number[]
}

export type UpdateTaskRequest = CreateTaskRequest

export interface ExhibitionStats {
  viewCount: number
  likeCount: number
  favoriteCount: number
  commentCount: number
}

export interface ExhibitionSummary {
  id: number
  taskId?: number | null
  title: string
  coverUrl?: string | null
  summary?: string | null
  status: string
  visibility: string
  groupName?: string | null
  ownerId: number
  author: SimpleUser
  latestVersionNo: number
  publishedVersionNo: number
  stats: ExhibitionStats
  tags: string[]
}

export interface CommunityExhibition {
  id: number
  taskId?: number | null
  title: string
  coverUrl?: string | null
  summary?: string | null
  status: string
  visibility: string
  groupName?: string | null
  ownerId: number
  author: SimpleUser
  latestVersionNo: number
  publishedVersionNo: number
  stats: ExhibitionStats
  tags: string[]
}

export type CommunityExhibitionSortBy = 'latest' | 'hot' | 'likes'

export interface CommunityExhibitionQuery {
  keyword?: string
  grade?: string
  theme?: string
  tag?: string
  sortBy?: CommunityExhibitionSortBy
  page?: number
  pageSize?: number
}

export interface ExhibitionMember {
  userId: number
  role: 'owner' | 'editor' | 'viewer'
  name: string
  avatarUrl?: string | null
  joinedAt: string
}

export interface ExhibitionDetail extends ExhibitionSummary {
  createdAt: string
  updatedAt: string
  collaborators: ExhibitionMember[]
}

export interface CreateExhibitionRequest {
  taskId?: number | null
  title: string
  summary?: string | null
  coverUrl?: string | null
  visibility?: 'private' | 'class' | 'public'
  groupName?: string | null
}

export interface UpdateExhibitionRequest {
  title: string
  summary?: string | null
  coverUrl?: string | null
  visibility?: 'private' | 'class' | 'public'
}

export interface AddExhibitionMembersRequest {
  memberUserIds: number[]
  role: 'owner' | 'editor' | 'viewer'
}

export interface TaskSummary {
  id: number
  title: string
  coverUrl?: string | null
  description: string
  teacher: SimpleUser
  startTime?: string | null
  dueTime?: string | null
  status: string
}

export interface TaskDetail extends TaskSummary {
  evaluationCriteria?: string | null
  backgroundMaterials: TaskMaterial[]
  targetClasses: ClassInfo[]
  excellentExhibitions: ExhibitionSummary[]
}

export interface TaskProgressGroup {
  exhibitionId: number
  groupName?: string | null
  leaderName?: string | null
  memberCount: number
  progressPercent: number
  submissionStatus?: string | null
  updatedAt: string
}

export interface TaskProgress {
  taskId: number
  submittedCount: number
  reviewedCount: number
  groups: TaskProgressGroup[]
}

export interface CreateSubmissionRequest {
  exhibitionId: number
  submitRemark?: string | null
}

export interface MuseumResource {
  id: number
  providerCode: string
  title: string
  category: string
  museumName?: string | null
  coverUrl?: string | null
  detailUrl?: string | null
  description?: string | null
  metadata?: Record<string, unknown>
}

export interface SyncMuseumResourcesRequest {
  providerCode: string
  category?: string | null
  keyword?: string | null
}

export interface ActivityFeed {
  id: number
  type: string
  content: string
  operator: SimpleUser
  createdAt: string
}

export interface DashboardOverview {
  ongoingTasks: TaskSummary[]
  recentExhibitions: ExhibitionSummary[]
  announcements: Announcement[]
  activityFeeds: ActivityFeed[]
  recommendedResources: MuseumResource[]
  featuredExhibitions: ExhibitionSummary[]
}

export interface CanvasConfig {
  width: number
  height: number
  background?: string | null
  zoom: number
}

export interface ExhibitionElement {
  componentType?: string | null
  x: number
  y: number
  width: number
  height: number
  props: Record<string, unknown>
}

export interface ExhibitionRenderData {
  canvasConfig: CanvasConfig
  elements: ExhibitionElement[]
}

export interface SaveExhibitionVersionRequest {
  saveType: 'manual' | 'autosave' | 'publish'
  versionNote?: string | null
  canvasConfig: CanvasConfig
  versionData: Record<string, unknown>
}

export interface PublishExhibitionRequest {
  versionNo: number
  visibility: 'private' | 'class' | 'public'
}

export interface ExhibitionVersion {
  id: number
  versionNo: number
  saveType: string
  versionNote?: string | null
  canvasConfig: CanvasConfig
  elementCount: number
  versionData: Record<string, unknown>
  createdBy: SimpleUser
  createdAt: string
}

export interface StoryTimelineItem {
  anchorCode?: string | null
  startSecond?: number | null
  endSecond?: number | null
  content?: string | null
}

export interface UpsertDigitalHumanRequest {
  name: string
  avatar2dUrl?: string | null
  model3dUrl?: string | null
  persona?: string | null
  voiceType?: string | null
  storyScript?: string | null
  storyTimeline?: StoryTimelineItem[]
}

export interface BindEquipmentRequest {
  slotCode: string
  museumResourceId: number
  displayOrder?: number | null
  anchorCode?: string | null
}

export interface DigitalHumanEquipment {
  id: number
  slotCode: string
  anchorCode?: string | null
  displayOrder: number
  resourceId: number
  resourceTitle: string
  museumName?: string | null
  resourceSnapshot?: Record<string, unknown> | null
}

export interface DigitalHuman {
  id: number
  exhibitionId: number
  name: string
  avatar2dUrl?: string | null
  model3dUrl?: string | null
  persona?: string | null
  voiceType?: string | null
  storyScript?: string | null
  storyTimeline: Array<Record<string, unknown>>
  equippedItems: DigitalHumanEquipment[]
}

export interface SubmissionReview {
  id: number
  reviewer: SimpleUser
  score?: number | null
  commentText?: string | null
  commentAudioUrl?: string | null
  isPublic: boolean
  createdAt: string
}

export interface SubmissionDetail {
  id: number
  taskId: number
  exhibitionId: number
  submitter: SimpleUser
  versionNo: number
  submitRemark?: string | null
  submissionStatus: string
  submittedAt: string
  reviewedAt?: string | null
  reviews: SubmissionReview[]
}

export interface CreateSubmissionReviewRequest {
  score?: number | null
  commentText?: string | null
  commentAudioUrl?: string | null
  isPublic?: boolean
}

export interface Comment {
  id: number
  exhibitionId?: number
  userId: number
  parentCommentId?: number | null
  rootCommentId?: number | null
  content: string
  status: string
  user: SimpleUser
  mentionUsers?: SimpleUser[]
  createdAt: string
}

export interface CreateCommentRequest {
  content: string
  parentCommentId?: number | null
  mentionUserIds?: number[]
}

export interface ShareExhibitionRequest {
  channel: string
}

export interface FeatureExhibitionRequest {
  featured: boolean
  featuredReason?: string | null
}

export interface ExhibitionViewerData {
  exhibition: ExhibitionDetail
  renderData: ExhibitionRenderData
  digitalHuman?: DigitalHuman | null
  teacherReviews: SubmissionReview[]
  comments: Comment[]
}

export interface CommunityComments {
  teacherReviews: SubmissionReview[]
  studentComments: Comment[]
}

export interface NotificationItem {
  id: number
  notificationType: string
  title: string
  content: string
  bizType?: string | null
  bizId?: number | null
  readStatus: 'read' | 'unread'
  createdAt: string
  readAt?: string | null
}

export interface ReadNotificationsRequest {
  notificationIds: number[]
}

export interface AssetUploadData {
  assetId: number
  fileName: string
  fileUrl: string
  mimeType?: string | null
  fileSize?: number | null
}

export interface Asset extends AssetUploadData {
  assetType: string
  sourceType: string
  createdAt: string
}

export interface PortfolioItem {
  exhibitionId: number
  title: string
  coverUrl?: string | null
  publishedAt?: string | null
  likeCount: number
}

export interface UserHomepageStats {
  exhibitionCount: number
  likeCount: number
  teacherPraiseCount: number
}

export interface UserHomepage {
  user: UserProfile
  stats: UserHomepageStats
  portfolio: PortfolioItem[]
}

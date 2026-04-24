import type { RouteRecordRaw } from 'vue-router'
import Layout from '@/layouts/Layout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'

export const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { public: true, title: '登录' },
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { public: true, title: '注册' },
  },
  {
    path: '/exhibitions/:exhibitionId/editor',
    name: 'exhibition-editor',
    component: () => import('@/views/exhibitions/Editor.vue'),
    meta: { title: '展厅编辑器' },
  },
  {
    path: '/exhibitions/:exhibitionId/view',
    name: 'exhibition-viewer',
    component: () => import('@/views/exhibitions/ExhibitionViewer.vue'),
    meta: { title: '展厅浏览' },
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { adminOnly: true },
    children: [
      {
        path: '',
        name: 'admin-dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '管理总览', adminOnly: true },
      },
      {
        path: 'classes',
        name: 'admin-classes',
        component: () => import('@/views/admin/AdminPlaceholderView.vue'),
        meta: { title: '班级审核', adminOnly: true },
      },
      {
        path: 'tasks',
        name: 'admin-tasks',
        component: () => import('@/views/admin/AdminPlaceholderView.vue'),
        meta: { title: '任务审核', adminOnly: true },
      },
      {
        path: 'community',
        name: 'admin-community',
        component: () => import('@/views/admin/AdminPlaceholderView.vue'),
        meta: { title: '社区审核', adminOnly: true },
      },
      {
        path: 'assets',
        name: 'admin-assets',
        component: () => import('@/views/admin/AdminPlaceholderView.vue'),
        meta: { title: '素材审核', adminOnly: true },
      },
      {
        path: 'museum',
        name: 'admin-museum',
        component: () => import('@/views/admin/AdminPlaceholderView.vue'),
        meta: { title: '文博资源管理', adminOnly: true },
      },
      {
        path: 'notifications',
        name: 'admin-notifications',
        component: () => import('@/views/admin/AdminPlaceholderView.vue'),
        meta: { title: '系统通知', adminOnly: true },
      },
    ],
  },
  {
    path: '/',
    component: Layout,
    children: [
      {
        path: '',
        name: 'dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '工作台' },
      },
      {
        path: 'classes',
        name: 'classes',
        component: () => import('@/views/classes/ClassListView.vue'),
        meta: { title: '班级管理' },
      },
      {
        path: 'classes/:classId',
        name: 'class-detail',
        component: () => import('@/views/classes/ClassDetailView.vue'),
        meta: { title: '班级详情' },
      },
      {
        path: 'tasks',
        name: 'tasks',
        component: () => import('@/views/tasks/TaskListView.vue'),
        meta: { title: '任务中心' },
      },
      {
        path: 'tasks/create',
        name: 'task-create',
        component: () => import('@/views/tasks/TaskCreateView.vue'),
        meta: { title: '发布任务' },
      },
      {
        path: 'tasks/:taskId',
        name: 'task-detail',
        component: () => import('@/views/tasks/TaskDetailView.vue'),
        meta: { title: '任务详情' },
      },
      {
        path: 'exhibitions',
        name: 'exhibitions',
        component: () => import('@/views/exhibitions/ExhibitionListView.vue'),
        meta: { title: '创作中心' },
      },
      {
        path: 'exhibitions/create',
        name: 'exhibition-create',
        component: () => import('@/views/exhibitions/ExhibitionCreateView.vue'),
        meta: { title: '创建展厅' },
      },
      {
        path: 'exhibitions/:exhibitionId',
        name: 'exhibition-detail',
        component: () => import('@/views/exhibitions/ExhibitionDetailView.vue'),
        meta: { title: '展厅详情' },
      },
      {
        path: 'community',
        name: 'community',
        component: () => import('@/views/community/CommunityView.vue'),
        meta: { title: '社区展厅' },
      },
      {
        path: 'community/:exhibitionId',
        name: 'community-detail',
        component: () => import('@/views/community/CommunityDetailView.vue'),
        meta: { title: '社区详情' },
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('@/views/profile/ProfileView.vue'),
        meta: { title: '个人中心' },
      },
      {
        path: 'profile/edit',
        name: 'profile-edit',
        component: () => import('@/views/profile/ProfileEditView.vue'),
        meta: { title: '编辑资料' },
      },
      {
        path: 'portfolio',
        name: 'portfolio',
        component: () => import('@/views/users/MyPortfolioView.vue'),
        meta: { title: '我的作品集' },
      },
      {
        path: 'users/:userId/homepage',
        name: 'user-homepage',
        component: () => import('@/views/users/UserHomepageView.vue'),
        meta: { title: '用户主页' },
      },
      {
        path: 'asset-library',
        name: 'asset-library',
        component: () => import('@/views/assets/AssetLibraryView.vue'),
        meta: { title: '素材库' },
      },
      {
        path: 'museum',
        name: 'museum',
        component: () => import('@/views/museum/MuseumResourceView.vue'),
        meta: { title: '文博资源' },
      },
      {
        path: 'museum/:resourceId',
        name: 'museum-detail',
        component: () => import('@/views/museum/MuseumResourceDetailView.vue'),
        meta: { title: '资源详情' },
      },
      {
        path: 'submissions/:submissionId',
        name: 'submission-detail',
        component: () => import('@/views/submissions/SubmissionDetailView.vue'),
        meta: { title: '提交详情' },
      },
      {
        path: 'notifications',
        name: 'notifications',
        component: () => import('@/views/notifications/NotificationView.vue'),
        meta: { title: '消息通知' },
      },
    ],
  },
]

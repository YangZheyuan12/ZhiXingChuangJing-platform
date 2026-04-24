<template>
  <div class="space-y-6">
    <PageHero
      eyebrow="Community Detail"
      :title="detail?.title || '社区展厅详情'"
      :description="detail?.summary || '暂无展厅摘要。'"
      back-to="/community"
      back-label="返回社区展厅"
    >
      <template #badge>
        <StatusPill :value="detail?.visibility || 'public'" />
      </template>
      <template #actions>
        <button
          type="button"
          :disabled="acting"
          class="action-btn"
          :class="[liked ? 'action-btn-liked' : 'action-btn-default', { 'action-btn-bump': likeBumping }]"
          @click="handleLike"
        >
          <svg class="h-4 w-4" viewBox="0 0 24 24" :fill="liked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
          </svg>
          <span>点赞</span>
          <span v-if="detail?.stats.likeCount !== undefined" class="ml-0.5 text-xs tabular-nums opacity-80">{{ detail.stats.likeCount }}</span>
        </button>
        <button
          type="button"
          :disabled="acting"
          class="action-btn"
          :class="[favorited ? 'action-btn-favorited' : 'action-btn-default', { 'action-btn-bump': favoriteBumping }]"
          @click="handleFavorite"
        >
          <svg class="h-4 w-4" viewBox="0 0 24 24" :fill="favorited ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
          </svg>
          <span>收藏</span>
          <span v-if="detail?.stats.favoriteCount !== undefined" class="ml-0.5 text-xs tabular-nums opacity-80">{{ detail.stats.favoriteCount }}</span>
        </button>
        <button
          type="button"
          :disabled="acting"
          class="rounded-full border border-slate-200 px-3 py-1 text-sm text-slate-600 transition hover:border-brand-300 hover:text-brand-700"
          @click="handleShare"
        >
          分享
        </button>
        <button
          v-if="canFeature"
          type="button"
          :disabled="acting"
          class="rounded-full border border-slate-200 px-3 py-1 text-sm text-slate-600 transition hover:border-brand-300 hover:text-brand-700"
          @click="handleFeature"
        >
          推荐到首页
        </button>
      </template>
    </PageHero>

    <div v-if="errorMessage" class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-600">
      {{ errorMessage }}
    </div>

    <div class="grid gap-6 lg:grid-cols-4">
      <MetricTile label="点赞数" :value="detail?.stats.likeCount ?? '--'" hint="社区热度指标" />
      <MetricTile label="收藏数" :value="detail?.stats.favoriteCount ?? '--'" hint="收藏热度指标" />
      <MetricTile label="评论数" :value="detail?.stats.commentCount ?? '--'" hint="互动条数" />
      <MetricTile label="教师点评" :value="comments?.teacherReviews.length ?? 0" hint="公开点评聚合" />
    </div>

    <section class="panel-card p-6">
      <SectionHeader title="展厅信息" description="公开作品详情与标签信息。" />
      <div v-if="loading" class="text-sm text-slate-500">加载中...</div>
      <div v-else-if="detail" class="space-y-5">
        <div class="flex flex-wrap gap-2">
          <span
            v-for="tag in detail.tags"
            :key="tag"
            class="rounded-full bg-brand-50 px-3 py-1 text-sm text-brand-700"
          >
            {{ tag }}
          </span>
          <span v-if="detail.tags.length === 0" class="rounded-full bg-slate-100 px-3 py-1 text-sm text-slate-500">
            无标签
          </span>
        </div>
        <div class="grid gap-4 md:grid-cols-3">
          <div class="rounded-2xl border border-slate-200 p-5">
            <p class="text-xs uppercase tracking-[0.18em] text-slate-400">创作者</p>
            <h3 class="mt-2 text-xl font-semibold text-slate-900">{{ detail.author.nickname || detail.author.name }}</h3>
            <p class="mt-2 text-sm text-slate-500">小组：{{ detail.groupName || '--' }}</p>
          </div>
          <div class="rounded-2xl border border-slate-200 p-5">
            <p class="text-xs uppercase tracking-[0.18em] text-slate-400">更新时间</p>
            <p class="mt-2 text-sm text-slate-600">{{ formatDateTime(detail.updatedAt) }}</p>
          </div>
          <div class="rounded-2xl border border-slate-200 p-5">
            <p class="text-xs uppercase tracking-[0.18em] text-slate-400">互动概览</p>
            <p class="mt-2 text-sm text-slate-600">{{ detail.stats.likeCount }} 赞 · {{ detail.stats.commentCount }} 条互动</p>
          </div>
        </div>
      </div>
    </section>

    <section class="panel-card overflow-hidden">
      <div class="bg-[linear-gradient(160deg,#fffaf7,#f5e5d6)] px-6 py-6">
        <SectionHeader title="发表评论" description="支持直接评论和回复指定留言。" />
        <div v-if="replyTarget || reviewReplyTarget" class="mb-4 rounded-2xl border border-brand-200 bg-white/80 px-4 py-3">
          <p class="text-xs uppercase tracking-[0.16em] text-brand-600">Replying</p>
          <p v-if="replyTarget" class="mt-1 text-sm text-slate-600">
            正在回复 <span class="font-medium text-brand-700">@{{ replyTarget.user.nickname || replyTarget.user.name }}</span>：
            {{ replyTarget.content }}
          </p>
          <p v-else-if="reviewReplyTarget" class="mt-1 text-sm text-slate-600">
            正在回复老师 <span class="font-medium text-amber-700">@{{ reviewReplyTarget.reviewer.nickname || reviewReplyTarget.reviewer.name }}</span> 的点评
            <span class="ml-1 text-xs text-slate-400">（{{ reviewReplyTarget.score ?? '--' }} 分）</span>
          </p>
          <button type="button" class="mt-3 text-sm text-brand-700 transition hover:text-brand-900" @click="clearReplyTarget">
            取消回复
          </button>
        </div>
        <form class="space-y-4" @submit.prevent="handleCreateComment">
          <textarea
            v-model="commentContent"
            rows="4"
            class="form-textarea"
            placeholder="写下你的看法、建议或对同学作品的反馈"
          />
          <div class="flex flex-wrap items-center justify-between gap-3">
            <p class="text-xs leading-6 text-slate-400">回复评论时会自动记录被回复对象，并在评论列表中显示。</p>
            <button
              type="submit"
              :disabled="creatingComment"
              class="rounded-2xl bg-brand-600 px-5 py-3 text-sm font-medium text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:bg-slate-300"
            >
              {{ creatingComment ? '提交中...' : '发布评论' }}
            </button>
          </div>
        </form>
      </div>
    </section>

    <section class="panel-card p-6">
      <div class="flex flex-col gap-4 border-b border-slate-100 pb-5 md:flex-row md:items-center md:justify-between">
        <div>
          <SectionHeader title="评论与点评" description="教师点评与学生评论分开展示，可切换查看。" />
        </div>
        <div class="inline-flex rounded-full bg-slate-100 p-1">
          <button
            type="button"
            class="rounded-full px-4 py-2 text-sm font-medium transition"
            :class="activeCommentTab === 'student' ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-500 hover:text-slate-800'"
            @click="activeCommentTab = 'student'"
          >
            学生评论 {{ studentThreads.length }}
          </button>
          <button
            type="button"
            class="rounded-full px-4 py-2 text-sm font-medium transition"
            :class="activeCommentTab === 'teacher' ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-500 hover:text-slate-800'"
            @click="activeCommentTab = 'teacher'"
          >
            教师点评 {{ comments?.teacherReviews.length ?? 0 }}
          </button>
        </div>
      </div>

      <div v-if="loading" class="pt-6 text-sm text-slate-500">加载中...</div>

      <div v-else-if="activeCommentTab === 'teacher'" class="pt-6">
        <EmptyStatePanel
          v-if="!comments || comments.teacherReviews.length === 0"
          eyebrow="Reviews"
          title="暂无教师点评"
          description="当前作品还没有公开点评。"
        />
        <div v-else class="space-y-4">
          <article
            v-for="review in comments.teacherReviews"
            :key="review.id"
            class="rounded-2xl border border-slate-200 p-5"
          >
            <div class="flex gap-3">
              <div class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-amber-50 text-sm font-medium text-amber-700">
                {{ getInitial(review.reviewer.nickname || review.reviewer.name) }}
              </div>
              <div class="min-w-0 flex-1">
                <div class="flex flex-wrap items-center justify-between gap-3">
                  <div>
                    <h4 class="font-medium text-slate-900">{{ review.reviewer.nickname || review.reviewer.name }}</h4>
                    <p class="mt-1 text-xs text-slate-400">{{ formatDateTime(review.createdAt) }}</p>
                  </div>
                  <div class="flex items-center gap-2">
                    <span class="rounded-full bg-amber-50 px-2.5 py-1 text-xs text-amber-700">{{ review.score ?? '--' }} 分</span>
                    <button
                      type="button"
                      class="rounded-full border border-amber-200 px-3 py-1 text-xs text-amber-700 transition hover:bg-amber-50"
                      @click="setReplyToTeacher(review)"
                    >
                      回复点评
                    </button>
                  </div>
                </div>
                <p :class="getTextClass(reviewKey(review.id), review.commentText || '暂无点评内容', 4)" class="mt-3 text-sm leading-7 text-slate-600">
                  {{ review.commentText || '暂无点评内容' }}
                </p>
                <button
                  v-if="shouldShowExpand(review.commentText || '', 160)"
                  type="button"
                  class="mt-2 text-sm text-brand-700 transition hover:text-brand-900"
                  @click="toggleExpanded(reviewKey(review.id))"
                >
                  {{ isExpanded(reviewKey(review.id)) ? '收起' : '展开查看更多' }}
                </button>

                <div v-if="teacherReplies(review.reviewer.id).length > 0" class="mt-4 rounded-2xl bg-amber-50/60 px-4 py-3">
                  <p class="text-xs font-semibold tracking-wide text-amber-700">同学回复</p>
                  <div class="mt-3 space-y-3">
                    <div
                      v-for="reply in teacherReplies(review.reviewer.id)"
                      :key="reply.id"
                      class="border-b border-amber-200/50 pb-3 last:border-b-0 last:pb-0"
                    >
                      <div class="flex flex-wrap items-center gap-2 text-sm">
                        <span class="font-medium text-slate-900">{{ reply.user.nickname || reply.user.name }}</span>
                        <span class="text-slate-400">回复老师</span>
                        <span class="font-medium text-amber-700">@{{ review.reviewer.nickname || review.reviewer.name }}</span>
                      </div>
                      <p class="mt-1 text-xs text-slate-400">{{ formatDateTime(reply.createdAt) }}</p>
                      <p class="mt-2 text-sm leading-6 text-slate-600">{{ reply.content }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </article>
        </div>
      </div>

      <div v-else class="pt-6">
        <EmptyStatePanel
          v-if="studentThreads.length === 0"
          eyebrow="Comments"
          title="暂无评论"
          description="还没有同学留言。"
        />

        <TransitionGroup
          v-else
          tag="div"
          name="comment-list"
          class="space-y-6"
        >
          <article
            v-for="thread in studentThreads"
            :key="thread.root.id"
            class="rounded-2xl border border-slate-200 p-5"
          >
            <div class="flex gap-3">
              <div class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-neutral-100 text-sm font-medium text-neutral-700">
                {{ getInitial(thread.root.user.nickname || thread.root.user.name) }}
              </div>
              <div class="min-w-0 flex-1">
                <div class="flex flex-wrap items-center justify-between gap-3">
                  <div>
                    <h4 class="font-medium text-slate-900">{{ thread.root.user.nickname || thread.root.user.name }}</h4>
                    <p class="mt-1 text-xs text-slate-400">{{ formatDateTime(thread.root.createdAt) }}</p>
                  </div>
                  <button type="button" class="text-sm text-slate-500 transition hover:text-slate-900" @click="setReplyTarget(thread.root)">
                    回复
                  </button>
                </div>

                <p :class="getTextClass(commentKey(thread.root.id), thread.root.content, 4)" class="mt-3 text-sm leading-7 text-slate-700">
                  {{ thread.root.content }}
                </p>
                <button
                  v-if="shouldShowExpand(thread.root.content, 160)"
                  type="button"
                  class="mt-2 text-sm text-brand-700 transition hover:text-brand-900"
                  @click="toggleExpanded(commentKey(thread.root.id))"
                >
                  {{ isExpanded(commentKey(thread.root.id)) ? '收起' : '展开查看更多' }}
                </button>

                <div v-if="thread.replies.length > 0" class="mt-4 rounded-2xl bg-slate-50 px-4 py-3">
                  <div class="space-y-4">
                    <div
                      v-for="reply in thread.replies"
                      :key="reply.id"
                      class="border-b border-slate-200/80 pb-4 last:border-b-0 last:pb-0"
                    >
                      <div class="flex items-start justify-between gap-3">
                        <div class="min-w-0 flex-1">
                          <div class="flex flex-wrap items-center gap-2 text-sm">
                            <span class="font-medium text-slate-900">{{ reply.user.nickname || reply.user.name }}</span>
                            <template v-if="getReplyTargetName(reply)">
                              <span class="text-slate-400">回复</span>
                              <span class="font-medium text-brand-700">@{{ getReplyTargetName(reply) }}</span>
                            </template>
                          </div>
                          <p class="mt-1 text-xs text-slate-400">{{ formatDateTime(reply.createdAt) }}</p>
                        </div>
                        <button type="button" class="shrink-0 text-sm text-slate-500 transition hover:text-slate-900" @click="setReplyTarget(reply)">
                          回复
                        </button>
                      </div>

                      <p :class="getTextClass(commentKey(reply.id), reply.content, 3)" class="mt-2 text-sm leading-7 text-slate-600">
                        {{ reply.content }}
                      </p>
                      <button
                        v-if="shouldShowExpand(reply.content, 120)"
                        type="button"
                        class="mt-2 text-sm text-brand-700 transition hover:text-brand-900"
                        @click="toggleExpanded(commentKey(reply.id))"
                      >
                        {{ isExpanded(commentKey(reply.id)) ? '收起' : '展开查看更多' }}
                      </button>
                    </div>
                  </div>
                </div>

                <div class="mt-4 text-xs text-slate-400">
                  共 {{ thread.replies.length }} 条回复
                </div>
              </div>
            </div>
          </article>
        </TransitionGroup>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getErrorMessage } from '@/utils/request'
import {
  createCommunityComment,
  favoriteCommunityExhibition,
  featureCommunityExhibition,
  getCommunityComments,
  getCommunityExhibitionDetail,
  likeCommunityExhibition,
  shareCommunityExhibition,
  unfavoriteCommunityExhibition,
  unlikeCommunityExhibition,
} from '@/api/modules/community'
import type { Comment, CommunityComments, ExhibitionDetail } from '@/api/types'
import EmptyStatePanel from '@/components/common/EmptyStatePanel.vue'
import MetricTile from '@/components/common/MetricTile.vue'
import PageHero from '@/components/common/PageHero.vue'
import SectionHeader from '@/components/common/SectionHeader.vue'
import StatusPill from '@/components/common/StatusPill.vue'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime } from '@/utils/format'

interface CommentThread {
  root: Comment
  replies: Comment[]
}

const route = useRoute()
const authStore = useAuthStore()
const appStore = useAppStore()
const exhibitionId = Number(route.params.exhibitionId)

const loading = ref(false)
const acting = ref(false)
const creatingComment = ref(false)
const errorMessage = ref('')
const detail = ref<ExhibitionDetail | null>(null)
const comments = ref<CommunityComments | null>(null)
const commentContent = ref('')
const replyTarget = ref<Comment | null>(null)
const reviewReplyTarget = ref<SubmissionReview | null>(null)
const liked = ref(false)
const favorited = ref(false)
const likeBumping = ref(false)
const favoriteBumping = ref(false)
const activeCommentTab = ref<'student' | 'teacher'>('student')
const expandedKeys = ref<Record<string, boolean>>({})
const commentFormRef = ref<HTMLElement | null>(null)

function triggerBump(target: 'like' | 'favorite') {
  const flag = target === 'like' ? likeBumping : favoriteBumping
  flag.value = false
  window.requestAnimationFrame(() => {
    flag.value = true
    window.setTimeout(() => {
      flag.value = false
    }, 420)
  })
}

const canFeature = computed(() => ['teacher', 'admin'].includes(authStore.user?.role || ''))

const commentMap = computed(() => {
  return new Map((comments.value?.studentComments || []).map((item) => [item.id, item]))
})

const studentThreads = computed<CommentThread[]>(() => {
  const list = comments.value?.studentComments || []
  const roots = list
    .filter((comment) => !comment.parentCommentId)
    .sort((left, right) => new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime())

  const replyGroups = new Map<number, Comment[]>()

  list
    .filter((comment) => comment.parentCommentId)
    .sort((left, right) => new Date(left.createdAt).getTime() - new Date(right.createdAt).getTime())
    .forEach((reply) => {
      const rootId = reply.rootCommentId || reply.parentCommentId || reply.id
      const group = replyGroups.get(rootId) || []
      group.push(reply)
      replyGroups.set(rootId, group)
    })

  return roots.map((root) => ({
    root,
    replies: replyGroups.get(root.id) || [],
  }))
})

async function fetchCommunityDetail() {
  loading.value = true
  errorMessage.value = ''

  try {
    await Promise.all([refreshDetail(), refreshComments()])
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '社区详情加载失败')
  } finally {
    loading.value = false
  }
}

async function refreshDetail() {
  const viewerData = await getCommunityExhibitionDetail(exhibitionId)
  detail.value = viewerData.exhibition
  liked.value = readInteractionState('like')
  favorited.value = readInteractionState('favorite')
}

async function refreshComments() {
  comments.value = await getCommunityComments(exhibitionId)
}

async function handleCreateComment() {
  const content = commentContent.value.trim()
  if (!content) {
    return
  }

  creatingComment.value = true
  errorMessage.value = ''

  const mentionUserIds: number[] = []
  let parentCommentId: number | null = null
  const wasTeacherReply = Boolean(reviewReplyTarget.value)

  if (replyTarget.value) {
    parentCommentId = replyTarget.value.id
    mentionUserIds.push(replyTarget.value.userId)
  } else if (reviewReplyTarget.value) {
    mentionUserIds.push(reviewReplyTarget.value.reviewer.id)
  }

  try {
    const newComment = await createCommunityComment(exhibitionId, {
      content,
      parentCommentId,
      mentionUserIds,
    })

    // 后端返回的 mentionUsers 目前为空数组，这里按当前回复目标兜底补齐，
    // 让乐观更新在「同学回复」区也能立即显示。
    const enrichedComment: Comment = {
      ...newComment,
      mentionUsers: buildMentionUsersFallback(newComment),
    }

    if (comments.value) {
      comments.value = {
        ...comments.value,
        studentComments: [...comments.value.studentComments, enrichedComment],
      }
    }

    if (detail.value) {
      detail.value = {
        ...detail.value,
        stats: {
          ...detail.value.stats,
          commentCount: detail.value.stats.commentCount + 1,
        },
      }
    }

    commentContent.value = ''
    replyTarget.value = null
    reviewReplyTarget.value = null
    activeCommentTab.value = wasTeacherReply ? 'teacher' : 'student'
    appStore.showToast('评论已发布', 'success')
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '评论发布失败')
  } finally {
    creatingComment.value = false
  }
}

function setReplyTarget(comment: Comment) {
  replyTarget.value = comment
  reviewReplyTarget.value = null
}

function setReplyToTeacher(review: SubmissionReview) {
  reviewReplyTarget.value = review
  replyTarget.value = null
  scrollToCommentForm()
}

function scrollToCommentForm() {
  if (commentFormRef.value) {
    commentFormRef.value.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

function clearReplyTarget() {
  replyTarget.value = null
  reviewReplyTarget.value = null
}

function buildMentionUsersFallback(newComment: Comment) {
  if (newComment.mentionUsers && newComment.mentionUsers.length > 0) {
    return newComment.mentionUsers
  }
  if (replyTarget.value) {
    return [replyTarget.value.user]
  }
  if (reviewReplyTarget.value) {
    return [reviewReplyTarget.value.reviewer]
  }
  return []
}

function teacherReplies(teacherId: number) {
  const list = comments.value?.studentComments || []
  return list
    .filter((comment) => !comment.parentCommentId && (comment.mentionUsers?.some((u) => u.id === teacherId) ?? false))
    .sort((l, r) => new Date(l.createdAt).getTime() - new Date(r.createdAt).getTime())
}

async function handleLike() {
  if (!detail.value) {
    return
  }

  const previousLikeCount = detail.value.stats.likeCount
  const nextLiked = !liked.value
  liked.value = nextLiked
  writeInteractionState('like', nextLiked)
  if (nextLiked) {
    triggerBump('like')
  }
  detail.value = {
    ...detail.value,
    stats: {
      ...detail.value.stats,
      likeCount: Math.max(0, previousLikeCount + (nextLiked ? 1 : -1)),
    },
  }

  try {
    await wrapAction(async () => {
      if (nextLiked) {
        await likeCommunityExhibition(exhibitionId)
        appStore.showToast('点赞成功', 'success')
        return
      }

      await unlikeCommunityExhibition(exhibitionId)
      appStore.showToast('已取消点赞', 'success')
    }, '点赞失败')
  } catch {
    liked.value = !nextLiked
    writeInteractionState('like', liked.value)
    if (detail.value) {
      detail.value = {
        ...detail.value,
        stats: {
          ...detail.value.stats,
          likeCount: previousLikeCount,
        },
      }
    }
  }
}

async function handleFavorite() {
  if (!detail.value) {
    return
  }

  const previousFavoriteCount = detail.value.stats.favoriteCount
  const nextFavorited = !favorited.value
  favorited.value = nextFavorited
  writeInteractionState('favorite', nextFavorited)
  if (nextFavorited) {
    triggerBump('favorite')
  }
  detail.value = {
    ...detail.value,
    stats: {
      ...detail.value.stats,
      favoriteCount: Math.max(0, previousFavoriteCount + (nextFavorited ? 1 : -1)),
    },
  }

  try {
    await wrapAction(async () => {
      if (nextFavorited) {
        await favoriteCommunityExhibition(exhibitionId)
        appStore.showToast('已加入收藏', 'success')
        return
      }

      await unfavoriteCommunityExhibition(exhibitionId)
      appStore.showToast('已取消收藏', 'success')
    }, '收藏失败')
  } catch {
    favorited.value = !nextFavorited
    writeInteractionState('favorite', favorited.value)
    if (detail.value) {
      detail.value = {
        ...detail.value,
        stats: {
          ...detail.value.stats,
          favoriteCount: previousFavoriteCount,
        },
      }
    }
  }
}

async function handleShare() {
  await wrapAction(async () => {
    await shareCommunityExhibition(exhibitionId, { channel: 'web' })
    appStore.showToast('已记录分享', 'success')
  }, '分享失败')
}

async function handleFeature() {
  await wrapAction(async () => {
    await featureCommunityExhibition(exhibitionId, {
      featured: true,
      featuredReason: '教学展示页推荐',
    })
    appStore.showToast('已提交推荐', 'success')
  }, '推荐失败')
}

async function wrapAction(action: () => Promise<void>, fallback: string) {
  acting.value = true
  errorMessage.value = ''

  try {
    await action()
  } catch (error) {
    errorMessage.value = getErrorMessage(error, fallback)
    throw error
  } finally {
    acting.value = false
  }
}

function getInitial(name?: string | null) {
  const value = name?.trim() || '评'
  return value.slice(0, 1).toUpperCase()
}

function getReplyTargetName(comment: Comment) {
  const mentionedUser = comment.mentionUsers?.[0]
  if (mentionedUser) {
    return mentionedUser.nickname || mentionedUser.name
  }

  const parentComment = comment.parentCommentId ? commentMap.value.get(comment.parentCommentId) : null
  return parentComment ? (parentComment.user.nickname || parentComment.user.name) : ''
}

function shouldShowExpand(content: string, limit: number) {
  return content.trim().length > limit
}

function isExpanded(key: string) {
  return Boolean(expandedKeys.value[key])
}

function toggleExpanded(key: string) {
  expandedKeys.value = {
    ...expandedKeys.value,
    [key]: !expandedKeys.value[key],
  }
}

function getTextClass(key: string, content: string, collapsedLines: number) {
  if (isExpanded(key) || !shouldShowExpand(content, collapsedLines === 4 ? 160 : 120)) {
    return ''
  }

  return collapsedLines === 4 ? 'line-clamp-4' : 'line-clamp-3'
}

function commentKey(id: number) {
  return `comment-${id}`
}

function reviewKey(id: number) {
  return `review-${id}`
}

function readInteractionState(type: 'like' | 'favorite') {
  return localStorage.getItem(buildInteractionStorageKey(type)) === '1'
}

function writeInteractionState(type: 'like' | 'favorite', value: boolean) {
  localStorage.setItem(buildInteractionStorageKey(type), value ? '1' : '0')
}

function buildInteractionStorageKey(type: 'like' | 'favorite') {
  return `zxcyj-community-${type}-${authStore.user?.id || 'guest'}-${exhibitionId}`
}

onMounted(fetchCommunityDetail)
</script>

<style scoped>
.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  border-radius: 9999px;
  border: 1px solid transparent;
  padding: 0.375rem 0.95rem;
  font-size: 0.875rem;
  font-weight: 500;
  transition: transform 0.18s ease, background-color 0.18s ease, border-color 0.18s ease, color 0.18s ease, box-shadow 0.18s ease;
}

.action-btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.action-btn-default {
  border-color: rgb(254 215 170);
  color: rgb(194 65 12);
}

.action-btn-default:hover {
  background-color: rgb(255 247 237);
}

.action-btn-liked {
  border-color: rgb(253 164 175);
  background-color: rgb(255 241 242);
  color: rgb(225 29 72);
  box-shadow: 0 6px 14px rgba(244, 63, 94, 0.18);
}

.action-btn-favorited {
  border-color: rgb(253 230 138);
  background-color: rgb(255 251 235);
  color: rgb(180 83 9);
  box-shadow: 0 6px 14px rgba(245, 158, 11, 0.18);
}

.action-btn:active:not(:disabled) {
  transform: scale(0.94);
}

.action-btn-bump svg {
  animation: heart-bump 0.42s cubic-bezier(0.22, 1, 0.36, 1);
}

@keyframes heart-bump {
  0% { transform: scale(1); }
  30% { transform: scale(1.35); }
  60% { transform: scale(0.92); }
  100% { transform: scale(1); }
}

.comment-list-enter-active,
.comment-list-leave-active {
  transition: transform 0.32s cubic-bezier(0.22, 1, 0.36, 1), opacity 0.24s ease;
}

.comment-list-enter-from {
  opacity: 0;
  transform: translateY(-10px) scale(0.98);
}

.comment-list-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

.comment-list-move {
  transition: transform 0.32s cubic-bezier(0.22, 1, 0.36, 1);
}
</style>

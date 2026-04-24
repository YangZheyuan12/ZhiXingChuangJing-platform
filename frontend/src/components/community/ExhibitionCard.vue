<template>
  <article class="overflow-hidden rounded-md border border-gray-200 bg-white">
    <RouterLink :to="detailLink" class="block">
      <div class="aspect-video overflow-hidden border-b border-gray-200 bg-gray-100">
        <img
          v-if="coverUrl"
          :src="coverUrl"
          :alt="exhibition.title"
          class="h-full w-full object-cover"
        />
        <div v-else class="flex h-full w-full items-center justify-center bg-gray-100 text-sm text-gray-400">
          暂无封面
        </div>
      </div>

      <div class="space-y-3 p-4">
        <div class="space-y-2">
          <h3 class="line-clamp-2 text-base font-medium text-gray-900">{{ exhibition.title }}</h3>
          <p class="text-sm text-gray-500">
            {{ exhibition.author.nickname || exhibition.author.name }}
          </p>
        </div>
      </div>
    </RouterLink>

    <div class="flex items-center justify-between border-t border-gray-200 px-4 py-3 text-sm text-gray-500">
      <div class="flex items-center gap-4">
        <span class="flex items-center gap-1.5">
          <svg class="h-4 w-4" viewBox="0 0 20 20" fill="none" aria-hidden="true">
            <path
              d="M10 4C5.8 4 2.64 6.58 1.25 10c1.39 3.42 4.55 6 8.75 6s7.36-2.58 8.75-6C17.36 6.58 14.2 4 10 4Z"
              stroke="currentColor"
              stroke-width="1.5"
            />
            <circle cx="10" cy="10" r="2.5" stroke="currentColor" stroke-width="1.5" />
          </svg>
          <span>{{ formatCount(exhibition.stats.viewCount) }}</span>
        </span>

        <button
          type="button"
          class="flex items-center gap-1.5 transition"
          :class="localLiked ? 'text-red-500' : 'text-gray-500 hover:text-gray-700'"
          :disabled="likePending"
          @click="handleLike"
        >
          <svg v-if="localLiked" class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
            <path d="M10 17.25 8.55 15.93C4.4 12.16 1.67 9.68 1.67 6.63 1.67 4.15 3.61 2.25 6.08 2.25c1.4 0 2.74.65 3.6 1.68a4.72 4.72 0 0 1 3.6-1.68c2.47 0 4.42 1.9 4.42 4.38 0 3.05-2.73 5.53-6.88 9.31L10 17.25Z" />
          </svg>
          <svg v-else class="h-4 w-4" viewBox="0 0 20 20" fill="none" aria-hidden="true">
            <path
              d="m10 17.25-1.45-1.32C4.4 12.16 1.67 9.68 1.67 6.63 1.67 4.15 3.61 2.25 6.08 2.25c1.4 0 2.74.65 3.6 1.68a4.72 4.72 0 0 1 3.6-1.68c2.47 0 4.42 1.9 4.42 4.38 0 3.05-2.73 5.53-6.88 9.31L10 17.25Z"
              stroke="currentColor"
              stroke-width="1.5"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
          <span>{{ formatCount(localLikeCount) }}</span>
        </button>
      </div>

      <RouterLink :to="detailLink" class="text-sm text-gray-700 transition hover:text-gray-900">
        查看
      </RouterLink>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { CommunityExhibition } from '@/api/types'
import { likeCommunityExhibition, unlikeCommunityExhibition } from '@/api/modules/community'
import { useAuthStore } from '@/stores/auth'
import { getErrorMessage } from '@/utils/request'

const props = defineProps<{
  exhibition: CommunityExhibition
}>()

const emit = defineEmits<{
  likeError: [message: string]
}>()

const authStore = useAuthStore()
const localLiked = ref(false)
const likePending = ref(false)
const localLikeCount = ref(props.exhibition.stats.likeCount)

const coverUrl = computed(() => props.exhibition.coverUrl?.trim() || '')
const detailLink = computed(() => `/community/${props.exhibition.id}`)

watch(
  () => [props.exhibition.id, props.exhibition.stats.likeCount] as const,
  ([, likeCount]) => {
    localLiked.value = readLikeState()
    localLikeCount.value = likeCount
    likePending.value = false
  },
  { immediate: true },
)

async function handleLike(event: MouseEvent) {
  event.preventDefault()
  event.stopPropagation()

  if (likePending.value) {
    return
  }

  const nextLiked = !localLiked.value
  localLiked.value = nextLiked
  writeLikeState(nextLiked)
  localLikeCount.value = Math.max(0, localLikeCount.value + (nextLiked ? 1 : -1))
  likePending.value = true

  try {
    if (nextLiked) {
      await likeCommunityExhibition(props.exhibition.id)
      return
    }

    await unlikeCommunityExhibition(props.exhibition.id)
  } catch (error) {
    localLiked.value = !nextLiked
    writeLikeState(localLiked.value)
    localLikeCount.value = Math.max(0, localLikeCount.value + (nextLiked ? -1 : 1))
    emit('likeError', getErrorMessage(error, '点赞失败'))
  } finally {
    likePending.value = false
  }
}

function formatCount(value: number) {
  return value.toLocaleString('zh-CN')
}

function readLikeState() {
  return localStorage.getItem(buildLikeStorageKey()) === '1'
}

function writeLikeState(value: boolean) {
  localStorage.setItem(buildLikeStorageKey(), value ? '1' : '0')
}

function buildLikeStorageKey() {
  return `zxcyj-community-like-${authStore.user?.id || 'guest'}-${props.exhibition.id}`
}
</script>

<template>
  <div class="space-y-3">
    <div class="flex gap-1 rounded-md bg-gray-100 p-1">
      <button
        v-for="tab in subTabs"
        :key="tab.value"
        type="button"
        class="flex-1 rounded px-2 py-1.5 text-xs transition"
        :class="activeSubTab === tab.value ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-500 hover:text-gray-700'"
        @click="activeSubTab = tab.value"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- 我的素材 -->
    <template v-if="activeSubTab === 'my'">
      <div class="flex gap-1 rounded-md bg-gray-50 p-1">
        <button
          v-for="f in mediaFilters"
          :key="f.value"
          type="button"
          class="flex-1 rounded px-1.5 py-1 text-[11px] transition"
          :class="mediaFilter === f.value ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-400 hover:text-gray-600'"
          @click="mediaFilter = f.value; fetchMyAssets()"
        >
          {{ f.label }}
        </button>
      </div>

      <p v-if="myLoading" class="text-xs text-gray-500">加载中...</p>
      <p v-else-if="myAssets.length === 0" class="rounded-md border border-gray-200 bg-gray-50 px-3 py-2 text-xs text-gray-500">
        暂无素材，可去素材库上传。
      </p>

      <div class="grid grid-cols-2 gap-2">
        <button
          v-for="asset in myAssets"
          :key="asset.assetId"
          type="button"
          class="group overflow-hidden rounded-md border border-gray-200 bg-white text-left transition hover:border-gray-300"
          @click="$emit('insert-asset', asset)"
        >
          <div v-if="isImage(asset)" class="aspect-[4/3] w-full overflow-hidden bg-gray-100">
            <img :src="asset.fileUrl" :alt="displayName(asset)" class="h-full w-full object-cover transition group-hover:scale-105" loading="lazy" />
          </div>
          <div v-else class="flex aspect-[4/3] w-full items-center justify-center bg-gray-50">
            <span class="text-2xl text-gray-300">{{ isVideo(asset) ? '▶' : '♫' }}</span>
          </div>
          <div class="p-1.5">
            <p class="truncate text-[11px] font-medium text-gray-700" :title="displayName(asset)">{{ displayName(asset) }}</p>
            <p class="text-[10px] text-gray-400">{{ asset.assetType }}</p>
          </div>
        </button>
      </div>
    </template>

    <!-- 博物馆素材 -->
    <template v-else>
      <p v-if="museumLoading" class="text-xs text-gray-500">素材加载中...</p>
      <p v-else-if="museumResources.length === 0" class="rounded-md border border-gray-200 bg-gray-50 px-3 py-2 text-xs text-gray-500">
        当前没有可用文博素材。
      </p>

      <div class="grid grid-cols-2 gap-2">
        <button
          v-for="resource in museumResources"
          :key="resource.id"
          type="button"
          class="group overflow-hidden rounded-md border border-gray-200 bg-white text-left transition hover:border-gray-300"
          @click="$emit('insert-museum', resource)"
        >
          <div class="aspect-[4/3] w-full overflow-hidden bg-gray-100">
            <img
              v-if="resource.coverUrl"
              :src="resource.coverUrl"
              :alt="resource.title"
              class="h-full w-full object-cover transition group-hover:scale-105"
              loading="lazy"
            />
            <div v-else class="flex h-full w-full items-center justify-center text-2xl text-gray-300">🏛</div>
          </div>
          <div class="p-1.5">
            <p class="truncate text-[11px] font-medium text-gray-700" :title="resource.title">{{ resource.title }}</p>
            <p class="truncate text-[10px] text-gray-400">{{ resource.museumName || resource.category }}</p>
          </div>
        </button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getMyAssets } from '@/api/modules/assets'
import { getMuseumResources } from '@/api/modules/museum'
import type { Asset, MuseumResource } from '@/api/types'

type SubTab = 'my' | 'museum'
type MediaFilter = '' | 'image' | 'video' | 'audio'

defineEmits<{
  'insert-asset': [asset: Asset]
  'insert-museum': [resource: MuseumResource]
}>()

const subTabs: { label: string; value: SubTab }[] = [
  { label: '我的素材', value: 'my' },
  { label: '文博素材', value: 'museum' },
]

const mediaFilters: { label: string; value: MediaFilter }[] = [
  { label: '全部', value: '' },
  { label: '图片', value: 'image' },
  { label: '视频', value: 'video' },
  { label: '音频', value: 'audio' },
]

const activeSubTab = ref<SubTab>('my')
const mediaFilter = ref<MediaFilter>('')

const myLoading = ref(false)
const myAssets = ref<Asset[]>([])
const museumLoading = ref(false)
const museumResources = ref<MuseumResource[]>([])

function displayName(asset: Asset) {
  return asset.originalFileName || asset.fileName
}

function isImage(asset: Asset) {
  return asset.assetType === 'image' || asset.mimeType?.startsWith('image/')
}

function isVideo(asset: Asset) {
  return asset.assetType === 'video' || asset.mimeType?.startsWith('video/')
}

async function fetchMyAssets() {
  myLoading.value = true
  try {
    const page = await getMyAssets({
      page: 1,
      pageSize: 30,
      assetType: mediaFilter.value || undefined,
    })
    myAssets.value = page.list.filter((a) => {
      const url = a.fileUrl.toLowerCase()
      return !(a.sourceType === 'profile' || url.includes('/avatar/') || url.includes('/avatars/'))
    })
  } catch {
    myAssets.value = []
  } finally {
    myLoading.value = false
  }
}

async function fetchMuseumResources() {
  museumLoading.value = true
  try {
    const page = await getMuseumResources({ page: 1, pageSize: 20 })
    museumResources.value = page.list
  } catch {
    museumResources.value = []
  } finally {
    museumLoading.value = false
  }
}

onMounted(() => {
  fetchMyAssets()
  fetchMuseumResources()
})
</script>

<template>
  <div class="space-y-6">
    <PageHero
      eyebrow="Asset Library"
      title="素材库"
      description="管理个人上传素材，适配图片、视频、音频、模型与文档资源，并集中查看历史记录。"
    />

    <p v-if="errorMessage" class="rounded-xl bg-rose-50 px-4 py-3 text-sm text-rose-600">{{ errorMessage }}</p>

    <div class="grid gap-6 xl:grid-cols-[0.92fr_1.08fr]">
      <section class="panel-card p-6">
        <SectionHeader title="上传素材" description="上传并登记新的素材资源。" />
        <form class="space-y-4" @submit.prevent="handleUpload">
          <label class="block">
            <span class="form-label">选择文件</span>
            <input type="file" class="form-file" @change="handleFileChange" />
          </label>
          <div class="grid gap-4 md:grid-cols-2">
            <label class="block">
              <span class="form-label">目录</span>
              <input v-model="uploadForm.folder" class="form-control" />
            </label>
            <label class="block">
              <span class="form-label">业务类型</span>
              <input v-model="uploadForm.bizType" class="form-control" />
            </label>
          </div>
          <button
            type="submit"
            :disabled="uploading || !selectedFile"
            class="rounded-2xl bg-brand-600 px-5 py-3 text-sm font-medium text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:bg-slate-300"
          >
            {{ uploading ? '上传中...' : '上传并登记素材' }}
          </button>
        </form>
      </section>

      <section class="panel-card p-6">
        <SectionHeader title="筛选与概览" description="当前页面展示最近 20 条素材。" />
        <div class="grid gap-4 md:grid-cols-2">
          <label class="block">
            <span class="form-label">素材类型</span>
            <select v-model="filters.assetType" class="form-select">
              <option value="">全部类型</option>
              <option value="image">图片</option>
              <option value="video">视频</option>
              <option value="audio">音频</option>
              <option value="model">模型</option>
              <option value="document">文档</option>
            </select>
          </label>
          <label class="block">
            <span class="form-label">来源</span>
            <select v-model="filters.sourceType" class="form-select">
              <option value="">全部来源</option>
              <option value="upload">上传</option>
              <option value="platform">平台</option>
              <option value="museum">文博资源</option>
            </select>
          </label>
        </div>
        <div class="mt-4 flex gap-3">
          <button
            type="button"
            class="rounded-2xl border border-brand-200 px-4 py-3 text-sm text-brand-700 transition hover:bg-brand-50"
            @click="fetchAssets"
          >
            刷新列表
          </button>
          <RouterLink to="/museum" class="rounded-2xl border border-slate-200 px-4 py-3 text-sm text-slate-600 transition hover:border-brand-300 hover:text-brand-700">
            去文博资源中心
          </RouterLink>
        </div>
      </section>
    </div>

    <section class="panel-card p-6">
      <SectionHeader title="素材列表" description="支持上传结果与历史记录查看。" />
      <div v-if="loading" class="text-sm text-slate-500">加载中...</div>
      <EmptyStatePanel
        v-else-if="libraryAssets.length === 0"
        eyebrow="Assets"
        title="当前没有素材记录"
        description="上传文件后，这里会显示素材元数据与访问地址。"
      />
      <div v-else class="grid gap-4 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4">
        <article v-for="asset in libraryAssets" :key="asset.assetId" class="rounded-xl border border-slate-200 p-4 shadow-sm">
          <div class="mb-3 overflow-hidden rounded-lg border border-slate-200 bg-slate-50">
            <img
              v-if="asset.assetType === 'image'"
              :src="asset.fileUrl"
              :alt="displayName(asset)"
              class="aspect-[4/3] w-full object-cover"
            />
            <video
              v-else-if="asset.assetType === 'video'"
              :src="asset.fileUrl"
              controls
              preload="metadata"
              class="aspect-[4/3] w-full bg-black object-cover"
            />
            <div
              v-else-if="asset.assetType === 'audio'"
              class="flex min-h-24 items-center justify-center bg-white px-3 py-4"
            >
              <audio :src="asset.fileUrl" controls class="w-full" />
            </div>
            <div v-else class="flex aspect-[4/3] items-center justify-center bg-white px-4 text-sm text-slate-500">
              {{ asset.assetType === 'model' ? '3D 模型文件' : '文档文件' }}
            </div>
          </div>

          <div class="flex items-start justify-between gap-3">
            <div class="min-w-0">
              <h3
                class="truncate text-sm font-medium text-slate-900"
                :title="displayName(asset)"
              >
                {{ displayName(asset) }}
              </h3>
              <p class="mt-1 text-xs text-slate-500">{{ asset.mimeType || '未知 MIME 类型' }}</p>
            </div>
            <div class="flex shrink-0 gap-1.5">
              <StatusPill :value="asset.assetType" />
            </div>
          </div>
          <div class="mt-3 space-y-1.5 text-xs text-slate-400">
            <p>文件大小：{{ formatFileSize(asset.fileSize) }}</p>
            <p>创建时间：{{ formatDateTime(asset.createdAt) }}</p>
          </div>
          <div class="mt-4 flex gap-2">
            <a
              :href="asset.fileUrl"
              target="_blank"
              rel="noreferrer"
              class="flex-1 rounded-md border border-neutral-200 px-3 py-2 text-center text-sm text-neutral-700 transition hover:border-neutral-300 hover:text-neutral-900"
            >
              打开文件
            </a>
            <a
              :href="asset.fileUrl"
              :download="displayName(asset)"
              class="flex-1 rounded-md bg-brand-600 px-3 py-2 text-center text-sm font-medium text-white transition hover:bg-brand-700"
            >
              下载素材
            </a>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { getErrorMessage } from '@/utils/request'
import { getMyAssets, uploadAsset } from '@/api/modules/assets'
import type { Asset } from '@/api/types'
import EmptyStatePanel from '@/components/common/EmptyStatePanel.vue'
import PageHero from '@/components/common/PageHero.vue'
import SectionHeader from '@/components/common/SectionHeader.vue'
import StatusPill from '@/components/common/StatusPill.vue'
import { useAppStore } from '@/stores/app'
import { formatDateTime } from '@/utils/format'

const appStore = useAppStore()
const loading = ref(false)
const uploading = ref(false)
const errorMessage = ref('')
const selectedFile = ref<File | null>(null)
const assets = ref<Asset[]>([])

const uploadForm = reactive({
  folder: 'exhibition',
  bizType: 'exhibition_material',
})

const filters = reactive({
  assetType: '',
  sourceType: '',
})

const libraryAssets = computed(() => assets.value.filter((asset) => !isProfileAsset(asset)))

async function fetchAssets() {
  loading.value = true
  errorMessage.value = ''

  try {
    const page = await getMyAssets({
      page: 1,
      pageSize: 20,
      assetType: filters.assetType || undefined,
      sourceType: filters.sourceType || undefined,
    })
    assets.value = page.list
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '素材列表加载失败')
  } finally {
    loading.value = false
  }
}

async function handleUpload() {
  if (!selectedFile.value) {
    return
  }

  uploading.value = true
  errorMessage.value = ''

  try {
    const result = await uploadAsset(selectedFile.value, uploadForm)
    appStore.showToast(`素材上传成功：${result.originalFileName || result.fileName}`, 'success')
    selectedFile.value = null
    await fetchAssets()
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '素材上传失败')
  } finally {
    uploading.value = false
  }
}

function handleFileChange(event: Event) {
  const target = event.target as HTMLInputElement
  selectedFile.value = target.files?.[0] || null
}

function displayName(asset: Asset) {
  return asset.originalFileName || asset.fileName
}

function formatFileSize(value?: number | null) {
  if (!value || value <= 0) {
    return '0 B'
  }

  if (value < 1024) {
    return `${value} B`
  }

  if (value < 1024 * 1024) {
    return `${(value / 1024).toFixed(1)} KB`
  }

  if (value < 1024 * 1024 * 1024) {
    return `${(value / (1024 * 1024)).toFixed(1)} MB`
  }

  return `${(value / (1024 * 1024 * 1024)).toFixed(1)} GB`
}

onMounted(fetchAssets)

function isProfileAsset(asset: Asset) {
  if (asset.sourceType === 'profile') {
    return true
  }

  const url = asset.fileUrl.toLowerCase()
  return url.includes('/avatar/')
    || url.includes('/avatars/')
    || url.includes('/users/avatars/')
}
</script>

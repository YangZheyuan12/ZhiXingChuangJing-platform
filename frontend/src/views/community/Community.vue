<template>
  <div class="space-y-6">
    <section class="rounded-md border border-gray-200 bg-white p-5">
      <div class="space-y-4">
        <div>
          <p class="text-xs uppercase tracking-[0.18em] text-gray-500">Community</p>
          <h1 class="mt-2 text-2xl font-medium text-gray-900">社区展厅</h1>
          <p class="mt-2 text-sm text-gray-500">浏览不同主题与年级的公开展厅作品。</p>
        </div>

        <div class="grid grid-cols-1 gap-4 md:grid-cols-3">
          <label class="block">
            <span class="form-label">主题</span>
            <select v-model="filters.theme" class="form-select" @change="fetchCommunityExhibitions">
              <option value="">全部主题</option>
              <option value="长征">长征</option>
              <option value="改革开放">改革开放</option>
            </select>
          </label>

          <label class="block">
            <span class="form-label">年级</span>
            <select v-model="filters.grade" class="form-select" @change="fetchCommunityExhibitions">
              <option value="">全部年级</option>
              <option value="七年级">七年级</option>
              <option value="八年级">八年级</option>
            </select>
          </label>

          <label class="block">
            <span class="form-label">排序</span>
            <select v-model="filters.sortBy" class="form-select" @change="fetchCommunityExhibitions">
              <option value="latest">最新</option>
              <option value="hot">最热</option>
            </select>
          </label>
        </div>
      </div>
    </section>

    <p v-if="errorMessage" class="rounded-md border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">{{ errorMessage }}</p>

    <section class="space-y-4">
      <div class="flex items-center justify-between">
        <p class="text-sm text-gray-500">
          <span class="font-medium text-gray-900">{{ exhibitions.length }}</span>
          个展厅
        </p>
      </div>

      <div v-if="loading" class="rounded-md border border-gray-200 bg-white px-4 py-10 text-center text-sm text-gray-500">
        加载中...
      </div>

      <div
        v-else-if="exhibitions.length === 0"
        class="rounded-md border border-dashed border-gray-200 bg-white px-4 py-10 text-center text-sm text-gray-500"
      >
        当前筛选条件下没有展厅内容。
      </div>

      <div v-else class="grid grid-cols-1 gap-6 md:grid-cols-3 lg:grid-cols-4">
        <ExhibitionCard
          v-for="item in exhibitions"
          :key="item.id"
          :exhibition="item"
          @like-error="handleLikeError"
        />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import ExhibitionCard from '@/components/community/ExhibitionCard.vue'
import { getCommunityExhibitions } from '@/api/modules/community'
import type { CommunityExhibition, CommunityExhibitionQuery } from '@/api/types'
import { getErrorMessage } from '@/utils/request'

const loading = ref(false)
const errorMessage = ref('')
const exhibitions = ref<CommunityExhibition[]>([])

const filters = reactive<Required<Pick<CommunityExhibitionQuery, 'grade' | 'theme' | 'sortBy'>>>({
  grade: '',
  theme: '',
  sortBy: 'latest',
})

async function fetchCommunityExhibitions() {
  loading.value = true
  errorMessage.value = ''

  try {
    const page = await getCommunityExhibitions({
      page: 1,
      pageSize: 24,
      grade: filters.grade || undefined,
      theme: filters.theme || undefined,
      sortBy: filters.sortBy,
    })
    exhibitions.value = page.list
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '社区展厅加载失败')
  } finally {
    loading.value = false
  }
}

function handleLikeError(message: string) {
  errorMessage.value = message
}

onMounted(fetchCommunityExhibitions)
</script>

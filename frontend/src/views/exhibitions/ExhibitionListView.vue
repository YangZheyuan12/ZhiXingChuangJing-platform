<template>
  <div class="mx-auto max-w-6xl min-h-screen bg-stone-50 p-6">
    <PageHero
      eyebrow="Creation Studio"
      title="创作中心"
      description="浏览我的数字展厅项目，查看协作状态和可见性信息。新建展厅改为独立创建页，可通过页面按钮或工作台快捷入口进入。"
    >
      <template #badge>
        共 {{ exhibitions.length }} 个创作项目
      </template>
      <template #actions>
        <RouterLink
          to="/exhibitions/create"
          class="page-primary-button"
        >
          开启新的数字展厅
        </RouterLink>
      </template>
    </PageHero>

    <p v-if="errorMessage" class="mb-6 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
      {{ errorMessage }}
    </p>

    <section class="mt-8 mb-10 grid grid-cols-1 gap-4 md:grid-cols-3">
      <MetricTile
        v-for="item in stats"
        :key="item.label"
        :label="item.label"
        :value="item.value"
        :hint="item.tip"
      />
    </section>

    <section>
      <div class="mb-6">
        <div>
          <p class="text-xs font-semibold uppercase tracking-[0.3em] text-stone-400">Project List</p>
          <h2 class="mt-2 text-xl font-bold text-stone-800">我的展厅</h2>
          <p class="mt-2 text-sm text-stone-500">点击任一项目卡片，进入详情页继续编辑与发布。</p>
        </div>
      </div>

      <div v-if="loading" class="rounded-xl border border-stone-200 bg-white px-4 py-6 text-sm text-stone-500">
        展厅加载中...
      </div>

      <div v-else-if="exhibitions.length === 0" class="rounded-xl border border-dashed border-stone-300 bg-white p-8 text-center">
        <p class="text-sm font-semibold uppercase tracking-[0.3em] text-stone-400">Empty</p>
        <h3 class="mt-3 text-lg font-bold text-stone-800">当前还没有展厅项目</h3>
        <p class="mt-2 text-sm text-stone-500">创建展厅后，这里会展示项目卡片与创作状态。</p>
      </div>

      <div v-else class="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
        <RouterLink
          v-for="exhibition in exhibitions"
          :key="exhibition.id"
          :to="`/exhibitions/${exhibition.id}`"
          class="relative overflow-hidden rounded-xl border border-stone-200 border-t-4 border-t-red-800 bg-white p-6 transition-all duration-300 hover:-translate-y-1 hover:shadow-xl hover:shadow-red-900/5"
        >
          <div class="pointer-events-none absolute -right-8 -top-8 h-24 w-24 rounded-full bg-red-100/60 blur-2xl" />
          <div class="relative">
            <div class="flex items-start justify-between gap-4">
              <p class="text-xs font-bold tracking-wider text-stone-400">EXHIBITION {{ exhibition.id }}</p>
              <span :class="statusPillClass(exhibition.status)">
                {{ getStatusLabel(exhibition.status) }}
              </span>
            </div>

            <h3 class="mt-2 text-lg font-bold text-stone-800">{{ exhibition.title }}</h3>
            <p class="mt-3 line-clamp-2 text-sm text-stone-500">
              {{ exhibition.summary || '围绕课程任务延展的数字展厅创作项目。' }}
            </p>

            <div class="mt-6 grid gap-2 text-xs text-stone-500">
              <div class="flex items-center justify-between">
                <span>小组名称</span>
                <span class="text-right text-red-700">{{ exhibition.groupName || '--' }}</span>
              </div>
              <div class="flex items-center justify-between">
                <span>可见性</span>
                <span class="text-right text-red-700">{{ getVisibilityLabel(exhibition.visibility) }}</span>
              </div>
            </div>

            <div class="mt-6 flex items-center justify-between border-t border-stone-100 pt-4 text-xs text-stone-500">
              <span>作者 {{ exhibition.author.nickname || exhibition.author.name }}</span>
              <span>{{ exhibition.stats.likeCount }} 赞 · {{ exhibition.stats.commentCount }} 评</span>
            </div>
          </div>
        </RouterLink>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getMyExhibitions } from '@/api/modules/exhibitions'
import type { ExhibitionSummary } from '@/api/types'
import MetricTile from '@/components/common/MetricTile.vue'
import PageHero from '@/components/common/PageHero.vue'
import { getErrorMessage } from '@/utils/request'

const loading = ref(false)
const errorMessage = ref('')
const exhibitions = ref<ExhibitionSummary[]>([])

const draftCount = computed(() => exhibitions.value.filter((item) => item.status === 'draft').length)
const publicCount = computed(() => exhibitions.value.filter((item) => item.visibility === 'public').length)
const linkedTaskCount = computed(() => exhibitions.value.filter((item) => item.taskId).length)

const stats = computed(() => [
  { label: '我的项目', value: exhibitions.value.length, tip: '当前账号名下的全部数字展厅项目。' },
  { label: '关联任务', value: linkedTaskCount.value, tip: '已经绑定课程任务的创作项目数量。' },
  { label: '公开展厅', value: publicCount.value, tip: '当前已公开展示的项目数量。' },
])

async function fetchExhibitions() {
  loading.value = true
  errorMessage.value = ''

  try {
    const page = await getMyExhibitions({ page: 1, pageSize: 20 })
    exhibitions.value = page.list
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '展厅列表加载失败')
  } finally {
    loading.value = false
  }
}

function getStatusLabel(status?: string | null) {
  switch (status) {
    case 'published':
      return '已发布'
    case 'draft':
      return '草稿'
    case 'archived':
      return '已归档'
    default:
      return status || '未知'
  }
}

function statusPillClass(status?: string | null) {
  if (status === 'published') {
    return 'rounded-full bg-red-50 px-3 py-1 text-xs font-medium text-red-700'
  }

  if (status === 'archived') {
    return 'rounded-full bg-stone-100 px-3 py-1 text-xs font-medium text-stone-600'
  }

  return 'rounded-full bg-amber-50 px-3 py-1 text-xs font-medium text-amber-700'
}

function getVisibilityLabel(visibility?: string | null) {
  switch (visibility) {
    case 'private':
      return '仅自己'
    case 'public':
      return '公开展示'
    case 'class':
      return '班级可见'
    default:
      return visibility || '--'
  }
}

onMounted(fetchExhibitions)
</script>

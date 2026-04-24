<template>
  <div class="space-y-6">
    <PageHero
      eyebrow="Museum Resource Detail"
      :title="detail?.title || '资源详情'"
      :description="detail?.description || '查看当前文博资源的主要信息、附加标签与原始详情链接。'"
      back-to="/museum"
      back-label="返回文博资源中心"
    >
      <template #badge>
        {{ detail?.category || '资源详情' }}
      </template>
      <template #meta>
        <span>提供方：{{ detail?.providerCode || '--' }}</span>
        <span>馆名：{{ detail?.museumName || '待补充' }}</span>
        <span>ID：{{ detail?.id || '--' }}</span>
      </template>
    </PageHero>

    <p v-if="errorMessage" class="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
      {{ errorMessage }}
    </p>

    <section v-if="loading && !detail" class="panel-card p-6 text-sm text-slate-500">
      资源详情加载中...
    </section>

    <template v-else-if="detail">
      <Transition name="fade">
        <div
          v-if="loading"
          class="pointer-events-none fixed right-6 top-20 z-30 rounded-full bg-white/95 px-4 py-1.5 text-xs font-medium text-red-700 shadow-md ring-1 ring-red-100"
        >
          切换中…
        </div>
      </Transition>
      <div class="grid gap-6 xl:grid-cols-[1.15fr_0.85fr]" :class="{ 'opacity-80 transition-opacity': loading }">
        <section class="panel-card p-6">
          <div class="border-b border-slate-100 pb-4">
            <p class="text-xs font-semibold uppercase tracking-[0.28em] text-red-700/70">Overview</p>
            <h2 class="mt-2 text-2xl font-semibold text-stone-800">资源信息</h2>
            <p class="mt-2 text-sm leading-7 text-stone-500">
              用于快速核对来源、分类与平台收录信息。
            </p>
          </div>

          <div class="mt-5 grid gap-3">
            <div v-for="item in detailFields" :key="item.label" class="museum-detail-row">
              <dt class="museum-detail-label">{{ item.label }}</dt>
              <dd class="museum-detail-value">{{ item.value }}</dd>
            </div>
          </div>
        </section>

        <section class="space-y-6">
          <section class="panel-card p-6">
            <div class="border-b border-slate-100 pb-4">
              <p class="text-xs font-semibold uppercase tracking-[0.28em] text-red-700/70">Preview</p>
              <h2 class="mt-2 text-2xl font-semibold text-stone-800">封面与链接</h2>
            </div>

            <div class="mt-5 space-y-4">
              <div v-if="detail.coverUrl" class="overflow-hidden rounded-[1.5rem] border border-stone-200 bg-stone-50">
                <img :src="detail.coverUrl" :alt="detail.title" class="aspect-[4/3] w-full object-cover" />
              </div>
              <div v-else class="rounded-[1.5rem] border border-dashed border-stone-300 bg-stone-50 px-5 py-8 text-sm text-stone-500">
                当前资源暂无封面图。
              </div>

              <a
                v-if="detail.detailUrl"
                :href="detail.detailUrl"
                target="_blank"
                rel="noreferrer"
                class="page-primary-button w-full justify-center"
              >
                打开原始详情
              </a>
              <div v-else class="rounded-[1.5rem] border border-stone-200 bg-stone-50 px-4 py-3 text-sm text-stone-500">
                当前资源暂无详情链接。
              </div>
            </div>
          </section>

          <section class="panel-card p-6">
            <div class="border-b border-slate-100 pb-4">
              <p class="text-xs font-semibold uppercase tracking-[0.28em] text-red-700/70">Tags</p>
              <h2 class="mt-2 text-2xl font-semibold text-stone-800">附加标签</h2>
              <p class="mt-2 text-sm leading-7 text-stone-500">
                将附加字段整理为标签，便于快速浏览主题、年代与扩展信息。
              </p>
            </div>

            <div v-if="metadataTags.length > 0" class="mt-5 flex flex-wrap gap-2.5">
              <span
                v-for="tag in metadataTags"
                :key="tag"
                class="rounded-full border border-red-200 bg-red-50 px-3 py-1.5 text-sm text-red-700"
              >
                {{ tag }}
              </span>
            </div>
            <div v-else class="mt-5 rounded-[1.5rem] border border-dashed border-stone-300 bg-stone-50 px-5 py-8 text-sm text-stone-500">
              当前资源暂无可展示的附加标签。
            </div>
          </section>
        </section>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getMuseumResourceDetail } from '@/api/modules/museum'
import type { MuseumResource } from '@/api/types'
import PageHero from '@/components/common/PageHero.vue'
import { getErrorMessage } from '@/utils/request'

const route = useRoute()

const loading = ref(false)
const errorMessage = ref('')
const detail = ref<MuseumResource | null>(null)

const metadataLabelMap: Record<string, string> = {
  author: '作者',
  creator: '作者',
  dynasty: '朝代',
  era: '年代',
  period: '时期',
  year: '年份',
  date: '时间',
  region: '地区',
  place: '地点',
  location: '地点',
  material: '材质',
  subject: '主题',
  theme: '主题',
  keyword: '关键词',
  keywords: '关键词',
  tag: '标签',
  tags: '标签',
  source: '来源',
  category: '分类',
  type: '类型',
  format: '格式',
  language: '语种',
  collection: '馆藏',
  museum: '馆藏单位',
}

const resourceId = computed(() => Number(route.params.resourceId))

const detailFields = computed(() => {
  if (!detail.value) {
    return []
  }

  return [
    { label: '资源标题', value: detail.value.title || '--' },
    { label: '资源分类', value: detail.value.category || '--' },
    { label: '提供方编码', value: detail.value.providerCode || '--' },
    { label: '馆名', value: detail.value.museumName || '待补充' },
    { label: '资源编号', value: String(detail.value.id) },
  ]
})

const metadataTags = computed(() => buildMetadataTags(detail.value?.metadata))

function extractMetadataValues(value: unknown): string[] {
  if (value == null) {
    return []
  }

  if (Array.isArray(value)) {
    return value.flatMap((item) => extractMetadataValues(item))
  }

  if (typeof value === 'object') {
    return Object.values(value as Record<string, unknown>).flatMap((item) => extractMetadataValues(item))
  }

  if (typeof value === 'boolean') {
    return [value ? '是' : '否']
  }

  return [String(value)]
}

function sanitizeTagContent(value: string) {
  const text = value
    .replace(/https?:\/\/\S+/g, ' ')
    .replace(/[A-Za-z_]+/g, ' ')
    .replace(/[()[\]{}]+/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
    .replace(/^[^\u4e00-\u9fa50-9]+|[^\u4e00-\u9fa50-9]+$/g, '')

  return /[\u4e00-\u9fa50-9]/.test(text) ? text : ''
}

function buildMetadataTags(metadata?: Record<string, unknown>) {
  if (!metadata) {
    return []
  }

  const tags = new Set<string>()

  Object.entries(metadata).forEach(([key, value]) => {
    const label = metadataLabelMap[key]
    const values = extractMetadataValues(value)

    values.forEach((item) => {
      const content = sanitizeTagContent(item)

      if (label && content) {
        tags.add(`${label}：${content}`)
        return
      }

      if (label) {
        tags.add(label)
        return
      }

      if (content) {
        tags.add(content)
      }
    })
  })

  return Array.from(tags)
}

async function fetchDetail() {
  if (!Number.isFinite(resourceId.value) || resourceId.value <= 0) {
    detail.value = null
    errorMessage.value = '资源编号无效'
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    detail.value = await getMuseumResourceDetail(resourceId.value)
  } catch (error) {
    detail.value = null
    errorMessage.value = getErrorMessage(error, '资源详情加载失败')
  } finally {
    loading.value = false
  }
}

watch(resourceId, fetchDetail)
onMounted(fetchDetail)
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.museum-detail-row {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  border: 1px solid #e7e5e4;
  border-radius: 22px;
  background: #ffffff;
  padding: 1rem 1.15rem;
}

.museum-detail-label {
  font-size: 0.78rem;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: #a8a29e;
}

.museum-detail-value {
  word-break: break-all;
  font-size: 0.95rem;
  line-height: 1.8;
  color: #44403c;
}

@media (min-width: 960px) {
  .museum-detail-row {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    gap: 1.5rem;
  }

  .museum-detail-label {
    width: 180px;
    flex-shrink: 0;
  }

  .museum-detail-value {
    flex: 1;
    text-align: right;
  }
}
</style>

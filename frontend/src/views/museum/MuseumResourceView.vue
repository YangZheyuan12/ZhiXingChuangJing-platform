<template>
  <div class="museum-page min-h-full w-full rounded-[32px] px-4 py-6 md:px-6 lg:px-8">
    <div class="mx-auto flex w-full max-w-[1440px] flex-col gap-6">
      <PageHero
        eyebrow="Museum Resource Center"
        title="文博资源中心"
        description="以红色文化叙事为主线，统一管理资源同步、资源检索、馆藏条目浏览与详情核验，支撑课堂创作和数字展厅内容生产。"
      >
        <template #chips>
          <span class="page-hero-chip">馆藏同步</span>
          <span class="page-hero-chip">资源检索</span>
          <span class="page-hero-chip">详情核验</span>
        </template>
      </PageHero>

      <p
        v-if="errorMessage"
        class="rounded-[24px] border border-red-200 bg-red-50 px-5 py-4 text-sm text-red-700"
      >
        {{ errorMessage }}
      </p>

      <section class="museum-card px-6 py-6 md:px-8">
        <div class="mb-5 border-b border-stone-200 pb-4">
          <p class="text-xs font-semibold uppercase tracking-[0.28em] text-red-700/70">Step 01</p>
          <h2 class="mt-2 text-2xl font-semibold text-stone-800">资源同步</h2>
          <p class="mt-2 text-sm leading-7 text-stone-500">
            选择提供方、资源分类和关键词后发起同步，将最新馆藏数据拉取到平台资源池。
          </p>
        </div>

        <form class="flex flex-col gap-4 xl:flex-row xl:items-end" @submit.prevent="handleSync">
          <label class="museum-field xl:flex-1">
            <span class="museum-label">提供方编码</span>
            <input
              v-model="syncForm.providerCode"
              class="museum-input"
              placeholder="如 yanan_memorial"
            />
          </label>
          <label class="museum-field xl:flex-1">
            <span class="museum-label">资源分类</span>
            <input
              v-model="syncForm.category"
              class="museum-input"
              placeholder="如 红色文化"
            />
          </label>
          <label class="museum-field xl:flex-1">
            <span class="museum-label">关键词</span>
            <input
              v-model="syncForm.keyword"
              class="museum-input"
              placeholder="如 数字策展"
            />
          </label>
          <button
            type="submit"
            :disabled="syncing"
            class="museum-solid-button w-full xl:w-auto xl:min-w-[168px]"
          >
            {{ syncing ? '同步中...' : '开始同步' }}
          </button>
        </form>
      </section>

      <section class="museum-card px-6 py-6 md:px-8">
        <div class="mb-5 border-b border-stone-200 pb-4">
          <p class="text-xs font-semibold uppercase tracking-[0.28em] text-red-700/70">Step 02</p>
          <h2 class="mt-2 text-2xl font-semibold text-stone-800">资源检索</h2>
          <p class="mt-2 text-sm leading-7 text-stone-500">
            使用提供方、分类和关键词进行精确筛选，快速定位适合课程与展厅的资源条目。
          </p>
        </div>

        <div class="flex flex-col gap-4 xl:flex-row xl:items-end">
          <label class="museum-field xl:flex-1">
            <span class="museum-label">提供方</span>
            <input
              v-model="filters.providerCode"
              class="museum-input"
              placeholder="按来源筛选"
            />
          </label>
          <label class="museum-field xl:flex-1">
            <span class="museum-label">分类</span>
            <input
              v-model="filters.category"
              class="museum-input"
              placeholder="按分类筛选"
            />
          </label>
          <label class="museum-field xl:flex-1">
            <span class="museum-label">关键词</span>
            <input
              v-model="filters.keyword"
              class="museum-input"
              placeholder="输入标题或主题词"
            />
          </label>
        </div>

        <div class="mt-5 flex flex-wrap gap-3">
          <button type="button" class="museum-outline-button" @click="fetchResources">
            查询资源
          </button>
          <button type="button" class="museum-outline-button" @click="resetFilters">
            重置条件
          </button>
        </div>
      </section>

      <section class="museum-card px-6 py-6 md:px-8">
        <div class="mb-5 border-b border-stone-200 pb-4">
          <div class="flex flex-wrap items-end justify-between gap-4">
            <div>
              <p class="text-xs font-semibold uppercase tracking-[0.28em] text-red-700/70">Step 03</p>
              <h2 class="mt-2 text-2xl font-semibold text-stone-800">资源列表</h2>
              <p class="mt-2 text-sm leading-7 text-stone-500">
                采用纵向档案列表展示，点击任意条目后，会跳转到独立详情页查看完整信息与附加标签。
              </p>
            </div>
            <div class="rounded-full border border-stone-200 bg-stone-50 px-4 py-2 text-sm text-stone-500">
              共 {{ resources.length }} 条资源
            </div>
          </div>
        </div>

        <div v-if="loading && resources.length === 0" class="rounded-[22px] border border-dashed border-stone-300 bg-stone-50 px-5 py-8 text-sm text-stone-500">
          正在加载文博资源，请稍候。
        </div>

        <div
          v-else-if="!loading && resources.length === 0"
          class="rounded-[22px] border border-dashed border-stone-300 bg-stone-50 px-5 py-8 text-sm text-stone-500"
        >
          当前没有可展示的资源数据。你可以先执行一次资源同步，或调整筛选条件后重新查询。
        </div>

        <div v-else class="flex flex-col gap-4" :class="{ 'opacity-70 transition-opacity': loading }">
          <RouterLink
            v-for="resource in resources"
            :key="resource.id"
            :to="`/museum/${resource.id}`"
            class="block"
          >
            <article class="museum-list-item">
              <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                <div class="min-w-0 flex-1">
                  <div class="flex flex-wrap items-center gap-3">
                    <h3 class="truncate text-xl font-semibold tracking-[0.02em] text-stone-800">
                      {{ resource.title }}
                    </h3>
                    <span class="museum-tag">{{ resource.category || '未分类' }}</span>
                  </div>
                  <p class="mt-3 text-sm leading-7 text-stone-500">
                    {{ resource.description || '该资源暂未补充说明，可进入详情页查看扩展字段与访问链接。' }}
                  </p>
                </div>

                <div class="flex shrink-0 flex-wrap items-center gap-x-5 gap-y-2 text-sm text-stone-500 lg:justify-end">
                  <span>来源：{{ resource.providerCode }}</span>
                  <span>馆名：{{ resource.museumName || '待补充' }}</span>
                  <span>ID：{{ resource.id }}</span>
                </div>
              </div>
            </article>
          </RouterLink>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { getErrorMessage } from '@/utils/request'
import { getMuseumResources, syncMuseumResources } from '@/api/modules/museum'
import type { MuseumResource } from '@/api/types'
import PageHero from '@/components/common/PageHero.vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
const loading = ref(false)
const syncing = ref(false)
const errorMessage = ref('')
const resources = ref<MuseumResource[]>([])

const syncForm = reactive({
  providerCode: 'yanan_memorial',
  category: '红色文化',
  keyword: '数字策展',
})

const filters = reactive({
  providerCode: '',
  category: '',
  keyword: '',
})

async function fetchResources() {
  loading.value = true
  errorMessage.value = ''

  try {
    const page = await getMuseumResources({
      page: 1,
      pageSize: 20,
      providerCode: filters.providerCode || undefined,
      category: filters.category || undefined,
      keyword: filters.keyword || undefined,
    })

    resources.value = page.list
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '文博资源加载失败')
  } finally {
    loading.value = false
  }
}

async function handleSync() {
  syncing.value = true
  errorMessage.value = ''

  try {
    await syncMuseumResources(syncForm)
    appStore.showToast('文博资源同步成功', 'success')
    Object.assign(filters, {
      providerCode: syncForm.providerCode,
      category: syncForm.category,
      keyword: syncForm.keyword,
    })
    await fetchResources()
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '文博资源同步失败')
  } finally {
    syncing.value = false
  }
}

function resetFilters() {
  Object.assign(filters, {
    providerCode: '',
    category: '',
    keyword: '',
  })
  fetchResources()
}

onMounted(fetchResources)
</script>

<style scoped>
.museum-page {
  background:
    radial-gradient(circle at top right, rgba(159, 43, 34, 0.08), transparent 24%),
    linear-gradient(180deg, #fafaf9 0%, #fef2f2 100%);
}

.museum-card {
  border: 1px solid #e7e5e4;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 18px 40px rgba(120, 113, 108, 0.08);
}

.museum-field {
  display: block;
}

.museum-label {
  margin-bottom: 0.65rem;
  display: block;
  font-size: 0.78rem;
  font-weight: 600;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: #9a3412;
}

.museum-input {
  width: 100%;
  border: 1px solid #d6d3d1;
  border-radius: 18px;
  background: #ffffff;
  padding: 0.95rem 1rem;
  color: #292524;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background-color 0.2s ease;
}

.museum-input::placeholder {
  color: #a8a29e;
}

.museum-input:focus {
  border-color: #9b2226;
  box-shadow: 0 0 0 4px rgba(155, 34, 38, 0.1);
  background: #ffffff;
}

.museum-solid-button {
  border: 1px solid #991b1b;
  border-radius: 18px;
  background: #991b1b;
  padding: 0.95rem 1.4rem;
  font-size: 0.95rem;
  font-weight: 600;
  color: #fff7f5;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background-color 0.2s ease;
}

.museum-solid-button:hover:enabled {
  transform: translateY(-1px);
  background: #7f1d1d;
  box-shadow: 0 16px 30px rgba(153, 27, 27, 0.18);
}

.museum-solid-button:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.museum-outline-button {
  border: 1px solid #fecaca;
  border-radius: 18px;
  background: transparent;
  padding: 0.9rem 1.3rem;
  font-size: 0.95rem;
  font-weight: 600;
  color: #991b1b;
  transition: transform 0.2s ease, background-color 0.2s ease, border-color 0.2s ease;
}

.museum-outline-button:hover {
  transform: translateY(-1px);
  border-color: #fca5a5;
  background: rgba(239, 68, 68, 0.08);
}

.museum-list-item {
  border: 1px solid #e7e5e4;
  border-radius: 24px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.94), rgba(250, 250, 249, 0.96));
  padding: 1.35rem 1.4rem;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.museum-list-item:hover {
  transform: translateY(-2px);
  border-color: #fca5a5;
  box-shadow: 0 16px 26px rgba(120, 113, 108, 0.08);
}

.museum-list-item-active {
  border-color: #991b1b;
  box-shadow: 0 18px 30px rgba(153, 27, 27, 0.12);
}

.museum-tag {
  display: inline-flex;
  align-items: center;
  border: 1px solid #fecaca;
  border-radius: 9999px;
  background: #fef2f2;
  padding: 0.35rem 0.8rem;
  font-size: 0.78rem;
  font-weight: 600;
  color: #991b1b;
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

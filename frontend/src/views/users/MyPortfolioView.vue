<template>
  <div class="space-y-6">
    <PageHero
      eyebrow="Portfolio"
      title="我的作品集"
      description="汇总当前账号已发布的展厅作品，可快速跳转到社区页和个人主页视角。"
    />

    <p v-if="errorMessage" class="rounded-xl bg-rose-50 px-4 py-3 text-sm text-rose-600">{{ errorMessage }}</p>

    <section class="panel-card p-6">
      <SectionHeader title="已发布作品" description="查看当前账号已经发布的展厅作品。" />
      <EmptyStatePanel
        v-if="!loading && portfolio.length === 0"
        eyebrow="Portfolio"
        title="作品集为空"
        description="先发布至少一个展厅，作品集页面才会展示内容。"
      />
      <div v-else class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <article v-for="item in portfolio" :key="item.exhibitionId" class="rounded-2xl border border-slate-200 p-5">
          <h3 class="font-medium text-slate-900">{{ item.title }}</h3>
          <p class="mt-3 text-sm text-slate-500">发布时间：{{ formatDateTime(item.publishedAt) }}</p>
          <p class="mt-2 text-sm text-slate-500">累计点赞：{{ item.likeCount }}</p>
          <div class="mt-4 flex gap-3">
            <RouterLink
              :to="`/community/${item.exhibitionId}`"
              class="rounded-full bg-brand-50 px-3 py-1 text-sm text-brand-700 transition hover:bg-brand-100"
            >
              社区查看
            </RouterLink>
            <RouterLink
              v-if="authStore.user?.id"
              :to="`/users/${authStore.user.id}/homepage`"
              class="rounded-full bg-slate-100 px-3 py-1 text-sm text-slate-600 transition hover:bg-slate-200"
            >
              我的主页
            </RouterLink>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getErrorMessage } from '@/utils/request'
import { getMyPortfolio } from '@/api/modules/users'
import type { PortfolioItem } from '@/api/types'
import EmptyStatePanel from '@/components/common/EmptyStatePanel.vue'
import PageHero from '@/components/common/PageHero.vue'
import SectionHeader from '@/components/common/SectionHeader.vue'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime } from '@/utils/format'

const authStore = useAuthStore()
const loading = ref(false)
const errorMessage = ref('')
const portfolio = ref<PortfolioItem[]>([])

async function fetchPortfolio() {
  loading.value = true
  errorMessage.value = ''

  try {
    portfolio.value = await getMyPortfolio()
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '作品集加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchPortfolio)
</script>

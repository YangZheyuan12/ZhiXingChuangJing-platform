<template>
  <div class="space-y-6">
    <PageHero
      eyebrow="Classes"
      title="班级空间"
      description="这里汇集你所属的班级、公告和成员入口。点击卡片进入班级详情，查看成员与公告列表。"
    />

    <p v-if="errorMessage" class="rounded-xl bg-rose-50 px-4 py-3 text-sm text-rose-600">{{ errorMessage }}</p>

    <section class="panel-card p-6">
      <SectionHeader title="我的班级" description="点击任一班级卡片进入详情页。" />
      <div v-if="loading" class="text-sm text-slate-500">加载中...</div>
      <EmptyStatePanel
        v-else-if="classes.length === 0"
        eyebrow="Classes"
        title="当前没有可见班级"
        description="请先由教师将当前账号加入班级。"
      />
      <div v-else class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <RouterLink
          v-for="item in classes"
          :key="item.id"
          :to="`/classes/${item.id}`"
          class="group rounded-[1.75rem] border border-white/60 bg-white/90 p-5 shadow-panel transition hover:-translate-y-0.5 hover:border-brand-200"
        >
          <p class="text-xs uppercase tracking-[0.2em] text-slate-400">Class {{ item.id }}</p>
          <h3 class="mt-3 text-xl font-semibold text-slate-900 transition group-hover:text-brand-700">{{ item.name }}</h3>
          <div class="mt-5 rounded-2xl bg-slate-50/80 p-4 text-sm text-slate-500">
            <p>年级：{{ item.grade || '--' }}</p>
            <p class="mt-2">学年：{{ item.academicYear || '--' }}</p>
          </div>
        </RouterLink>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getErrorMessage } from '@/utils/request'
import { getMyClasses } from '@/api/modules/classes'
import type { ClassInfo } from '@/api/types'
import EmptyStatePanel from '@/components/common/EmptyStatePanel.vue'
import PageHero from '@/components/common/PageHero.vue'
import SectionHeader from '@/components/common/SectionHeader.vue'

const loading = ref(false)
const errorMessage = ref('')
const classes = ref<ClassInfo[]>([])

async function fetchClasses() {
  loading.value = true
  errorMessage.value = ''

  try {
    classes.value = await getMyClasses()
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '班级列表加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchClasses)
</script>

<template>
  <div class="mx-auto max-w-6xl min-h-screen bg-stone-50 p-6">
    <PageHero
      eyebrow="Create Exhibition"
      title="开启新的数字展厅"
      description="从课程任务延伸到沉浸式成果创作，先完成展厅壳体搭建，再进入详情页保存版本、配置数字人与正式发布。"
      back-to="/exhibitions"
      back-label="返回创作中心"
    >
      <template #badge>
        创作初始化
      </template>
    </PageHero>

    <p v-if="errorMessage" class="mb-6 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
      {{ errorMessage }}
    </p>

    <section class="mb-10 grid grid-cols-1 gap-4 md:grid-cols-3">
      <MetricTile
        v-for="item in stats"
        :key="item.label"
        :label="item.label"
        :value="item.value"
        :hint="item.tip"
      />
    </section>

    <section class="overflow-hidden rounded-2xl border border-stone-100 bg-white shadow-lg shadow-stone-200/50">
      <div class="border-b border-stone-100 bg-white p-8">
        <p class="text-xs font-semibold uppercase tracking-[0.34em] text-red-700/70">Creation Panel</p>
        <h2 class="mt-3 text-3xl font-bold leading-tight text-stone-800">建立新的展厅项目</h2>
        <p class="mt-3 max-w-3xl text-sm leading-7 text-stone-500">
          先确认关联任务、展示可见性和封面摘要，再进入详情页继续处理版本与协作。
        </p>
      </div>

      <div class="bg-white p-8">
        <div class="mb-8">
          <h3 class="text-xl font-bold text-stone-800">表单基础信息</h3>
          <p class="mt-2 text-sm leading-6 text-stone-500">创建成功后会返回创作中心，后续版本、成员与发布动作在详情页继续完成。</p>
        </div>

        <form class="grid grid-cols-1 gap-6 md:grid-cols-2" @submit.prevent="handleCreateExhibition">
          <label class="block md:col-span-2">
            <span :class="labelClass">展厅标题</span>
            <input
              v-model="exhibitionForm.title"
              :class="fieldClass"
              maxlength="128"
              placeholder="例如：长征精神沉浸式数字展厅"
            />
          </label>

          <label class="block">
            <span :class="labelClass">关联任务</span>
            <div class="relative">
              <select v-model="exhibitionForm.taskId" :class="selectClass">
                <option value="">暂不关联任务</option>
                <option v-for="task in taskOptions" :key="task.id" :value="String(task.id)">{{ task.title }}</option>
              </select>
              <svg class="pointer-events-none absolute right-4 top-1/2 h-4 w-4 -translate-y-1/2 text-stone-400" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M5 7.5L10 12.5L15 7.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
              </svg>
            </div>
          </label>

          <label class="block">
            <span :class="labelClass">可见性</span>
            <div class="relative">
              <select v-model="exhibitionForm.visibility" :class="selectClass">
                <option value="private">仅自己</option>
                <option value="class">班级可见</option>
                <option value="public">公开展示</option>
              </select>
              <svg class="pointer-events-none absolute right-4 top-1/2 h-4 w-4 -translate-y-1/2 text-stone-400" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M5 7.5L10 12.5L15 7.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
              </svg>
            </div>
          </label>

          <label class="block">
            <span :class="labelClass">小组名称</span>
            <input
              v-model="exhibitionForm.groupName"
              :class="fieldClass"
              placeholder="例如：红色记忆策展组"
            />
          </label>

          <div class="block">
            <span :class="labelClass">当前模式</span>
            <div class="rounded-lg border border-stone-200 bg-stone-50 px-4 py-3 text-sm text-stone-600">
              {{ getVisibilityLabel(exhibitionForm.visibility) }} · {{ exhibitionForm.taskId ? '已绑定课程任务' : '独立创作项目' }}
            </div>
          </div>

          <label class="block md:col-span-2">
            <span :class="labelClass">展厅封面</span>
            <div class="rounded-xl border border-dashed border-red-200 bg-red-50/40 p-4">
              <input
                type="file"
                accept="image/*"
                :class="fileClass"
                :disabled="coverUploading"
                @change="handleCoverFileChange"
              />
              <div class="mt-3 flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
                <p class="text-xs leading-5 text-stone-500">
                  {{ coverUploading ? '封面上传中...' : exhibitionForm.coverUrl ? '封面已上传，创建后将在列表中展示。' : '建议使用更具场景感的横版封面。' }}
                </p>
                <img
                  v-if="exhibitionForm.coverUrl"
                  :src="exhibitionForm.coverUrl"
                  alt="展厅封面"
                  class="h-20 w-full rounded-lg border border-stone-200 object-cover md:w-40"
                />
              </div>
            </div>
          </label>

          <label class="block md:col-span-2">
            <span :class="labelClass">展厅摘要</span>
            <textarea
              v-model="exhibitionForm.summary"
              rows="4"
              :class="textareaClass"
              placeholder="描述展厅主题、叙事方向与预期展示效果"
            />
          </label>

          <div class="mt-8 flex items-center justify-between border-t border-stone-100 pt-6 md:col-span-2">
            <p class="max-w-md text-sm leading-6 text-stone-500">
              建议先绑定任务和可见性，再进入详情页继续处理版本发布与成员协作。
            </p>
            <button
              type="submit"
              :disabled="creatingExhibition"
              class="page-primary-button px-6"
            >
              {{ creatingExhibition ? '创建中...' : '创建展厅' }}
            </button>
          </div>
        </form>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { uploadAsset } from '@/api/modules/assets'
import { createExhibition, getMyExhibitions } from '@/api/modules/exhibitions'
import { getTaskList } from '@/api/modules/tasks'
import type { CreateExhibitionRequest, ExhibitionSummary, TaskSummary } from '@/api/types'
import MetricTile from '@/components/common/MetricTile.vue'
import PageHero from '@/components/common/PageHero.vue'
import { useAppStore } from '@/stores/app'
import { getErrorMessage } from '@/utils/request'

const router = useRouter()
const appStore = useAppStore()

const fieldClass = 'w-full rounded-lg border border-stone-200 bg-white px-4 py-2.5 text-sm text-stone-900 shadow-sm transition-all placeholder:text-stone-400 focus:border-slate-400 focus:outline-none focus:ring-2 focus:ring-slate-200/70'
const selectClass = 'w-full rounded-lg border border-stone-200 bg-white px-4 py-2.5 pr-11 text-sm text-stone-900 shadow-sm transition-all appearance-none placeholder:text-stone-400 focus:border-slate-400 focus:outline-none focus:ring-2 focus:ring-slate-200/70 clean-select'
const textareaClass = `${fieldClass} min-h-[120px] resize-y`
const fileClass = 'block w-full rounded-lg border border-stone-200 bg-white px-4 py-2.5 text-sm text-stone-700 shadow-sm transition-all file:mr-4 file:rounded-lg file:border-0 file:bg-red-800 file:px-4 file:py-2 file:text-sm file:font-medium file:text-white hover:file:bg-red-900 focus:border-slate-400 focus:outline-none focus:ring-2 focus:ring-slate-200/70 disabled:cursor-not-allowed disabled:bg-stone-100'
const labelClass = 'mb-2 block text-sm font-medium text-stone-700'

const creatingExhibition = ref(false)
const coverUploading = ref(false)
const errorMessage = ref('')
const exhibitions = ref<ExhibitionSummary[]>([])
const taskOptions = ref<TaskSummary[]>([])

const exhibitionForm = reactive({
  taskId: '',
  title: '',
  summary: '',
  coverUrl: '',
  visibility: 'class' as 'private' | 'class' | 'public',
  groupName: '',
})

const draftCount = computed(() => exhibitions.value.filter((item) => item.status === 'draft').length)
const publicCount = computed(() => exhibitions.value.filter((item) => item.visibility === 'public').length)
const stats = computed(() => [
  { label: '可关联任务', value: taskOptions.value.length, tip: '可直接绑定到创作流程的课程任务' },
  { label: '草稿项目', value: draftCount.value, tip: '当前账号下尚未发布的项目数' },
  { label: '公开展厅', value: publicCount.value, tip: '当前账号下已公开展示的项目数' },
])

async function fetchMeta() {
  try {
    const [myPage, taskPage] = await Promise.all([
      getMyExhibitions({ page: 1, pageSize: 20 }),
      getTaskList({ page: 1, pageSize: 50 }),
    ])
    exhibitions.value = myPage.list
    taskOptions.value = taskPage.list
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '展厅基础数据加载失败')
  }
}

async function handleCreateExhibition() {
  creatingExhibition.value = true
  errorMessage.value = ''

  try {
    const payload: CreateExhibitionRequest = {
      taskId: exhibitionForm.taskId ? Number(exhibitionForm.taskId) : null,
      title: exhibitionForm.title.trim(),
      summary: exhibitionForm.summary.trim() || null,
      coverUrl: exhibitionForm.coverUrl.trim() || null,
      visibility: exhibitionForm.visibility,
      groupName: exhibitionForm.groupName.trim() || null,
    }

    await createExhibition(payload)
    appStore.showToast('展厅已创建', 'success')
    await router.push('/exhibitions')
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '展厅创建失败')
  } finally {
    creatingExhibition.value = false
  }
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

async function handleCoverFileChange(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]

  if (!file) {
    return
  }

  coverUploading.value = true
  errorMessage.value = ''

  try {
    const result = await uploadAsset(file, {
      folder: 'exhibitions/covers',
      bizType: 'exhibition_cover',
    })
    exhibitionForm.coverUrl = result.fileUrl
    appStore.showToast('展厅封面已上传', 'success')
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '展厅封面上传失败')
  } finally {
    coverUploading.value = false
    target.value = ''
  }
}

onMounted(fetchMeta)
</script>

<style scoped>
.clean-select {
  background-image: none !important;
}

.clean-select::-ms-expand {
  display: none;
}
</style>

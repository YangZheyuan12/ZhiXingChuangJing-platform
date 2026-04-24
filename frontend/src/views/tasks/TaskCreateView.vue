<template>
  <div class="mx-auto max-w-6xl min-h-screen bg-stone-50 p-6">
    <PageHero
      eyebrow="Create Task"
      title="发布课程任务"
      description="统一维护任务标题、时间节奏、评价标准与背景资料，让教学活动从布置到投放保持清晰有序。"
      back-to="/tasks"
      back-label="返回任务中心"
    >
      <template #badge>
        仅教师 / 管理员可用
      </template>
    </PageHero>

    <p v-if="errorMessage" class="mb-6 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
      {{ errorMessage }}
    </p>

    <template v-if="canCreateTask">
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
          <p class="text-xs font-semibold uppercase tracking-[0.34em] text-red-700/70">Publishing Panel</p>
          <h2 class="mt-3 text-3xl font-bold leading-tight text-stone-800">建立新的课程任务</h2>
          <p class="mt-3 max-w-3xl text-sm leading-7 text-stone-500">
            从任务标题、时间节点到评价标准与资料链接，集中完成一次性配置，再投放到目标班级。
          </p>
        </div>

        <div class="bg-white p-8">
        <div class="mb-8">
          <h3 class="text-xl font-bold text-stone-800">表单基础信息</h3>
          <p class="mt-2 text-sm leading-6 text-stone-500">提交后将即时刷新任务列表，并同步到已勾选班级。</p>
        </div>

        <form class="grid grid-cols-1 gap-6 md:grid-cols-2" @submit.prevent="handleCreateTask">
          <label class="block md:col-span-2">
            <span :class="labelClass">任务标题</span>
            <input
              v-model="taskForm.title"
              :class="fieldClass"
              maxlength="128"
              placeholder="例如：长征精神数字展厅创作"
            />
          </label>

          <label class="block">
            <span :class="labelClass">开始时间</span>
            <div class="relative">
              <input
                v-model="taskForm.startTime"
                type="datetime-local"
                :class="[fieldClass, 'calendar-field appearance-none pr-4', !taskForm.startTime && 'empty-calendar']"
              />
              <svg
                v-if="!taskForm.startTime"
                class="pointer-events-none absolute left-1/2 top-1/2 h-5 w-5 -translate-x-1/2 -translate-y-1/2 text-stone-400"
                viewBox="0 0 24 24"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path d="M8 2V5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" />
                <path d="M16 2V5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" />
                <path d="M3.5 9.5H20.5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" />
                <rect x="3.5" y="4.5" width="17" height="16" rx="2.5" stroke="currentColor" stroke-width="1.8" />
              </svg>
            </div>
          </label>

          <label class="block">
            <span :class="labelClass">截止时间</span>
            <div class="relative">
              <input
                v-model="taskForm.dueTime"
                type="datetime-local"
                :class="[fieldClass, 'calendar-field appearance-none pr-4', !taskForm.dueTime && 'empty-calendar']"
              />
              <svg
                v-if="!taskForm.dueTime"
                class="pointer-events-none absolute left-1/2 top-1/2 h-5 w-5 -translate-x-1/2 -translate-y-1/2 text-stone-400"
                viewBox="0 0 24 24"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path d="M8 2V5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" />
                <path d="M16 2V5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" />
                <path d="M3.5 9.5H20.5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" />
                <rect x="3.5" y="4.5" width="17" height="16" rx="2.5" stroke="currentColor" stroke-width="1.8" />
              </svg>
            </div>
          </label>

          <label class="block md:col-span-2">
            <span :class="labelClass">任务说明</span>
            <textarea
              v-model="taskForm.description"
              rows="4"
              :class="textareaClass"
              placeholder="补充学习目标、交付要求与任务背景"
            />
          </label>

          <label class="block md:col-span-2">
            <span :class="labelClass">评价标准</span>
            <textarea
              v-model="taskForm.evaluationCriteria"
              rows="3"
              :class="textareaClass"
              placeholder="例如：主题表达、史料引用、叙事结构、互动设计"
            />
          </label>

          <label class="block">
            <span :class="labelClass">背景资料标题</span>
            <input
              v-model="taskForm.materialTitle"
              :class="fieldClass"
              placeholder="例如：课程参考资料"
            />
          </label>

          <label class="block">
            <span :class="labelClass">背景资料链接</span>
            <input
              v-model="taskForm.materialUrl"
              :class="fieldClass"
              placeholder="https://"
            />
          </label>

          <label class="block md:col-span-2">
            <span :class="labelClass">任务封面</span>
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
                  {{ coverUploading ? '封面上传中...' : taskForm.coverUrl ? '封面已上传，可继续提交任务。' : '建议使用横版封面，强化任务识别度。' }}
                </p>
                <img
                  v-if="taskForm.coverUrl"
                  :src="taskForm.coverUrl"
                  alt="任务封面"
                  class="h-20 w-full rounded-lg border border-stone-200 object-cover md:w-40"
                />
              </div>
            </div>
          </label>

          <div class="md:col-span-2">
            <div class="mb-3 flex items-center justify-between gap-3">
              <span :class="labelClass">目标班级</span>
              <span class="rounded-full bg-red-50 px-3 py-1 text-xs font-medium text-red-700">
                已选择 {{ taskForm.targetClassIds.length }} / {{ classes.length }}
              </span>
            </div>

            <div v-if="classes.length > 0" class="grid gap-4 sm:grid-cols-2">
              <label
                v-for="classItem in classes"
                :key="classItem.id"
                :class="[
                  'flex cursor-pointer items-start gap-3 rounded-xl border p-4 transition-all',
                  taskForm.targetClassIds.includes(classItem.id)
                    ? 'border-red-300 bg-red-50 shadow-sm shadow-red-100/60'
                    : 'border-stone-200 bg-white hover:border-red-200 hover:bg-red-50/40',
                ]"
              >
                <input
                  v-model="taskForm.targetClassIds"
                  :value="classItem.id"
                  type="checkbox"
                  class="mt-1 h-4 w-4 rounded border-stone-300 text-red-700 focus:ring-slate-200"
                />
                <span class="min-w-0">
                  <span class="block text-sm font-semibold text-stone-800">{{ classItem.name }}</span>
                  <span class="mt-1 block text-xs leading-5 text-stone-500">
                    {{ classItem.grade || '未设置年级' }} · {{ classItem.academicYear || '未设置学年' }}
                  </span>
                </span>
              </label>
            </div>

            <div v-else class="rounded-xl border border-stone-200 bg-stone-50 px-4 py-5 text-sm text-stone-500">
              当前账号下没有可投放班级，暂时无法发布任务。
            </div>
          </div>

          <div class="mt-8 flex items-center justify-between border-t border-stone-100 pt-6 md:col-span-2">
            <p class="max-w-md text-sm leading-6 text-stone-500">
              建议先补齐时间与评价标准，再进行班级投放，减少后续重复沟通。
            </p>
            <button
              type="submit"
              :disabled="creatingTask || classes.length === 0"
              class="page-primary-button px-6"
            >
              {{ creatingTask ? '发布中...' : '发布任务' }}
            </button>
          </div>
        </form>
        </div>
      </section>
    </template>

    <section v-else class="rounded-2xl border border-red-100 bg-white p-8 shadow-lg shadow-stone-200/50">
      <h2 class="text-xl font-bold text-stone-800">当前账号无发布权限</h2>
      <p class="mt-3 max-w-2xl text-sm leading-6 text-stone-500">
        仅教师与管理员可以发布课程任务。学生账号可返回任务中心浏览任务并参与后续学习流程。
      </p>
      <RouterLink
        to="/tasks"
        class="page-primary-button mt-6 px-6"
      >
        返回任务中心
      </RouterLink>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { uploadAsset } from '@/api/modules/assets'
import { getMyClasses } from '@/api/modules/classes'
import { createTask } from '@/api/modules/tasks'
import type { ClassInfo, CreateTaskRequest } from '@/api/types'
import MetricTile from '@/components/common/MetricTile.vue'
import PageHero from '@/components/common/PageHero.vue'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { getErrorMessage } from '@/utils/request'

const router = useRouter()
const authStore = useAuthStore()
const appStore = useAppStore()

const fieldClass = 'w-full rounded-lg border border-stone-200 bg-white px-4 py-2.5 text-sm text-stone-900 shadow-sm transition-all placeholder:text-stone-400 focus:border-slate-400 focus:outline-none focus:ring-2 focus:ring-slate-200/70'
const textareaClass = `${fieldClass} min-h-[120px] resize-y`
const fileClass = 'block w-full rounded-lg border border-stone-200 bg-white px-4 py-2.5 text-sm text-stone-700 shadow-sm transition-all file:mr-4 file:rounded-lg file:border-0 file:bg-red-800 file:px-4 file:py-2 file:text-sm file:font-medium file:text-white hover:file:bg-red-900 focus:border-slate-400 focus:outline-none focus:ring-2 focus:ring-slate-200/70 disabled:cursor-not-allowed disabled:bg-stone-100'
const labelClass = 'mb-2 block text-sm font-medium text-stone-700'

const creatingTask = ref(false)
const coverUploading = ref(false)
const errorMessage = ref('')
const classes = ref<ClassInfo[]>([])

const taskForm = reactive({
  title: '',
  description: '',
  evaluationCriteria: '',
  startTime: '',
  dueTime: '',
  coverUrl: '',
  materialTitle: '',
  materialUrl: '',
  targetClassIds: [] as number[],
})

const canCreateTask = computed(() => ['teacher', 'admin'].includes(authStore.user?.role || ''))
const stats = computed(() => [
  { label: '可投放班级', value: classes.value.length, tip: '当前账号可直接触达的班级范围' },
  { label: '已选班级', value: taskForm.targetClassIds.length, tip: '本次任务将同步分发的班级数' },
  { label: '资料附件', value: Number(Boolean(taskForm.materialTitle && taskForm.materialUrl)), tip: '当前任务附带的资料链接数' },
])

async function fetchClasses() {
  if (!canCreateTask.value) {
    return
  }

  try {
    classes.value = await getMyClasses()
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '班级列表加载失败')
  }
}

async function handleCreateTask() {
  creatingTask.value = true
  errorMessage.value = ''

  try {
    const payload: CreateTaskRequest = {
      title: taskForm.title.trim(),
      description: taskForm.description.trim(),
      evaluationCriteria: taskForm.evaluationCriteria.trim() || null,
      startTime: taskForm.startTime || null,
      dueTime: taskForm.dueTime,
      coverUrl: taskForm.coverUrl.trim() || null,
      targetClassIds: taskForm.targetClassIds,
      backgroundMaterials: taskForm.materialTitle.trim() && taskForm.materialUrl.trim()
        ? [{
            title: taskForm.materialTitle.trim(),
            materialType: 'link',
            url: taskForm.materialUrl.trim(),
            description: '页面快速创建附带资料',
          }]
        : [],
    }

    await createTask(payload)
    appStore.showToast('任务已发布', 'success')
    await router.push('/tasks')
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '任务发布失败')
  } finally {
    creatingTask.value = false
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
      folder: 'tasks/covers',
      bizType: 'task_cover',
    })
    taskForm.coverUrl = result.fileUrl
    appStore.showToast('任务封面已上传', 'success')
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '任务封面上传失败')
  } finally {
    coverUploading.value = false
    target.value = ''
  }
}

onMounted(fetchClasses)
</script>

<style scoped>
.calendar-field::-webkit-calendar-picker-indicator {
  opacity: 0.38;
  cursor: pointer;
}

.empty-calendar {
  color: transparent;
  caret-color: transparent;
}

.empty-calendar::-webkit-datetime-edit,
.empty-calendar::-webkit-datetime-edit-fields-wrapper,
.empty-calendar::-webkit-datetime-edit-text,
.empty-calendar::-webkit-datetime-edit-month-field,
.empty-calendar::-webkit-datetime-edit-day-field,
.empty-calendar::-webkit-datetime-edit-year-field,
.empty-calendar::-webkit-datetime-edit-hour-field,
.empty-calendar::-webkit-datetime-edit-minute-field {
  color: transparent;
}

.empty-calendar::-webkit-calendar-picker-indicator {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  margin: 0;
  opacity: 0;
}
</style>

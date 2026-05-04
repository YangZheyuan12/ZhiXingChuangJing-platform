<template>
  <div class="space-y-6">
    <PageHero
      eyebrow="Exhibition Detail"
      :title="detail?.title || '展厅详情'"
      :description="detail?.summary || '暂无展厅摘要。'"
      back-to="/exhibitions"
      back-label="返回创作中心"
    >
      <template #badge>
        <div class="flex gap-2">
          <StatusPill :value="detail?.status" />
          <StatusPill :value="detail?.visibility" />
        </div>
      </template>
    </PageHero>

    <div class="flex flex-wrap gap-3">
      <router-link
        :to="`/exhibitions/${exhibitionId}/editor`"
        class="inline-flex items-center gap-2 rounded-2xl bg-brand-600 px-5 py-2.5 text-sm font-medium text-white transition hover:bg-brand-700"
      >
        ✎ 进入编辑器
      </router-link>
      <router-link
        v-if="detail?.publishedVersionNo"
        :to="`/exhibitions/${exhibitionId}/view`"
        class="inline-flex items-center gap-2 rounded-2xl border border-brand-200 px-5 py-2.5 text-sm font-medium text-brand-700 transition hover:bg-brand-50"
      >
        👁 观众浏览
      </router-link>
    </div>

    <div v-if="errorMessage" class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-600">
      {{ errorMessage }}
    </div>
    <div class="grid gap-6 lg:grid-cols-4">
      <MetricTile label="最新版本" :value="detail?.latestVersionNo ?? '--'" hint="当前保存的最新版本号" />
      <MetricTile label="已发布版本" :value="detail?.publishedVersionNo ?? '--'" hint="观众模式使用版本" />
      <MetricTile label="展示元素" :value="viewer?.renderData.elements.length ?? 0" hint="来自版本渲染数据" />
      <MetricTile label="协作成员" :value="detail?.collaborators.length ?? 0" hint="包含 owner / editor / viewer" />
    </div>

    <div class="grid gap-6 xl:grid-cols-[0.95fr_1.05fr]">
      <section class="space-y-6">
        <div v-if="canSaveVersion || canPublishExhibition" class="panel-card p-6">
          <SectionHeader title="版本操作台" description="保存当前内容并管理版本发布。" />
          <div class="grid gap-5 xl:grid-cols-[0.95fr_1.05fr]">
            <div class="space-y-4 rounded-[1.8rem] bg-[linear-gradient(155deg,#972d20,#6b190f)] p-5 text-white">
              <p class="text-xs uppercase tracking-[0.18em] text-white/65">Version Center</p>
              <h3 class="text-2xl font-semibold">保存当前版本</h3>
              <p class="text-sm leading-7 text-white/78">
                基于当前展厅内容生成新版本，可用于后续预览与发布。
              </p>
              <div v-if="lastSavedVersion" class="rounded-2xl border border-white/15 bg-white/10 p-4">
                <p class="text-xs uppercase tracking-[0.18em] text-white/60">最近保存</p>
                <p class="mt-2 text-lg font-semibold">V{{ lastSavedVersion.versionNo }}</p>
                <p class="mt-2 text-sm text-white/72">{{ lastSavedVersion.versionNote || '无版本说明' }}</p>
              </div>
            </div>

            <div class="space-y-4">
              <label class="block">
                <span class="form-label">版本说明</span>
                <textarea
                  v-model="versionNote"
                  rows="4"
                  class="form-textarea"
                />
              </label>
              <div class="grid gap-4 md:grid-cols-2">
                <label class="block">
                  <span class="form-label">发布可见性</span>
                  <select v-model="publishVisibility" class="form-select">
                    <option value="private">仅自己</option>
                    <option value="class">班级可见</option>
                    <option value="public">公开展示</option>
                  </select>
                </label>
                <div class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3">
                  <p class="text-xs uppercase tracking-[0.16em] text-slate-400">渲染基准</p>
                  <p class="mt-2 text-sm text-slate-600">
                    {{ viewer?.renderData.canvasConfig.width || 1920 }} × {{ viewer?.renderData.canvasConfig.height || 1080 }}
                  </p>
                </div>
              </div>
              <div class="flex flex-wrap gap-3">
                <button
                  v-if="canSaveVersion"
                  type="button"
                  :disabled="savingVersion"
                  class="rounded-2xl bg-brand-600 px-5 py-3 text-sm font-medium text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:bg-slate-300"
                  @click="handleSaveVersion"
                >
                  {{ savingVersion ? '保存中...' : '保存版本' }}
                </button>
                <button
                  v-if="canPublishExhibition"
                  type="button"
                  :disabled="publishing || !detail?.latestVersionNo"
                  class="rounded-2xl border border-brand-200 px-5 py-3 text-sm font-medium text-brand-700 transition hover:bg-brand-50 disabled:cursor-not-allowed disabled:border-slate-200 disabled:text-slate-400"
                  @click="handlePublish"
                >
                  {{ publishing ? '发布中...' : '发布当前最新版本' }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <div v-if="canPublishExhibition" class="panel-card p-6">
          <SectionHeader title="协作管理" description="支持邀请协作者并查看历史版本。" />
          <div class="grid gap-5 xl:grid-cols-[0.9fr_1.1fr]">
            <form class="space-y-4 rounded-2xl border border-slate-200 p-4" @submit.prevent="handleAddMembers">
              <label class="block">
                <span class="form-label">成员 ID 列表</span>
                <input v-model="memberIdsInput" class="form-control" />
              </label>
              <label class="block">
                <span class="form-label">角色</span>
                <select v-model="memberRole" class="form-select">
                  <option value="editor">编辑者</option>
                  <option value="viewer">查看者</option>
                  <option value="owner">负责人</option>
                </select>
              </label>
              <button
                type="submit"
                :disabled="addingMembers"
                class="rounded-2xl bg-brand-600 px-4 py-3 text-sm font-medium text-white transition hover:bg-brand-700 disabled:bg-slate-300"
              >
                {{ addingMembers ? '添加中...' : '添加协作者' }}
              </button>
            </form>

            <div class="space-y-3">
              <h4 class="text-sm font-semibold uppercase tracking-[0.18em] text-slate-400">版本历史</h4>
              <EmptyStatePanel
                v-if="versions.length === 0"
                eyebrow="Versions"
                title="暂无版本历史"
                description="保存版本后，这里会展示版本说明和创建时间。"
              />
              <article v-for="version in versions" :key="version.id" class="rounded-2xl border border-slate-200 p-4">
                <div class="flex items-center justify-between gap-3">
                  <h5 class="font-medium text-slate-900">V{{ version.versionNo }}</h5>
                  <StatusPill :value="version.saveType" />
                </div>
                <p class="mt-2 text-sm text-slate-500">{{ version.versionNote || '无版本说明' }}</p>
                <p class="mt-2 text-xs text-slate-400">{{ formatDateTime(version.createdAt) }}</p>
              </article>
            </div>
          </div>
        </div>

        <div class="panel-card p-6">
          <SectionHeader title="协作成员" description="查看当前展厅的协作成员与角色分工。" />
          <EmptyStatePanel
            v-if="!loading && (!detail || detail.collaborators.length === 0)"
            eyebrow="Collaborators"
            title="暂无协作者"
            description="当前展厅还没有协作成员。"
          />
          <div v-else class="space-y-3">
            <article v-for="member in detail?.collaborators || []" :key="member.userId" class="rounded-2xl border border-slate-200 p-4">
              <div class="flex items-center justify-between gap-4">
                <div>
                  <h3 class="font-medium text-slate-900">{{ member.name }}</h3>
                  <p class="mt-1 text-xs text-slate-400">{{ formatDateTime(member.joinedAt) }}</p>
                </div>
                <StatusPill :value="member.role" />
              </div>
            </article>
          </div>
        </div>

        <div class="panel-card p-6">
          <SectionHeader title="数字人角色库" description="同一展厅可创建多个讲解员，配合下方编辑器拖到不同展区。" />

          <!-- 角色卡片网格 -->
          <div class="mt-5 grid gap-4 md:grid-cols-2">
            <article
              v-for="dh in digitalHumans"
              :key="dh.id"
              class="rounded-2xl border bg-white p-4 transition"
              :class="editingDigitalHumanId === dh.id ? 'border-brand-400 ring-2 ring-brand-200' : 'border-slate-200 hover:border-brand-200'"
            >
              <div class="flex items-start gap-3">
                <img
                  v-if="dh.avatar2dUrl"
                  :src="dh.avatar2dUrl"
                  :alt="dh.name"
                  class="h-16 w-16 shrink-0 rounded-xl border border-slate-200 object-cover"
                />
                <div v-else class="flex h-16 w-16 shrink-0 items-center justify-center rounded-xl border border-dashed border-slate-300 bg-slate-50 text-xs text-slate-400">
                  无形象
                </div>
                <div class="min-w-0 flex-1">
                  <h4 class="truncate text-base font-semibold text-slate-900">{{ dh.name }}</h4>
                  <p class="mt-1 text-xs text-slate-500">{{ dh.voiceType || '默认音色' }}</p>
                  <p class="mt-2 line-clamp-2 text-xs text-slate-600">{{ dh.persona || '暂无人设描述' }}</p>
                </div>
              </div>
              <div v-if="dh.equippedItems?.length" class="mt-3 flex flex-wrap gap-1.5">
                <span
                  v-for="item in dh.equippedItems"
                  :key="item.id"
                  class="rounded-full bg-slate-100 px-2 py-0.5 text-xs text-slate-600"
                >
                  {{ item.resourceTitle }}
                </span>
              </div>
              <div v-if="canPublishExhibition" class="mt-4 flex flex-wrap gap-2">
                <button
                  type="button"
                  class="rounded-lg border border-brand-200 px-3 py-1.5 text-xs font-medium text-brand-700 transition hover:bg-brand-50"
                  @click="handleStartEditDigitalHuman(dh)"
                >
                  编辑
                </button>
                <button
                  type="button"
                  :disabled="deletingDigitalHumanId === dh.id"
                  class="rounded-lg border border-rose-200 px-3 py-1.5 text-xs font-medium text-rose-700 transition hover:bg-rose-50 disabled:opacity-50"
                  @click="handleDeleteDigitalHuman(dh)"
                >
                  {{ deletingDigitalHumanId === dh.id ? '删除中...' : '删除' }}
                </button>
              </div>
            </article>

            <!-- 「新建角色」入口卡片 -->
            <button
              v-if="canPublishExhibition && editingDigitalHumanId !== null"
              type="button"
              class="flex min-h-[140px] items-center justify-center rounded-2xl border-2 border-dashed border-slate-300 text-sm font-medium text-slate-500 transition hover:border-brand-400 hover:text-brand-600"
              @click="handleStartCreateDigitalHuman"
            >
              + 新建数字人角色
            </button>
          </div>

          <!-- 编辑/新建表单（inline） -->
          <div v-if="canPublishExhibition && editingDigitalHumanId !== null" class="mt-6 rounded-2xl border border-slate-200 bg-slate-50 p-5">
            <div class="mb-4 flex items-center justify-between">
              <h4 class="text-sm font-semibold uppercase tracking-[0.16em] text-slate-500">
                {{ editingDigitalHumanId === 0 ? '新建角色' : '编辑角色' }}
              </h4>
              <button
                type="button"
                class="text-xs text-slate-500 transition hover:text-slate-700"
                @click="handleCancelEditDigitalHuman"
              >
                取消
              </button>
            </div>
            <form class="grid gap-4 md:grid-cols-2" @submit.prevent="handleSaveDigitalHuman">
              <label class="block md:col-span-2">
                <span class="form-label">数字人名称</span>
                <input v-model="digitalHumanForm.name" class="form-control" required />
              </label>
              <label class="block">
                <span class="form-label">音色</span>
                <input v-model="digitalHumanForm.voiceType" class="form-control" placeholder="例如：温柔女声" />
              </label>
              <label class="block">
                <span class="form-label">上传 2D 形象</span>
                <input
                  type="file"
                  accept="image/*"
                  class="form-file"
                  :disabled="avatarUploading"
                  @change="handleDigitalHumanAvatarChange"
                />
                <p v-if="avatarUploading" class="mt-2 text-xs text-slate-500">形象上传中...</p>
                <img
                  v-else-if="digitalHumanForm.avatar2dUrl"
                  :src="digitalHumanForm.avatar2dUrl"
                  alt="数字人 2D 形象"
                  class="mt-3 h-24 w-full rounded-md border border-neutral-200 object-cover"
                />
              </label>
              <label class="block md:col-span-2">
                <span class="form-label">角色设定</span>
                <textarea v-model="digitalHumanForm.persona" rows="2" class="form-textarea" placeholder="例如：考古学家张教授，亲切博学" />
              </label>
              <label class="block md:col-span-2">
                <span class="form-label">故事脚本（可选，作为整个展厅的开场词）</span>
                <textarea v-model="digitalHumanForm.storyScript" rows="3" class="form-textarea" />
              </label>
              <div class="md:col-span-2 flex flex-wrap gap-3">
                <button
                  type="submit"
                  :disabled="savingDigitalHuman"
                  class="rounded-2xl bg-brand-600 px-4 py-3 text-sm font-medium text-white transition hover:bg-brand-700 disabled:bg-slate-300"
                >
                  {{ savingDigitalHuman ? '保存中...' : (editingDigitalHumanId === 0 ? '创建角色' : '保存修改') }}
                </button>
              </div>
            </form>
          </div>

          <EmptyStatePanel
            v-if="!loading && digitalHumans.length === 0 && editingDigitalHumanId === null"
            eyebrow="Digital Human"
            title="尚未创建任何数字人"
            description="点击下方按钮新建第一个角色。"
          />

          <button
            v-if="canPublishExhibition && editingDigitalHumanId === null"
            type="button"
            class="mt-5 inline-flex items-center gap-2 rounded-2xl bg-brand-600 px-4 py-3 text-sm font-medium text-white transition hover:bg-brand-700"
            @click="handleStartCreateDigitalHuman"
          >
            + 新建数字人角色
          </button>
        </div>
      </section>

      <section class="panel-card p-6">
        <SectionHeader title="观众预览数据" description="查看面向观众展示的画布与互动内容。" />
        <div v-if="loading" class="text-sm text-slate-500">加载中...</div>
        <div v-else class="space-y-5">
          <div class="rounded-[1.75rem] border border-slate-200 bg-[linear-gradient(135deg,#fcfaf9,#f3ebe4)] p-5">
            <div class="flex items-center justify-between gap-4">
              <div>
                <p class="text-xs uppercase tracking-[0.18em] text-slate-400">Canvas</p>
                <h3 class="mt-2 text-xl font-semibold text-slate-900">
                  {{ viewer?.renderData.canvasConfig.width || 1920 }} × {{ viewer?.renderData.canvasConfig.height || 1080 }}
                </h3>
              </div>
              <p class="text-sm text-slate-500">缩放 {{ viewer?.renderData.canvasConfig.zoom || 1 }}</p>
            </div>
            <div class="mt-5 grid gap-3 md:grid-cols-2">
              <article
                v-for="(element, index) in viewer?.renderData.elements.slice(0, 6) || []"
                :key="`${element.componentType}-${index}`"
                class="rounded-2xl border border-white/70 bg-white/80 p-4"
              >
                <div class="flex items-center justify-between gap-3">
                  <h4 class="font-medium text-slate-900">{{ element.props?.title || element.componentType || '组件' }}</h4>
                  <span class="text-xs uppercase tracking-[0.16em] text-slate-400">{{ element.componentType }}</span>
                </div>
                <p class="mt-2 text-xs text-slate-400">
                  {{ Math.round(element.width) }} × {{ Math.round(element.height) }} · x={{ Math.round(element.x) }} · y={{ Math.round(element.y) }}
                </p>
              </article>
            </div>
          </div>

          <div class="grid gap-4 md:grid-cols-2">
            <article class="rounded-2xl border border-slate-200 p-5">
              <h3 class="text-sm font-semibold uppercase tracking-[0.18em] text-slate-400">教师点评</h3>
              <p class="mt-3 text-3xl font-semibold text-slate-900">{{ viewer?.teacherReviews.length || 0 }}</p>
            </article>
            <article class="rounded-2xl border border-slate-200 p-5">
              <h3 class="text-sm font-semibold uppercase tracking-[0.18em] text-slate-400">评论条数</h3>
              <p class="mt-3 text-3xl font-semibold text-slate-900">{{ viewer?.comments.length || 0 }}</p>
            </article>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { uploadAsset } from '@/api/modules/assets'
import { useRoute } from 'vue-router'
import { getErrorMessage } from '@/utils/request'
import {
  addExhibitionMembers,
  getExhibitionDetail,
  getExhibitionVersions,
  getExhibitionViewer,
  publishExhibition,
  saveExhibitionVersion,
} from '@/api/modules/exhibitions'
import {
  createDigitalHuman,
  deleteDigitalHuman as deleteDigitalHumanApi,
  listDigitalHumans,
  updateDigitalHuman,
} from '@/api/modules/digital-human'
import { getMuseumResources } from '@/api/modules/museum'
import type {
  DigitalHuman,
  ExhibitionDetail,
  ExhibitionVersion,
  ExhibitionViewerData,
  MuseumResource,
  SaveExhibitionVersionRequest,
  UpsertDigitalHumanRequest,
} from '@/api/types'
import EmptyStatePanel from '@/components/common/EmptyStatePanel.vue'
import MetricTile from '@/components/common/MetricTile.vue'
import PageHero from '@/components/common/PageHero.vue'
import SectionHeader from '@/components/common/SectionHeader.vue'
import StatusPill from '@/components/common/StatusPill.vue'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const authStore = useAuthStore()
const appStore = useAppStore()
const exhibitionId = Number(route.params.exhibitionId)

const loading = ref(false)
const savingVersion = ref(false)
const publishing = ref(false)
const errorMessage = ref('')
const detail = ref<ExhibitionDetail | null>(null)
const viewer = ref<ExhibitionViewerData | null>(null)
const digitalHumans = ref<DigitalHuman[]>([])
/**
 * 当前编辑中的数字人 id：
 * - null = 未进入编辑/新建
 * - 0    = 新建模式
 * - 其他 = 编辑该 id 的角色
 */
const editingDigitalHumanId = ref<number | null>(null)
const deletingDigitalHumanId = ref<number | null>(null)
const versions = ref<ExhibitionVersion[]>([])
const museumResources = ref<MuseumResource[]>([])
const versionNote = ref('')
const publishVisibility = ref<'private' | 'class' | 'public'>('class')
const lastSavedVersion = ref<ExhibitionVersion | null>(null)
const addingMembers = ref(false)
const savingDigitalHuman = ref(false)
const avatarUploading = ref(false)
const memberIdsInput = ref('')
const memberRole = ref<'owner' | 'editor' | 'viewer'>('editor')
const digitalHumanForm = ref({
  name: '',
  avatar2dUrl: '',
  model3dUrl: '',
  persona: '',
  voiceType: '',
  storyScript: '',
})

const canSaveVersion = computed(() => {
  const userId = authStore.user?.id
  if (!userId || !detail.value) {
    return false
  }
  return detail.value.ownerId === userId || detail.value.collaborators.some((member) => member.userId === userId)
})

const canPublishExhibition = computed(() => {
  const user = authStore.user
  if (!user || !detail.value) {
    return false
  }
  return user.role === 'admin' || detail.value.ownerId === user.id
})

async function fetchExhibitionDetail() {
  loading.value = true
  errorMessage.value = ''

  try {
    const [detailData, viewerData, digitalHumanList, versionData, museumData] = await Promise.all([
      getExhibitionDetail(exhibitionId),
      getExhibitionViewer(exhibitionId),
      listDigitalHumans(exhibitionId).catch(() => [] as DigitalHuman[]),
      getExhibitionVersions(exhibitionId).catch(() => []),
      getMuseumResources({ page: 1, pageSize: 20 }).then((page) => page.list).catch(() => []),
    ])
    detail.value = detailData
    viewer.value = viewerData
    digitalHumans.value = digitalHumanList
    versions.value = versionData
    museumResources.value = museumData
    publishVisibility.value = (detailData.visibility as 'private' | 'class' | 'public') || 'class'
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '展厅详情加载失败')
  } finally {
    loading.value = false
  }
}

async function handleSaveVersion() {
  savingVersion.value = true
  errorMessage.value = ''

  try {
    const payload: SaveExhibitionVersionRequest = {
      saveType: 'manual',
      versionNote: versionNote.value.trim() || '页面快速保存版本',
      canvasConfig: viewer.value?.renderData.canvasConfig || {
        width: 1920,
        height: 1080,
        background: '#f7efe9',
        zoom: 1,
      },
      versionData: buildVersionData(),
    }

    const version = await saveExhibitionVersion(exhibitionId, payload)
    lastSavedVersion.value = version
    appStore.showToast(`版本 V${version.versionNo} 已保存`, 'success')
    await fetchExhibitionDetail()
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '版本保存失败')
  } finally {
    savingVersion.value = false
  }
}

async function handlePublish() {
  if (!detail.value?.latestVersionNo) {
    errorMessage.value = '请先至少保存一个版本后再发布'
    return
  }

  publishing.value = true
  errorMessage.value = ''

  try {
    await publishExhibition(exhibitionId, {
      versionNo: detail.value.latestVersionNo,
      visibility: publishVisibility.value,
    })
    appStore.showToast(`展厅已发布，当前版本为 V${detail.value.latestVersionNo}`, 'success')
    await fetchExhibitionDetail()
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '展厅发布失败')
  } finally {
    publishing.value = false
  }
}

async function handleAddMembers() {
  const memberUserIds = memberIdsInput.value
    .split(',')
    .map((item) => Number(item.trim()))
    .filter((item) => Number.isFinite(item) && item > 0)

  if (memberUserIds.length === 0) {
    errorMessage.value = '请至少输入一个有效成员 ID'
    return
  }

  addingMembers.value = true
  errorMessage.value = ''

  try {
    await addExhibitionMembers(exhibitionId, { memberUserIds, role: memberRole.value })
    appStore.showToast('协作成员已添加', 'success')
    memberIdsInput.value = ''
    await fetchExhibitionDetail()
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '协作成员添加失败')
  } finally {
    addingMembers.value = false
  }
}

function resetDigitalHumanForm() {
  digitalHumanForm.value = {
    name: '',
    avatar2dUrl: '',
    model3dUrl: '',
    persona: '',
    voiceType: '',
    storyScript: '',
  }
}

function handleStartCreateDigitalHuman() {
  resetDigitalHumanForm()
  editingDigitalHumanId.value = 0
}

function handleStartEditDigitalHuman(dh: DigitalHuman) {
  digitalHumanForm.value = {
    name: dh.name || '',
    avatar2dUrl: dh.avatar2dUrl || '',
    model3dUrl: dh.model3dUrl || '',
    persona: dh.persona || '',
    voiceType: dh.voiceType || '',
    storyScript: dh.storyScript || '',
  }
  editingDigitalHumanId.value = dh.id
}

function handleCancelEditDigitalHuman() {
  resetDigitalHumanForm()
  editingDigitalHumanId.value = null
}

async function handleSaveDigitalHuman() {
  const editingId = editingDigitalHumanId.value
  if (editingId === null) return

  savingDigitalHuman.value = true
  errorMessage.value = ''

  try {
    const payload: UpsertDigitalHumanRequest = {
      ...digitalHumanForm.value,
      storyTimeline: [],
    }
    if (editingId === 0) {
      const created = await createDigitalHuman(exhibitionId, payload)
      appStore.showToast(`角色「${created.name}」已创建`, 'success')
    } else {
      const updated = await updateDigitalHuman(editingId, payload)
      appStore.showToast(`角色「${updated.name}」已保存`, 'success')
    }
    editingDigitalHumanId.value = null
    resetDigitalHumanForm()
    await fetchExhibitionDetail()
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '数字人保存失败')
  } finally {
    savingDigitalHuman.value = false
  }
}

async function handleDeleteDigitalHuman(dh: DigitalHuman) {
  if (!window.confirm(`确认删除角色「${dh.name}」？被该角色驻守的展区会同时撤下。`)) {
    return
  }
  deletingDigitalHumanId.value = dh.id
  errorMessage.value = ''
  try {
    await deleteDigitalHumanApi(dh.id)
    appStore.showToast(`角色「${dh.name}」已删除`, 'success')
    if (editingDigitalHumanId.value === dh.id) {
      handleCancelEditDigitalHuman()
    }
    await fetchExhibitionDetail()
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '数字人删除失败')
  } finally {
    deletingDigitalHumanId.value = null
  }
}

async function handleDigitalHumanAvatarChange(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]

  if (!file) {
    return
  }

  avatarUploading.value = true
  errorMessage.value = ''

  try {
    const result = await uploadAsset(file, {
      folder: 'digital-humans/avatars',
      bizType: 'digital_human_avatar',
    })
    digitalHumanForm.value.avatar2dUrl = result.fileUrl
    appStore.showToast('数字人形象已上传', 'success')
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '数字人形象上传失败')
  } finally {
    avatarUploading.value = false
    target.value = ''
  }
}

function buildVersionData() {
  const currentElements = viewer.value?.renderData.elements || []
  if (currentElements.length > 0) {
    return {
      source: 'quick-save',
      generatedAt: new Date().toISOString(),
      elements: currentElements,
      meta: {
        title: detail.value?.title,
        summary: detail.value?.summary,
      },
    }
  }

  return {
    source: 'quick-save',
    generatedAt: new Date().toISOString(),
    elements: [
      {
        componentType: 'hero-banner',
        x: 120,
        y: 120,
        width: 860,
        height: 320,
        props: {
          title: detail.value?.title || '展厅标题',
          subtitle: detail.value?.summary || '欢迎进入展厅导览',
        },
      },
      {
        componentType: 'story-card',
        x: 180,
        y: 500,
        width: 640,
        height: 220,
        props: {
          title: '创作说明',
          description: versionNote.value.trim() || '该版本由当前展厅内容生成，可用于后续预览与发布。',
        },
      },
    ],
    meta: {
      title: detail.value?.title,
      summary: detail.value?.summary,
      groupName: detail.value?.groupName,
    },
  }
}

onMounted(fetchExhibitionDetail)
</script>

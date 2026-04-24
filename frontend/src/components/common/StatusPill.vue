<template>
  <span class="inline-flex items-center rounded-full px-3 py-1 text-xs font-medium tracking-[0.08em]" :class="classes">
    {{ label }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  value?: string | null
}>()

const label = computed(() => {
  switch (props.value) {
    case 'public':
      return '公共'
    case 'published':
      return '已发布'
    case 'private':
      return '私有'
    case 'class':
      return '班级'
    case 'draft':
      return '草稿'
    case 'closed':
      return '已关闭'
    case 'archived':
      return '已归档'
    case 'read':
      return '已读'
    case 'unread':
      return '未读'
    case 'submitted':
      return '已提交'
    case 'reviewed':
      return '已评审'
    case 'inactive':
      return '停用'
    case 'image':
      return '图片'
    case 'video':
      return '视频'
    case 'audio':
      return '音频'
    case 'model':
      return '模型'
    case 'document':
      return '文档'
    case 'upload':
      return '上传'
    case 'platform':
      return '平台'
    case 'museum':
      return '文博'
    default:
      return props.value || '未知'
  }
})

const classes = computed(() => {
  switch (props.value) {
    case 'published':
    case 'public':
    case 'read':
      return 'bg-emerald-50 text-emerald-700'
    case 'draft':
    case 'private':
    case 'unread':
      return 'bg-amber-50 text-amber-700'
    case 'closed':
    case 'archived':
    case 'inactive':
      return 'bg-slate-100 text-slate-600'
    case 'class':
      return 'bg-brand-50 text-brand-700'
    case 'submitted':
      return 'bg-sky-50 text-sky-700'
    case 'reviewed':
      return 'bg-violet-50 text-violet-700'
    default:
      return 'bg-slate-100 text-slate-600'
  }
})
</script>

<template>
  <div class="space-y-2">
    <h4 class="text-xs font-semibold uppercase tracking-widest text-gray-400">模板</h4>
    <p class="text-[11px] text-gray-400">点击模板，将预设元素插入画布。</p>

    <button
      v-for="tpl in templates"
      :key="tpl.id"
      type="button"
      class="w-full rounded-lg border border-gray-200 bg-white p-3 text-left transition hover:border-gray-300 hover:bg-gray-50"
      @click="$emit('apply', tpl.id)"
    >
      <div class="flex items-center gap-3">
        <span class="text-xl text-gray-300">{{ tpl.icon }}</span>
        <div class="min-w-0 flex-1">
          <p class="text-xs font-medium text-gray-700">{{ tpl.name }}</p>
          <p class="truncate text-[10px] text-gray-400">{{ tpl.description }}</p>
        </div>
      </div>
    </button>
  </div>
</template>

<script setup lang="ts">
export interface TemplateItem {
  id: string
  icon: string
  name: string
  description: string
}

const templates: TemplateItem[] = [
  { id: 'title-page', icon: '🎨', name: '标题页', description: '大标题 + 副标题居中布局' },
  { id: 'image-text', icon: '🖼', name: '图文混排', description: '左图右文经典两栏' },
  { id: 'dual-panel', icon: '▦', name: '双栏面板', description: '左右对称面板布局' },
  { id: 'quote-block', icon: '❝', name: '引用框', description: '大号引用文本 + 出处' },
  { id: 'gallery-grid', icon: '⊞', name: '画廊网格', description: '2×2 图片占位网格' },
]

defineEmits<{
  apply: [templateId: string]
}>()
</script>

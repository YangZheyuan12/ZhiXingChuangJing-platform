<template>
  <div class="space-y-3">
    <h4 class="text-xs font-semibold uppercase tracking-widest text-gray-400">文本样式</h4>

    <label class="block">
      <span class="text-[11px] text-gray-500">字体</span>
      <select class="form-select mt-1 text-sm" :value="fontFamily" @change="onSelectChange('fontFamily', $event)">
        <option v-for="f in fonts" :key="f.value" :value="f.value">{{ f.label }}</option>
      </select>
    </label>

    <div class="grid grid-cols-2 gap-2">
      <label class="block">
        <span class="text-[11px] text-gray-500">字号</span>
        <input type="number" class="form-input mt-1 text-sm" :value="fontSize" min="8" max="200" @change="onNumberChange('fontSize', $event)" />
      </label>
      <label class="block">
        <span class="text-[11px] text-gray-500">颜色</span>
        <input type="color" class="mt-1 h-9 w-full cursor-pointer rounded-md border border-gray-200" :value="fill" @input="onSelectChange('fill', $event)" />
      </label>
    </div>

    <div class="flex gap-1">
      <button
        v-for="fmt in formatButtons"
        :key="fmt.prop"
        type="button"
        class="rounded-md border px-2.5 py-1.5 text-xs font-medium transition"
        :class="fmt.active ? 'border-brand-300 bg-brand-50 text-brand-700' : 'border-gray-200 text-gray-500 hover:bg-gray-50'"
        @click="emit('update', fmt.prop, fmt.nextValue)"
      >
        {{ fmt.label }}
      </button>
    </div>

    <div class="flex gap-1">
      <button
        v-for="align in alignButtons"
        :key="align.value"
        type="button"
        class="rounded-md border px-2 py-1.5 text-xs transition"
        :class="textAlign === align.value ? 'border-brand-300 bg-brand-50 text-brand-700' : 'border-gray-200 text-gray-500 hover:bg-gray-50'"
        @click="emit('update', 'textAlign', align.value)"
      >
        {{ align.label }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  fontFamily: string
  fontSize: number
  fill: string
  fontWeight: string | number
  fontStyle: string
  underline: boolean
  textAlign: string
}>()

const emit = defineEmits<{
  update: [prop: string, value: unknown]
}>()

function onSelectChange(prop: string, e: Event) {
  emit('update', prop, (e.target as HTMLSelectElement).value)
}

function onNumberChange(prop: string, e: Event) {
  emit('update', prop, Number((e.target as HTMLInputElement).value))
}

const fonts = [
  { label: '无衬线', value: 'sans-serif' },
  { label: '衬线体', value: 'serif' },
  { label: '等宽', value: 'monospace' },
  { label: '楷体', value: 'KaiTi, STKaiti, serif' },
  { label: '宋体', value: 'SimSun, STSong, serif' },
  { label: '黑体', value: 'SimHei, STHeiti, sans-serif' },
]

const formatButtons = computed(() => [
  {
    label: 'B',
    prop: 'fontWeight',
    active: props.fontWeight === 'bold' || Number(props.fontWeight) >= 700,
    nextValue: props.fontWeight === 'bold' || Number(props.fontWeight) >= 700 ? 'normal' : 'bold',
  },
  {
    label: 'I',
    prop: 'fontStyle',
    active: props.fontStyle === 'italic',
    nextValue: props.fontStyle === 'italic' ? 'normal' : 'italic',
  },
  {
    label: 'U',
    prop: 'underline',
    active: props.underline,
    nextValue: !props.underline,
  },
])

const alignButtons = [
  { label: '左', value: 'left' },
  { label: '中', value: 'center' },
  { label: '右', value: 'right' },
]
</script>

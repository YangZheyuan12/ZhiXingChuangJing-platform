<template>
  <div v-if="hotspot" class="space-y-4">
    <div class="flex items-center justify-between">
      <h3 class="text-xs font-semibold uppercase tracking-widest text-gray-400">热点属性</h3>
      <button
        type="button"
        class="text-xs text-rose-500 hover:text-rose-700"
        @click="$emit('delete')"
      >删除</button>
    </div>

    <label class="block">
      <span class="mb-1 block text-xs font-medium text-gray-600">类型</span>
      <select
        :value="hotspot.hotspotType"
        class="w-full rounded-lg border border-gray-200 bg-white px-3 py-1.5 text-sm"
        @change="$emit('update', 'hotspotType', ($event.target as HTMLSelectElement).value)"
      >
        <option value="navigation">导航 - 跳转到展区</option>
        <option value="info">信息 - 弹出文字说明</option>
        <option value="exhibit_popup">展品 - 打开展品弹窗</option>
        <option value="narration_trigger">讲解 - 播放语音</option>
        <option value="external_link">外链 - 打开 URL</option>
      </select>
    </label>

    <label class="block">
      <span class="mb-1 block text-xs font-medium text-gray-600">名称</span>
      <input
        :value="hotspot.label ?? ''"
        maxlength="64"
        class="w-full rounded-lg border border-gray-200 bg-white px-3 py-1.5 text-sm"
        placeholder="热点标签（可选）"
        @input="$emit('update', 'label', ($event.target as HTMLInputElement).value || null)"
      />
    </label>

    <label class="block">
      <span class="mb-1 block text-xs font-medium text-gray-600">图标</span>
      <input
        :value="hotspot.icon ?? ''"
        maxlength="32"
        class="w-full rounded-lg border border-gray-200 bg-white px-3 py-1.5 text-sm"
        placeholder="例如：→ 或 info-circle"
        @input="$emit('update', 'icon', ($event.target as HTMLInputElement).value || null)"
      />
    </label>

    <label v-if="hotspot.hotspotType === 'navigation'" class="block">
      <span class="mb-1 block text-xs font-medium text-gray-600">跳转目标展区</span>
      <select
        :value="hotspot.targetZoneId ?? ''"
        class="w-full rounded-lg border border-gray-200 bg-white px-3 py-1.5 text-sm"
        @change="onTargetZoneChange(($event.target as HTMLSelectElement).value)"
      >
        <option value="">（未设置）</option>
        <option
          v-for="zone in availableZones"
          :key="zone.id"
          :value="zone.id"
          :disabled="zone.id === hotspot.zoneId"
        >
          {{ zone.title }}{{ zone.id === hotspot.zoneId ? '（当前）' : '' }}
        </option>
      </select>
    </label>

    <div class="border-t border-gray-100 pt-4">
      <h4 class="mb-2 text-xs font-medium text-gray-500">位置 / 尺寸（百分比 0-100）</h4>
      <div class="grid grid-cols-2 gap-3">
        <label class="block">
          <span class="text-xs text-gray-500">X</span>
          <input
            type="number"
            min="0"
            max="100"
            step="0.1"
            :value="hotspot.xPercent"
            class="mt-1 w-full rounded-md border border-gray-200 px-2.5 py-1.5 text-sm focus:border-brand-400 focus:outline-none"
            @change="onCoordChange('xPercent', $event)"
          />
        </label>
        <label class="block">
          <span class="text-xs text-gray-500">Y</span>
          <input
            type="number"
            min="0"
            max="100"
            step="0.1"
            :value="hotspot.yPercent"
            class="mt-1 w-full rounded-md border border-gray-200 px-2.5 py-1.5 text-sm focus:border-brand-400 focus:outline-none"
            @change="onCoordChange('yPercent', $event)"
          />
        </label>
        <label class="block">
          <span class="text-xs text-gray-500">W</span>
          <input
            type="number"
            min="0"
            max="100"
            step="0.1"
            :value="hotspot.wPercent"
            class="mt-1 w-full rounded-md border border-gray-200 px-2.5 py-1.5 text-sm focus:border-brand-400 focus:outline-none"
            @change="onCoordChange('wPercent', $event)"
          />
        </label>
        <label class="block">
          <span class="text-xs text-gray-500">H</span>
          <input
            type="number"
            min="0"
            max="100"
            step="0.1"
            :value="hotspot.hPercent"
            class="mt-1 w-full rounded-md border border-gray-200 px-2.5 py-1.5 text-sm focus:border-brand-400 focus:outline-none"
            @change="onCoordChange('hPercent', $event)"
          />
        </label>
      </div>
    </div>

    <button
      type="button"
      class="w-full rounded-md border border-gray-200 py-1.5 text-xs text-gray-500 transition hover:bg-gray-50"
      @click="$emit('deselect')"
    >取消选中</button>
  </div>

  <div v-else class="space-y-3 py-6 text-center text-xs text-gray-400">
    <p>当前未选中热点。</p>
    <p class="text-gray-300">点击画布上的热点以编辑，或：</p>
    <button
      type="button"
      class="mx-auto block rounded-md bg-brand-600 px-4 py-1.5 text-xs font-medium text-white transition hover:bg-brand-700"
      @click="$emit('create')"
    >
      + 在当前展区添加热点
    </button>
  </div>
</template>

<script setup lang="ts">
import type { HotspotDetail, ZoneDetail } from '@/api/types'

const props = defineProps<{
  hotspot: HotspotDetail | null
  availableZones: ZoneDetail[]
}>()

const emit = defineEmits<{
  update: [field: string, value: unknown]
  delete: []
  deselect: []
  create: []
}>()

function onTargetZoneChange(value: string) {
  emit('update', 'targetZoneId', value === '' ? null : Number(value))
}

function onCoordChange(field: 'xPercent' | 'yPercent' | 'wPercent' | 'hPercent', event: Event) {
  const num = Number((event.target as HTMLInputElement).value)
  if (Number.isFinite(num)) {
    const clamped = Math.max(0, Math.min(100, num))
    emit('update', field, clamped)
  }
}

void props
</script>

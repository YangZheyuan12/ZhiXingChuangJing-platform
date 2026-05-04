<template>
  <section class="overflow-hidden rounded-2xl border border-brand-200 bg-white shadow-xl">
    <header class="flex items-center justify-between gap-2 border-b border-slate-100 bg-gradient-to-r from-brand-50 to-sky-50 px-4 py-2.5">
      <div class="flex min-w-0 items-center gap-2">
        <span class="inline-flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-brand-600 text-xs text-white">AI</span>
        <div class="min-w-0">
          <p class="truncate text-xs font-semibold text-slate-900">{{ card.title }}</p>
          <p class="text-[11px] text-slate-500">{{ card.sourceLabel }} · {{ index + 1 }} / {{ total }}</p>
        </div>
      </div>
      <button
        type="button"
        class="rounded-md p-1 text-slate-400 hover:bg-white/70 hover:text-slate-600"
        title="收起"
        @click="emit('close')"
      >
        <svg class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M5.22 14.78a.75.75 0 001.06 0L10 11.06l3.72 3.72a.75.75 0 101.06-1.06l-3.72-3.72 3.72-3.72a.75.75 0 10-1.06-1.06L10 8.94 6.28 5.22a.75.75 0 00-1.06 1.06L8.94 10l-3.72 3.72a.75.75 0 000 1.06z" clip-rule="evenodd" />
        </svg>
      </button>
    </header>

    <div class="max-h-56 overflow-y-auto px-4 py-3">
      <p class="whitespace-pre-wrap text-sm leading-6 text-slate-700">
        {{ displayText }}<span
          v-if="typing"
          class="ml-0.5 inline-block w-0.5 animate-pulse bg-brand-500"
          style="height: 1em; vertical-align: middle;"
        />
      </p>
    </div>

    <audio
      v-if="card.audioUrl"
      :src="card.audioUrl"
      preload="metadata"
      class="w-full px-2"
      controls
    />

    <footer class="flex items-center justify-between gap-2 border-t border-slate-100 bg-slate-50 px-3 py-2">
      <div class="flex items-center gap-1">
        <button
          type="button"
          class="rounded-md p-1.5 text-slate-600 hover:bg-white disabled:opacity-40"
          :disabled="total <= 1"
          title="上一条"
          @click="emit('prev')"
        >
          <svg class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M12.79 5.23a.75.75 0 01-.02 1.06L8.832 10l3.938 3.71a.75.75 0 11-1.04 1.08l-4.5-4.25a.75.75 0 010-1.08l4.5-4.25a.75.75 0 011.06.02z" clip-rule="evenodd" />
          </svg>
        </button>
        <button
          type="button"
          class="rounded-md p-1.5 text-slate-600 hover:bg-white disabled:opacity-40"
          :disabled="total <= 1"
          title="下一条"
          @click="emit('next')"
        >
          <svg class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M7.21 14.77a.75.75 0 01.02-1.06L11.168 10 7.23 6.29a.75.75 0 111.04-1.08l4.5 4.25a.75.75 0 010 1.08l-4.5 4.25a.75.75 0 01-1.06-.02z" clip-rule="evenodd" />
          </svg>
        </button>
      </div>
      <label class="flex items-center gap-1.5 text-[11px] text-slate-500">
        <input
          :checked="autoTyping"
          type="checkbox"
          class="h-3 w-3 rounded border-slate-300"
          @change="onToggleAutoTyping"
        />
        打字机
      </label>
      <button
        type="button"
        class="rounded-md px-2 py-1 text-xs text-brand-700 hover:bg-white"
        @click="emit('replay')"
      >
        重播
      </button>
    </footer>
  </section>
</template>

<script setup lang="ts">
interface NarrationCard {
  key: string
  title: string
  sourceLabel: string
  content: string
  audioUrl?: string | null
}

defineProps<{
  card: NarrationCard
  displayText: string
  typing: boolean
  autoTyping: boolean
  index: number
  total: number
}>()

const emit = defineEmits<{
  close: []
  prev: []
  next: []
  replay: []
  'update:autoTyping': [value: boolean]
}>()

function onToggleAutoTyping(e: Event) {
  const target = e.target as HTMLInputElement
  emit('update:autoTyping', target.checked)
}
</script>

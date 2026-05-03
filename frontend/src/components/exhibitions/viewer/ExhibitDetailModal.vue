<template>
  <Teleport to="body">
    <div v-if="exhibit" class="fixed inset-0 z-50 flex items-center justify-center bg-black/60" @click.self="$emit('close')">
      <div class="relative max-h-[85vh] w-full max-w-2xl overflow-y-auto rounded-2xl bg-white p-6 shadow-2xl">
        <button type="button" class="absolute right-4 top-4 rounded-full p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600" @click="$emit('close')">✕</button>

        <div v-if="exhibit.coverUrl" class="mb-4 overflow-hidden rounded-xl">
          <img :src="exhibit.coverUrl" :alt="exhibit.title" class="h-64 w-full object-cover" />
        </div>

        <h2 class="text-xl font-bold text-gray-900">{{ exhibit.title }}</h2>
        <p v-if="exhibit.subtitle" class="mt-1 text-sm text-gray-500">{{ exhibit.subtitle }}</p>

        <div v-if="exhibit.description" class="mt-4 text-sm leading-relaxed text-gray-700">{{ exhibit.description }}</div>

        <div v-if="exhibit.knowledgePoints && exhibit.knowledgePoints.length" class="mt-4">
          <h3 class="mb-2 text-xs font-semibold uppercase tracking-widest text-gray-400">知识点</h3>
          <div class="flex flex-wrap gap-2">
            <span v-for="kp in exhibit.knowledgePoints" :key="kp" class="rounded-full bg-brand-50 px-3 py-1 text-xs text-brand-700">{{ kp }}</span>
          </div>
        </div>

        <div v-if="exhibit.mediaUrl && exhibit.exhibitType === 'video'" class="mt-4">
          <video :src="exhibit.mediaUrl" controls preload="metadata" class="w-full rounded-xl" />
        </div>
        <div v-else-if="exhibit.mediaUrl && exhibit.exhibitType === 'audio'" class="mt-4">
          <audio :src="exhibit.mediaUrl" controls preload="metadata" class="w-full" />
        </div>

        <div v-if="exhibit.narrations && exhibit.narrations.length" class="mt-6 border-t border-gray-100 pt-4">
          <h3 class="mb-2 text-xs font-semibold uppercase tracking-widest text-gray-400">讲解</h3>
          <div v-for="n in exhibit.narrations" :key="n.id" class="mb-3 rounded-lg bg-gray-50 p-3">
            <p class="text-sm text-gray-700">{{ n.content }}</p>
            <audio v-if="n.audioUrl" :src="n.audioUrl" controls preload="metadata" class="mt-2 w-full" />
          </div>
        </div>

        <div v-if="exhibit.sourceInfo" class="mt-4 text-xs text-gray-400">
          来源：{{ JSON.stringify(exhibit.sourceInfo) }}
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import type { ExhibitDetail } from '@/api/types'

defineProps<{
  exhibit: ExhibitDetail | null
}>()

defineEmits<{
  close: []
}>()
</script>

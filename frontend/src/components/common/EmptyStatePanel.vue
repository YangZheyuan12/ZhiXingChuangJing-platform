<template>
  <div class="rounded-[1.75rem] border border-dashed border-brand-200 bg-white/70 px-6 py-10 text-center">
    <p class="text-xs uppercase tracking-[0.28em] text-brand-500">{{ eyebrow }}</p>
    <h3 class="mt-3 text-xl font-semibold text-slate-900">{{ title }}</h3>
    <p class="mx-auto mt-3 max-w-xl text-sm leading-6 text-slate-500">{{ description }}</p>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  eyebrow?: string
  title: string
  description: string
}>(), {
  eyebrow: 'Empty',
})
</script>

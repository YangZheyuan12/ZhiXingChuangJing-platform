<template>
  <article class="metric-tile">
    <p class="metric-tile-label">{{ label }}</p>
    <p class="metric-tile-value">{{ value }}</p>
    <p v-if="hint" class="metric-tile-hint">{{ hint }}</p>
  </article>
</template>

<script setup lang="ts">
defineProps<{
  label: string
  value: string | number
  hint?: string
}>()
</script>

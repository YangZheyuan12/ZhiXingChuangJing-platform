<template>
  <section class="page-hero">
    <div v-if="backTo || hasBadge" class="flex flex-wrap items-center justify-between gap-3">
      <RouterLink v-if="backTo" :to="backTo" class="page-hero-link">
        {{ backLabel }}
      </RouterLink>
      <div v-if="hasBadge" class="page-hero-badge">
        <slot name="badge" />
      </div>
    </div>

    <p class="page-hero-eyebrow">{{ eyebrow }}</p>

    <div class="mt-4 flex flex-col gap-5 xl:flex-row xl:items-end xl:justify-between">
      <div class="min-w-0 max-w-4xl">
        <h1 class="page-hero-title">{{ title }}</h1>
        <p v-if="description" class="page-hero-description">
          {{ description }}
        </p>
        <div v-if="hasMeta" class="page-hero-meta">
          <slot name="meta" />
        </div>
        <div v-if="hasChips" class="page-hero-chip-row">
          <slot name="chips" />
        </div>
      </div>

      <div v-if="hasActions" class="page-hero-actions">
        <slot name="actions" />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, useSlots } from 'vue'

const props = withDefaults(defineProps<{
  eyebrow: string
  title: string
  description?: string
  backTo?: string
  backLabel?: string
}>(), {
  description: '',
  backTo: '',
  backLabel: '返回',
})

const slots = useSlots()

const hasBadge = computed(() => Boolean(slots.badge))
const hasMeta = computed(() => Boolean(slots.meta))
const hasChips = computed(() => Boolean(slots.chips))
const hasActions = computed(() => Boolean(slots.actions))
</script>

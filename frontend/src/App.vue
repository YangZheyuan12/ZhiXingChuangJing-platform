<template>
  <RouterView />
  <ToastHost />
  <AiChatBot />
</template>

<script setup lang="ts">
import ToastHost from '@/components/common/ToastHost.vue'
import AiChatBot from '@/components/common/AiChatBot.vue'
</script>

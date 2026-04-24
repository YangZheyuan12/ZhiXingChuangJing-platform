<template>
  <div class="min-h-screen bg-slate-100 text-neutral-900">
    <AdminNavbar />
    <div class="mx-auto flex w-full max-w-7xl items-center gap-2 px-4 pt-4 text-xs text-slate-500 sm:px-6 lg:px-8">
      <span class="inline-flex items-center rounded-full bg-indigo-50 px-2.5 py-1 text-indigo-600 ring-1 ring-indigo-100">
        当前处于管理后台
      </span>
      <span class="text-slate-400">仅管理员角色可以访问此区域</span>
    </div>
    <main class="mx-auto w-full max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import AdminNavbar from '@/components/layout/AdminNavbar.vue'
</script>

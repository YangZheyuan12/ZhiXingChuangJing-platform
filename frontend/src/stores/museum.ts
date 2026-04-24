import { ref } from 'vue'
import { defineStore } from 'pinia'

export const useMuseumStore = defineStore('museum', () => {
  const keyword = ref('')

  function setKeyword(value: string) {
    keyword.value = value
  }

  return {
    keyword,
    setKeyword,
  }
})

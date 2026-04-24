import { ref } from 'vue'
import { defineStore } from 'pinia'

export const useExhibitionStore = defineStore('exhibition', () => {
  const currentExhibitionId = ref<number | null>(null)

  function setCurrentExhibitionId(exhibitionId: number | null) {
    currentExhibitionId.value = exhibitionId
  }

  return {
    currentExhibitionId,
    setCurrentExhibitionId,
  }
})

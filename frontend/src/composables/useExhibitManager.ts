import { computed, ref, type Ref } from 'vue'
import type { ExhibitDetail, ZoneDetail } from '@/api/types'

export function useExhibitManager(
  allExhibits: Ref<ExhibitDetail[]>,
  currentZone: Ref<ZoneDetail | null>,
) {
  const selectedExhibitId = ref<number | null>(null)

  const zoneExhibits = computed(() => {
    if (!currentZone.value) return []
    return allExhibits.value
      .filter(e => e.zoneId === currentZone.value!.id)
      .sort((a, b) => a.sortOrder - b.sortOrder)
  })

  const selectedExhibit = computed(() =>
    selectedExhibitId.value
      ? allExhibits.value.find(e => e.id === selectedExhibitId.value) ?? null
      : null,
  )

  function selectExhibit(id: number | null) {
    selectedExhibitId.value = id
  }

  function addExhibit(exhibit: ExhibitDetail) {
    allExhibits.value.push(exhibit)
  }

  function updateExhibit(exhibitId: number, patch: Partial<ExhibitDetail>) {
    const idx = allExhibits.value.findIndex(e => e.id === exhibitId)
    if (idx >= 0) {
      allExhibits.value[idx] = { ...allExhibits.value[idx], ...patch } as ExhibitDetail
    }
  }

  function removeExhibit(exhibitId: number) {
    const idx = allExhibits.value.findIndex(e => e.id === exhibitId)
    if (idx >= 0) allExhibits.value.splice(idx, 1)
    if (selectedExhibitId.value === exhibitId) selectedExhibitId.value = null
  }

  return {
    zoneExhibits,
    selectedExhibitId,
    selectedExhibit,
    selectExhibit,
    addExhibit,
    updateExhibit,
    removeExhibit,
  }
}

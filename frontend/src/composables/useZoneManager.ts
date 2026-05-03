import { computed, ref, shallowRef, type Ref } from 'vue'
import type { ZoneDetail } from '@/api/types'

export interface ZoneManagerOptions {
  exhibitionId: Ref<number>
  onZoneSwitch?: (from: ZoneDetail | null, to: ZoneDetail) => Promise<void> | void
}

export function useZoneManager(options: ZoneManagerOptions) {
  const zones = ref<ZoneDetail[]>([])
  const currentZone = shallowRef<ZoneDetail | null>(null)
  const currentZoneIndex = computed(() =>
    currentZone.value ? zones.value.findIndex(z => z.id === currentZone.value!.id) : -1,
  )
  const switching = ref(false)

  function setZones(list: ZoneDetail[]) {
    zones.value = list.sort((a, b) => a.sortOrder - b.sortOrder)
    if (!currentZone.value && zones.value.length > 0) {
      currentZone.value = zones.value[0]
    }
  }

  async function switchToZone(zone: ZoneDetail) {
    if (currentZone.value?.id === zone.id || switching.value) return
    switching.value = true
    try {
      await options.onZoneSwitch?.(currentZone.value, zone)
      currentZone.value = zone
    } finally {
      switching.value = false
    }
  }

  async function switchToIndex(index: number) {
    if (index >= 0 && index < zones.value.length) {
      await switchToZone(zones.value[index])
    }
  }

  async function switchToNext() {
    const idx = currentZoneIndex.value
    if (idx < zones.value.length - 1) await switchToIndex(idx + 1)
  }

  async function switchToPrev() {
    const idx = currentZoneIndex.value
    if (idx > 0) await switchToIndex(idx - 1)
  }

  function updateZoneInList(zoneId: number, patch: Partial<ZoneDetail>) {
    const idx = zones.value.findIndex(z => z.id === zoneId)
    if (idx >= 0) {
      zones.value[idx] = { ...zones.value[idx], ...patch }
      if (currentZone.value?.id === zoneId) {
        currentZone.value = zones.value[idx]
      }
    }
  }

  function addZoneToList(zone: ZoneDetail) {
    zones.value.push(zone)
    zones.value.sort((a, b) => a.sortOrder - b.sortOrder)
  }

  function removeZoneFromList(zoneId: number) {
    const idx = zones.value.findIndex(z => z.id === zoneId)
    if (idx >= 0) {
      zones.value.splice(idx, 1)
      if (currentZone.value?.id === zoneId) {
        currentZone.value = zones.value[0] ?? null
      }
    }
  }

  return {
    zones,
    currentZone,
    currentZoneIndex,
    switching,
    setZones,
    switchToZone,
    switchToIndex,
    switchToNext,
    switchToPrev,
    updateZoneInList,
    addZoneToList,
    removeZoneFromList,
  }
}

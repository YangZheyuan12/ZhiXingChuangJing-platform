import { ref, type ShallowRef } from 'vue'
import type { Canvas } from 'fabric'
import { saveExhibitionVersion } from '@/api/modules/exhibitions'
import type { SaveExhibitionVersionRequest } from '@/api/types'
import { isDrawingGuides } from './useAlignmentGuides'

const AUTOSAVE_DELAY_MS = 10_000

export function useCanvasAutosave(
  fabricCanvas: ShallowRef<Canvas | null>,
  exhibitionId: () => number,
  buildPayload: () => SaveExhibitionVersionRequest,
) {
  const lastAutosaveAt = ref<Date | null>(null)
  const autosaveError = ref('')
  const autosaving = ref(false)
  const paused = ref(false)

  let dirty = false
  let timer: ReturnType<typeof setTimeout> | null = null
  let saving = false

  function markDirty() {
    if (paused.value || isDrawingGuides()) return
    dirty = true
    scheduleAutosave()
  }

  function scheduleAutosave() {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      void flush()
    }, AUTOSAVE_DELAY_MS)
  }

  async function flush() {
    if (!dirty || saving || !fabricCanvas.value) return
    saving = true
    autosaving.value = true
    autosaveError.value = ''
    try {
      const payload = buildPayload()
      payload.saveType = 'autosave'
      payload.versionNote = '自动保存'
      await saveExhibitionVersion(exhibitionId(), payload)
      dirty = false
      lastAutosaveAt.value = new Date()
    } catch (e: unknown) {
      autosaveError.value = e instanceof Error ? e.message : '自动保存失败'
    } finally {
      saving = false
      autosaving.value = false
    }
  }

  function clearDirty() {
    dirty = false
    if (timer) { clearTimeout(timer); timer = null }
  }

  function pauseAutosave() {
    paused.value = true
    if (timer) { clearTimeout(timer); timer = null }
  }

  function resumeAutosave() {
    paused.value = false
  }

  function bindCanvasEvents() {
    const c = fabricCanvas.value
    if (!c) return
    c.on('object:added', markDirty)
    c.on('object:modified', markDirty)
    c.on('object:removed', markDirty)
  }

  function unbindCanvasEvents() {
    const c = fabricCanvas.value
    if (!c) return
    c.off('object:added', markDirty)
    c.off('object:modified', markDirty)
    c.off('object:removed', markDirty)
  }

  function destroy() {
    if (timer) { clearTimeout(timer); timer = null }
    unbindCanvasEvents()
  }

  return {
    lastAutosaveAt,
    autosaveError,
    autosaving,
    markDirty,
    flush,
    clearDirty,
    pauseAutosave,
    resumeAutosave,
    bindCanvasEvents,
    unbindCanvasEvents,
    destroy,
  }
}

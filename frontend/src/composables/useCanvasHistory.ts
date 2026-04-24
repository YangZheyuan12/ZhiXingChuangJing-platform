import { ref, type ShallowRef } from 'vue'
import type { Canvas } from 'fabric'
import { isDrawingGuides } from './useAlignmentGuides'

const MAX_STACK_SIZE = 50

export function useCanvasHistory(fabricCanvas: ShallowRef<Canvas | null>) {
  const undoStack = ref<string[]>([])
  const redoStack = ref<string[]>([])
  const canUndo = ref(false)
  const canRedo = ref(false)

  let isReplaying = false
  let debounceTimer: ReturnType<typeof setTimeout> | null = null

  function refreshFlags() {
    canUndo.value = undoStack.value.length > 1
    canRedo.value = redoStack.value.length > 0
  }

  function takeSnapshot() {
    if (isReplaying || isDrawingGuides() || !fabricCanvas.value) return
    if (debounceTimer) clearTimeout(debounceTimer)
    debounceTimer = setTimeout(() => {
      if (!fabricCanvas.value) return
      const json = JSON.stringify((fabricCanvas.value as any).toJSON(['assetType', 'assetId', 'mediaUrl', 'assetName']))
      if (undoStack.value.length > 0 && undoStack.value[undoStack.value.length - 1] === json) return
      undoStack.value.push(json)
      if (undoStack.value.length > MAX_STACK_SIZE) {
        undoStack.value.splice(0, undoStack.value.length - MAX_STACK_SIZE)
      }
      redoStack.value = []
      refreshFlags()
    }, 200)
  }

  function reset() {
    undoStack.value = []
    redoStack.value = []
    if (fabricCanvas.value) {
      const json = JSON.stringify((fabricCanvas.value as any).toJSON(['assetType', 'assetId', 'mediaUrl', 'assetName']))
      undoStack.value.push(json)
    }
    refreshFlags()
  }

  async function undo() {
    if (!canUndo.value || !fabricCanvas.value) return
    if (debounceTimer) { clearTimeout(debounceTimer); debounceTimer = null }
    isReplaying = true
    try {
      const current = undoStack.value.pop()!
      redoStack.value.push(current)
      const prev = undoStack.value[undoStack.value.length - 1]
      await fabricCanvas.value.loadFromJSON(prev)
      fabricCanvas.value.requestRenderAll()
    } finally {
      isReplaying = false
      refreshFlags()
    }
  }

  async function redo() {
    if (!canRedo.value || !fabricCanvas.value) return
    if (debounceTimer) { clearTimeout(debounceTimer); debounceTimer = null }
    isReplaying = true
    try {
      const next = redoStack.value.pop()!
      undoStack.value.push(next)
      await fabricCanvas.value.loadFromJSON(next)
      fabricCanvas.value.requestRenderAll()
    } finally {
      isReplaying = false
      refreshFlags()
    }
  }

  function bindCanvasEvents() {
    const c = fabricCanvas.value
    if (!c) return
    c.on('object:added', takeSnapshot)
    c.on('object:modified', takeSnapshot)
    c.on('object:removed', takeSnapshot)
  }

  function unbindCanvasEvents() {
    const c = fabricCanvas.value
    if (!c) return
    c.off('object:added', takeSnapshot)
    c.off('object:modified', takeSnapshot)
    c.off('object:removed', takeSnapshot)
  }

  return {
    canUndo,
    canRedo,
    undo,
    redo,
    reset,
    takeSnapshot,
    bindCanvasEvents,
    unbindCanvasEvents,
  }
}

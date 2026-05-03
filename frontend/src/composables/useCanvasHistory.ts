import { ref, type ShallowRef } from 'vue'
import type { Canvas } from 'fabric'
import { isDrawingGuides } from './useAlignmentGuides'

const MAX_STACK_SIZE = 50
const CUSTOM_PROPS = ['assetType', 'assetId', 'mediaUrl', 'assetName']

interface ZoneStack {
  undo: string[]
  redo: string[]
}

export function useCanvasHistory(fabricCanvas: ShallowRef<Canvas | null>) {
  const canUndo = ref(false)
  const canRedo = ref(false)

  let isReplaying = false
  let debounceTimer: ReturnType<typeof setTimeout> | null = null

  // ─── per-zone history stacks ───
  const stacks = new Map<string, ZoneStack>()
  let currentZoneKey = '__default__'

  function getStack(): ZoneStack {
    let s = stacks.get(currentZoneKey)
    if (!s) {
      s = { undo: [], redo: [] }
      stacks.set(currentZoneKey, s)
    }
    return s
  }

  function refreshFlags() {
    const s = getStack()
    canUndo.value = s.undo.length > 1
    canRedo.value = s.redo.length > 0
  }

  function switchZone(zoneKey: string) {
    currentZoneKey = zoneKey
    refreshFlags()
  }

  function takeSnapshot() {
    if (isReplaying || isDrawingGuides() || !fabricCanvas.value) return
    if (debounceTimer) clearTimeout(debounceTimer)
    debounceTimer = setTimeout(() => {
      if (!fabricCanvas.value) return
      const s = getStack()
      const json = JSON.stringify((fabricCanvas.value as any).toJSON(CUSTOM_PROPS))
      if (s.undo.length > 0 && s.undo[s.undo.length - 1] === json) return
      s.undo.push(json)
      if (s.undo.length > MAX_STACK_SIZE) {
        s.undo.splice(0, s.undo.length - MAX_STACK_SIZE)
      }
      s.redo = []
      refreshFlags()
    }, 200)
  }

  function reset() {
    const s = getStack()
    s.undo = []
    s.redo = []
    if (fabricCanvas.value) {
      const json = JSON.stringify((fabricCanvas.value as any).toJSON(CUSTOM_PROPS))
      s.undo.push(json)
    }
    refreshFlags()
  }

  async function undo() {
    const s = getStack()
    if (s.undo.length <= 1 || !fabricCanvas.value) return
    if (debounceTimer) { clearTimeout(debounceTimer); debounceTimer = null }
    isReplaying = true
    try {
      const current = s.undo.pop()!
      s.redo.push(current)
      const prev = s.undo[s.undo.length - 1]
      await fabricCanvas.value.loadFromJSON(prev)
      fabricCanvas.value.requestRenderAll()
    } finally {
      isReplaying = false
      refreshFlags()
    }
  }

  async function redo() {
    const s = getStack()
    if (s.redo.length === 0 || !fabricCanvas.value) return
    if (debounceTimer) { clearTimeout(debounceTimer); debounceTimer = null }
    isReplaying = true
    try {
      const next = s.redo.pop()!
      s.undo.push(next)
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
    switchZone,
    takeSnapshot,
    bindCanvasEvents,
    unbindCanvasEvents,
  }
}

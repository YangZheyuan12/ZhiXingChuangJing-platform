import type { ShallowRef } from 'vue'
import type { Canvas, FabricObject } from 'fabric'

export interface CanvasShortcutHandlers {
  undo: () => void
  redo: () => void
  save: () => void
}

export function useCanvasShortcuts(
  fabricCanvas: ShallowRef<Canvas | null>,
  handlers: CanvasShortcutHandlers,
) {
  let clipboard: FabricObject | null = null

  function isInputFocused() {
    const tag = document.activeElement?.tagName?.toLowerCase()
    return tag === 'input' || tag === 'textarea' || tag === 'select'
  }

  async function handleKeyDown(e: KeyboardEvent) {
    if (isInputFocused()) return
    const canvas = fabricCanvas.value
    if (!canvas) return

    const ctrl = e.ctrlKey || e.metaKey

    if (e.key === 'Delete' || e.key === 'Backspace') {
      e.preventDefault()
      deleteSelected(canvas)
      return
    }

    if (ctrl && e.key === 'z') {
      e.preventDefault()
      handlers.undo()
      return
    }

    if (ctrl && e.key === 'y') {
      e.preventDefault()
      handlers.redo()
      return
    }

    if (ctrl && e.key === 's') {
      e.preventDefault()
      handlers.save()
      return
    }

    if (ctrl && e.key === 'c') {
      e.preventDefault()
      await copySelected(canvas)
      return
    }

    if (ctrl && e.key === 'v') {
      e.preventDefault()
      await pasteClipboard(canvas)
      return
    }

    if (ctrl && e.key === 'd') {
      e.preventDefault()
      await duplicateSelected(canvas)
      return
    }
  }

  function deleteSelected(canvas: Canvas) {
    const active = canvas.getActiveObjects()
    if (active.length === 0) return
    active.forEach((obj) => canvas.remove(obj))
    canvas.discardActiveObject()
    canvas.requestRenderAll()
  }

  async function copySelected(canvas: Canvas) {
    const active = canvas.getActiveObject()
    if (!active) return
    clipboard = await active.clone()
  }

  async function pasteClipboard(canvas: Canvas) {
    if (!clipboard) return
    const cloned = await clipboard.clone()
    cloned.set({ left: (cloned.left ?? 0) + 20, top: (cloned.top ?? 0) + 20 })
    canvas.add(cloned)
    canvas.setActiveObject(cloned)
    canvas.requestRenderAll()
  }

  async function duplicateSelected(canvas: Canvas) {
    const active = canvas.getActiveObject()
    if (!active) return
    const cloned = await active.clone()
    cloned.set({ left: (cloned.left ?? 0) + 20, top: (cloned.top ?? 0) + 20 })
    canvas.add(cloned)
    canvas.setActiveObject(cloned)
    canvas.requestRenderAll()
  }

  function bind() {
    document.addEventListener('keydown', handleKeyDown)
  }

  function unbind() {
    document.removeEventListener('keydown', handleKeyDown)
  }

  return { bind, unbind }
}

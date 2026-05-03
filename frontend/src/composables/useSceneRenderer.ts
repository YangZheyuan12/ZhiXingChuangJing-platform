import { ref, shallowRef, type Ref } from 'vue'
import { Canvas } from 'fabric'
import type { HotspotDetail, ZoneDetail } from '@/api/types'

const LOGICAL_WIDTH = 1920
const LOGICAL_HEIGHT = 1080
const CUSTOM_PROPS = ['assetType', 'assetId', 'mediaUrl', 'assetName']

export interface SceneRendererOptions {
  canvasElRef: Ref<HTMLCanvasElement | null>
  wrapperRef: Ref<HTMLElement | null>
  allHotspots: Ref<HotspotDetail[]>
  readonly?: boolean
  onCanvasReady?: (canvas: Canvas) => void
  onCanvasDestroyed?: () => void
}

export function useSceneRenderer(options: SceneRendererOptions) {
  const fabricCanvas = shallowRef<Canvas | null>(null)
  const currentZoom = ref(1)
  const transitioning = ref(false)
  const currentBackgroundUrl = ref<string | null>(null)
  const currentHotspots = ref<HotspotDetail[]>([])

  function getCurrentCanvasJson(): Record<string, unknown> | null {
    const canvas = fabricCanvas.value
    if (!canvas) return null
    return (canvas as any).toJSON(CUSTOM_PROPS)
  }

  async function initScene(zone: ZoneDetail) {
    if (!options.canvasElRef.value) return

    currentBackgroundUrl.value = zone.backgroundUrl ?? null
    currentHotspots.value = options.allHotspots.value.filter(h => h.zoneId === zone.id)

    const canvas = new Canvas(options.canvasElRef.value, {
      width: LOGICAL_WIDTH,
      height: LOGICAL_HEIGHT,
      backgroundColor: 'transparent',
      selection: !options.readonly,
    })
    fabricCanvas.value = canvas

    if (zone.canvasData && typeof zone.canvasData === 'object' && 'objects' in zone.canvasData) {
      await canvas.loadFromJSON(zone.canvasData)
    }

    if (options.readonly) {
      canvas.getObjects().forEach((obj) => {
        obj.set({ selectable: false, evented: false })
      })
    }

    fitToContainer()
    options.onCanvasReady?.(canvas)
  }

  function destroyScene() {
    options.onCanvasDestroyed?.()
    fabricCanvas.value?.dispose()
    fabricCanvas.value = null
    currentBackgroundUrl.value = null
    currentHotspots.value = []
  }

  async function switchScene(
    fromZone: ZoneDetail | null,
    toZone: ZoneDetail,
    saveCanvasData: (zoneId: number, json: Record<string, unknown>) => void,
  ) {
    transitioning.value = true
    try {
      if (fromZone && fabricCanvas.value) {
        const json = getCurrentCanvasJson()
        if (json) saveCanvasData(fromZone.id, json)
      }
      destroyScene()
      await new Promise(r => setTimeout(r, 300))
      await initScene(toZone)
    } finally {
      transitioning.value = false
    }
  }

  function fitToContainer() {
    const canvas = fabricCanvas.value
    const wrapper = options.wrapperRef.value
    if (!canvas || !wrapper) return

    const availW = wrapper.clientWidth
    const availH = wrapper.clientHeight
    const fitRatio = Math.min(availW / LOGICAL_WIDTH, availH / LOGICAL_HEIGHT)

    currentZoom.value = fitRatio
    canvas.setZoom(fitRatio)
    canvas.setDimensions({
      width: LOGICAL_WIDTH * fitRatio,
      height: LOGICAL_HEIGHT * fitRatio,
    })
    canvas.requestRenderAll()
  }

  return {
    fabricCanvas,
    currentZoom,
    transitioning,
    currentBackgroundUrl,
    currentHotspots,
    LOGICAL_WIDTH,
    LOGICAL_HEIGHT,
    initScene,
    destroyScene,
    switchScene,
    fitToContainer,
    getCurrentCanvasJson,
  }
}

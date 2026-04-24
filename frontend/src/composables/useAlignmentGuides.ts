import { type ShallowRef } from 'vue'
import { Line, type Canvas, type FabricObject } from 'fabric'

const SNAP_THRESHOLD = 8
const GUIDE_COLOR = '#3b82f6'

let _isDrawingGuides = false
export function isDrawingGuides() { return _isDrawingGuides }

export function useAlignmentGuides(
  fabricCanvas: ShallowRef<Canvas | null>,
  logicalWidth: number,
  logicalHeight: number,
) {
  const guideLines: Line[] = []

  function clearGuides() {
    _isDrawingGuides = true
    const canvas = fabricCanvas.value
    if (!canvas) return
    guideLines.forEach((line) => canvas.remove(line))
    guideLines.length = 0
    _isDrawingGuides = false
  }

  function addGuideLine(x1: number, y1: number, x2: number, y2: number) {
    const canvas = fabricCanvas.value
    if (!canvas) return
    _isDrawingGuides = true
    const line = new Line([x1, y1, x2, y2], {
      stroke: GUIDE_COLOR,
      strokeWidth: 1,
      strokeDashArray: [4, 4],
      selectable: false,
      evented: false,
      excludeFromExport: true,
    })
    ;(line as any).__isGuide = true
    guideLines.push(line)
    canvas.add(line)
    _isDrawingGuides = false
  }

  function getSnapPoints(movingObj: FabricObject): { hSnaps: number[]; vSnaps: number[] } {
    const canvas = fabricCanvas.value
    if (!canvas) return { hSnaps: [], vSnaps: [] }

    const hSnaps: number[] = [0, logicalHeight / 2, logicalHeight]
    const vSnaps: number[] = [0, logicalWidth / 2, logicalWidth]

    canvas.getObjects().forEach((obj) => {
      if (obj === movingObj || (obj as any).__isGuide) return
      const left = obj.left ?? 0
      const top = obj.top ?? 0
      const w = (obj.width ?? 0) * (obj.scaleX ?? 1)
      const h = (obj.height ?? 0) * (obj.scaleY ?? 1)

      vSnaps.push(left, left + w / 2, left + w)
      hSnaps.push(top, top + h / 2, top + h)
    })

    return { hSnaps, vSnaps }
  }

  function onObjectMoving(e: { target?: FabricObject }) {
    clearGuides()
    const obj = e.target
    if (!obj) return

    const left = obj.left ?? 0
    const top = obj.top ?? 0
    const w = (obj.width ?? 0) * (obj.scaleX ?? 1)
    const h = (obj.height ?? 0) * (obj.scaleY ?? 1)
    const objCenterX = left + w / 2
    const objCenterY = top + h / 2
    const objRight = left + w
    const objBottom = top + h

    const { hSnaps, vSnaps } = getSnapPoints(obj)

    let snappedLeft: number | null = null
    let snappedTop: number | null = null

    for (const vx of vSnaps) {
      if (Math.abs(left - vx) < SNAP_THRESHOLD) {
        snappedLeft = vx
        addGuideLine(vx, 0, vx, logicalHeight)
        break
      }
      if (Math.abs(objCenterX - vx) < SNAP_THRESHOLD) {
        snappedLeft = vx - w / 2
        addGuideLine(vx, 0, vx, logicalHeight)
        break
      }
      if (Math.abs(objRight - vx) < SNAP_THRESHOLD) {
        snappedLeft = vx - w
        addGuideLine(vx, 0, vx, logicalHeight)
        break
      }
    }

    for (const hy of hSnaps) {
      if (Math.abs(top - hy) < SNAP_THRESHOLD) {
        snappedTop = hy
        addGuideLine(0, hy, logicalWidth, hy)
        break
      }
      if (Math.abs(objCenterY - hy) < SNAP_THRESHOLD) {
        snappedTop = hy - h / 2
        addGuideLine(0, hy, logicalWidth, hy)
        break
      }
      if (Math.abs(objBottom - hy) < SNAP_THRESHOLD) {
        snappedTop = hy - h
        addGuideLine(0, hy, logicalWidth, hy)
        break
      }
    }

    if (snappedLeft !== null) obj.set('left', snappedLeft)
    if (snappedTop !== null) obj.set('top', snappedTop)
  }

  function onObjectModified() {
    clearGuides()
  }

  function bind() {
    const canvas = fabricCanvas.value
    if (!canvas) return
    canvas.on('object:moving', onObjectMoving as any)
    canvas.on('object:modified', onObjectModified)
    canvas.on('mouse:up', onObjectModified)
  }

  function unbind() {
    const canvas = fabricCanvas.value
    if (!canvas) return
    canvas.off('object:moving', onObjectMoving as any)
    canvas.off('object:modified', onObjectModified)
    canvas.off('mouse:up', onObjectModified)
    clearGuides()
  }

  return { bind, unbind, clearGuides }
}

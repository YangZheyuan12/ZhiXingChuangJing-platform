import type { HotspotDetail } from '@/api/types'

/**
 * 预设图标名到 Unicode 符号/Emoji 的映射。
 * 与 HotspotPropertiesPanel 里提供的 icon 选项保持一一对应。
 */
export const HOTSPOT_ICON_SYMBOLS: Record<string, string> = {
  'arrow-right': '→',
  'arrow-left': '←',
  'arrow-up': '↑',
  'arrow-down': '↓',
  'chevron-right': '›',
  'chevron-left': '‹',
  info: 'ℹ',
  'info-circle': 'ℹ',
  link: '🔗',
  image: '🖼',
  picture: '🖼',
  mic: '🎙',
  home: '⌂',
  close: '✕',
  plus: '+',
  play: '▶',
  pause: '⏸',
  star: '★',
}

/**
 * 热点类型的默认图标，作为用户未配置 icon 字段时的兜底。
 */
const HOTSPOT_TYPE_DEFAULT_ICONS: Record<string, string> = {
  navigation: '→',
  exhibit_popup: '🖼',
  external_link: '🔗',
  narration_trigger: '🎙',
}

/**
 * 将热点的 icon 字段解析为可直接渲染的符号。
 *
 * 优先级：用户配置的 icon（预设名→映射；非预设名→原样）→ 热点类型默认图标 → 圆点兜底。
 */
export function resolveHotspotIcon(hotspot: Pick<HotspotDetail, 'icon' | 'hotspotType'>): string {
  const configured = hotspot.icon?.trim()
  if (configured) {
    return HOTSPOT_ICON_SYMBOLS[configured] ?? configured
  }
  return HOTSPOT_TYPE_DEFAULT_ICONS[hotspot.hotspotType] ?? '•'
}

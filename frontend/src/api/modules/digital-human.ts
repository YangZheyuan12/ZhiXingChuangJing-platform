import { http } from '@/utils/request'
import type {
  BindEquipmentRequest,
  DigitalHuman,
  DigitalHumanEquipment,
  PlaceDigitalHumanRequest,
  UpsertDigitalHumanRequest,
  ZoneDigitalHumanPlacement,
} from '@/api/types'

// ═══ 旧接口（兼容期保留）═══

/** 旧接口：拿展厅最早创建的角色，新逻辑请用 listDigitalHumans。 */
export function getDigitalHuman(exhibitionId: number) {
  return http.get<DigitalHuman | null>(`/exhibitions/${exhibitionId}/digital-human`)
}

// ═══ 角色库 CRUD ═══

/** 列展厅所有数字人角色。 */
export function listDigitalHumans(exhibitionId: number) {
  return http.get<DigitalHuman[]>(`/exhibitions/${exhibitionId}/digital-humans`)
}

/** 创建一个新数字人角色。 */
export function createDigitalHuman(exhibitionId: number, payload: UpsertDigitalHumanRequest) {
  return http.post<DigitalHuman>(`/exhibitions/${exhibitionId}/digital-humans`, payload)
}

/** 编辑数字人角色（不变更归属展厅）。 */
export function updateDigitalHuman(digitalHumanId: number, payload: UpsertDigitalHumanRequest) {
  return http.put<DigitalHuman>(`/digital-humans/${digitalHumanId}`, payload)
}

/** 删除数字人角色。后端会校验该角色未被任何展区引用。 */
export function deleteDigitalHuman(digitalHumanId: number) {
  return http.delete<void>(`/digital-humans/${digitalHumanId}`)
}

// ═══ 展区数字人摆放 ═══

/**
 * 把数字人摆到某个展区（或更新位置/朝向）。
 * 同一展区已存在则覆盖（zone : 数字人 = 1 : 1）。
 */
export function placeZoneDigitalHuman(
  exhibitionId: number,
  zoneId: number,
  payload: PlaceDigitalHumanRequest,
) {
  return http.put<ZoneDigitalHumanPlacement>(
    `/exhibitions/${exhibitionId}/zones/${zoneId}/digital-human`,
    payload,
  )
}

/** 撤掉某个展区的数字人摆放（不删除数字人本身）。 */
export function removeZoneDigitalHuman(exhibitionId: number, zoneId: number) {
  return http.delete<void>(`/exhibitions/${exhibitionId}/zones/${zoneId}/digital-human`)
}

// ═══ 装备绑定（既有）═══

export function addDigitalHumanEquipment(digitalHumanId: number, payload: BindEquipmentRequest) {
  return http.post<DigitalHumanEquipment>(`/digital-humans/${digitalHumanId}/equipments`, payload)
}

export function deleteDigitalHumanEquipment(digitalHumanId: number, equipmentId: number) {
  return http.delete<void>(`/digital-humans/${digitalHumanId}/equipments/${equipmentId}`)
}

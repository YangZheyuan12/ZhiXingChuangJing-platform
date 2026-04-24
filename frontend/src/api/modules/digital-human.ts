import { http } from '@/utils/request'
import type { BindEquipmentRequest, DigitalHuman, DigitalHumanEquipment } from '@/api/types'

export function getDigitalHuman(exhibitionId: number) {
  return http.get<DigitalHuman | null>(`/exhibitions/${exhibitionId}/digital-human`)
}

export function addDigitalHumanEquipment(digitalHumanId: number, payload: BindEquipmentRequest) {
  return http.post<DigitalHumanEquipment>(`/digital-humans/${digitalHumanId}/equipments`, payload)
}

export function deleteDigitalHumanEquipment(digitalHumanId: number, equipmentId: number) {
  return http.delete<void>(`/digital-humans/${digitalHumanId}/equipments/${equipmentId}`)
}

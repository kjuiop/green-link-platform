import { authHeaders, fetchWithAuth } from './fetchWithAuth'

export type SettlementType = 'VENDOR' | 'ORDERING'

export interface SettlementSummary {
  companyId: number
  companyName: string
  count: number
  orderAmountTotal: number
  vendorAmountTotal: number
  completed: boolean
  completedAt: string | null
}

export interface SettlementOrder {
  orderId: number
  orderNumber: string
  counterpartCompanyName: string
  productName: string
  placeName: string
  ordererName: string
  deliveredAt: string
  orderAmount: number
  vendorAmount: number | null
}

export async function fetchAdminSettlements(
  fromDate: string,
  toDate: string,
  type: SettlementType
): Promise<SettlementSummary[]> {
  const params = new URLSearchParams({ fromDate, toDate, type })
  const res = await fetchWithAuth(`/api/admin/settlements?${params}`, { headers: authHeaders() })
  if (!res.ok) throw new Error('정산 목록 조회에 실패했습니다.')
  return res.json()
}

export async function fetchAdminSettlementDetail(
  companyId: number,
  fromDate: string,
  toDate: string,
  type: SettlementType
): Promise<SettlementOrder[]> {
  const params = new URLSearchParams({ fromDate, toDate, type })
  const res = await fetchWithAuth(`/api/admin/settlements/${companyId}?${params}`, { headers: authHeaders() })
  if (!res.ok) throw new Error('정산 상세 조회에 실패했습니다.')
  return res.json()
}

export async function completeSettlement(
  companyId: number,
  fromDate: string,
  toDate: string,
  type: SettlementType
): Promise<void> {
  const params = new URLSearchParams({ fromDate, toDate, type })
  const res = await fetchWithAuth(`/api/admin/settlements/${companyId}/complete?${params}`, {
    method: 'POST',
    headers: authHeaders(),
  })
  if (!res.ok) throw new Error('정산완료 처리에 실패했습니다.')
}

export async function cancelSettlement(
  companyId: number,
  fromDate: string,
  toDate: string,
  type: SettlementType
): Promise<void> {
  const params = new URLSearchParams({ fromDate, toDate, type })
  const res = await fetchWithAuth(`/api/admin/settlements/${companyId}/complete?${params}`, {
    method: 'DELETE',
    headers: authHeaders(),
  })
  if (!res.ok) throw new Error('정산완료 취소에 실패했습니다.')
}

export function getSettlementExcelUrl(
  companyId: number,
  fromDate: string,
  toDate: string,
  type: SettlementType
): string {
  const params = new URLSearchParams({ fromDate, toDate, type, companyId: String(companyId) })
  return `/api/admin/settlements/excel?${params}`
}

export async function fetchMySettlement(
  fromDate: string,
  toDate: string,
  type: SettlementType
): Promise<SettlementOrder[]> {
  const params = new URLSearchParams({ fromDate, toDate, type })
  const res = await fetchWithAuth(`/api/settlements/my?${params}`, { headers: authHeaders() })
  if (!res.ok) throw new Error('내 정산내역 조회에 실패했습니다.')
  return res.json()
}

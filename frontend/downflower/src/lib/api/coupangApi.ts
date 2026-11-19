import { authHeaders, fetchWithAuth } from './fetchWithAuth'
import { PageResult } from '@/types/page'

export type CoupangOrderStatus = 'PENDING' | 'HELD' | 'APPROVED' | 'CANCELED'

export interface CoupangCredential {
  companyId: number
  vendorCode: string
  accessKeyMasked: string
  secretKeyRegistered: boolean
  keyIssuedAt: string
  keyExpiresAt: string
  daysUntilExpire: number | null
  expired: boolean
  enabled: boolean
  lastCollectedAt: string | null
  lastError: string | null
  lastErrorAt: string | null
}

export interface SaveCoupangCredentialData {
  companyId?: number | null
  vendorCode: string
  accessKey: string
  secretKey: string
  keyIssuedAt: string
  enabled: boolean
}

export interface CoupangSyncLog {
  id: number
  triggerType: 'SCHEDULED' | 'MANUAL'
  rangeFrom: string | null
  rangeTo: string | null
  fetchedCount: number
  insertedCount: number
  success: boolean
  errorMessage: string | null
  createdAt: string
}

export interface CoupangOrderItem {
  name: string
  quantity: number
  salesPrice: number
}

export interface CoupangOrder {
  id: number
  companyId: number
  orderId: number
  shipmentBoxId: number
  status: CoupangOrderStatus
  coupangStatus: string | null
  orderedAt: string | null
  ordererName: string | null
  ordererPhone: string | null
  receiverName: string | null
  receiverPhone: string | null
  receiverAddress: string | null
  receiverAddressDetail: string | null
  deliveryMessage: string | null
  items: CoupangOrderItem[]
  totalPrice: number
  linkedOrderId: number | null
  approvedAt: string | null
  createdAt: string | null
}

export interface CoupangSyncResult {
  from: string
  to: string
  fetchedCount: number
  insertedCount: number
}

async function errorMessage(res: Response, fallback: string): Promise<string> {
  try {
    const body = await res.json()
    return body.message || fallback
  } catch {
    return fallback
  }
}

function withCompany(path: string, companyId?: number | null, extra?: Record<string, string>): string {
  const params = new URLSearchParams(extra)
  if (companyId) params.set('companyId', String(companyId))
  const qs = params.toString()
  return qs ? `${path}?${qs}` : path
}

export async function fetchCoupangCredential(companyId?: number | null): Promise<CoupangCredential | null> {
  const res = await fetchWithAuth(withCompany('/api/admin/coupang/credentials', companyId), { headers: authHeaders() })
  if (res.status === 204) return null
  if (!res.ok) throw new Error(await errorMessage(res, '쿠팡 연동 정보를 불러오지 못했습니다.'))
  return res.json()
}

export async function saveCoupangCredential(data: SaveCoupangCredentialData): Promise<CoupangCredential> {
  const res = await fetchWithAuth('/api/admin/coupang/credentials', {
    method: 'PUT',
    headers: authHeaders(),
    body: JSON.stringify(data),
  })
  if (!res.ok) throw new Error(await errorMessage(res, '쿠팡 연동 정보 저장에 실패했습니다.'))
  return res.json()
}

export async function testCoupangCredential(companyId?: number | null): Promise<{ success: boolean; message: string }> {
  const res = await fetchWithAuth(withCompany('/api/admin/coupang/credentials/test', companyId), {
    method: 'POST',
    headers: authHeaders(),
  })
  if (!res.ok) throw new Error(await errorMessage(res, '연결 테스트에 실패했습니다.'))
  return res.json()
}

export async function fetchCoupangSyncLogs(companyId?: number | null, limit = 10): Promise<CoupangSyncLog[]> {
  const res = await fetchWithAuth(withCompany('/api/admin/coupang/sync-logs', companyId, { limit: String(limit) }), {
    headers: authHeaders(),
  })
  if (!res.ok) throw new Error(await errorMessage(res, '수집 이력을 불러오지 못했습니다.'))
  return res.json()
}

export async function fetchCoupangOrders(
  companyId: number | null | undefined,
  status: CoupangOrderStatus,
  page = 0,
  size = 20,
): Promise<PageResult<CoupangOrder>> {
  const res = await fetchWithAuth(
    withCompany('/api/admin/coupang/orders', companyId, { status, page: String(page), size: String(size) }),
    { headers: authHeaders() },
  )
  if (!res.ok) throw new Error(await errorMessage(res, '쿠팡 주문을 불러오지 못했습니다.'))
  return res.json()
}

export async function fetchCoupangOrder(id: number): Promise<CoupangOrder> {
  const res = await fetchWithAuth(`/api/admin/coupang/orders/${id}`, { headers: authHeaders() })
  if (!res.ok) throw new Error(await errorMessage(res, '쿠팡 주문을 불러오지 못했습니다.'))
  return res.json()
}

export async function fetchCoupangPendingCount(companyId?: number | null): Promise<number> {
  const res = await fetchWithAuth(withCompany('/api/admin/coupang/orders/pending-count', companyId), {
    headers: authHeaders(),
  })
  if (!res.ok) return 0
  const body = await res.json()
  return body.count ?? 0
}

export async function collectCoupangOrders(companyId?: number | null): Promise<CoupangSyncResult> {
  const res = await fetchWithAuth(withCompany('/api/admin/coupang/orders/collect', companyId), {
    method: 'POST',
    headers: authHeaders(),
  })
  if (!res.ok) throw new Error(await errorMessage(res, '쿠팡 주문을 불러오지 못했습니다.'))
  return res.json()
}

export async function holdCoupangOrder(id: number): Promise<CoupangOrder> {
  const res = await fetchWithAuth(`/api/admin/coupang/orders/${id}/hold`, { method: 'POST', headers: authHeaders() })
  if (!res.ok) throw new Error(await errorMessage(res, '보류 처리에 실패했습니다.'))
  return res.json()
}

export async function releaseCoupangOrder(id: number): Promise<CoupangOrder> {
  const res = await fetchWithAuth(`/api/admin/coupang/orders/${id}/release`, { method: 'POST', headers: authHeaders() })
  if (!res.ok) throw new Error(await errorMessage(res, '보류 해제에 실패했습니다.'))
  return res.json()
}

export async function approveCoupangOrder(id: number, body: unknown): Promise<{ id: number; orderNumber: string }> {
  const res = await fetchWithAuth(`/api/admin/coupang/orders/${id}/approve`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify(body),
  })
  if (!res.ok) throw new Error(await errorMessage(res, '승인(발주) 처리에 실패했습니다.'))
  return res.json()
}

import { authHeaders, fetchWithAuth } from './fetchWithAuth'
import { PageResult } from '@/types/page'

export type CustomerOrderStatus = 'PENDING' | 'HELD' | 'APPROVED' | 'CANCELLED'
export type OrderChannel = 'COUPANG' | 'SHOP' | 'DIRECT'

export interface CustomerOrderItem {
  name: string
  quantity: number
  salesPrice: number
}

export interface CustomerOrderCoupangMeta {
  coupangOrderId: number
  coupangShipmentBoxId: number
  coupangStatus: string | null
  items: CustomerOrderItem[]
}

export interface CustomerOrder {
  id: number
  companyId: number
  channel: OrderChannel
  status: CustomerOrderStatus
  ordererName: string | null
  ordererPhone: string | null
  receiverName: string | null
  receiverPhone: string | null
  receiverAddress: string | null
  receiverAddressDetail: string | null
  deliveryMessage: string | null
  totalPrice: number
  orderedAt: string | null
  approvedBy: number | null
  approvedAt: string | null
  createdAt: string
  coupangMeta: CustomerOrderCoupangMeta | null
}

async function errorMessage(res: Response, fallback: string): Promise<string> {
  try {
    const body = await res.json()
    return body.message || fallback
  } catch {
    return fallback
  }
}

function buildParams(opts: Record<string, string | number | undefined | null>): string {
  const p = new URLSearchParams()
  for (const [k, v] of Object.entries(opts)) {
    if (v != null && v !== '') p.set(k, String(v))
  }
  const s = p.toString()
  return s ? `?${s}` : ''
}

export async function fetchCustomerOrders(
  companyId: number | null | undefined,
  channel: OrderChannel | null,
  status: CustomerOrderStatus | null,
  page = 0,
  size = 20,
): Promise<PageResult<CustomerOrder>> {
  const qs = buildParams({ companyId, channel: channel ?? undefined, status: status ?? undefined, page, size })
  const res = await fetchWithAuth(`/api/admin/customer-orders${qs}`, { headers: authHeaders() })
  if (!res.ok) throw new Error(await errorMessage(res, '주문을 불러오지 못했습니다.'))
  return res.json()
}

export async function fetchCustomerOrderPendingCount(
  companyId: number | null | undefined,
  channel: OrderChannel | null,
): Promise<number> {
  const qs = buildParams({ companyId, channel: channel ?? undefined })
  const res = await fetchWithAuth(`/api/admin/customer-orders/pending-count${qs}`, { headers: authHeaders() })
  if (!res.ok) return 0
  const body = await res.json()
  return body.count ?? 0
}

export async function holdCustomerOrder(id: number): Promise<CustomerOrder> {
  const res = await fetchWithAuth(`/api/admin/customer-orders/${id}/hold`, { method: 'POST', headers: authHeaders() })
  if (!res.ok) throw new Error(await errorMessage(res, '보류 처리에 실패했습니다.'))
  return res.json()
}

export async function releaseCustomerOrder(id: number): Promise<CustomerOrder> {
  const res = await fetchWithAuth(`/api/admin/customer-orders/${id}/release`, { method: 'POST', headers: authHeaders() })
  if (!res.ok) throw new Error(await errorMessage(res, '보류 해제에 실패했습니다.'))
  return res.json()
}

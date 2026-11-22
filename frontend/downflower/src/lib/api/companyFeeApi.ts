import { authHeaders, fetchWithAuth } from './fetchWithAuth'
import { PageResult } from '@/types/page'

export type FeeDiscountType = 'NONE' | 'PERCENT' | 'FIXED'

export interface OrderingFee {
  companyId: number
  companyName: string
  type: 'ORDERING' | 'BOTH'
  businessNo: string | null
  orderingFeeDiscountType: FeeDiscountType
  orderingFeeDiscountValue: number
}

async function errorMessage(res: Response, fallback: string): Promise<string> {
  try {
    const body = await res.json()
    return body.message || fallback
  } catch {
    return fallback
  }
}

export async function fetchOrderingFees(keyword: string, page = 0, size = 20): Promise<PageResult<OrderingFee>> {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  if (keyword.trim()) params.set('keyword', keyword.trim())
  const res = await fetchWithAuth(`/api/admin/ordering-fees?${params}`, { headers: authHeaders() })
  if (!res.ok) throw new Error(await errorMessage(res, '수수료 감면 목록 조회에 실패했습니다.'))
  return res.json()
}

export async function updateOrderingFee(
  companyId: number,
  discountType: FeeDiscountType,
  discountValue: number,
): Promise<OrderingFee> {
  const res = await fetchWithAuth(`/api/admin/ordering-fees/${companyId}`, {
    method: 'PUT',
    headers: { ...authHeaders(), 'Content-Type': 'application/json' },
    body: JSON.stringify({ discountType, discountValue }),
  })
  if (!res.ok) throw new Error(await errorMessage(res, '수수료 감면 수정에 실패했습니다.'))
  return res.json()
}

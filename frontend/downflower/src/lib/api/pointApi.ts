import { authHeaders, fetchWithAuth } from './fetchWithAuth'
import { PageResult } from '@/types/page'

export interface CompanyPoint {
  companyId: number
  companyName: string
  companyType: 'VENDOR' | 'ORDERING' | 'BOTH'
  balance: number
  depositBalance: number
}

export interface PointTransaction {
  id: number
  type: 'CHARGE' | 'USE' | 'REFUND' | 'SETTLE'
  amount: number
  balanceAfter: number
  memo: string | null
  memberLoginId: string | null
  orderId: number | null
  createdAt: string
  taxInvoiceStatus?: 'ISSUING' | 'SUCCESS' | 'FAILED' | null
  taxInvoiceFailReason?: string | null
}

async function errorMessage(res: Response, fallback: string): Promise<string> {
  try {
    const body = await res.json()
    return body.message || fallback
  } catch {
    return fallback
  }
}

export async function fetchAdminPointList(): Promise<CompanyPoint[]> {
  const res = await fetchWithAuth('/api/admin/points', { headers: authHeaders() })
  if (!res.ok) throw new Error('포인트 목록 조회에 실패했습니다.')
  return res.json()
}

export async function chargePoint(companyId: number, amount: number, memo: string): Promise<void> {
  const res = await fetchWithAuth(`/api/admin/points/${companyId}/charge`, {
    method: 'POST',
    headers: { ...authHeaders(), 'Content-Type': 'application/json' },
    body: JSON.stringify({ amount, memo: memo.trim() || null }),
  })
  if (!res.ok) throw new Error(await errorMessage(res, '포인트 충전에 실패했습니다.'))
}

export async function fetchAdminPointTransactions(
  companyId: number,
  page = 0,
  size = 10
): Promise<PageResult<PointTransaction>> {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  const res = await fetchWithAuth(`/api/admin/points/${companyId}/transactions?${params}`, { headers: authHeaders() })
  if (!res.ok) throw new Error('포인트 이력 조회에 실패했습니다.')
  return res.json()
}

export async function fetchMyPoint(companyId?: number | null): Promise<CompanyPoint> {
  const qs = companyId ? `?companyId=${companyId}` : ''
  const res = await fetchWithAuth(`/api/points/my${qs}`, { headers: authHeaders() })
  if (!res.ok) throw new Error('포인트 조회에 실패했습니다.')
  return res.json()
}

export async function fetchMyPointTransactions(page = 0, size = 20, companyId?: number | null): Promise<PageResult<PointTransaction>> {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  if (companyId) params.set('companyId', String(companyId))
  const res = await fetchWithAuth(`/api/points/my/transactions?${params}`, { headers: authHeaders() })
  if (!res.ok) throw new Error('포인트 이력 조회에 실패했습니다.')
  return res.json()
}

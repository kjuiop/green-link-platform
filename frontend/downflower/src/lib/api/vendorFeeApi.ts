import { authHeaders, fetchWithAuth } from './fetchWithAuth'
import { PageResult } from '@/types/page'

export interface VendorFee {
  companyId: number
  companyName: string
  type: 'VENDOR' | 'BOTH'
  businessNo: string | null
  vendorFee: number
}

async function errorMessage(res: Response, fallback: string): Promise<string> {
  try {
    const body = await res.json()
    return body.message || fallback
  } catch {
    return fallback
  }
}

export async function fetchVendorFees(keyword: string, page = 0, size = 20): Promise<PageResult<VendorFee>> {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  if (keyword.trim()) params.set('keyword', keyword.trim())
  const res = await fetchWithAuth(`/api/admin/vendor-fees?${params}`, { headers: authHeaders() })
  if (!res.ok) throw new Error(await errorMessage(res, '수수료 목록 조회에 실패했습니다.'))
  return res.json()
}

export async function updateVendorFee(companyId: number, vendorFee: number): Promise<VendorFee> {
  const res = await fetchWithAuth(`/api/admin/vendor-fees/${companyId}`, {
    method: 'PUT',
    headers: { ...authHeaders(), 'Content-Type': 'application/json' },
    body: JSON.stringify({ vendorFee }),
  })
  if (!res.ok) throw new Error(await errorMessage(res, '수수료 수정에 실패했습니다.'))
  return res.json()
}

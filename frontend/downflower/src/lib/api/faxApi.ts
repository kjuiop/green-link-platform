import { authBearerHeader, authHeaders, fetchWithAuth } from './fetchWithAuth'
import { PageResult } from '@/types/page'

export interface FaxVendor {
  companyId: number
  companyName: string
  businessNo: string | null
  faxNumber: string | null
  faxEnabled: boolean
}

export type FaxStatus = 'SENDING' | 'SUBMITTED' | 'DELIVERED' | 'FAILED'

export interface FaxMessage {
  id: number
  orderId: number
  orderNumber: string | null
  sequence: number
  vendorCompanyId: number
  vendorCompanyName: string | null
  toNumber: string
  status: FaxStatus
  failReason: string | null
  manual: boolean
  requestedAt: string | null
  completedAt: string | null
}

export interface FaxSettings {
  faxSenderNumber: string | null
  faxUrl: string | null
  ftpHost: string | null
  ftpPort: number | null
  ftpUsername: string | null
  ftpPassword: string | null
  ftpDirectory: string | null
}

async function errorMessage(res: Response, fallback: string): Promise<string> {
  try {
    const body = await res.json()
    return body.message || fallback
  } catch {
    return fallback
  }
}

export async function fetchFaxSettings(): Promise<FaxSettings> {
  const res = await fetchWithAuth('/api/settings/sms/fax', { headers: authBearerHeader() })
  if (!res.ok) throw new Error(await errorMessage(res, '팩스 설정을 불러오지 못했습니다.'))
  return res.json()
}

export async function saveFaxSettings(data: FaxSettings): Promise<FaxSettings> {
  const res = await fetchWithAuth('/api/settings/sms/fax', {
    method: 'PUT',
    headers: { ...authBearerHeader(), 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
  if (!res.ok) throw new Error(await errorMessage(res, '팩스 설정 저장에 실패했습니다.'))
  return res.json()
}

export async function fetchFaxVendors(keyword: string, page = 0, size = 20): Promise<PageResult<FaxVendor>> {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  if (keyword.trim()) params.set('keyword', keyword.trim())
  const res = await fetchWithAuth(`/api/admin/fax/vendors?${params}`, { headers: authHeaders() })
  if (!res.ok) throw new Error(await errorMessage(res, '수주사 팩스 설정을 불러오지 못했습니다.'))
  return res.json()
}

export async function updateFaxVendor(companyId: number, faxEnabled: boolean, faxNumber: string): Promise<FaxVendor> {
  const res = await fetchWithAuth(`/api/admin/fax/vendors/${companyId}`, {
    method: 'PUT',
    headers: { ...authHeaders(), 'Content-Type': 'application/json' },
    body: JSON.stringify({ faxEnabled, faxNumber }),
  })
  if (!res.ok) throw new Error(await errorMessage(res, '팩스 설정 저장에 실패했습니다.'))
  return res.json()
}

export async function fetchFaxMessages(
  status: FaxStatus | '',
  keyword: string,
  page = 0,
  size = 20,
): Promise<PageResult<FaxMessage>> {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  if (status) params.set('status', status)
  if (keyword.trim()) params.set('keyword', keyword.trim())
  const res = await fetchWithAuth(`/api/admin/fax/messages?${params}`, { headers: authHeaders() })
  if (!res.ok) throw new Error(await errorMessage(res, '팩스 발송 내역을 불러오지 못했습니다.'))
  return res.json()
}

export async function resendFax(orderId: number): Promise<FaxMessage> {
  const res = await fetchWithAuth(`/api/admin/fax/orders/${orderId}/resend`, {
    method: 'POST',
    headers: authHeaders(),
  })
  if (!res.ok) throw new Error(await errorMessage(res, '팩스 재발송에 실패했습니다.'))
  return res.json()
}

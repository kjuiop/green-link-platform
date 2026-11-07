import { PageResult } from '@/types/page'
import { authHeaders, fetchWithAuth } from './fetchWithAuth'

export type CompanyType = 'ORDERING' | 'VENDOR' | 'BOTH'
export type CompanyStatus = 'ACTIVE' | 'INACTIVE'

export type CompanyInfo = {
  id: number
  businessNo?: string
  companyName: string
  ceoName?: string
  phone?: string
  address?: string
  detailAddress?: string
  type: CompanyType
  status: CompanyStatus
}

export async function fetchCompaniesPaged(page = 0, size = 20): Promise<PageResult<CompanyInfo>> {
  const query = new URLSearchParams({ page: String(page), size: String(size) })
  const res = await fetchWithAuth(`/api/companies/paged?${query}`, { headers: authHeaders() })
  if (!res.ok) throw new Error('업체 목록을 불러오지 못했습니다.')
  return res.json()
}

export type SmsProvider = 'ALIGO' | 'MESSAGEME'

export type CompanySmsIntegration = {
  id: number
  companyId: number
  provider: SmsProvider
  apiKey: string | null
  senderNumber: string | null
}

export async function fetchCompanySmsIntegration(companyId: number): Promise<CompanySmsIntegration | null> {
  const res = await fetchWithAuth(`/api/companies/${companyId}/integrations/sms`, { headers: authHeaders() })
  if (res.status === 204) return null
  if (!res.ok) throw new Error('SMS 연동 정보를 불러오지 못했습니다.')
  return res.json()
}

export async function saveCompanySmsIntegration(
  companyId: number,
  data: { provider: SmsProvider; apiKey: string; senderNumber: string },
): Promise<CompanySmsIntegration> {
  const res = await fetchWithAuth(`/api/companies/${companyId}/integrations/sms`, {
    method: 'PUT',
    headers: authHeaders(),
    body: JSON.stringify(data),
  })
  if (!res.ok) throw new Error('SMS 연동 정보 저장에 실패했습니다.')
  return res.json()
}

export async function fetchCompanies(): Promise<CompanyInfo[]> {
  const res = await fetchWithAuth('/api/companies/paged?page=0&size=100', { headers: authHeaders() })
  if (!res.ok) throw new Error('업체 목록을 불러오지 못했습니다.')
  const data = await res.json()
  return data.content ?? []
}

export async function updateCompanyType(id: number, type: CompanyType): Promise<void> {
  const res = await fetchWithAuth(`/api/companies/${id}/type`, {
    method: 'PUT',
    headers: authHeaders(),
    body: JSON.stringify({ type }),
  })
  if (!res.ok) {
    const data = await res.json().catch(() => ({}))
    throw new Error(data.message ?? '업체유형 변경에 실패했습니다.')
  }
}

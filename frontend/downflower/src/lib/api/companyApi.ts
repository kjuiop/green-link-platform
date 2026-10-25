import { PageResult } from '@/types/page'

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

function authHeaders(): HeadersInit {
  const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  }
}

export async function fetchCompaniesPaged(page = 0, size = 20): Promise<PageResult<CompanyInfo>> {
  const query = new URLSearchParams({ page: String(page), size: String(size) })
  const res = await fetch(`/api/companies/paged?${query}`, { headers: authHeaders() })
  if (!res.ok) throw new Error('업체 목록을 불러오지 못했습니다.')
  return res.json()
}

export async function updateCompanyType(id: number, type: CompanyType): Promise<void> {
  const res = await fetch(`/api/companies/${id}/type`, {
    method: 'PUT',
    headers: authHeaders(),
    body: JSON.stringify({ type }),
  })
  if (!res.ok) {
    const data = await res.json().catch(() => ({}))
    throw new Error(data.message ?? '업체유형 변경에 실패했습니다.')
  }
}

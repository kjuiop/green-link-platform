import { authHeaders, fetchWithAuth } from './fetchWithAuth'
import { PageResult } from '@/types/page'

export type OnurimCompanyType = 'ORDERING' | 'VENDOR' | 'BOTH'
export type OnurimApplicationStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

export interface OnurimApplication {
  id: number
  companyId: number
  companyName: string | null
  applicantAdminId: number
  applicantName: string | null
  desiredType: OnurimCompanyType
  status: OnurimApplicationStatus
  companyPhone: string | null
  address: string | null
  detailAddress: string | null
  taxInvoiceEmail: string | null
  memo: string | null
  faxNumber: string | null
  bankName: string | null
  accountNumber: string | null
  accountHolder: string | null
  regions: { sido: string; sigungu: string | null }[]
  businessRegUrl: string | null
  bankBookUrl: string | null
  approvedType: OnurimCompanyType | null
  rejectReason: string | null
  processedAt: string | null
  createdAt: string
}

async function errorMessage(res: Response, fallback: string): Promise<string> {
  try {
    const body = await res.json()
    return body.message || fallback
  } catch {
    return fallback
  }
}

export async function fetchOnurimApplications(
  status: OnurimApplicationStatus, page = 0, size = 20,
): Promise<PageResult<OnurimApplication>> {
  const params = new URLSearchParams({ status, page: String(page), size: String(size) })
  const res = await fetchWithAuth(`/api/admin/onurim-applications?${params}`, { headers: authHeaders() })
  if (!res.ok) throw new Error(await errorMessage(res, '이용 신청 목록 조회에 실패했습니다.'))
  return res.json()
}

export async function approveOnurimApplication(id: number, type: OnurimCompanyType): Promise<OnurimApplication> {
  const res = await fetchWithAuth(`/api/admin/onurim-applications/${id}/approve`, {
    method: 'POST',
    headers: { ...authHeaders(), 'Content-Type': 'application/json' },
    body: JSON.stringify({ type }),
  })
  if (!res.ok) throw new Error(await errorMessage(res, '승인에 실패했습니다.'))
  return res.json()
}

export async function rejectOnurimApplication(id: number, reason: string): Promise<OnurimApplication> {
  const res = await fetchWithAuth(`/api/admin/onurim-applications/${id}/reject`, {
    method: 'POST',
    headers: { ...authHeaders(), 'Content-Type': 'application/json' },
    body: JSON.stringify({ reason }),
  })
  if (!res.ok) throw new Error(await errorMessage(res, '거절에 실패했습니다.'))
  return res.json()
}

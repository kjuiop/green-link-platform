import { fetchWithAuth, authBearerHeader } from './fetchWithAuth'
import type { MemberSummary } from '@/types/member'

export async function fetchMemberSummary(): Promise<MemberSummary | null> {
  const res = await fetchWithAuth('/api/member/summary', { headers: authBearerHeader() })
  if (res.status === 204) return null
  if (!res.ok) throw new Error('회원 요약 조회 실패')
  return res.json()
}

export interface CompanyAccount {
  bankName: string | null
  accountNumber: string | null
  accountHolder: string | null
}

export async function fetchMyAccount(): Promise<CompanyAccount> {
  const res = await fetchWithAuth('/api/member/account', { headers: authBearerHeader() })
  if (!res.ok) throw new Error('계좌 조회에 실패했습니다.')
  return res.json()
}

export async function updateMyBusinessStatus(status: 'OPEN' | 'CLOSED'): Promise<void> {
  const res = await fetchWithAuth('/api/member/business-status', {
    method: 'PATCH',
    headers: authBearerHeader(),
    body: JSON.stringify({ status }),
  })
  if (!res.ok) throw new Error('영업상태 변경에 실패했습니다.')
}

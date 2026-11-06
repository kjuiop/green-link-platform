import { fetchWithAuth, authBearerHeader } from './fetchWithAuth'
import type { MemberSummary } from '@/types/member'

export async function fetchMemberSummary(): Promise<MemberSummary | null> {
  const res = await fetchWithAuth('/api/member/summary', { headers: authBearerHeader() })
  if (res.status === 204) return null
  if (!res.ok) throw new Error('회원 요약 조회 실패')
  return res.json()
}

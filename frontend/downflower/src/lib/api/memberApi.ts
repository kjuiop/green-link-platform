import { fetchWithAuth } from './fetchWithAuth'
import type { MemberSummary } from '@/types/member'

export async function fetchMemberSummary(): Promise<MemberSummary> {
  const res = await fetchWithAuth('/api/member/summary')
  if (!res.ok) throw new Error('회원 요약 조회 실패')
  return res.json()
}

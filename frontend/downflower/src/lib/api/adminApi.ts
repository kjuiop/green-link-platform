import { authHeaders, fetchWithAuth } from './fetchWithAuth'

export type AdminProfile = {
  id: number
  loginId: string
  name: string | null
  phone: string | null
}

export async function fetchAdminProfile(): Promise<AdminProfile> {
  const res = await fetchWithAuth('/api/admin/me', { headers: authHeaders() })
  if (!res.ok) throw new Error('계정 정보를 불러오지 못했습니다.')
  return res.json()
}

export async function updateAdminProfile(name: string, phone: string): Promise<void> {
  const res = await fetchWithAuth('/api/admin/me', {
    method: 'PUT',
    headers: authHeaders(),
    body: JSON.stringify({ name, phone }),
  })
  if (!res.ok) {
    const data = await res.json().catch(() => ({}))
    throw new Error(data.message ?? '저장에 실패했습니다.')
  }
}

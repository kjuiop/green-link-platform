import type { MyAccess } from '@/types/shopAdmin'
import { apiFetch } from './client'

export function fetchMyAccess(): Promise<MyAccess> {
  return apiFetch<MyAccess>('/api/me')
}

export async function submitAccessRequest(): Promise<void> {
  await apiFetch<MyAccess>('/api/access-requests', { method: 'POST' })
}

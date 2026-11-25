import type { ShopRole } from '@/types/auth'
import type { MyAccess } from '@/types/shopAdmin'
import { apiFetch } from './client'

export function fetchMyAccess(): Promise<MyAccess> {
  return apiFetch<MyAccess>('/api/me')
}

export async function submitAccessRequest(requestedRole?: ShopRole): Promise<void> {
  await apiFetch<MyAccess>('/api/access-requests', {
    method: 'POST',
    body: requestedRole ? { requestedRole } : undefined,
  })
}

import type { ShopRole } from '@/types/auth'
import type { ApplicableBrand, MyAccess } from '@/types/shopAdmin'
import { apiFetch } from './client'

export function fetchMyAccess(): Promise<MyAccess> {
  return apiFetch<MyAccess>('/api/me')
}

export function fetchApplicableBrands(): Promise<ApplicableBrand[]> {
  return apiFetch<ApplicableBrand[]>('/api/access-requests/brands')
}

export async function submitAccessRequest(requestedRole?: ShopRole, requestedBrandIds?: number[]): Promise<void> {
  await apiFetch<MyAccess>('/api/access-requests', {
    method: 'POST',
    body: requestedRole ? { requestedRole, requestedBrandIds } : undefined,
  })
}

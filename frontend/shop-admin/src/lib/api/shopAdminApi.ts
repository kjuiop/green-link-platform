import type { ShopRole } from '@/types/auth'
import type { ShopAdmin, ShopAdminStatus } from '@/types/shopAdmin'
import { apiFetch } from './client'

export function fetchShopAdmins(status?: ShopAdminStatus): Promise<ShopAdmin[]> {
  const query = status ? `?status=${status}` : ''
  return apiFetch<ShopAdmin[]>(`/api/members${query}`)
}

export function approveShopAdmin(id: number, role?: ShopRole, brandIds?: number[]): Promise<void> {
  return apiFetch<void>(`/api/members/${id}/approve`, {
    method: 'POST',
    body: role || brandIds ? { role, brandIds } : undefined,
  })
}

export function rejectShopAdmin(id: number, reason: string): Promise<void> {
  return apiFetch<void>(`/api/members/${id}/reject`, { method: 'POST', body: { reason } })
}

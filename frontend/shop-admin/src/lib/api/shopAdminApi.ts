import type { ShopRole } from '@/types/auth'
import type { ShopAdmin, ShopAdminStatus } from '@/types/shopAdmin'
import { apiFetch } from './client'

export function fetchShopAdmins(status?: ShopAdminStatus): Promise<ShopAdmin[]> {
  const query = status ? `?status=${status}` : ''
  return apiFetch<ShopAdmin[]>(`/api/admin/shop-admins${query}`)
}

export function approveShopAdmin(id: number, role?: ShopRole): Promise<void> {
  return apiFetch<void>(`/api/admin/shop-admins/${id}/approve`, {
    method: 'POST',
    body: role ? { role } : undefined,
  })
}

export function rejectShopAdmin(id: number, reason: string): Promise<void> {
  return apiFetch<void>(`/api/admin/shop-admins/${id}/reject`, { method: 'POST', body: { reason } })
}

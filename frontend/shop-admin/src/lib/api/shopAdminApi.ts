import type { ShopRole } from '@/types/auth'
import type { CompanyOption } from '@/types/brand'
import type { ShopAdmin, ShopAdminStatus } from '@/types/shopAdmin'
import { apiFetch } from './client'

export function fetchShopAdmins(status?: ShopAdminStatus, companyId?: number): Promise<ShopAdmin[]> {
  const params = new URLSearchParams()
  if (status) params.set('status', status)
  if (companyId !== undefined) params.set('companyId', String(companyId))
  const query = params.size > 0 ? `?${params}` : ''
  return apiFetch<ShopAdmin[]>(`/api/members${query}`)
}

export async function fetchCompanies(): Promise<CompanyOption[]> {
  const admins = await fetchShopAdmins()
  const byId = new Map<number, CompanyOption>()
  for (const admin of admins) {
    if (!byId.has(admin.companyId)) {
      byId.set(admin.companyId, { id: admin.companyId, name: admin.companyName ?? `#${admin.companyId}` })
    }
  }
  return [...byId.values()].sort((a, b) => a.name.localeCompare(b.name, 'ko'))
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

export function changeShopAdminRole(id: number, role: ShopRole): Promise<void> {
  return apiFetch<void>(`/api/members/${id}/role`, { method: 'PATCH', body: { role } })
}

export function assignShopAdminBrands(id: number, brandIds: number[]): Promise<void> {
  return apiFetch<void>(`/api/members/${id}/brands`, { method: 'PUT', body: { brandIds } })
}

export function removeShopAdmin(id: number): Promise<void> {
  return apiFetch<void>(`/api/members/${id}`, { method: 'DELETE' })
}

export interface CreateShopAdminInput {
  loginId: string
  name: string
  phone: string
  email?: string
  role: Extract<ShopRole, 'PARTNER_ADMIN' | 'BRAND_ADMIN'>
  companyId?: number
  brandIds?: number[]
}

export function createShopAdmin(input: CreateShopAdminInput): Promise<{ id: number }> {
  return apiFetch<{ id: number }>('/api/members', { method: 'POST', body: input })
}

export function changeOnurimAccess(id: number, enabled: boolean): Promise<void> {
  return apiFetch<void>(`/api/members/${id}/onurim-access`, { method: 'PATCH', body: { enabled } })
}

export function reissuePasswordSetup(id: number): Promise<void> {
  return apiFetch<void>(`/api/members/${id}/password-setup/reissue`, { method: 'POST' })
}

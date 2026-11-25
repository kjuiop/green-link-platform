import type { Brand } from '@/types/brand'
import { apiFetch } from './client'

export function fetchBrands(companyId?: number): Promise<Brand[]> {
  const query = companyId !== undefined ? `?companyId=${companyId}` : ''
  return apiFetch<Brand[]>(`/api/brands${query}`)
}

export function createBrand(name: string, companyId?: number): Promise<Brand> {
  return apiFetch<Brand>('/api/brands', { method: 'POST', body: { name, companyId } })
}

export function renameBrand(id: number, name: string): Promise<Brand> {
  return apiFetch<Brand>(`/api/brands/${id}`, { method: 'PATCH', body: { name } })
}

export function deleteBrand(id: number): Promise<void> {
  return apiFetch<void>(`/api/brands/${id}`, { method: 'DELETE' })
}

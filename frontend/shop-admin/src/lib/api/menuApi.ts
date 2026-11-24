import type { MenuItem } from '@/types/menu'
import { apiFetch } from './client'

export function fetchMenus(): Promise<MenuItem[]> {
  return apiFetch<MenuItem[]>('/api/menus')
}

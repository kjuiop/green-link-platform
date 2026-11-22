import type { ShopRole } from '@/types/auth'
import type { MenuItem } from '@/types/menu'
import { mockMenusByRole } from '@/lib/mock/menu'

export async function fetchMenusByRole(role: ShopRole): Promise<MenuItem[]> {
  return mockMenusByRole(role)
}

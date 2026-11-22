import type { ShopAdmin, ShopAdminStatus } from '@/types/shopAdmin'
import { mockApprove, mockListAdmins, mockReject } from '@/lib/mock/shopAdmins'

export function fetchShopAdmins(status?: ShopAdminStatus): Promise<ShopAdmin[]> {
  return mockListAdmins(status)
}

export function approveShopAdmin(id: number): Promise<void> {
  return mockApprove(id)
}

export function rejectShopAdmin(id: number, reason: string): Promise<void> {
  return mockReject(id, reason)
}

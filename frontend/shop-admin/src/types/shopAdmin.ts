import type { AccessStatus, ShopRole } from '@/types/auth'

export type ShopAdminStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

export interface ShopAdmin {
  id: number
  loginId: string
  name: string
  companyName: string | null
  role: ShopRole
  status: ShopAdminStatus
  requestedAt: string
  processedAt: string | null
  rejectReason: string | null
}

export interface MyAccess {
  accessStatus: AccessStatus
  rejectReason: string | null
}

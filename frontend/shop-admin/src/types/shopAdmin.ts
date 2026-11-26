import type { AccessRequestType, AccessStatus, ShopRole } from '@/types/auth'

export type ShopAdminStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'REMOVED'

export interface ShopAdmin {
  id: number
  adminId: number
  companyId: number
  loginId: string | null
  name: string | null
  companyName: string | null
  role: ShopRole | null
  requestedRole: ShopRole
  status: ShopAdminStatus
  requestedAt: string
  processedAt: string | null
  rejectReason: string | null
  brandIds: number[]
  passwordSetupPending: boolean
}

export interface MyAccess {
  accessStatus: AccessStatus
  rejectReason: string | null
  requestType: AccessRequestType | null
  requestedRole: ShopRole | null
}

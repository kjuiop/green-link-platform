export type VendorStatus = 'ACTIVE' | 'INACTIVE' | 'PENDING'

export interface Vendor {
  id: string
  name: string
  businessNumber?: string
  managerName: string
  phone: string
  address: string
  loginId: string
  status: VendorStatus
  createdAt?: string
}

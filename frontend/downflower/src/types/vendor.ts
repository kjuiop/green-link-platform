export type VendorStatus = 'active' | 'suspended'

export interface Vendor {
  id: string
  name: string
  businessNumber?: string
  managerName: string
  phone: string
  email?: string
  address: string
  loginId: string
  status: VendorStatus
  memo?: string
  businessDocUrl?: string
  bankDocUrl?: string
  createdAt: string
}

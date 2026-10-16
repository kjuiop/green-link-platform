export type VendorStatus = 'active' | 'suspended'

export interface Vendor {
  id: string
  name: string
  managerName: string
  phone: string
  address: string
  loginId: string
  status: VendorStatus
  memo?: string
  createdAt: string
}

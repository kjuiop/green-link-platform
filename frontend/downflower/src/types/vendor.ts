export type VendorStatus = 'ACTIVE' | 'INACTIVE' | 'PENDING'
export type CompanyType = 'ORDERING' | 'VENDOR' | 'BOTH'

export interface Vendor {
  id: string
  name: string
  businessNumber?: string
  faxNumber?: string
  managerName: string
  phone: string
  address: string
  loginId: string
  status: VendorStatus
  createdAt?: string
  companyType?: CompanyType
}

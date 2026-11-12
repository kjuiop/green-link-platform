export type VendorStatus = 'ACTIVE' | 'INACTIVE' | 'PENDING'
export type CompanyType = 'ORDERING' | 'VENDOR' | 'BOTH'

export interface RegionDto {
  sido: string
  sigungu: string
}

export interface Vendor {
  id: string
  name: string
  businessNumber?: string
  faxNumber?: string
  companyPhone?: string
  managerName: string
  phone: string
  email?: string
  address: string
  detailAddress?: string
  loginId: string
  status: VendorStatus
  createdAt?: string
  companyType?: CompanyType
  regions?: RegionDto[]
  capabilities?: string[]
  businessRegUrl?: string
  bankBookUrl?: string
}

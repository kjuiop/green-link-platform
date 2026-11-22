export type CompanyType = 'ORDERING' | 'VENDOR' | 'BOTH'

export interface MemberSummary {
  companyType: CompanyType
  orderingAmount: number
  vendorAmount: number
  pointBalance: number
}

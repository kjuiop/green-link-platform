export type CompanyType = 'ORDERING' | 'VENDOR' | 'BOTH'

export interface MemberSummary {
  companyType: CompanyType
  orderingAmount: number
  vendorAmount: number
  depositBalance: number | null
}

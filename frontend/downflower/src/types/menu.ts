export type AdminRole = 'SUPER_ADMIN' | 'PARTNER_ADMIN' | 'BRAND_ADMIN'
export type CompanyType = 'ORDERING' | 'VENDOR' | 'BOTH'

export const ROLE_LABELS: Record<AdminRole, string> = {
  SUPER_ADMIN: '슈퍼관리자',
  PARTNER_ADMIN: '파트너관리자',
  BRAND_ADMIN: '브랜드관리자',
}

export const ALL_ROLES: AdminRole[] = ['SUPER_ADMIN', 'PARTNER_ADMIN', 'BRAND_ADMIN']

export const COMPANY_TYPE_LABELS: Record<CompanyType, string> = {
  ORDERING: '발주사',
  VENDOR: '수주사',
  BOTH: '수발주사',
}

export const MENU_COMPANY_TYPES: CompanyType[] = ['ORDERING', 'VENDOR']

export type MenuItem = {
  id: number
  name: string
  url: string
  sortOrder: number
  roles: AdminRole[]
  companyTypes?: CompanyType[]
  children?: MenuItem[]
}

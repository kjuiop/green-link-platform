export type ShopRole = 'SUPER_ADMIN' | 'PARTNER_ADMIN' | 'BRAND_ADMIN'
export type AccessRequestType = 'COMPANY' | 'JOIN'
export type AccessStatus = 'APPROVED' | 'PENDING' | 'REJECTED' | 'NONE' | 'SITE_DISABLED'

export interface Session {
  accessToken: string
  loginId: string
  name: string
  companyId: number | null
  companyName: string | null
  role: ShopRole | null
  accessStatus: AccessStatus
}

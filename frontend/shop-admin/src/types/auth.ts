export type ShopRole = 'SUPER_ADMIN' | 'SHOP_ADMIN'
export type AccessStatus = 'APPROVED' | 'PENDING' | 'REJECTED' | 'NONE'

export interface Session {
  accessToken: string
  loginId: string
  name: string
  companyName: string | null
  role: ShopRole
  accessStatus: AccessStatus
}

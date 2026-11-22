import type { Session } from '@/types/auth'

export type MockAccount = Pick<Session, 'name' | 'companyName' | 'role'>

export const ACCOUNTS: Record<string, MockAccount> = {
  admin: { name: '최고관리자', companyName: null, role: 'SUPER_ADMIN' },
  shop: { name: '김쇼핑', companyName: '꽃피는날', role: 'SHOP_ADMIN' },
  pending: { name: '이대기', companyName: '푸른화원', role: 'SHOP_ADMIN' },
  new: { name: '박신규', companyName: '새싹플라워', role: 'SHOP_ADMIN' },
  rejected: { name: '정거절', companyName: '하늘꽃집', role: 'SHOP_ADMIN' },
}

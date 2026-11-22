import type { Session } from '@/types/auth'
import { delay } from './delay'

const ACCOUNTS: Record<string, Omit<Session, 'accessToken' | 'loginId'>> = {
  admin: { name: '최고관리자', companyName: null, role: 'SUPER_ADMIN', accessStatus: 'APPROVED' },
  shop: { name: '김쇼핑', companyName: '꽃피는날', role: 'SHOP_ADMIN', accessStatus: 'APPROVED' },
  pending: { name: '이대기', companyName: '푸른화원', role: 'SHOP_ADMIN', accessStatus: 'PENDING' },
  new: { name: '박신규', companyName: '새싹플라워', role: 'SHOP_ADMIN', accessStatus: 'NONE' },
}

export async function mockLogin(loginId: string): Promise<Session> {
  await delay()
  const account = ACCOUNTS[loginId]
  if (!account) throw new Error('아이디 또는 비밀번호가 올바르지 않습니다.')
  return { accessToken: `mock-${loginId}`, loginId, ...account }
}

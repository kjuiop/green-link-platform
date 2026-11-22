import type { Session } from '@/types/auth'
import { ACCOUNTS } from './accounts'
import { delay } from './delay'
import { resolveAccessStatus } from './shopAdmins'

export async function mockLogin(loginId: string): Promise<Session> {
  await delay()
  const account = ACCOUNTS[loginId]
  if (!account) throw new Error('아이디 또는 비밀번호가 올바르지 않습니다.')
  return {
    accessToken: `mock-${loginId}`,
    loginId,
    ...account,
    accessStatus: resolveAccessStatus(loginId),
  }
}

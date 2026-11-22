import type { Session } from '@/types/auth'
import { mockLogin } from '@/lib/mock/auth'

export async function login(loginId: string, password: string): Promise<Session> {
  void password
  return mockLogin(loginId)
}

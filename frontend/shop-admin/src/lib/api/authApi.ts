import type { Session } from '@/types/auth'
import { apiFetch } from './client'

export function login(loginId: string, password: string): Promise<Session> {
  return apiFetch<Session>('/api/auth/login', { method: 'POST', body: { loginId, password }, auth: false })
}

export function refreshSession(): Promise<Session> {
  return apiFetch<Session>('/api/auth/refresh', { method: 'POST' })
}

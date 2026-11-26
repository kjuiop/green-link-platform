import type { Session } from '@/types/auth'
import { apiFetch } from './client'

export function login(loginId: string, password: string): Promise<Session> {
  return apiFetch<Session>('/api/auth/login', { method: 'POST', body: { loginId, password }, auth: false })
}

export function refreshSession(): Promise<Session> {
  return apiFetch<Session>('/api/auth/refresh', { method: 'POST' })
}

export interface SignupInput {
  loginId: string
  password: string
  name: string
  phone: string
  email?: string
  businessNo: string
  companyName?: string
  ceoName?: string
  requestedRole?: 'PARTNER_ADMIN' | 'BRAND_ADMIN'
}

export function signup(input: SignupInput): Promise<{ founder: boolean }> {
  return apiFetch<{ founder: boolean }>('/api/auth/signup', { method: 'POST', body: input, auth: false })
}

export async function businessNoExists(businessNo: string): Promise<boolean> {
  const res = await apiFetch<{ exists: boolean }>(
    `/api/auth/business-numbers/${encodeURIComponent(businessNo)}/exists`,
    { auth: false },
  )
  return res.exists
}

export function setupPassword(loginId: string, phone: string, newPassword: string): Promise<void> {
  return apiFetch<void>('/api/auth/password-setup', {
    method: 'POST',
    body: { loginId, phone, newPassword },
    auth: false,
  })
}

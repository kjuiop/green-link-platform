import type { AccessStatus, Session, ShopRole } from '@/types/auth'

const KEY = 'shopAdminSession'
const ROLES: ShopRole[] = ['SUPER_ADMIN', 'PARTNER_ADMIN', 'BRAND_ADMIN']
const ACCESS_STATUSES: AccessStatus[] = ['APPROVED', 'PENDING', 'REJECTED', 'NONE']
export const SESSION_EVENT = 'shop-admin-session'

export function readSessionRaw(): string | null {
  return localStorage.getItem(KEY)
}

export function parseSession(raw: string | null): Session | null {
  if (!raw) return null
  try {
    const parsed = JSON.parse(raw) as Partial<Session> | null
    if (!parsed || typeof parsed !== 'object') return null
    if (parsed.role === undefined || (parsed.role !== null && !ROLES.includes(parsed.role))) return null
    if (parsed.companyId === undefined) return null
    if (!parsed.accessStatus || !ACCESS_STATUSES.includes(parsed.accessStatus)) return null
    return parsed as Session
  } catch {
    return null
  }
}

export function getSession(): Session | null {
  if (typeof window === 'undefined') return null
  return parseSession(readSessionRaw())
}

export function setSession(session: Session) {
  localStorage.setItem(KEY, JSON.stringify(session))
  window.dispatchEvent(new Event(SESSION_EVENT))
}

export function clearSession() {
  localStorage.removeItem(KEY)
  window.dispatchEvent(new Event(SESSION_EVENT))
}

import type { Session } from '@/types/auth'

const KEY = 'shopAdminSession'
export const SESSION_EVENT = 'shop-admin-session'

export function readSessionRaw(): string | null {
  return localStorage.getItem(KEY)
}

export function parseSession(raw: string | null): Session | null {
  if (!raw) return null
  try {
    return JSON.parse(raw) as Session
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

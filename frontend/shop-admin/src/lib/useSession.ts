'use client'

import { useMemo, useSyncExternalStore } from 'react'
import { SESSION_EVENT, parseSession, readSessionRaw } from '@/lib/session'
import type { Session } from '@/types/auth'

function subscribe(callback: () => void) {
  window.addEventListener(SESSION_EVENT, callback)
  window.addEventListener('storage', callback)
  return () => {
    window.removeEventListener(SESSION_EVENT, callback)
    window.removeEventListener('storage', callback)
  }
}

// 서버/하이드레이션 중에는 undefined(확인 전), 클라이언트에서는 저장된 문자열 또는 null
const getServerSnapshot = (): string | null | undefined => undefined

export function useSession(): { session: Session | null; loading: boolean } {
  const raw = useSyncExternalStore<string | null | undefined>(subscribe, readSessionRaw, getServerSnapshot)
  const session = useMemo(() => (raw === undefined ? null : parseSession(raw)), [raw])
  return { session, loading: raw === undefined }
}

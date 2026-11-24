'use client'

import { useEffect } from 'react'
import { refreshSession } from '@/lib/api/authApi'
import { getSession, setSession } from '@/lib/session'

const REFRESH_INTERVAL_MS = 10 * 60 * 1000
const CHECK_INTERVAL_MS = 60 * 1000

let lastRefreshedAt = 0
let inFlight = false

export function markTokenFresh() {
  lastRefreshedAt = Date.now()
}

async function refreshIfStale() {
  if (inFlight || !getSession()) return
  if (Date.now() - lastRefreshedAt < REFRESH_INTERVAL_MS) return
  inFlight = true
  try {
    setSession(await refreshSession())
    lastRefreshedAt = Date.now()
  } catch {
    // 401은 apiFetch가 로그아웃 처리한다. 그 외(서버 불가 등)는 세션을 유지하고 다음 주기에 재시도한다.
  } finally {
    inFlight = false
  }
}

export function useTokenRefresh(enabled: boolean) {
  useEffect(() => {
    if (!enabled) return
    void refreshIfStale()
    const timer = setInterval(() => void refreshIfStale(), CHECK_INTERVAL_MS)
    const onVisible = () => {
      if (document.visibilityState === 'visible') void refreshIfStale()
    }
    document.addEventListener('visibilitychange', onVisible)
    return () => {
      clearInterval(timer)
      document.removeEventListener('visibilitychange', onVisible)
    }
  }, [enabled])
}

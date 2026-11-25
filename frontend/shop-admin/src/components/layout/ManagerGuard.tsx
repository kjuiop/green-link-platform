'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useSession } from '@/lib/useSession'
import type { Session } from '@/types/auth'

export function isManager(session: Session | null): boolean {
  return session?.role === 'SUPER_ADMIN' || session?.role === 'PARTNER_ADMIN'
}

export function ManagerGuard({ children }: { children: React.ReactNode }) {
  const router = useRouter()
  const { session, loading } = useSession()
  const allowed = isManager(session)

  useEffect(() => {
    if (loading) return
    if (!allowed) router.replace('/dashboard')
  }, [loading, allowed, router])

  if (loading || !allowed) return null
  return <>{children}</>
}

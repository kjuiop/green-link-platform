'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useSession } from '@/lib/useSession'

export function PartnerGuard({ children }: { children: React.ReactNode }) {
  const router = useRouter()
  const { session, loading } = useSession()
  const allowed = session?.role === 'PARTNER_ADMIN'

  useEffect(() => {
    if (loading) return
    if (!allowed) router.replace('/dashboard')
  }, [loading, allowed, router])

  if (loading || !allowed) return null
  return <>{children}</>
}

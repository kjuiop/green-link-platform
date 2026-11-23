'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { Sidebar } from '@/components/layout/Sidebar'
import { Header } from '@/components/layout/Header'
import { Skeleton } from '@/components/ui/skeleton'
import { useSession } from '@/lib/useSession'
import { useTokenRefresh } from '@/lib/useTokenRefresh'

function FullScreenSkeleton() {
  return (
    <div className="flex h-screen overflow-hidden">
      <div className="hidden w-56 shrink-0 space-y-3 border-r bg-white p-4 md:block">
        <Skeleton className="h-6 w-32" />
        <Skeleton className="h-20 w-full" />
        <Skeleton className="h-6 w-full" />
        <Skeleton className="h-6 w-full" />
      </div>
      <div className="flex-1">
        <div className="h-14 border-b bg-white" />
        <div className="space-y-4 p-6">
          <Skeleton className="h-8 w-40" />
          <Skeleton className="h-24 w-full" />
        </div>
      </div>
    </div>
  )
}

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const router = useRouter()
  const { session, loading } = useSession()
  const [drawerOpen, setDrawerOpen] = useState(false)
  useTokenRefresh(!!session && session.accessStatus === 'APPROVED')

  useEffect(() => {
    if (loading) return
    if (!session) router.replace('/login')
    else if (session.accessStatus !== 'APPROVED') router.replace('/access')
  }, [loading, session, router])

  if (loading || !session || session.accessStatus !== 'APPROVED') return <FullScreenSkeleton />

  return (
    <div className="flex h-screen overflow-hidden">
      <Sidebar session={session} mobileOpen={drawerOpen} onMobileClose={() => setDrawerOpen(false)} />
      <div className="flex flex-1 flex-col overflow-hidden">
        <Header session={session} onMenuOpen={() => setDrawerOpen(true)} />
        <main className="flex-1 overflow-y-auto">
          <div className="p-4 md:p-6">{children}</div>
        </main>
      </div>
    </div>
  )
}

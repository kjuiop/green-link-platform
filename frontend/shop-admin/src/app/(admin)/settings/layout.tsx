'use client'

import { useEffect } from 'react'
import Link from 'next/link'
import { usePathname, useRouter } from 'next/navigation'
import { cn } from '@/lib/utils'
import { useSession } from '@/lib/useSession'

const TABS = [{ href: '/settings/admins', label: '관리자 관리' }]

export default function SettingsLayout({ children }: { children: React.ReactNode }) {
  const router = useRouter()
  const pathname = usePathname()
  const { session, loading } = useSession()
  const allowed = session?.role === 'SUPER_ADMIN'

  useEffect(() => {
    if (loading) return
    if (!allowed) router.replace('/dashboard')
  }, [loading, allowed, router])

  if (loading || !allowed) return null

  return (
    <div className="space-y-4">
      <nav className="flex gap-1 border-b">
        {TABS.map((tab) => {
          const active = pathname === tab.href || pathname.startsWith(tab.href + '/')
          return (
            <Link
              key={tab.href}
              href={tab.href}
              className={cn(
                '-mb-px border-b-2 px-3 py-2 text-sm transition-colors',
                active
                  ? 'border-green-700 font-medium text-green-700'
                  : 'border-transparent text-gray-500 hover:text-gray-900',
              )}
            >
              {tab.label}
            </Link>
          )
        })}
      </nav>
      {children}
    </div>
  )
}

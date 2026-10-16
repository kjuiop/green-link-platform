'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'
import { useState } from 'react'
import {
  LayoutDashboard,
  Plus,
  List,
  ListChecks,
  MessageSquare,
  Users,
  Settings,
  ChevronDown,
} from 'lucide-react'
import { cn } from '@/lib/utils'

const navItems = [
  { label: '대시보드', href: '/dashboard', icon: LayoutDashboard },
  { label: '신규 발주', href: '/orders/new', icon: Plus },
  { label: '발주 목록', href: '/orders', icon: List },
  { label: '수주 목록', href: '/orders/received', icon: ListChecks },
]

const settingsItems = [
  { label: '문자 발송내역', href: '/messages', icon: MessageSquare },
  { label: '업체 계정관리', href: '/vendors', icon: Users },
]

export function AdminSidebar() {
  const pathname = usePathname()
  const isSettingsActive =
    pathname.startsWith('/messages') ||
    pathname.startsWith('/vendors')
  const [settingsOpen, setSettingsOpen] = useState(isSettingsActive)

  return (
    <aside className="flex h-screen w-56 flex-col border-r bg-white">
      <div className="flex h-14 items-center border-b px-4">
        <span className="text-sm font-bold text-green-700">🌿 다운플라워</span>
        <span className="ml-2 text-xs text-gray-400">관리자</span>
      </div>

      <nav className="flex-1 overflow-y-auto py-2">
        {navItems.map((item) => {
          const Icon = item.icon
          const isActive =
            item.href === '/orders'
              ? pathname === '/orders'
              : item.href === '/orders/new'
              ? pathname === '/orders/new'
              : pathname.startsWith(item.href)

          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                'flex items-center gap-3 px-4 py-2.5 text-sm transition-colors',
                isActive
                  ? 'bg-green-50 font-medium text-green-700'
                  : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900',
              )}
            >
              <Icon className="h-4 w-4 shrink-0" />
              {item.label}
            </Link>
          )
        })}

        <button
          onClick={() => setSettingsOpen(!settingsOpen)}
          className={cn(
            'flex w-full items-center gap-3 px-4 py-2.5 text-sm transition-colors',
            isSettingsActive
              ? 'bg-green-50 font-medium text-green-700'
              : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900',
          )}
        >
          <Settings className="h-4 w-4 shrink-0" />
          설정
          <ChevronDown
            className={cn(
              'ml-auto h-3.5 w-3.5 transition-transform duration-200',
              settingsOpen && 'rotate-180',
            )}
          />
        </button>

        {settingsOpen && (
          <div>
            {settingsItems.map((item) => {
              const Icon = item.icon
              const isActive = pathname.startsWith(item.href)
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={cn(
                    'flex items-center gap-3 py-2 pl-11 pr-4 text-sm transition-colors',
                    isActive
                      ? 'font-medium text-green-700'
                      : 'text-gray-500 hover:bg-gray-50 hover:text-gray-900',
                  )}
                >
                  <Icon className="h-3.5 w-3.5 shrink-0" />
                  {item.label}
                </Link>
              )
            })}
          </div>
        )}
      </nav>
    </aside>
  )
}

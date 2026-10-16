'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'
import { List, Calculator, User, LogOut } from 'lucide-react'
import { cn } from '@/lib/utils'

const navItems = [
  { label: '수주 리스트', href: '/vendor/orders', icon: List },
  { label: '정산내역', href: '/vendor/settlement', icon: Calculator },
  { label: '내 계정 정보', href: '/vendor/account', icon: User },
]

export function VendorSidebar() {
  const pathname = usePathname()

  return (
    <aside className="flex h-screen w-56 flex-col border-r bg-white">
      <div className="flex h-14 items-center border-b px-4">
        <span className="text-sm font-bold text-green-700">🌿 다운플라워</span>
        <span className="ml-2 text-xs text-gray-400">수주업체</span>
      </div>

      <nav className="flex-1 overflow-y-auto py-2">
        {navItems.map((item) => {
          const Icon = item.icon
          const isActive = pathname.startsWith(item.href)

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
      </nav>

      <div className="border-t p-4">
        <div className="mb-2 text-xs font-medium text-gray-700">서울꽃배달</div>
        <button className="flex w-full items-center gap-2 text-xs text-gray-400 hover:text-gray-600">
          <LogOut className="h-3.5 w-3.5" />
          로그아웃
        </button>
      </div>
    </aside>
  )
}

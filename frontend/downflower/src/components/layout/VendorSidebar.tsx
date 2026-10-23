'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'
import { LayoutDashboard, List } from 'lucide-react'
import { cn } from '@/lib/utils'
import { Sheet, SheetContent } from '@/components/ui/sheet'

const navItems = [
  { label: '대시보드', href: '/vendor/dashboard', icon: LayoutDashboard },
  { label: '수주 목록', href: '/vendor/orders', icon: List },
]

function SidebarNav({ onNavClick, companyName }: { onNavClick?: () => void; companyName?: string }) {
  const pathname = usePathname()

  return (
    <>
      <div className="flex h-14 items-center border-b px-4">
        <span className="text-sm font-bold text-green-700">🌿 다운플라워</span>
        {companyName && <span className="ml-2 truncate text-xs text-gray-500">{companyName}</span>}
      </div>

      <nav className="flex-1 overflow-y-auto py-2">
        {navItems.map((item) => {
          const Icon = item.icon
          const isActive = pathname.startsWith(item.href)

          return (
            <Link
              key={item.href}
              href={item.href}
              onClick={onNavClick}
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
    </>
  )
}

interface VendorSidebarProps {
  mobileOpen?: boolean
  onMobileClose?: () => void
  companyName?: string
}

export function VendorSidebar({ mobileOpen, onMobileClose, companyName }: VendorSidebarProps) {
  return (
    <>
      {/* 데스크톱 사이드바 */}
      <aside className="hidden h-screen w-56 flex-col border-r bg-white md:flex">
        <SidebarNav companyName={companyName} />
      </aside>

      {/* 모바일 드로어 */}
      <Sheet open={mobileOpen} onOpenChange={(open) => { if (!open) onMobileClose?.() }}>
        <SheetContent side="left" className="w-56 p-0" showCloseButton={false}>
          <SidebarNav onNavClick={onMobileClose} companyName={companyName} />
        </SheetContent>
      </Sheet>
    </>
  )
}

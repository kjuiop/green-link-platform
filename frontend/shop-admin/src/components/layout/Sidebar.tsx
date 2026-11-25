'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'
import { useEffect, useState } from 'react'
import {
  LayoutDashboard,
  Package,
  Settings,
  ShoppingCart,
  Store,
  Tag,
  UserCog,
  type LucideIcon,
} from 'lucide-react'
import { cn } from '@/lib/utils'
import { Sheet, SheetContent, SheetTitle } from '@/components/ui/sheet'
import { Skeleton } from '@/components/ui/skeleton'
import { fetchMenus } from '@/lib/api/menuApi'
import type { Session, ShopRole } from '@/types/auth'
import type { MenuItem } from '@/types/menu'

const ICON_MAP: Record<string, LucideIcon> = {
  dashboard: LayoutDashboard,
  orders: ShoppingCart,
  products: Package,
  shop: Store,
  settings: Settings,
  brands: Tag,
  admins: UserCog,
}

const ROLE_LABEL: Record<ShopRole, string> = {
  SUPER_ADMIN: '슈퍼관리자',
  PARTNER_ADMIN: '파트너 관리자',
  BRAND_ADMIN: '브랜드 관리자',
}

function isActive(pathname: string, url: string): boolean {
  return pathname === url || pathname.startsWith(url + '/')
}

interface SidebarNavProps {
  session: Session
  menus: MenuItem[] | null
  menuError?: boolean
  onNavClick?: () => void
}

function SidebarNav({ session, menus, menuError, onNavClick }: SidebarNavProps) {
  const pathname = usePathname()

  return (
    <>
      <div className="flex h-14 items-center border-b px-4">
        <span className="text-sm font-bold text-green-700">🛍️ 쇼핑몰 관리</span>
      </div>

      <div className="mx-3 my-2 rounded-lg bg-green-700 p-3 text-xs text-white">
        <div className="mb-1 flex items-center justify-between">
          <span className="font-semibold text-green-100">로그인 정보</span>
          <span className="rounded bg-green-600 px-1.5 py-0.5 text-green-100">
            {session.role ? ROLE_LABEL[session.role] : '승인 대기'}
          </span>
        </div>
        <div className="flex items-center gap-1 text-green-200">
          <span className="font-medium text-white">{session.name}</span>
          <span>·</span>
          <span>{session.loginId}</span>
        </div>
        <div className="mt-0.5 text-green-200">{session.companyName ?? '전체 쇼핑몰'}</div>
      </div>

      <nav className="flex-1 overflow-y-auto py-2">
        {menus === null ? (
          <div className="space-y-2 px-4 py-2">
            <Skeleton className="h-6 w-full" />
            <Skeleton className="h-6 w-full" />
            <Skeleton className="h-6 w-full" />
          </div>
        ) : menuError ? (
          <p className="px-4 py-2 text-xs text-red-500">메뉴를 불러오지 못했습니다.</p>
        ) : (
          menus.map((menu) => {
            const Icon = ICON_MAP[menu.icon] ?? Settings
            const active = isActive(pathname, menu.url)
            return (
              <Link
                key={menu.id}
                href={menu.url}
                onClick={onNavClick}
                className={cn(
                  'flex items-center gap-3 px-4 py-2.5 text-sm transition-colors',
                  active
                    ? 'bg-green-50 font-medium text-green-700'
                    : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900',
                )}
              >
                <Icon className="h-4 w-4 shrink-0" />
                {menu.name}
              </Link>
            )
          })
        )}
      </nav>
    </>
  )
}

interface SidebarProps {
  session: Session
  mobileOpen: boolean
  onMobileClose: () => void
}

export function Sidebar({ session, mobileOpen, onMobileClose }: SidebarProps) {
  const [menus, setMenus] = useState<MenuItem[] | null>(null)
  const [menuError, setMenuError] = useState(false)

  useEffect(() => {
    let cancelled = false
    fetchMenus()
      .then((result) => { if (!cancelled) setMenus(result) })
      .catch(() => { if (!cancelled) { setMenus([]); setMenuError(true) } })
    return () => { cancelled = true }
  }, [session.role])

  return (
    <>
      <aside className="hidden h-screen w-56 shrink-0 flex-col border-r bg-white md:flex">
        <SidebarNav session={session} menus={menus} menuError={menuError} />
      </aside>

      <Sheet open={mobileOpen} onOpenChange={(open) => { if (!open) onMobileClose() }}>
        <SheetContent side="left" className="w-56 p-0" showCloseButton={false}>
          <SheetTitle className="sr-only">메뉴</SheetTitle>
          <SidebarNav session={session} menus={menus} menuError={menuError} onNavClick={onMobileClose} />
        </SheetContent>
      </Sheet>
    </>
  )
}

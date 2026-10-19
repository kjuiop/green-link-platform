'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'
import { useState, useEffect } from 'react'
import {
  LayoutDashboard,
  Plus,
  List,
  ListChecks,
  MessageSquare,
  Users,
  Settings,
  ChevronDown,
  SlidersHorizontal,
  Calculator,
  Menu,
  type LucideIcon,
} from 'lucide-react'
import { cn } from '@/lib/utils'
import { Sheet, SheetContent } from '@/components/ui/sheet'
import { fetchMenusByRole } from '@/lib/api/menuApi'
import { type MenuItem, type UserRole } from '@/types/menu'

const ICON_MAP: Record<string, LucideIcon> = {
  '/dashboard': LayoutDashboard,
  '/orders/new': Plus,
  '/orders': List,
  '/orders/received': ListChecks,
  '/settlement': Calculator,
  '/settings': Settings,
  '/messages': MessageSquare,
  '/settings/messages': SlidersHorizontal,
  '/vendors': Users,
  '/settings/menus': Menu,
}

function isActive(pathname: string, url: string): boolean {
  if (url === '/orders') return pathname === '/orders' || pathname.startsWith('/orders/')
    && !pathname.startsWith('/orders/new') && !pathname.startsWith('/orders/received')
  return pathname === url || pathname.startsWith(url + '/')
}

interface SidebarNavProps {
  menus: MenuItem[]
  onNavClick?: () => void
  openId: number | null
  onToggle: (id: number) => void
}

function SidebarNav({ menus, onNavClick, openId, onToggle }: SidebarNavProps) {
  const pathname = usePathname()

  return (
    <>
      <div className="flex h-14 items-center border-b px-4">
        <span className="text-sm font-bold text-green-700">🌿 다운플라워</span>
        <span className="ml-2 text-xs text-gray-400">관리자</span>
      </div>

      <nav className="flex-1 overflow-y-auto py-2">
        {menus.map((menu) => {
          const Icon = ICON_MAP[menu.url] ?? Settings
          const hasChildren = (menu.children?.length ?? 0) > 0
          const expanded = openId === menu.id
          const active = isActive(pathname, menu.url)

          if (hasChildren) {
            return (
              <div key={menu.id}>
                <button
                  onClick={() => onToggle(menu.id)}
                  className={cn(
                    'flex w-full items-center gap-3 px-4 py-2.5 text-sm transition-colors',
                    active
                      ? 'bg-green-50 font-medium text-green-700'
                      : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900',
                  )}
                >
                  <Icon className="h-4 w-4 shrink-0" />
                  {menu.name}
                  <ChevronDown
                    className={cn(
                      'ml-auto h-3.5 w-3.5 transition-transform duration-200',
                      expanded && 'rotate-180',
                    )}
                  />
                </button>
                {expanded && (
                  <div>
                    {menu.children!.map((child) => {
                      const ChildIcon = ICON_MAP[child.url] ?? Settings
                      const childActive = isActive(pathname, child.url)
                      return (
                        <Link
                          key={child.id}
                          href={child.url}
                          onClick={onNavClick}
                          className={cn(
                            'flex items-center gap-3 py-2 pl-11 pr-4 text-sm transition-colors',
                            childActive
                              ? 'font-medium text-green-700'
                              : 'text-gray-500 hover:bg-gray-50 hover:text-gray-900',
                          )}
                        >
                          <ChildIcon className="h-3.5 w-3.5 shrink-0" />
                          {child.name}
                        </Link>
                      )
                    })}
                  </div>
                )}
              </div>
            )
          }

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
        })}
      </nav>
    </>
  )
}

interface AdminSidebarProps {
  mobileOpen?: boolean
  onMobileClose?: () => void
}

export function AdminSidebar({ mobileOpen, onMobileClose }: AdminSidebarProps) {
  const pathname = usePathname()
  const [menus, setMenus] = useState<MenuItem[]>([])
  const [openId, setOpenId] = useState<number | null>(null)

  useEffect(() => {
    const role = (localStorage.getItem('role') as UserRole) ?? 'SUPER_ADMIN'
    fetchMenusByRole(role)
      .then(setMenus)
      .catch(() => {})
  }, [])

  useEffect(() => {
    if (menus.length === 0) return
    const settingsMenu = menus.find((m) => m.children?.some((c) => pathname.startsWith(c.url)))
    setOpenId(settingsMenu?.id ?? null)
  }, [menus, pathname])

  const handleToggle = (id: number) => setOpenId((prev) => (prev === id ? null : id))

  return (
    <>
      <aside className="hidden h-screen w-56 flex-col border-r bg-white md:flex">
        <SidebarNav menus={menus} openId={openId} onToggle={handleToggle} />
      </aside>

      <Sheet open={mobileOpen} onOpenChange={(open) => { if (!open) onMobileClose?.() }}>
        <SheetContent side="left" className="w-56 p-0" showCloseButton={false}>
          <SidebarNav
            menus={menus}
            onNavClick={onMobileClose}
            openId={openId}
            onToggle={handleToggle}
          />
        </SheetContent>
      </Sheet>
    </>
  )
}

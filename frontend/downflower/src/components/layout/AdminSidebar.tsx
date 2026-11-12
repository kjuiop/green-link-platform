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
  Menu,
  Package,
  Newspaper,
  Calculator,
  type LucideIcon,
} from 'lucide-react'
import { cn } from '@/lib/utils'
import { Sheet, SheetContent } from '@/components/ui/sheet'
import { fetchMenusByRole } from '@/lib/api/menuApi'
import { type MenuItem } from '@/types/menu'
import { fetchMemberSummary } from '@/lib/api/memberApi'
import { type MemberSummary } from '@/types/member'

const ICON_MAP: Record<string, LucideIcon> = {
  '/dashboard': LayoutDashboard,
  '/orders/new': Plus,
  '/orders': List,
  '/orders/received': ListChecks,
  '/settings': Settings,
  '/settings/message-log': MessageSquare,
  '/settings/messages': SlidersHorizontal,
  '/vendors': Users,
  '/settings/menus': Menu,
  '/settings/products': Package,
  '/products': Package,
  '/settings/vendors': Users,
  '/community': Newspaper,
  '/settings/settlement': Calculator,
  '/vendor/settlement': Calculator,
  '/ordering/settlement': Calculator,
}

function isActive(pathname: string, url: string): boolean {
  if (url === '/orders') return (
    pathname === '/orders' ||
    (pathname.startsWith('/orders/') && !pathname.startsWith('/orders/new') && !pathname.startsWith('/orders/received')) ||
    pathname.startsWith('/ordering/orders/')
  )
  return pathname === url || pathname.startsWith(url + '/')
}

const COMPANY_TYPE_LABEL: Record<string, string> = {
  ORDERING: '발주사',
  VENDOR: '수주사',
  BOTH: '수발주사',
}

function MemberInfoCard({ summary, name, loginId }: { summary: MemberSummary; name: string; loginId: string }) {
  const isOrdering = summary.companyType === 'ORDERING' || summary.companyType === 'BOTH'
  const isVendor = summary.companyType === 'VENDOR' || summary.companyType === 'BOTH'

  return (
    <div className="mx-3 my-2 rounded-lg bg-green-700 p-3 text-white text-xs">
      <div className="mb-1.5 flex items-center justify-between">
        <span className="font-semibold text-green-100">회원정보</span>
        <span className="rounded bg-green-600 px-1.5 py-0.5 text-green-100">
          {COMPANY_TYPE_LABEL[summary.companyType] ?? summary.companyType}
        </span>
      </div>
      {name && (
        <div className="mb-1.5 flex items-center gap-1 text-green-200">
          <span className="font-medium text-white">{name}</span>
          <span>·</span>
          <span>{loginId}</span>
        </div>
      )}
      {isOrdering && (
        <div className="flex justify-between py-0.5 border-b border-green-600">
          <span>발주금액</span>
          <span className="font-medium">{summary.orderingAmount.toLocaleString()}원</span>
        </div>
      )}
      {isVendor && (
        <div className={cn('flex justify-between py-0.5', isOrdering && 'border-b border-green-600')}>
          <span>수주금액</span>
          <span className="font-medium">{summary.vendorAmount.toLocaleString()}원</span>
        </div>
      )}
      {isOrdering && (
        <div className="flex justify-between pt-0.5">
          <span className="text-green-200">예치금 잔액</span>
          <span className="text-green-200">
            {summary.depositBalance != null ? `${summary.depositBalance.toLocaleString()}원` : '-'}
          </span>
        </div>
      )}
    </div>
  )
}

interface SidebarNavProps {
  menus: MenuItem[]
  onNavClick?: () => void
  openId: number | null
  onToggle: (id: number) => void
  memberName?: string
  memberLoginId?: string
  memberSummary?: MemberSummary | null
}

function SidebarNav({ menus, onNavClick, openId, onToggle, memberName, memberLoginId, memberSummary }: SidebarNavProps) {
  const pathname = usePathname()

  return (
    <>
      <div className="flex h-14 items-center border-b px-4">
        <span className="text-sm font-bold text-green-700">🌿 온우림</span>
      </div>

      {memberSummary && <MemberInfoCard summary={memberSummary} name={memberName ?? ''} loginId={memberLoginId ?? ''} />}

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
  const [memberName, setMemberName] = useState<string>('')
  const [memberLoginId, setMemberLoginId] = useState<string>('')
  const [memberSummary, setMemberSummary] = useState<MemberSummary | null>(null)

  useEffect(() => {
    setMemberName(localStorage.getItem('name') ?? '')
    setMemberLoginId(localStorage.getItem('loginId') ?? '')
    fetchMenusByRole().then(setMenus).catch(() => {})
    fetchMemberSummary().then(setMemberSummary).catch(() => {})
  }, [])

  useEffect(() => {
    if (typeof window === 'undefined') return
    const handler = () => fetchMemberSummary().then(setMemberSummary).catch(() => {})
    window.addEventListener('member-summary-refresh', handler)
    return () => window.removeEventListener('member-summary-refresh', handler)
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
        <SidebarNav menus={menus} openId={openId} onToggle={handleToggle} memberName={memberName} memberLoginId={memberLoginId} memberSummary={memberSummary} />
      </aside>

      <Sheet open={mobileOpen} onOpenChange={(open) => { if (!open) onMobileClose?.() }}>
        <SheetContent side="left" className="w-56 p-0" showCloseButton={false}>
          <SidebarNav
            menus={menus}
            onNavClick={onMobileClose}
            openId={openId}
            onToggle={handleToggle}
            memberName={memberName}
            memberLoginId={memberLoginId}
            memberSummary={memberSummary}
          />
        </SheetContent>
      </Sheet>
    </>
  )
}

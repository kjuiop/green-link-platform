'use client'

import { useRouter } from 'next/navigation'
import { LogOut, Menu, User } from 'lucide-react'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { clearSession } from '@/lib/session'
import type { Session } from '@/types/auth'

interface HeaderProps {
  session: Session
  onMenuOpen?: () => void
}

export function Header({ session, onMenuOpen }: HeaderProps) {
  const router = useRouter()

  function handleLogout() {
    clearSession()
    router.replace('/login')
  }

  return (
    <header className="flex h-14 shrink-0 items-center border-b bg-white px-4">
      <button
        className="mr-2 rounded-md p-1.5 text-gray-500 hover:bg-gray-100 md:hidden"
        onClick={onMenuOpen}
        aria-label="메뉴 열기"
      >
        <Menu className="h-5 w-5" />
      </button>
      <div className="flex-1" />
      <DropdownMenu>
        <DropdownMenuTrigger aria-label="사용자 메뉴" className="flex items-center gap-2 rounded-full p-1.5 text-gray-500 hover:bg-gray-100 hover:text-gray-700">
          <User className="h-5 w-5" />
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end" className="w-44">
          <DropdownMenuGroup>
            <DropdownMenuLabel className="text-xs text-gray-500">
              {session.name} ({session.loginId})
            </DropdownMenuLabel>
          </DropdownMenuGroup>
          <DropdownMenuSeparator />
          <DropdownMenuItem
            className="cursor-pointer text-red-500 focus:text-red-500"
            onClick={handleLogout}
          >
            <LogOut className="mr-2 h-4 w-4" />
            로그아웃
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </header>
  )
}

'use client'

import { useRouter } from 'next/navigation'
import { User, LogOut, Settings, Menu } from 'lucide-react'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'

interface HeaderProps {
  userName?: string
  onMenuOpen?: () => void
}

export function Header({ userName, onMenuOpen }: HeaderProps) {
  const router = useRouter()

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
        <DropdownMenuTrigger className="flex items-center gap-2 rounded-full p-1.5 text-gray-500 hover:bg-gray-100 hover:text-gray-700">
          {userName && <span className="text-sm">{userName}</span>}
          <User className="h-5 w-5" />
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end" className="w-36">
          <DropdownMenuItem
            className="cursor-pointer"
            onClick={() => router.push('/settings/account')}
          >
            <Settings className="mr-2 h-4 w-4" />
            내 계정
          </DropdownMenuItem>
          <DropdownMenuSeparator />
          <DropdownMenuItem
            className="cursor-pointer text-red-500 focus:text-red-500"
            onClick={() => router.push('/login')}
          >
            <LogOut className="mr-2 h-4 w-4" />
            로그아웃
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </header>
  )
}

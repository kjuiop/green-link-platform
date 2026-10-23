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
  accountHref?: string
}

export function Header({ userName, onMenuOpen, accountHref = '/settings/account' }: HeaderProps) {
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
      {/* 연락처 */}
      <div className="hidden items-center gap-3 sm:flex">
        <div className="flex items-center gap-1.5 rounded-md bg-green-50 px-3 py-1.5">
          <span className="text-xs font-bold text-green-600">TEL</span>
          <span className="text-sm font-bold text-green-800 tracking-wide">1855-4400</span>
        </div>
        <div className="flex items-center gap-1.5 rounded-md bg-gray-100 px-3 py-1.5">
          <span className="text-xs font-bold text-gray-500">PHONE</span>
          <span className="text-sm font-bold text-gray-800 tracking-wide">010-7551-1922</span>
        </div>
      </div>
      <div className="flex-1" />
      <DropdownMenu>
        <DropdownMenuTrigger className="flex items-center gap-2 rounded-full p-1.5 text-gray-500 hover:bg-gray-100 hover:text-gray-700">
          {userName && <span className="text-sm">{userName}</span>}
          <User className="h-5 w-5" />
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end" className="w-36">
          <DropdownMenuItem
            className="cursor-pointer"
            onClick={() => router.push(accountHref)}
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

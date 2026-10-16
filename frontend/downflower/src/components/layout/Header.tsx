'use client'

import { useRouter } from 'next/navigation'
import { User, LogOut, Settings } from 'lucide-react'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'

interface HeaderProps {
  userName?: string
}

export function Header({ userName }: HeaderProps) {
  const router = useRouter()

  return (
    <header className="flex h-14 shrink-0 items-center justify-end border-b bg-white px-6">
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

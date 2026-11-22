'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { clearSession } from '@/lib/session'
import { useSession } from '@/lib/useSession'

const MESSAGE: Record<'PENDING' | 'NONE', { title: string; body: string }> = {
  PENDING: {
    title: '승인 대기 중',
    body: '쇼핑몰 연동 신청이 접수되었습니다. 최고관리자의 승인 후 이용할 수 있습니다.',
  },
  NONE: {
    title: '쇼핑몰 관리 권한이 없습니다',
    body: '쇼핑몰 연동 신청 후 승인을 받아야 이용할 수 있습니다. 연동 신청 기능은 준비 중입니다.',
  },
}

export default function AccessPage() {
  const router = useRouter()
  const { session, loading } = useSession()

  useEffect(() => {
    if (loading) return
    if (!session) router.replace('/login')
    else if (session.accessStatus === 'APPROVED') router.replace('/dashboard')
  }, [loading, session, router])

  if (loading || !session || session.accessStatus === 'APPROVED') return null

  const message = MESSAGE[session.accessStatus]

  function handleLogout() {
    clearSession()
    router.replace('/login')
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 p-4">
      <div className="w-full max-w-sm space-y-4">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-green-700">🛍️ 쇼핑몰 관리</h1>
          <p className="mt-1 text-sm text-gray-500">{session.name} ({session.loginId})</p>
        </div>
        <Card>
          <CardHeader>
            <CardTitle className="text-base">{message.title}</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <p className="text-sm text-gray-600">{message.body}</p>
            <Button variant="outline" className="w-full" onClick={handleLogout}>
              로그아웃
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

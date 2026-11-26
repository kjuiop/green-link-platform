'use client'

import { Suspense, useEffect, useState } from 'react'
import Link from 'next/link'
import { useRouter, useSearchParams } from 'next/navigation'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import { ApiError } from '@/lib/api/client'
import { login } from '@/lib/api/authApi'
import { getSession, setSession } from '@/lib/session'
import { markTokenFresh } from '@/lib/useTokenRefresh'
import type { Session } from '@/types/auth'

function homeOf(session: Session): string {
  return session.accessStatus === 'APPROVED' ? '/dashboard' : '/access'
}

export default function LoginPage() {
  return (
    <Suspense>
      <LoginForm />
    </Suspense>
  )
}

function LoginForm() {
  const router = useRouter()
  const passwordJustSet = useSearchParams().get('notice') === 'password-set'
  const [loginId, setLoginId] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    const session = getSession()
    if (session) router.replace(homeOf(session))
  }, [router])

  async function handleLogin() {
    if (loading) return
    if (!loginId) { setError('아이디를 입력하세요'); return }
    if (!password) { setError('비밀번호를 입력하세요'); return }
    setError('')
    setLoading(true)
    try {
      const session = await login(loginId, password)
      setSession(session)
      markTokenFresh()
      router.push(homeOf(session))
    } catch (e) {
      if (e instanceof ApiError && e.code === 'SHOP_AUTH_004') {
        router.push(`/password-setup?loginId=${encodeURIComponent(loginId)}`)
        return
      }
      setError(e instanceof Error ? e.message : '서버 연결에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 p-4">
      <div className="w-full max-w-sm space-y-4">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-green-700">🛍️ 쇼핑몰 관리</h1>
          <p className="mt-1 text-sm text-gray-500">온우림 계정으로 로그인합니다</p>
        </div>

        <Card>
          <CardHeader>
            <CardTitle className="text-base">로그인</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="loginId">아이디</Label>
              <Input
                id="loginId"
                placeholder="아이디를 입력하세요"
                autoComplete="username"
                value={loginId}
                onChange={(e) => { setLoginId(e.target.value); setError('') }}
                onKeyDown={(e) => e.key === 'Enter' && handleLogin()}
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="password">비밀번호</Label>
              <Input
                id="password"
                type="password"
                placeholder="비밀번호를 입력하세요"
                autoComplete="current-password"
                value={password}
                onChange={(e) => { setPassword(e.target.value); setError('') }}
                onKeyDown={(e) => e.key === 'Enter' && handleLogin()}
              />
            </div>
            {passwordJustSet && !error && (
              <p className="text-xs text-green-700">비밀번호가 설정되었습니다. 로그인하세요.</p>
            )}
            {error && <p className="text-xs text-red-500">{error}</p>}
            <Button
              className="w-full bg-green-700 hover:bg-green-800"
              onClick={handleLogin}
              disabled={loading}
            >
              {loading ? '로그인 중...' : '로그인'}
            </Button>
          </CardContent>
        </Card>
        <p className="text-center text-sm text-gray-500">
          계정이 없으신가요?{' '}
          <Link href="/signup" className="font-medium text-green-700 hover:underline">회원가입</Link>
        </p>
      </div>
    </div>
  )
}

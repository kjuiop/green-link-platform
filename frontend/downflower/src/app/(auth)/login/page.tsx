'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import Link from 'next/link'

export default function LoginPage() {
  const router = useRouter()
  const [loginId, setLoginId] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleLogin() {
    if (!loginId) { setError('아이디를 입력하세요'); return }
    if (!password) { setError('비밀번호를 입력하세요'); return }
    setError('')
    setLoading(true)
    try {
      const res = await fetch('/api/auth/admin/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ loginId, password }),
      })
      if (!res.ok) {
        const data = await res.json().catch(() => ({}))
        setError(data.message ?? '아이디 또는 비밀번호가 올바르지 않습니다.')
        return
      }
      const { accessToken, name, vendorId, companyName } = await res.json()
      localStorage.setItem('accessToken', accessToken)
      if (name) localStorage.setItem('name', name)
      localStorage.removeItem('vendorId')
      localStorage.removeItem('companyName')
      if (vendorId != null) localStorage.setItem('vendorId', String(vendorId))
      if (companyName) localStorage.setItem('companyName', companyName)
      localStorage.setItem('loginId', loginId)
      let role = ''
      try {
        const base64 = accessToken.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
        const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=')
        const payload = JSON.parse(atob(padded))
        const roles: string[] = payload.roles ?? []
        role = roles[0]?.replace('ROLE_', '') ?? ''
        if (role) localStorage.setItem('role', role)
      } catch {
        // JWT 디코드 실패해도 로그인은 계속 진행
      }
      router.push(role === 'VENDOR' ? '/vendor/dashboard' : '/dashboard')
    } catch {
      setError('서버 연결에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50">
      <div className="w-full max-w-sm space-y-4">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-green-700">🌿 온우림</h1>
          <p className="mt-1 text-sm text-gray-500">발주·수주 관리 시스템</p>
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
                value={password}
                onChange={(e) => { setPassword(e.target.value); setError('') }}
                onKeyDown={(e) => e.key === 'Enter' && handleLogin()}
              />
            </div>
            {error && <p className="text-xs text-red-500">{error}</p>}
            <Button
              className="w-full bg-green-700 hover:bg-green-800"
              onClick={handleLogin}
              disabled={loading}
            >
              {loading ? '로그인 중...' : '로그인'}
            </Button>

            <div className="flex items-center justify-center gap-3 pt-1 text-xs text-gray-400">
              <Link href="/signup" className="hover:text-gray-600">회원가입</Link>
              <span>|</span>
              <Link href="/find-id" className="hover:text-gray-600">아이디 찾기</Link>
              <span>|</span>
              <Link href="/find-password" className="hover:text-gray-600">비밀번호 찾기</Link>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

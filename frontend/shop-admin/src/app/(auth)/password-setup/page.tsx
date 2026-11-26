'use client'

import { Suspense, useState } from 'react'
import Link from 'next/link'
import { useRouter, useSearchParams } from 'next/navigation'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import { setupPassword } from '@/lib/api/authApi'

function validate(loginId: string, phone: string, password: string, confirm: string): string {
  if (!loginId.trim()) return '아이디를 입력하세요'
  if (!/^[0-9-]{9,20}$/.test(phone)) return '연락처를 숫자와 하이픈으로 입력하세요'
  if (password.length < 8 || password.length > 64 || !/[A-Za-z]/.test(password) || !/\d/.test(password)) {
    return '비밀번호는 영문과 숫자를 포함한 8~64자로 입력하세요'
  }
  if (password !== confirm) return '비밀번호가 일치하지 않습니다'
  return ''
}

export default function PasswordSetupPage() {
  return (
    <Suspense>
      <PasswordSetupForm />
    </Suspense>
  )
}

function PasswordSetupForm() {
  const router = useRouter()
  const [loginId, setLoginId] = useState(useSearchParams().get('loginId') ?? '')
  const [phone, setPhone] = useState('')
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit() {
    if (loading) return
    const message = validate(loginId, phone, password, confirm)
    if (message) { setError(message); return }
    setError('')
    setLoading(true)
    try {
      await setupPassword(loginId.trim(), phone, password)
      router.replace('/login?notice=password-set')
    } catch (e) {
      setError(e instanceof Error ? e.message : '서버 연결에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  function clearError() { setError('') }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 p-4">
      <div className="w-full max-w-sm space-y-4">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-green-700">🛍️ 쇼핑몰 관리</h1>
          <p className="mt-1 text-sm text-gray-500">관리자가 만든 계정의 비밀번호를 처음 설정합니다</p>
        </div>

        <Card>
          <CardHeader>
            <CardTitle className="text-base">비밀번호 설정</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="loginId">아이디</Label>
              <Input
                id="loginId"
                autoComplete="username"
                value={loginId}
                onChange={(e) => { setLoginId(e.target.value); clearError() }}
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="phone">등록된 연락처</Label>
              <Input
                id="phone"
                type="tel"
                placeholder="010-0000-0000"
                autoComplete="tel"
                maxLength={20}
                value={phone}
                onChange={(e) => { setPhone(e.target.value); clearError() }}
              />
              <p className="text-xs text-gray-500">관리자가 계정을 만들 때 등록한 연락처를 입력하세요.</p>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="password">새 비밀번호</Label>
              <Input
                id="password"
                type="password"
                placeholder="영문+숫자 포함 8자 이상"
                autoComplete="new-password"
                maxLength={64}
                value={password}
                onChange={(e) => { setPassword(e.target.value); clearError() }}
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="confirm">새 비밀번호 확인</Label>
              <Input
                id="confirm"
                type="password"
                autoComplete="new-password"
                maxLength={64}
                value={confirm}
                onChange={(e) => { setConfirm(e.target.value); clearError() }}
                onKeyDown={(e) => e.key === 'Enter' && handleSubmit()}
              />
            </div>
            {error && <p className="text-xs text-red-500">{error}</p>}
            <Button className="w-full bg-green-700 hover:bg-green-800" onClick={handleSubmit} disabled={loading}>
              {loading ? '설정 중...' : '비밀번호 설정'}
            </Button>
          </CardContent>
        </Card>
        <p className="text-center text-sm text-gray-500">
          <Link href="/login" className="font-medium text-green-700 hover:underline">로그인으로 돌아가기</Link>
        </p>
      </div>
    </div>
  )
}

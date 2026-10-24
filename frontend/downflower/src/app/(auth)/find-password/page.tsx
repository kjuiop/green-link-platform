'use client'

import { useState, useEffect } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import Link from 'next/link'

export default function FindPasswordPage() {
  const [loginId, setLoginId] = useState('')
  const [phone, setPhone] = useState('')
  const [codeSent, setCodeSent] = useState(false)
  const [codeValue, setCodeValue] = useState('')
  const [verified, setVerified] = useState(false)
  const [codeError, setCodeError] = useState('')
  const [countdown, setCountdown] = useState(0)

  const [newPassword, setNewPassword] = useState('')
  const [newPasswordConfirm, setNewPasswordConfirm] = useState('')
  const [resetDone, setResetDone] = useState(false)

  const formatPhone = (value: string) => {
    const digits = value.replace(/\D/g, '').slice(0, 11)
    if (digits.length <= 3) return digits
    if (digits.length <= 7) return `${digits.slice(0, 3)}-${digits.slice(3)}`
    return `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7)}`
  }

  useEffect(() => {
    if (countdown <= 0) return
    const timer = setInterval(() => setCountdown((c) => c - 1), 1000)
    return () => clearInterval(timer)
  }, [countdown])

  const formatCountdown = (s: number) =>
    `${String(Math.floor(s / 60)).padStart(2, '0')}:${String(s % 60).padStart(2, '0')}`

  const handleSendCode = async () => {
    if (!phone) return
    try {
      const res = await fetch('/api/auth/phone/send', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ phone }),
      })
      if (!res.ok) {
        const body = await res.json().catch(() => null)
        alert(body?.message ?? '인증번호 발송에 실패했습니다.')
        return
      }
      setCodeSent(true)
      setCodeValue('')
      setCodeError('')
      setCountdown(180)
    } catch {
      alert('인증번호 발송 중 오류가 발생했습니다.')
    }
  }

  const handleVerify = async () => {
    try {
      const res = await fetch('/api/auth/phone/verify', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ phone, code: codeValue }),
      })
      if (res.ok) {
        setVerified(true)
        setCodeError('')
      } else {
        setCodeError('인증번호가 올바르지 않거나 만료되었습니다.')
      }
    } catch {
      setCodeError('인증 확인 중 오류가 발생했습니다.')
    }
  }

  const handleResetPassword = async () => {
    if (newPassword !== newPasswordConfirm) {
      alert('비밀번호가 일치하지 않습니다.')
      return
    }
    if (newPassword.length < 8) {
      alert('비밀번호는 8자 이상이어야 합니다.')
      return
    }
    try {
      const res = await fetch('/api/auth/reset-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ phone, loginId, newPassword }),
      })
      if (res.ok) {
        setResetDone(true)
      } else {
        const body = await res.json().catch(() => null)
        alert(body?.message ?? '비밀번호 변경에 실패했습니다.')
      }
    } catch {
      alert('비밀번호 변경 중 오류가 발생했습니다.')
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50">
      <div className="w-full max-w-sm space-y-4">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-green-700">🌿 다운플라워</h1>
          <p className="mt-1 text-sm text-gray-500">발주·수주 관리 시스템</p>
        </div>

        {resetDone ? (
          <Card>
            <CardHeader>
              <CardTitle className="text-base">비밀번호 변경 완료</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <p className="text-sm text-gray-600 text-center">
                비밀번호가 성공적으로 변경되었습니다.
              </p>
              <Link href="/login">
                <Button className="w-full bg-green-700 hover:bg-green-800">
                  로그인하러 가기
                </Button>
              </Link>
            </CardContent>
          </Card>
        ) : !verified ? (
          <Card>
            <CardHeader>
              <CardTitle className="text-base">비밀번호 찾기</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <div className="space-y-1.5">
                <Label>아이디</Label>
                <Input
                  placeholder="아이디를 입력하세요"
                  value={loginId}
                  onChange={(e) => setLoginId(e.target.value)}
                />
              </div>

              <div className="space-y-1.5">
                <Label>연락처</Label>
                <div className="flex gap-2">
                  <Input
                    placeholder="010-0000-0000"
                    value={phone}
                    onChange={(e) => setPhone(formatPhone(e.target.value))}
                    inputMode="numeric"
                    className="flex-1"
                  />
                  <button
                    type="button"
                    onClick={handleSendCode}
                    className="shrink-0 rounded-md border border-green-700 px-3 py-2 text-xs font-medium text-green-700 hover:bg-green-50"
                  >
                    {codeSent ? '재발송' : '인증번호 받기'}
                  </button>
                </div>
              </div>

              {codeSent && (
                <div className="space-y-1.5">
                  <div className="flex gap-2">
                    <div className="relative flex-1">
                      <Input
                        placeholder="인증번호 6자리"
                        value={codeValue}
                        onChange={(e) => setCodeValue(e.target.value)}
                        maxLength={6}
                      />
                      {countdown > 0 && (
                        <span className="absolute right-3 top-1/2 -translate-y-1/2 text-xs text-red-500">
                          {formatCountdown(countdown)}
                        </span>
                      )}
                    </div>
                    <button
                      type="button"
                      onClick={handleVerify}
                      disabled={countdown === 0}
                      className="shrink-0 rounded-md bg-green-700 px-3 py-2 text-xs font-medium text-white hover:bg-green-800 disabled:opacity-40"
                    >
                      확인
                    </button>
                  </div>
                  {codeError && <p className="text-xs text-red-500">{codeError}</p>}
                  {countdown === 0 && (
                    <p className="text-xs text-gray-400">인증 시간이 만료되었습니다. 재발송해주세요.</p>
                  )}
                </div>
              )}

              <div className="flex items-center justify-center gap-3 text-xs text-gray-400">
                <Link href="/login" className="hover:text-gray-600">로그인</Link>
                <span>|</span>
                <Link href="/find-id" className="hover:text-gray-600">아이디 찾기</Link>
              </div>
            </CardContent>
          </Card>
        ) : (
          <Card>
            <CardHeader>
              <CardTitle className="text-base">비밀번호 재설정</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <p className="text-sm text-gray-500">본인 인증이 완료되었습니다. 새 비밀번호를 설정해주세요.</p>

              <div className="space-y-1.5">
                <Label>새 비밀번호</Label>
                <Input
                  type="password"
                  placeholder="8자 이상"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                />
              </div>
              <div className="space-y-1.5">
                <Label>새 비밀번호 확인</Label>
                <Input
                  type="password"
                  placeholder="비밀번호 재입력"
                  value={newPasswordConfirm}
                  onChange={(e) => setNewPasswordConfirm(e.target.value)}
                />
                {newPassword && newPasswordConfirm && newPassword !== newPasswordConfirm && (
                  <p className="text-xs text-red-500">비밀번호가 일치하지 않습니다.</p>
                )}
              </div>

              <Button
                className="w-full bg-green-700 hover:bg-green-800"
                onClick={handleResetPassword}
              >
                비밀번호 변경
              </Button>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  )
}

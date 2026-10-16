'use client'

import { useState, useEffect } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import Link from 'next/link'

export default function FindIdPage() {
  const [phone, setPhone] = useState('')
  const [codeSent, setCodeSent] = useState(false)
  const [codeValue, setCodeValue] = useState('')
  const [verified, setVerified] = useState(false)
  const [codeError, setCodeError] = useState('')
  const [countdown, setCountdown] = useState(0)

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

  const handleSendCode = () => {
    setCodeSent(true)
    setCodeValue('')
    setCodeError('')
    setCountdown(60)
  }

  const handleVerify = () => {
    if (codeValue === '123456') {
      setVerified(true)
      setCodeError('')
    } else {
      setCodeError('인증번호가 올바르지 않습니다.')
    }
  }

  const formatCountdown = (s: number) =>
    `${String(Math.floor(s / 60)).padStart(2, '0')}:${String(s % 60).padStart(2, '0')}`

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50">
      <div className="w-full max-w-sm space-y-4">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-green-700">🌿 다운플라워</h1>
          <p className="mt-1 text-sm text-gray-500">발주·수주 관리 시스템</p>
        </div>

        {!verified ? (
          <Card>
            <CardHeader>
              <CardTitle className="text-base">아이디 찾기</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
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
                <Link href="/find-password" className="hover:text-gray-600">비밀번호 찾기</Link>
              </div>
            </CardContent>
          </Card>
        ) : (
          <Card>
            <CardHeader>
              <CardTitle className="text-base">아이디 찾기 결과</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="rounded-lg border border-green-200 bg-green-50 p-4 text-center">
                <p className="text-xs text-gray-500">고객님의 아이디</p>
                <p className="mt-1 text-lg font-bold text-green-700">vendor_001</p>
                <p className="mt-1 text-xs text-gray-400">가입일: 2026-01-15</p>
              </div>

              <div className="flex gap-2">
                <Link href="/login" className="flex-1">
                  <Button className="w-full bg-green-700 hover:bg-green-800">
                    로그인하러 가기
                  </Button>
                </Link>
                <Link href="/find-password" className="flex-1">
                  <Button variant="outline" className="w-full">
                    비밀번호 찾기
                  </Button>
                </Link>
              </div>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  )
}

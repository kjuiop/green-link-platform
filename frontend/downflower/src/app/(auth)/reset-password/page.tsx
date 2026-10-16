'use client'

import { useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import Link from 'next/link'

export default function ResetPasswordPage() {
  const [done, setDone] = useState(false)

  if (done) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-50">
        <div className="w-full max-w-sm space-y-4">
          <div className="text-center">
            <h1 className="text-2xl font-bold text-green-700">🌿 다운플라워</h1>
          </div>
          <Card>
            <CardContent className="space-y-4 pt-6 text-center">
              <p className="text-4xl">✅</p>
              <p className="font-medium">비밀번호가 변경되었습니다.</p>
              <p className="text-sm text-gray-400">새 비밀번호로 로그인해주세요.</p>
              <Link href="/login">
                <Button className="w-full bg-green-700 hover:bg-green-800">로그인하러 가기</Button>
              </Link>
            </CardContent>
          </Card>
        </div>
      </div>
    )
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50">
      <div className="w-full max-w-sm space-y-4">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-green-700">🌿 다운플라워</h1>
          <p className="mt-1 text-sm text-gray-500">비밀번호 재설정</p>
        </div>

        <Card>
          <CardHeader>
            <CardTitle className="text-base">새 비밀번호 설정</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="space-y-1.5">
              <Label>새 비밀번호</Label>
              <Input type="password" placeholder="8자 이상" />
            </div>
            <div className="space-y-1.5">
              <Label>새 비밀번호 확인</Label>
              <Input type="password" placeholder="비밀번호 재입력" />
            </div>

            <Button
              className="w-full bg-green-700 hover:bg-green-800"
              onClick={() => setDone(true)}
            >
              비밀번호 변경
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

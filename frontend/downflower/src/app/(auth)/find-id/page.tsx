import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import Link from 'next/link'

export default function FindIdPage() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50">
      <div className="w-full max-w-sm space-y-4">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-green-700">🌿 다운플라워</h1>
          <p className="mt-1 text-sm text-gray-500">발주·수주 관리 시스템</p>
        </div>

        <Card>
          <CardHeader>
            <CardTitle className="text-base">아이디 찾기</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="space-y-1.5">
              <Label>업체명</Label>
              <Input placeholder="가입 시 등록한 업체명" />
            </div>
            <div className="space-y-1.5">
              <Label>담당자명</Label>
              <Input placeholder="담당자 성함" />
            </div>
            <div className="space-y-1.5">
              <Label>연락처</Label>
              <Input placeholder="010-0000-0000" />
            </div>

            <Button className="w-full bg-green-700 hover:bg-green-800">
              아이디 찾기
            </Button>

            <div className="flex items-center justify-center gap-3 text-xs text-gray-400">
              <Link href="/login" className="hover:text-gray-600">로그인</Link>
              <span>|</span>
              <Link href="/find-password" className="hover:text-gray-600">비밀번호 찾기</Link>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

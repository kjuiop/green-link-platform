import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import Link from 'next/link'

export default function SignupPage() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50">
      <div className="w-full max-w-sm space-y-4">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-green-700">🌿 다운플라워</h1>
          <p className="mt-1 text-sm text-gray-500">수주업체 회원가입</p>
        </div>

        <Card>
          <CardHeader>
            <CardTitle className="text-base">회원가입</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="space-y-1.5">
              <Label>업체명 *</Label>
              <Input placeholder="업체명을 입력하세요" />
            </div>
            <div className="space-y-1.5">
              <Label>담당자명 *</Label>
              <Input placeholder="담당자 성함" />
            </div>
            <div className="space-y-1.5">
              <Label>아이디 *</Label>
              <Input placeholder="영문·숫자 조합 6자 이상" />
            </div>
            <div className="space-y-1.5">
              <Label>비밀번호 *</Label>
              <Input type="password" placeholder="8자 이상" />
            </div>
            <div className="space-y-1.5">
              <Label>비밀번호 확인 *</Label>
              <Input type="password" placeholder="비밀번호 재입력" />
            </div>
            <div className="space-y-1.5">
              <Label>연락처 *</Label>
              <Input placeholder="010-0000-0000" />
            </div>
            <div className="space-y-1.5">
              <Label>주소</Label>
              <Input placeholder="업체 주소" />
            </div>

            <Button className="w-full bg-green-700 hover:bg-green-800">
              가입 신청
            </Button>

            <p className="text-center text-xs text-gray-400">
              가입 신청 후 관리자 승인이 완료되면 로그인하실 수 있습니다.
            </p>

            <div className="text-center text-xs text-gray-400">
              이미 계정이 있으신가요?{' '}
              <Link href="/login" className="text-green-700 hover:underline">로그인</Link>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

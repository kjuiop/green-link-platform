import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'

export default function SettingsAccountPage() {
  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">내 계정</h1>

      <Card className="max-w-lg">
        <CardHeader>
          <CardTitle className="text-sm">기본 정보</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="space-y-1.5">
            <Label>아이디</Label>
            <Input defaultValue="admin" disabled className="bg-gray-50" />
          </div>
          <div className="space-y-1.5">
            <Label>이름</Label>
            <Input defaultValue="관리자" />
          </div>
          <div className="space-y-1.5">
            <Label>연락처</Label>
            <Input defaultValue="010-0000-0000" />
          </div>
          <Button className="bg-green-700 hover:bg-green-800">저장</Button>
        </CardContent>
      </Card>

      <Card className="max-w-lg">
        <CardHeader>
          <CardTitle className="text-sm">비밀번호 변경</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="space-y-1.5">
            <Label>현재 비밀번호</Label>
            <Input type="password" placeholder="현재 비밀번호" />
          </div>
          <div className="space-y-1.5">
            <Label>새 비밀번호</Label>
            <Input type="password" placeholder="8자 이상" />
          </div>
          <div className="space-y-1.5">
            <Label>새 비밀번호 확인</Label>
            <Input type="password" placeholder="새 비밀번호 재입력" />
          </div>
          <Button className="bg-green-700 hover:bg-green-800">변경</Button>
        </CardContent>
      </Card>
    </div>
  )
}

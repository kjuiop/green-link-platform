import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import { mockVendors } from '@/lib/mock/vendors'

export default function VendorAccountPage() {
  const vendor = mockVendors[0]

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">내 계정정보</h1>

      <div className="grid gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-sm">업체정보</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="space-y-1.5">
              <Label>업체명</Label>
              <Input defaultValue={vendor.name} />
            </div>
            <div className="space-y-1.5">
              <Label>담당자명</Label>
              <Input defaultValue={vendor.managerName} />
            </div>
            <div className="space-y-1.5">
              <Label>연락처</Label>
              <Input defaultValue={vendor.phone} />
            </div>
            <div className="space-y-1.5">
              <Label>주소</Label>
              <Input defaultValue={vendor.address} />
            </div>
          </CardContent>
        </Card>

        <Card>
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
              <Input type="password" placeholder="새 비밀번호" />
            </div>
            <div className="space-y-1.5">
              <Label>새 비밀번호 확인</Label>
              <Input type="password" placeholder="새 비밀번호 확인" />
            </div>
            <Button className="w-full bg-green-700 hover:bg-green-800">비밀번호 변경</Button>
          </CardContent>
        </Card>
      </div>

      <div className="flex justify-end">
        <Button className="bg-green-700 hover:bg-green-800">정보 저장</Button>
      </div>
    </div>
  )
}

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'

export default function SettingsMessagesPage() {
  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">문자발송 설정</h1>

      <Card>
        <CardHeader>
          <CardTitle className="text-sm">발신 정보</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="space-y-1.5">
            <Label>발신번호</Label>
            <Input placeholder="010-0000-0000" />
          </div>
          <div className="space-y-1.5">
            <Label>발신자명</Label>
            <Input placeholder="다운플라워" />
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-sm">문자 템플릿</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="space-y-1.5">
            <Label>배송완료 문자 내용</Label>
            <Textarea
              rows={4}
              defaultValue={"안녕하세요.\n주문하신 화환이 배송 완료되었습니다.\n배송 사진을 확인해주세요.\n\n[사진 링크]"}
            />
          </div>
          <p className="text-xs text-gray-400">
            사진 링크는 배송완료 처리 시 자동으로 삽입됩니다.
          </p>
          <Button className="bg-green-700 hover:bg-green-800">저장</Button>
        </CardContent>
      </Card>
    </div>
  )
}

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'

export default function NewOrderPage() {
  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">신규 발주</h1>

      <div className="grid gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-sm">기본정보</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <Label>주문번호</Label>
                <Input value="DF-20261016-004" disabled className="bg-gray-50" />
              </div>
              <div className="space-y-1.5">
                <Label>발주일시</Label>
                <Input value="2026-10-16 15:00" disabled className="bg-gray-50" />
              </div>
            </div>
            <div className="space-y-1.5">
              <Label>수주업체 *</Label>
              <Input placeholder="업체 선택" />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <Label>상품명 *</Label>
                <Input placeholder="예: 근조화환 3단" />
              </div>
              <div className="space-y-1.5">
                <Label>수량 *</Label>
                <Input type="number" defaultValue={1} />
              </div>
            </div>
            <div className="space-y-1.5">
              <Label>발주금액 *</Label>
              <Input type="number" placeholder="0" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-sm">배송정보</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="space-y-1.5">
              <Label>배송 희망일시 *</Label>
              <Input type="datetime-local" />
            </div>
            <div className="space-y-1.5">
              <Label>주소 *</Label>
              <Input placeholder="도로명 주소 검색" />
            </div>
            <div className="space-y-1.5">
              <Label>상세주소</Label>
              <Input placeholder="상세주소" />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <Label>장소명 (장례식장·예식장)</Label>
                <Input placeholder="예: 한독병원장례식장" />
              </div>
              <div className="space-y-1.5">
                <Label>빈소·호실</Label>
                <Input placeholder="예: 무궁화실" />
              </div>
            </div>
            <div className="space-y-1.5">
              <Label>행사정보 (고인명·상주명 등)</Label>
              <Input placeholder="예: 고인 김순영" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-sm">리본 문구</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="space-y-1.5">
              <Label>리본 왼쪽 (보내는 사람·회사명)</Label>
              <Input placeholder="예: 주식회사 효진토건 부장 신효섭" />
            </div>
            <div className="space-y-1.5">
              <Label>리본 오른쪽 (축하·근조 문구)</Label>
              <Input placeholder="예: 삼가 고인의 명복을 빕니다" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-sm">주문자 / 받는 분</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <Label>주문자 성함 *</Label>
                <Input placeholder="주문자 성함" />
              </div>
              <div className="space-y-1.5">
                <Label>주문자 휴대폰 *</Label>
                <Input placeholder="010-0000-0000" />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <Label>받는 분 성함</Label>
                <Input placeholder="받는 분 성함" />
              </div>
              <div className="space-y-1.5">
                <Label>받는 분 휴대폰</Label>
                <Input placeholder="010-0000-0000" />
              </div>
            </div>
            <div className="space-y-1.5">
              <Label>사진 발송 대상</Label>
              <div className="flex gap-4">
                <label className="flex items-center gap-1.5 text-sm">
                  <input type="checkbox" defaultChecked /> 주문자에게 발송
                </label>
                <label className="flex items-center gap-1.5 text-sm">
                  <input type="checkbox" /> 받는 분에게 발송
                </label>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle className="text-sm">기타</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <Label>관리자 메모</Label>
                <Input placeholder="내부 메모" />
              </div>
              <div className="space-y-1.5">
                <Label>수주업체 전달사항</Label>
                <Input placeholder="업체에 전달할 내용" />
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <div className="flex justify-end gap-2">
        <Button variant="outline">취소</Button>
        <Button className="bg-green-700 hover:bg-green-800">발주 등록</Button>
      </div>
    </div>
  )
}

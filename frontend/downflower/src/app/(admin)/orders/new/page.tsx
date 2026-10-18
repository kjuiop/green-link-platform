'use client'

import { useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'
import { mockVendors } from '@/lib/mock/vendors'

const activeVendors = mockVendors.filter((v) => v.status === 'active')

export default function NewOrderPage() {
  const [vendorId, setVendorId] = useState('')
  const [deliveryDate, setDeliveryDate] = useState('')
  const [address, setAddress] = useState('')
  const [quantity, setQuantity] = useState(1)
  const [amount, setAmount] = useState('')
  const [selectedPreset, setSelectedPreset] = useState('')
  const [deliveryHour, setDeliveryHour] = useState('')
  const [deliveryMinute, setDeliveryMinute] = useState('00')
  const [deliveryTimeType, setDeliveryTimeType] = useState('까지')
  const [deliveryTimeCustom, setDeliveryTimeCustom] = useState('')
  const [messageSchedule, setMessageSchedule] = useState<'immediate' | 'scheduled'>('immediate')
  const [scheduledHour, setScheduledHour] = useState('9')
  const [scheduledMinute, setScheduledMinute] = useState('00')
  const [errors, setErrors] = useState<{ deliveryDate?: string; address?: string }>({})

  const AMOUNT_PRESETS = [
    { label: '기본32', value: 32000 },
    { label: '대품42', value: 42000 },
    { label: '특품52', value: 52000 },
    { label: '4단62', value: 62000 },
    { label: '쌀10키로62', value: 62000 },
    { label: '오브제1단42', value: 42000 },
  ]

  function handleSubmit() {
    const next: typeof errors = {}
    if (!deliveryDate) next.deliveryDate = '배송날짜를 입력해주세요.'
    if (!address.trim()) next.address = '주소를 입력해주세요.'
    setErrors(next)
    if (Object.keys(next).length > 0) return
    // TODO: 발주 등록 API 호출
  }

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">신규 발주</h1>

      <div className="grid gap-4 lg:grid-cols-2">
        {/* 기본정보 */}
        <Card>
          <CardHeader>
            <CardTitle className="text-sm">기본정보</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
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
              <select
                value={vendorId}
                onChange={(e) => setVendorId(e.target.value)}
                className="w-full rounded-md border bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
              >
                <option value="">업체 선택</option>
                {activeVendors.map((v) => (
                  <option key={v.id} value={v.id}>{v.name}</option>
                ))}
              </select>
            </div>
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
              <div className="space-y-1.5">
                <Label>상품명 *</Label>
                <Input placeholder="예: 근조화환 3단" />
              </div>
              <div className="space-y-1.5">
                <Label>수량 *</Label>
                <Input
                  type="number"
                  min={1}
                  value={quantity}
                  onChange={(e) => setQuantity(Math.max(1, Number(e.target.value)))}
                />
              </div>
            </div>
            <div className="space-y-1.5">
              <Label>발주금액 *</Label>
              <Input
                type="number"
                placeholder="0"
                value={amount}
                onChange={(e) => { setAmount(e.target.value); setSelectedPreset('') }}
              />
              <div className="flex flex-wrap gap-1.5">
                {AMOUNT_PRESETS.map(({ label, value }) => (
                  <button
                    key={label}
                    type="button"
                    onClick={() => { setAmount(String(value)); setSelectedPreset(label) }}
                    className={cn(
                      'rounded-md border px-2.5 py-1 text-xs transition-colors',
                      selectedPreset === label
                        ? 'border-green-700 bg-green-700 text-white'
                        : 'border-gray-300 text-gray-600 hover:border-gray-400 hover:bg-gray-50',
                    )}
                  >
                    {label}
                  </button>
                ))}
              </div>
              {amount && quantity > 1 && (
                <p className="text-sm font-medium text-green-700">
                  총 발주금액: {(Number(amount) * quantity).toLocaleString()}원
                  <span className="ml-1.5 text-xs font-normal text-gray-400">({Number(amount).toLocaleString()} × {quantity})</span>
                </p>
              )}
            </div>
            <div className="space-y-1.5">
              <Label>추가비용</Label>
              <Input type="number" placeholder="0" />
              <p className="text-xs text-gray-400">기본 발주금액 외 추가로 발생한 비용</p>
            </div>
          </CardContent>
        </Card>

        {/* 배송정보 */}
        <Card>
          <CardHeader>
            <CardTitle className="text-sm">배송정보</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">


            <div className="space-y-1.5">
              <Label>배송날짜 *</Label>
              <Input
                type="date"
                value={deliveryDate}
                onChange={(e) => {
                  setDeliveryDate(e.target.value)
                  if (e.target.value) setErrors((prev) => ({ ...prev, deliveryDate: undefined }))
                }}
                className={cn(errors.deliveryDate && 'border-red-500 focus-visible:ring-red-500')}
              />
              {errors.deliveryDate && <p className="text-xs text-red-500">{errors.deliveryDate}</p>}
            </div>
            <div className="space-y-1.5">
              <Label>배송시간</Label>
              <div className="flex flex-wrap items-center gap-2">
                <select
                  value={deliveryHour}
                  onChange={(e) => setDeliveryHour(e.target.value)}
                  className="rounded-md border px-2 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
                >
                  <option value="">시</option>
                  {Array.from({ length: 24 }, (_, i) => (
                    <option key={i} value={String(i)}>{i}시</option>
                  ))}
                </select>
                <select
                  value={deliveryMinute}
                  onChange={(e) => setDeliveryMinute(e.target.value)}
                  className="rounded-md border px-2 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
                >
                  {['00', '10', '20', '30', '40', '50'].map((m) => (
                    <option key={m} value={m}>{m}분</option>
                  ))}
                </select>
                <select
                  value={deliveryTimeType}
                  onChange={(e) => setDeliveryTimeType(e.target.value)}
                  className="rounded-md border px-2 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
                >
                  {['까지', '예식', '행사', '3시간내', '직접입력'].map((opt) => (
                    <option key={opt} value={opt}>{opt}</option>
                  ))}
                </select>
              </div>
              {deliveryTimeType === '직접입력' && (
                <Input
                  placeholder="직접 입력"
                  value={deliveryTimeCustom}
                  onChange={(e) => setDeliveryTimeCustom(e.target.value)}
                />
              )}
            </div>

            <div className="space-y-1.5">
              <Label>주소 *</Label>
              <Input
                placeholder="도로명 주소 검색"
                value={address}
                onChange={(e) => {
                  setAddress(e.target.value)
                  if (e.target.value.trim()) setErrors((prev) => ({ ...prev, address: undefined }))
                }}
                className={cn(errors.address && 'border-red-500 focus-visible:ring-red-500')}
              />
              {errors.address && <p className="text-xs text-red-500">{errors.address}</p>}
            </div>
            <div className="space-y-1.5">
              <Label>상세주소</Label>
              <Input placeholder="상세주소" />
            </div>
          </CardContent>
        </Card>

        {/* 행사정보 + 리본문구 (왼쪽 스택) */}
        <div className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="text-sm">행사정보</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              {/* 장례식 */}
              <div className="space-y-3">
                <p className="text-xs font-semibold text-gray-400">장례식</p>
                <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                  <div className="space-y-1.5">
                    <Label>장례식장명</Label>
                    <Input placeholder="예: 한독병원장례식장" />
                  </div>
                  <div className="space-y-1.5">
                    <Label>빈소</Label>
                    <Input placeholder="예: 무궁화실" />
                  </div>
                </div>
                <div className="space-y-1.5">
                  <Label>고인명</Label>
                  <Input placeholder="예: 고인 김순영" />
                </div>
              </div>

              <hr />

              {/* 결혼식 */}
              <div className="space-y-3">
                <p className="text-xs font-semibold text-gray-400">결혼식</p>
                <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                  <div className="space-y-1.5">
                    <Label>예식장명</Label>
                    <Input placeholder="예: 더파티움 강남" />
                  </div>
                  <div className="space-y-1.5">
                    <Label>홀</Label>
                    <Input placeholder="예: 그랜드볼룸 A홀" />
                  </div>
                </div>
                <div className="space-y-1.5">
                  <Label>신랑/신부명</Label>
                  <Input placeholder="예: 김철수 ♥ 이영희" />
                </div>
              </div>

              <hr />

              {/* 기타 */}
              <div className="space-y-3">
                <p className="text-xs font-semibold text-gray-400">기타</p>
                <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                  <div className="space-y-1.5">
                    <Label>행사명</Label>
                    <Input placeholder="예: 주식회사 효진토건 창립기념식" />
                  </div>
                  <div className="space-y-1.5">
                    <Label>장소</Label>
                    <Input placeholder="예: 강남파이낸스센터 1층" />
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* 주문자 / 받는 분 + 리본 문구 + 기타 (오른쪽 스택) */}
        <div className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="text-sm">주문자 / 받는 분</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                <div className="space-y-1.5">
                  <Label>주문자 성함 *</Label>
                  <Input placeholder="주문자 성함" />
                </div>
                <div className="space-y-1.5">
                  <Label>주문자 휴대폰 *</Label>
                  <Input placeholder="010-0000-0000" />
                </div>
              </div>
              <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
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
                <Label>문자 발송 대상</Label>
                <div className="flex flex-wrap gap-4">
                  <label className="flex items-center gap-1.5 text-sm">
                    <input type="checkbox" defaultChecked /> 주문자에게 발송
                  </label>
                  <label className="flex items-center gap-1.5 text-sm">
                    <input type="checkbox" /> 받는 분에게 발송
                  </label>
                </div>
              </div>

              <div className="space-y-2">
                <Label>문자 발송 설정</Label>
                <div className="space-y-1.5">
                  {/* 자동발송 */}
                  <label className="flex cursor-pointer items-center gap-2">
                    <input
                      type="radio"
                      name="messageSchedule"
                      checked={messageSchedule === 'immediate'}
                      onChange={() => setMessageSchedule('immediate')}
                      className="accent-green-700"
                    />
                    <span className="text-sm">자동발송</span>
                    {messageSchedule === 'immediate' && (
                      <span className="text-xs text-gray-400">배송완료 처리 즉시 발송</span>
                    )}
                  </label>
                  {/* 예약발송 */}
                  <label className="flex cursor-pointer items-center gap-2">
                    <input
                      type="radio"
                      name="messageSchedule"
                      checked={messageSchedule === 'scheduled'}
                      onChange={() => setMessageSchedule('scheduled')}
                      className="accent-green-700"
                    />
                    <span className="text-sm">예약발송</span>
                    {messageSchedule === 'scheduled' && (
                      <div className="flex items-center gap-2">
                        <select
                          value={scheduledHour}
                          onChange={(e) => setScheduledHour(e.target.value)}
                          className="rounded-md border px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
                        >
                          {Array.from({ length: 24 }, (_, i) => (
                            <option key={i} value={String(i)}>{i}시</option>
                          ))}
                        </select>
                        <select
                          value={scheduledMinute}
                          onChange={(e) => setScheduledMinute(e.target.value)}
                          className="rounded-md border px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
                        >
                          {['00', '10', '20', '30', '40', '50'].map((m) => (
                            <option key={m} value={m}>{m}분</option>
                          ))}
                        </select>
                        <span className="text-xs text-gray-400">배송날짜 기준</span>
                      </div>
                    )}
                  </label>
                </div>
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
              <CardTitle className="text-sm">기타</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <div className="space-y-1.5">
                <Label>관리자 메모</Label>
                <Input placeholder="내부 메모" />
              </div>
              <div className="space-y-1.5">
                <Label>수주업체 전달사항</Label>
                <Textarea placeholder="업체에 전달할 내용" className="min-h-[80px]" />
              </div>
            </CardContent>
          </Card>
        </div>
      </div>

      <div className="flex justify-end gap-2">
        <Button variant="outline">취소</Button>
        <Button className="bg-green-700 hover:bg-green-800" onClick={handleSubmit}>발주 등록</Button>
      </div>
    </div>
  )
}

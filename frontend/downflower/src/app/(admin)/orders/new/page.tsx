'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import { cn, RIBBON_PHRASES } from '@/lib/utils'
import { fetchVendors } from '@/lib/api/vendorApi'
import { createOrder, fetchOrderingCompanies, uploadReferencePhotos, OrderingCompany } from '@/lib/api/orderApi'
import { fetchAllProducts } from '@/lib/api/productApi'
import { Vendor } from '@/types/vendor'
import { Product } from '@/types/product'

function formatPhone(value: string): string {
  const digits = value.replace(/\D/g, '').slice(0, 11)
  if (digits.length <= 3) return digits
  if (digits.length <= 7) return `${digits.slice(0, 3)}-${digits.slice(3)}`
  return `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7)}`
}

export default function NewOrderPage() {
  const router = useRouter()

  // 기본정보
  const [vendorId, setVendorId] = useState('')
  const [productName, setProductName] = useState('')
  const [quantity, setQuantity] = useState(1)
  const [amount, setAmount] = useState('')
  const [extraCost, setExtraCost] = useState('')

  // 배송정보
  const [deliveryDate, setDeliveryDate] = useState('')
  const [deliveryHour, setDeliveryHour] = useState('')
  const [deliveryMinute, setDeliveryMinute] = useState('00')
  const [deliveryTimeType, setDeliveryTimeType] = useState('까지')
  const [deliveryTimeCustom, setDeliveryTimeCustom] = useState('')
  const [address, setAddress] = useState('')
  const [addressDetail, setAddressDetail] = useState('')

  // 행사정보
  const [selectedEventType, setSelectedEventType] = useState<'장례식' | '결혼식' | '기타'>('장례식')
  const [venueName, setVenueName] = useState('')

  // 주문자 / 받는 분
  const [ordererName, setOrdererName] = useState('')
  const [ordererPhone, setOrdererPhone] = useState('')
  const [recipientName, setRecipientName] = useState('')
  const [recipientPhone, setRecipientPhone] = useState('')
  const [sendPhotoToOrderer, setSendPhotoToOrderer] = useState(true)
  const [sendPhotoToRecipient, setSendPhotoToRecipient] = useState(false)
  const [messageSchedule, setMessageSchedule] = useState<'immediate' | 'scheduled'>('immediate')
  const [scheduledHour, setScheduledHour] = useState('9')
  const [scheduledMinute, setScheduledMinute] = useState('00')

  // 리본 / 메모
  const [ribbonLeft, setRibbonLeft] = useState('')
  const [ribbonRight, setRibbonRight] = useState('')
  const [adminMemo, setAdminMemo] = useState('')
  const [vendorMemo, setVendorMemo] = useState('')
  const [referenceItems, setReferenceItems] = useState<{ file: File; url: string }[]>([])

  useEffect(() => {
    return () => referenceItems.forEach((item) => URL.revokeObjectURL(item.url))
  }, [])

  // 업체 목록
  const [vendors, setVendors] = useState<Vendor[]>([])
  // 발주업체 (SUPER_ADMIN 전용)
  const [isSuperAdmin, setIsSuperAdmin] = useState(false)
  const [orderingCompanies, setOrderingCompanies] = useState<OrderingCompany[]>([])
  const [orderingCompanyId, setOrderingCompanyId] = useState('')
  // 상품 목록
  const [products, setProducts] = useState<Product[]>([])
  const [selectedProductId, setSelectedProductId] = useState('')

  // 제출 상태
  const [errors, setErrors] = useState<{
    vendorId?: string
    productName?: string
    deliveryDate?: string
    deliveryHour?: string
    amount?: string
    recipientName?: string
    ordererPhone?: string
  }>({})
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState('')

  useEffect(() => {
    fetchVendors('ACTIVE').then((list) => setVendors(list.filter((v) => v.companyType !== 'ORDERING'))).catch(() => {})
    fetchAllProducts().then(setProducts).catch(() => {})
    const role = typeof window !== 'undefined' ? localStorage.getItem('role') : null
    if (role === 'SUPER_ADMIN') {
      setIsSuperAdmin(true)
      fetchOrderingCompanies().then(setOrderingCompanies).catch(() => {})
    }
  }, [])

  async function handleSubmit() {
    const next: typeof errors = {}
    if (!vendorId) next.vendorId = '수주업체를 선택해주세요.'
    if (!productName.trim()) next.productName = '상품명을 입력해주세요.'
    if (!deliveryDate) next.deliveryDate = '배송날짜를 입력해주세요.'
    if (!deliveryHour && deliveryTimeType !== '3시간내' && deliveryTimeType !== '직접입력') next.deliveryHour = '배송시간을 선택해주세요.'
    if (!amount) next.amount = '발주금액을 입력해주세요.'
    if (!recipientName.trim()) next.recipientName = '받는 분 성함을 입력해주세요.'
    if (!ordererPhone.trim()) next.ordererPhone = '주문자 휴대폰을 입력해주세요.'
    setErrors(next)
    if (Object.keys(next).length > 0) return

    let eventType = selectedEventType
    let placeName = venueName
    let roomName = ''
    let eventInfo = ''

    setSubmitting(true)
    setSubmitError('')
    try {
      const created = await createOrder({
        vendorId: Number(vendorId),
        ...(isSuperAdmin && orderingCompanyId ? { orderingCompanyId: Number(orderingCompanyId) } : {}),
        ...(selectedProductId && selectedProductId !== '__direct__' ? { productId: Number(selectedProductId) } : {}),
        productName,
        quantity,
        amount: Number(amount),
        extraCost: extraCost ? Number(extraCost) : undefined,
        deliveryDate,
        deliveryTimeType,
        deliveryTimeHour: ['3시간내', '직접입력'].includes(deliveryTimeType) ? '' : deliveryHour,
        deliveryTimeMinute: ['3시간내', '직접입력'].includes(deliveryTimeType) ? '' : deliveryMinute,
        deliveryTimeCustom,
        address,
        addressDetail,
        eventType,
        placeName,
        roomName,
        eventInfo,
        ribbonLeft,
        ribbonRight,
        ordererName,
        ordererPhone,
        recipientName,
        recipientPhone,
        sendPhotoToOrderer,
        sendPhotoToRecipient,
        messageSchedule,
        scheduledMessageHour: scheduledHour,
        scheduledMessageMinute: scheduledMinute,
        adminMemo,
        vendorMemo,
      })
      if (referenceItems.length > 0) {
        await uploadReferencePhotos(String(created.id), referenceItems.map((i) => i.file)).catch(() => {})
      }
      router.push('/orders')
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : '발주 등록에 실패했습니다.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">신규 발주</h1>

      <div className="grid gap-4 lg:grid-cols-2 lg:items-start">
        <div className="space-y-4">
        {/* 기본정보 */}
        <Card>
          <CardHeader>
            <CardTitle className="text-sm">기본정보</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="space-y-1.5">
              <Label>주문번호</Label>
              <Input value="자동 생성" disabled className="bg-gray-50" />
            </div>
            {isSuperAdmin && (
              <div className="space-y-1.5">
                <Label>발주업체</Label>
                <select
                  value={orderingCompanyId}
                  onChange={(e) => setOrderingCompanyId(e.target.value)}
                  className="w-full rounded-md border bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
                >
                  <option value="">선택 안함</option>
                  {orderingCompanies.map((c) => (
                    <option key={c.id} value={String(c.id)}>{c.companyName}</option>
                  ))}
                </select>
              </div>
            )}
            <div className="space-y-1.5">
              <Label>수주업체 *</Label>
              <select
                value={vendorId}
                onChange={(e) => { setVendorId(e.target.value); setErrors((prev) => ({ ...prev, vendorId: undefined })) }}
                className={cn(
                  'w-full rounded-md border bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-700',
                  errors.vendorId && 'border-red-500',
                )}
              >
                <option value="">업체 선택</option>
                {vendors.map((v) => (
                  <option key={v.id} value={v.id}>{v.name}</option>
                ))}
              </select>
              {errors.vendorId && <p className="text-xs text-red-500">{errors.vendorId}</p>}
            </div>
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
              <div className="space-y-1.5">
                <Label>상품명 *</Label>
                <select
                  value={selectedProductId}
                  onChange={(e) => {
                    const id = e.target.value
                    setSelectedProductId(id)
                    if (id === '__direct__') {
                      setProductName('')
                      setAmount('')
                    } else if (id === '') {
                      setProductName('')
                    } else {
                      const found = products.find((p) => p.id === id)
                      if (found) {
                        setProductName(found.name)
                        setAmount(String(found.price))
                      }
                    }
                    setErrors((prev) => ({ ...prev, productName: undefined }))
                  }}
                  className={cn(
                    'w-full rounded-md border bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-700',
                    errors.productName && 'border-red-500',
                  )}
                >
                  <option value="">상품 선택</option>
                  {products.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.name} — {p.price.toLocaleString()}원
                    </option>
                  ))}
                  <option value="__direct__">직접 입력</option>
                </select>
                {selectedProductId === '__direct__' && (
                  <Input
                    placeholder="예: 근조화환 3단"
                    value={productName}
                    onChange={(e) => { setProductName(e.target.value); setErrors((prev) => ({ ...prev, productName: undefined })) }}
                    className={cn(errors.productName && 'border-red-500 focus-visible:ring-red-500')}
                  />
                )}
                {errors.productName && <p className="text-xs text-red-500">{errors.productName}</p>}
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
                onChange={(e) => { setAmount(e.target.value); setErrors((prev) => ({ ...prev, amount: undefined })) }}
                className={cn(errors.amount && 'border-red-500 focus-visible:ring-red-500')}
              />
              {errors.amount && <p className="text-xs text-red-500">{errors.amount}</p>}
              {amount && quantity > 1 && (
                <p className="text-sm font-medium text-green-700">
                  총 발주금액: {(Number(amount) * quantity).toLocaleString()}원
                  <span className="ml-1.5 text-xs font-normal text-gray-400">({Number(amount).toLocaleString()} × {quantity})</span>
                </p>
              )}
            </div>
            <div className="space-y-1.5">
              <Label>추가비용</Label>
              <Input
                type="number"
                placeholder="0"
                value={extraCost}
                onChange={(e) => setExtraCost(e.target.value)}
              />
              {extraCost && amount ? (
                <p className="text-xs text-gray-500">
                  최종 발주금액: {(Number(amount) + Number(extraCost)).toLocaleString()}원
                  <span className="ml-1 text-gray-400">({Number(amount).toLocaleString()} + {Number(extraCost).toLocaleString()})</span>
                </p>
              ) : (
                <p className="text-xs text-gray-400">기본 발주금액 외 추가로 발생한 비용</p>
              )}
            </div>
          </CardContent>
        </Card>

        {/* 행사정보 */}
          <Card>
            <CardHeader>
              <CardTitle className="text-sm">행사정보</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              {/* 유형 선택 */}
              <div className="flex flex-wrap gap-1.5">
                {(['장례식', '결혼식', '기타'] as const).map((type) => (
                  <button
                    key={type}
                    type="button"
                    onClick={() => { setSelectedEventType(type); setVenueName('') }}
                    className={cn(
                      'rounded-md border px-3 py-1.5 text-sm font-medium transition-colors',
                      selectedEventType === type
                        ? 'border-green-700 bg-green-700 text-white'
                        : 'border-gray-300 text-gray-600 hover:border-gray-400 hover:bg-gray-50',
                    )}
                  >
                    {type}
                  </button>
                ))}
              </div>

              <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                <div className="space-y-1.5">
                  <Label>
                    {selectedEventType === '장례식' ? '장례식장' : selectedEventType === '결혼식' ? '예식장' : '장소'}
                  </Label>
                  <Input
                    placeholder={
                      selectedEventType === '장례식' ? '예: 한독병원장례식장' :
                      selectedEventType === '결혼식' ? '예: 더파티움 강남' :
                      '예: 강남파이낸스센터 1층'
                    }
                    value={venueName}
                    onChange={(e) => setVenueName(e.target.value)}
                  />
                </div>
                <div className="space-y-1.5">
                  <Label>
                    {selectedEventType === '장례식' ? '받는 분 (예: 상주 홍길동)' :
                     selectedEventType === '결혼식' ? '받는 분 (예: 신랑 홍길동)' :
                     '받는 분'}
                  </Label>
                  <Input
                    placeholder={
                      selectedEventType === '장례식' ? '예: 상주 홍길동' :
                      selectedEventType === '결혼식' ? '예: 신랑 홍길동' :
                      '예: 홍길동'
                    }
                    value={recipientName}
                    onChange={(e) => { setRecipientName(e.target.value); setErrors((prev) => ({ ...prev, recipientName: undefined })) }}
                    className={cn(errors.recipientName && 'border-red-500 focus-visible:ring-red-500')}
                  />
                  {errors.recipientName && <p className="text-xs text-red-500">{errors.recipientName}</p>}
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
                <Label>보내는이</Label>
                <Input
                  placeholder="예: 주식회사 OO 부장 홍길동"
                  value={ribbonLeft}
                  onChange={(e) => setRibbonLeft(e.target.value)}
                />
              </div>
              <div className="space-y-1.5">
                <Label>경조문구</Label>
                <Input
                  placeholder="예: 삼가 고인의 명복을 빕니다"
                  value={ribbonRight}
                  onChange={(e) => setRibbonRight(e.target.value)}
                />
              </div>
              <div className="space-y-1.5">
                <p className="text-xs text-gray-400">경조문구 선택 (클릭하면 위 입력란에 자동 입력)</p>
                <div className="flex flex-wrap gap-1.5">
                  {RIBBON_PHRASES.map((phrase) => (
                    <button
                      key={phrase}
                      type="button"
                      onClick={() => setRibbonRight(phrase)}
                      className="rounded border border-gray-200 bg-gray-50 px-2 py-1 text-xs text-gray-700 hover:border-green-600 hover:bg-green-50 hover:text-green-700"
                    >
                      {phrase}
                    </button>
                  ))}
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        <div className="space-y-4">
        {/* 배송정보 */}
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm">배송정보</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2 pt-2 pb-3">
            <div className="space-y-1">
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
            <div className="space-y-1">
              <Label>{['3시간내', '직접입력'].includes(deliveryTimeType) ? '배송시간' : '배송시간 *'}</Label>
              <div className="flex flex-wrap items-center gap-2">
                {!['3시간내', '직접입력'].includes(deliveryTimeType) && (
                  <>
                    <select
                      value={deliveryHour}
                      onChange={(e) => {
                        setDeliveryHour(e.target.value)
                        if (e.target.value) setErrors((prev) => ({ ...prev, deliveryHour: undefined }))
                      }}
                      className={cn('rounded-md border px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-green-700', errors.deliveryHour && 'border-red-500')}
                    >
                      <option value="">시</option>
                      {Array.from({ length: 24 }, (_, i) => (
                        <option key={i} value={String(i)}>{i}시</option>
                      ))}
                    </select>
                    <select
                      value={deliveryMinute}
                      onChange={(e) => setDeliveryMinute(e.target.value)}
                      className="rounded-md border px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
                    >
                      {['00', '10', '20', '30', '40', '50'].map((m) => (
                        <option key={m} value={m}>{m}분</option>
                      ))}
                    </select>
                  </>
                )}
                <select
                  value={deliveryTimeType}
                  onChange={(e) => setDeliveryTimeType(e.target.value)}
                  className="rounded-md border px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
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
              {errors.deliveryHour && <p className="text-xs text-red-500">{errors.deliveryHour}</p>}
            </div>

            <div className="space-y-1">
              <Label>주소</Label>
              <Input
                placeholder="도로명 주소 검색"
                value={address}
                onChange={(e) => setAddress(e.target.value)}
              />
            </div>
            <div className="space-y-1">
              <Label>상세주소</Label>
              <Input
                placeholder="상세주소"
                value={addressDetail}
                onChange={(e) => setAddressDetail(e.target.value)}
              />
            </div>
          </CardContent>
        </Card>

        {/* 주문자 / 받는 분 + 기타 */}
          <Card>
            <CardHeader>
              <CardTitle className="text-sm">주문자 / 받는 분</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                <div className="space-y-1.5">
                  <Label>주문자 성함</Label>
                  <Input
                    placeholder="주문자 성함"
                    value={ordererName}
                    onChange={(e) => setOrdererName(e.target.value)}
                  />
                </div>
                <div className="space-y-1.5">
                  <Label>주문자 휴대폰 *</Label>
                  <Input
                    placeholder="010-0000-0000"
                    value={ordererPhone}
                    onChange={(e) => { setOrdererPhone(formatPhone(e.target.value)); setErrors((prev) => ({ ...prev, ordererPhone: undefined })) }}
                    className={cn(errors.ordererPhone && 'border-red-500 focus-visible:ring-red-500')}
                  />
                  {errors.ordererPhone && <p className="text-xs text-red-500">{errors.ordererPhone}</p>}
                </div>
              </div>
              <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                <div className="space-y-1.5">
                  <Label>받는 분 성함</Label>
                  <Input
                    placeholder="받는 분 성함"
                    value={recipientName}
                    onChange={(e) => setRecipientName(e.target.value)}
                  />
                </div>
                <div className="space-y-1.5">
                  <Label>받는 분 휴대폰</Label>
                  <Input
                    placeholder="010-0000-0000"
                    value={recipientPhone}
                    onChange={(e) => setRecipientPhone(formatPhone(e.target.value))}
                  />
                </div>
              </div>
              <div className="space-y-1.5">
                <Label>문자 발송 대상</Label>
                <div className="flex flex-wrap gap-4">
                  <label className="flex items-center gap-1.5 text-sm">
                    <input
                      type="checkbox"
                      checked={sendPhotoToOrderer}
                      onChange={(e) => setSendPhotoToOrderer(e.target.checked)}
                    /> 주문자에게 발송
                  </label>
                  <label className="flex items-center gap-1.5 text-sm">
                    <input
                      type="checkbox"
                      checked={sendPhotoToRecipient}
                      onChange={(e) => setSendPhotoToRecipient(e.target.checked)}
                    /> 받는 분에게 발송
                  </label>
                </div>
              </div>

              <div className="space-y-2">
                <Label>문자 발송 설정</Label>
                <div className="space-y-1.5">
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
              <CardTitle className="text-sm">기타</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <div className="space-y-1.5">
                <Label>관리자 메모</Label>
                <Input
                  placeholder="내부 메모"
                  value={adminMemo}
                  onChange={(e) => setAdminMemo(e.target.value)}
                />
              </div>
              <div className="space-y-1.5">
                <Label>수주업체 전달사항</Label>
                <Textarea
                  placeholder="업체에 전달할 내용"
                  className="min-h-[80px]"
                  value={vendorMemo}
                  onChange={(e) => setVendorMemo(e.target.value)}
                />
              </div>
              <div className="space-y-1.5">
                <Label>참고 이미지</Label>
                <label className="flex cursor-pointer items-center gap-2 rounded-md border border-dashed border-gray-300 px-3 py-2 text-sm text-gray-500 hover:border-gray-400 hover:bg-gray-50">
                  <input
                    type="file"
                    accept="image/*"
                    multiple
                    className="hidden"
                    onChange={(e) => {
                      const added = Array.from(e.target.files ?? []).map((f) => ({ file: f, url: URL.createObjectURL(f) }))
                      setReferenceItems((prev) => [...prev, ...added])
                      e.target.value = ''
                    }}
                  />
                  + 이미지 추가
                </label>
                {referenceItems.length > 0 && (
                  <div className="flex flex-wrap gap-2 pt-1">
                    {referenceItems.map((item, idx) => (
                      <div key={idx} className="relative h-16 w-16 overflow-hidden rounded border border-gray-200">
                        <img src={item.url} alt={item.file.name} className="h-full w-full object-cover" />
                        <button
                          type="button"
                          onClick={() => setReferenceItems((prev) => {
                            URL.revokeObjectURL(prev[idx].url)
                            return prev.filter((_, i) => i !== idx)
                          })}
                          className="absolute right-0.5 top-0.5 rounded-full bg-black/60 p-0.5 text-white hover:bg-black/80"
                        >
                          <svg className="h-2.5 w-2.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3"><path d="M18 6L6 18M6 6l12 12"/></svg>
                        </button>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        </div>
      </div>

      {submitError && (
        <p className="text-sm text-red-500">{submitError}</p>
      )}

      <div className="flex justify-end gap-2">
        <Button variant="outline" onClick={() => router.push('/orders')}>취소</Button>
        <Button
          className="bg-green-700 hover:bg-green-800"
          onClick={handleSubmit}
          disabled={submitting}
        >
          {submitting ? '등록 중...' : '발주 등록'}
        </Button>
      </div>
    </div>
  )
}

'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import { cn, RIBBON_PHRASES, formatPhone } from '@/lib/utils'
import { createOrder, fetchOrderingCompanies, uploadReferencePhotos, OrderingCompany } from '@/lib/api/orderApi'
import { fetchAdminPointList, fetchMyPoint } from '@/lib/api/pointApi'
import VendorSelectModal from '@/components/common/VendorSelectModal'
import { fetchCoupangOrder, approveCoupangOrder, CoupangOrder } from '@/lib/api/coupangApi'
import { fetchShopOrder, approveShopOrder, type ShopOrder } from '@/lib/api/shopApi'


export default function NewOrderPage() {
  const router = useRouter()

  // 기본정보
  const [vendorId, setVendorId] = useState('')
  const [selectedVendorName, setSelectedVendorName] = useState('')
  const [selectedProductId, setSelectedProductId] = useState('')
  const [vendorModalOpen, setVendorModalOpen] = useState(false)
  const [productName, setProductName] = useState('')
  const [quantity, setQuantity] = useState('1')
  const [unitPrice, setUnitPrice] = useState(0)
  const [amount, setAmount] = useState('')
  const [vendorAmount, setVendorAmount] = useState('')
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
  const [ribbonLefts, setRibbonLefts] = useState<string[]>([''])
  const [ribbonRights, setRibbonRights] = useState<string[]>([''])
  const [adminMemo, setAdminMemo] = useState('')
  const [vendorMemo, setVendorMemo] = useState('')
  const [referenceItems, setReferenceItems] = useState<{ file: File; url: string }[]>([])

  useEffect(() => {
    return () => referenceItems.forEach((item) => URL.revokeObjectURL(item.url))
  }, [])

  // 발주업체 (SUPER_ADMIN 전용)
  const [isSuperAdmin, setIsSuperAdmin] = useState(false)
  const [roleChecked, setRoleChecked] = useState(false)
  const [orderingCompanies, setOrderingCompanies] = useState<OrderingCompany[]>([])
  const [orderingCompanyId, setOrderingCompanyId] = useState('')

  // 제출 상태
  const [errors, setErrors] = useState<{
    vendorId?: string
    orderingCompanyId?: string
    productName?: string
    deliveryDate?: string
    deliveryHour?: string
    amount?: string
    venueName?: string
    recipientName?: string
    ribbonLeft?: string
    ordererPhone?: string
    messageSchedule?: string
  }>({})
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState('')
  const [availablePoint, setAvailablePoint] = useState<number | null>(null)
  const requiredPoint = (Number(amount) || 0) + (Number(extraCost) || 0)
  const pointInsufficient = availablePoint !== null && requiredPoint > availablePoint

  useEffect(() => {
    if (!roleChecked) return
    if (isSuperAdmin) {
      if (!orderingCompanyId) { setAvailablePoint(null); return }
      fetchAdminPointList()
        .then((list) => setAvailablePoint(list.find((c) => String(c.companyId) === orderingCompanyId)?.balance ?? 0))
        .catch(() => setAvailablePoint(null))
    } else {
      fetchMyPoint().then((p) => setAvailablePoint(p.balance)).catch(() => setAvailablePoint(null))
    }
  }, [roleChecked, isSuperAdmin, orderingCompanyId])

  useEffect(() => {
    const role = typeof window !== 'undefined' ? localStorage.getItem('role') : null
    if (role === 'SUPER_ADMIN') {
      setIsSuperAdmin(true)
      fetchOrderingCompanies().then(setOrderingCompanies).catch(() => {})
    }
    setRoleChecked(true)
  }, [])

  // 쿠팡 주문 승인 모드 (?coupangOrderId=)
  const [coupangOrder, setCoupangOrder] = useState<CoupangOrder | null>(null)
  const [coupangLoadError, setCoupangLoadError] = useState('')
  const [coupangOrderId, setCoupangOrderId] = useState<string | null>(null)

  useEffect(() => {
    setCoupangOrderId(new URLSearchParams(window.location.search).get('coupangOrderId'))
  }, [])

  useEffect(() => {
    if (!coupangOrderId) return
    fetchCoupangOrder(Number(coupangOrderId))
      .then((o) => {
        setCoupangOrder(o)
        setOrderingCompanyId(String(o.companyId))
        setOrdererName(o.ordererName ?? '')
        setOrdererPhone(formatPhone(o.ordererPhone ?? ''))
        setRecipientName(o.receiverName ?? '')
        setRecipientPhone(formatPhone(o.receiverPhone ?? ''))
        setAddress(o.receiverAddress ?? '')
        setAddressDetail(o.receiverAddressDetail ?? '')
        if (o.deliveryMessage) setAdminMemo(`[쿠팡 배송메시지] ${o.deliveryMessage}`)
        setSendPhotoToOrderer(false)
        setSendPhotoToRecipient(false)
      })
      .catch((e) => setCoupangLoadError(e instanceof Error ? e.message : '쿠팡 주문을 불러오지 못했습니다.'))
  }, [coupangOrderId])

  // 쇼핑몰 주문 승인 모드 (?shopOrderId=)
  const [shopOrder, setShopOrder] = useState<ShopOrder | null>(null)
  const [shopLoadError, setShopLoadError] = useState('')
  const [shopOrderId, setShopOrderId] = useState<string | null>(null)

  useEffect(() => {
    setShopOrderId(new URLSearchParams(window.location.search).get('shopOrderId'))
  }, [])

  useEffect(() => {
    if (!shopOrderId) return
    fetchShopOrder(Number(shopOrderId))
      .then((o) => {
        setShopOrder(o)
        setOrderingCompanyId(String(o.companyId))
        setOrdererName(o.ordererName ?? '')
        setOrdererPhone(formatPhone(o.ordererPhone ?? ''))
        setRecipientName(o.recipientName ?? '')
        setRecipientPhone(formatPhone(o.recipientPhone ?? ''))
        setAddress(o.address ?? '')
        setAddressDetail(o.addressDetail ?? '')
        if (o.deliveryDate) setDeliveryDate(o.deliveryDate)
        if (o.deliveryTime) setDeliveryTimeCustom(o.deliveryTime)
        if (o.eventType === '장례식' || o.eventType === '결혼식' || o.eventType === '기타') setSelectedEventType(o.eventType)
        if (o.placeName) setVenueName(o.placeName)
        if (o.ribbonLeft) setRibbonLefts([o.ribbonLeft])
        if (o.ribbonRight) setRibbonRights([o.ribbonRight])
        if (o.memo) setAdminMemo(`[쇼핑몰 요청사항] ${o.memo}`)
        setSendPhotoToRecipient(false)
      })
      .catch((e) => setShopLoadError(e instanceof Error ? e.message : '쇼핑몰 주문을 불러오지 못했습니다.'))
  }, [shopOrderId])

  async function handleSubmit() {
    const next: typeof errors = {}
    if (!vendorId) next.vendorId = '수주업체와 상품을 선택해주세요.'
    if (isSuperAdmin && !orderingCompanyId) next.orderingCompanyId = '발주업체를 선택해주세요.'
    if (!deliveryDate) next.deliveryDate = '배송날짜를 입력해주세요.'
    if (!deliveryHour && deliveryTimeType !== '3시간이내' && deliveryTimeType !== '직접입력') next.deliveryHour = '배송시간을 선택해주세요.'
    if (!venueName.trim()) next.venueName = '장소를 입력해주세요.'
    if (!recipientName.trim()) next.recipientName = '받는 분 성함을 입력해주세요.'
    if (!(ribbonLefts[0] ?? '').trim()) next.ribbonLeft = '보내는이를 입력해주세요.'
    if (!ordererPhone.trim()) next.ordererPhone = '주문자 휴대폰을 입력해주세요.'
    if (selectedEventType === '결혼식' && messageSchedule !== 'scheduled') next.messageSchedule = '결혼식 주문은 사진 예약발송 시각을 반드시 설정해주세요.'
    setErrors(next)
    if (Object.keys(next).length > 0) return

    let eventType = selectedEventType
    let placeName = venueName
    let roomName = ''
    let eventInfo = ''

    setSubmitting(true)
    setSubmitError('')
    try {
      const payload: Parameters<typeof createOrder>[0] = {
        vendorId: Number(vendorId),
        ...(isSuperAdmin && orderingCompanyId ? { orderingCompanyId: Number(orderingCompanyId) } : {}),
        ...(selectedProductId ? { productId: Number(selectedProductId) } : {}),
        productName,
        quantity: Number(quantity) || 1,
        amount: Number(amount),
        vendorAmount: vendorAmount ? Number(vendorAmount) : undefined,
        extraCost: extraCost ? Number(extraCost) : undefined,
        deliveryDate,
        deliveryTimeType,
        deliveryTimeHour: ['3시간이내', '직접입력'].includes(deliveryTimeType) ? '' : deliveryHour,
        deliveryTimeMinute: ['3시간이내', '직접입력'].includes(deliveryTimeType) ? '' : deliveryMinute,
        deliveryTimeCustom,
        address,
        addressDetail,
        eventType,
        placeName,
        roomName,
        eventInfo,
        ribbonLeft: ribbonLefts[0] ?? '',
        ribbonRight: ribbonRights[0] ?? '',
        ribbonExtras: (() => {
          const maxLen = Math.max(ribbonLefts.length, ribbonRights.length)
          if (maxLen <= 1) return undefined
          const extras = Array.from({ length: maxLen - 1 }, (_, i) => ({
            left: ribbonLefts[i + 1] ?? '',
            right: ribbonRights[i + 1] ?? '',
          }))
          return JSON.stringify(extras)
        })(),
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
      }
      const created = coupangOrder
        ? await approveCoupangOrder(coupangOrder.id, payload)
        : shopOrder
          ? await approveShopOrder(shopOrder.id, payload)
          : await createOrder(payload)
      if (referenceItems.length > 0) {
        await uploadReferencePhotos(String(created.id), referenceItems.map((i) => i.file)).catch(() => {})
      }
      router.push(coupangOrder ? '/coupang/orders' : shopOrder ? '/shop/orders' : '/orders')
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : '발주 등록에 실패했습니다.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">{coupangOrderId ? '쿠팡 주문 승인 발주' : shopOrderId ? '쇼핑몰 주문 승인 발주' : '신규 발주'}</h1>

      {coupangLoadError && (
        <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-600">{coupangLoadError}</div>
      )}
      {coupangOrder && (
        <Card className="border-orange-200 bg-orange-50">
          <CardContent className="space-y-1 py-3 text-sm">
            <div className="flex flex-wrap items-center justify-between gap-1">
              <span className="font-semibold text-orange-800">쿠팡 주문 #{coupangOrder.orderId}</span>
              <span className="text-xs text-gray-500">쿠팡 상태: {coupangOrder.coupangStatus ?? '-'}</span>
            </div>
            {coupangOrder.items.map((it, i) => (
              <p key={i} className="text-gray-700">{it.name} × {it.quantity} ({it.salesPrice.toLocaleString()}원)</p>
            ))}
            <p className="font-medium text-gray-800">쿠팡 결제 합계 {coupangOrder.totalPrice.toLocaleString()}원</p>
            <p className="text-xs text-gray-500">
              수주업체·상품, 배송시간, 리본 문구를 입력한 뒤 승인하면 발주가 생성됩니다. 주문자/받는 분 번호는 쿠팡 안심번호일 수 있어
              문자 발송 대상은 기본 해제되어 있습니다.
            </p>
          </CardContent>
        </Card>
      )}

      {shopLoadError && (
        <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-600">{shopLoadError}</div>
      )}
      {shopOrder && (
        <Card className="border-green-200 bg-green-50">
          <CardContent className="space-y-1 py-3 text-sm">
            <div className="flex flex-wrap items-center justify-between gap-1">
              <span className="font-semibold text-green-800">쇼핑몰 주문 {shopOrder.orderNo}</span>
              <span className="text-xs text-gray-500">고객 희망 {shopOrder.deliveryDate ?? '-'} {shopOrder.deliveryTime ?? ''}</span>
            </div>
            {shopOrder.items.map((it, i) => (
              <p key={i} className="text-gray-700">{it.name} × {it.quantity} ({it.unitPrice.toLocaleString()}원)</p>
            ))}
            <p className="font-medium text-gray-800">고객 결제 예정 합계 {shopOrder.totalPrice.toLocaleString()}원</p>
            <p className="text-xs text-gray-500">
              수주업체·상품과 배송시간을 확인한 뒤 승인하면 발주가 생성됩니다. 발주 금액은 수주업체 상품 단가로 계산되며 판매가와 무관합니다.
            </p>
          </CardContent>
        </Card>
      )}

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
                <Label>발주업체 *</Label>
                <select
                  value={orderingCompanyId}
                  onChange={(e) => setOrderingCompanyId(e.target.value)}
                  disabled={!!coupangOrderId || !!shopOrderId}
                  className={`w-full rounded-md border bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-700 ${errors.orderingCompanyId ? 'border-red-500' : ''}`}
                >
                  <option value="">선택하세요</option>
                  {orderingCompanies.map((c) => (
                    <option key={c.id} value={String(c.id)}>{c.companyName}</option>
                  ))}
                </select>
                {errors.orderingCompanyId && <p className="text-xs text-red-500">{errors.orderingCompanyId}</p>}
              </div>
            )}
            <div className="space-y-1.5">
              <Label>수주업체 · 상품 *</Label>
              <button
                type="button"
                onClick={() => setVendorModalOpen(true)}
                className={cn(
                  'w-full rounded-md border bg-white px-3 py-2 text-left text-sm focus:outline-none focus:ring-2 focus:ring-green-700',
                  errors.vendorId && 'border-red-500',
                  !vendorId && 'text-gray-400',
                )}
              >
                {vendorId ? (
                  <span>
                    <span className="font-medium">{selectedVendorName}</span>
                    <span className="mx-1.5 text-gray-300">|</span>
                    {productName} · {unitPrice.toLocaleString()}원
                  </span>
                ) : (
                  '수주업체·상품 선택'
                )}
              </button>
              {errors.vendorId && <p className="text-xs text-red-500">{errors.vendorId}</p>}
            </div>
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
              <div className="space-y-1.5">
                <Label>수량 *</Label>
                <Input
                  type="text"
                  inputMode="numeric"
                  pattern="[0-9]*"
                  value={quantity}
                  onChange={(e) => {
                    const val = e.target.value.replace(/[^0-9]/g, '').replace(/^0+([1-9])/, '$1')
                    setQuantity(val)
                    if (unitPrice > 0) setAmount(String(unitPrice * (Number(val) || 1)))
                  }}
                  onBlur={() => {
                    if (!quantity || Number(quantity) < 1) setQuantity('1')
                  }}
                />
              </div>
            </div>
            <div className="space-y-1.5">
              <Label>상품금액</Label>
              <Input
                type="text"
                readOnly
                tabIndex={-1}
                placeholder="수주업체·상품을 선택하면 자동 계산됩니다"
                value={amount ? Number(amount).toLocaleString() : ''}
                className="bg-gray-50"
              />
              {unitPrice > 0 && (
                <p className="text-xs text-gray-400">
                  단가 {unitPrice.toLocaleString()}원 × {Number(quantity) || 1}개
                </p>
              )}
            </div>
            <div className="space-y-1.5">
              <Label>추가비용</Label>
              <div className="flex items-center gap-1.5">
                <Input
                  type="number"
                  placeholder="0"
                  value={extraCost}
                  onChange={(e) => setExtraCost(e.target.value)}
                  className="flex-1"
                />
                {[10000, 20000].map((amt) => (
                  <button
                    key={amt}
                    type="button"
                    onClick={() => setExtraCost(String(amt))}
                    className="shrink-0 rounded-md border px-2.5 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-50"
                  >
                    {amt.toLocaleString()}원
                  </button>
                ))}
              </div>
              {extraCost && amount ? (
                <p className="text-xs text-gray-500">
                  최종 발주금액: {(Number(amount) + Number(extraCost)).toLocaleString()}원
                  <span className="ml-1 text-gray-400">({Number(amount).toLocaleString()} + {Number(extraCost).toLocaleString()})</span>
                </p>
              ) : (
                <p className="text-xs text-gray-400">기본 발주금액 외 추가로 발생한 비용</p>
              )}
            </div>
            <div className="space-y-1.5">
              <Label>정산금액</Label>
              <Input
                type="number"
                placeholder="0"
                value={vendorAmount}
                onChange={(e) => setVendorAmount(e.target.value)}
              />
              <p className="text-xs text-gray-400">수주업체에 지급할 금액 (미입력 시 발주금액 기준)</p>
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

              <div className="grid grid-cols-1 gap-3 sm:grid-cols-[2fr_1fr]">
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
                    onChange={(e) => { setVenueName(e.target.value); setErrors((prev) => ({ ...prev, venueName: undefined })) }}
                    className={cn(errors.venueName && 'border-red-500 focus-visible:ring-red-500')}
                  />
                  {errors.venueName && <p className="text-xs text-red-500">{errors.venueName}</p>}
                </div>
                <div className="space-y-1.5">
                  <Label>받는 분</Label>
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
            <CardContent className="space-y-5">
              {/* 경조문구 */}
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <Label>경조문구</Label>
                  <div className="flex gap-1.5">
                    <button
                      type="button"
                      onClick={() => setRibbonRights((prev) => [...prev, ''])}
                      className="rounded border border-gray-300 px-2 py-0.5 text-xs text-gray-600 hover:bg-gray-50"
                    >
                      추가
                    </button>
                    {ribbonRights.length > 1 && (
                      <button
                        type="button"
                        onClick={() => setRibbonRights((prev) => prev.slice(0, -1))}
                        className="rounded border border-gray-300 px-2 py-0.5 text-xs text-gray-600 hover:bg-gray-50"
                      >
                        삭제
                      </button>
                    )}
                  </div>
                </div>
                {ribbonRights.map((right, idx) => (
                  <div key={idx} className="space-y-1.5">
                    {ribbonRights.length > 1 && (
                      <p className="text-xs font-medium text-gray-400">{idx + 1}번</p>
                    )}
                    <Input
                      placeholder="예: 삼가 고인의 명복을 빕니다"
                      value={right}
                      onChange={(e) => {
                        const val = e.target.value
                        setRibbonRights((prev) => prev.map((r, i) => i === idx ? val : r))
                      }}
                    />
                    <div className="flex flex-wrap gap-1.5">
                      {RIBBON_PHRASES.map((phrase) => (
                        <button
                          key={phrase}
                          type="button"
                          onClick={() => setRibbonRights((prev) => prev.map((r, i) => i === idx ? phrase : r))}
                          className="rounded border border-gray-200 bg-gray-50 px-2 py-1 text-xs text-gray-700 hover:border-green-600 hover:bg-green-50 hover:text-green-700"
                        >
                          {phrase}
                        </button>
                      ))}
                    </div>
                  </div>
                ))}
              </div>

              {/* 보내는이 */}
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <Label>보내는이 *</Label>
                  <div className="flex gap-1.5">
                    <button
                      type="button"
                      onClick={() => setRibbonLefts((prev) => [...prev, ''])}
                      className="rounded border border-gray-300 px-2 py-0.5 text-xs text-gray-600 hover:bg-gray-50"
                    >
                      추가
                    </button>
                    {ribbonLefts.length > 1 && (
                      <button
                        type="button"
                        onClick={() => setRibbonLefts((prev) => prev.slice(0, -1))}
                        className="rounded border border-gray-300 px-2 py-0.5 text-xs text-gray-600 hover:bg-gray-50"
                      >
                        삭제
                      </button>
                    )}
                  </div>
                </div>
                {ribbonLefts.map((left, idx) => (
                  <div key={idx} className="space-y-1">
                    {ribbonLefts.length > 1 && (
                      <p className="text-xs font-medium text-gray-400">{idx + 1}번</p>
                    )}
                    <Input
                      placeholder="예: 주식회사 OO 부장 홍길동"
                      value={left}
                      onChange={(e) => {
                        const val = e.target.value
                        setRibbonLefts((prev) => prev.map((r, i) => i === idx ? val : r))
                        if (idx === 0) setErrors((prev) => ({ ...prev, ribbonLeft: undefined }))
                      }}
                      className={cn(idx === 0 && errors.ribbonLeft && 'border-red-500 focus-visible:ring-red-500')}
                    />
                    {idx === 0 && errors.ribbonLeft && <p className="text-xs text-red-500">{errors.ribbonLeft}</p>}
                  </div>
                ))}
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
              <div className="flex items-center gap-1.5">
                <Input
                  type="date"
                  value={deliveryDate}
                  onChange={(e) => {
                    setDeliveryDate(e.target.value)
                    if (e.target.value) setErrors((prev) => ({ ...prev, deliveryDate: undefined }))
                  }}
                  className={cn('flex-1', errors.deliveryDate && 'border-red-500 focus-visible:ring-red-500')}
                />
                {(['금일', '익일'] as const).map((label, offset) => (
                  <button
                    key={label}
                    type="button"
                    onClick={() => {
                      const d = new Date()
                      d.setDate(d.getDate() + offset)
                      const yyyy = d.getFullYear()
                      const mm = String(d.getMonth() + 1).padStart(2, '0')
                      const dd = String(d.getDate()).padStart(2, '0')
                      setDeliveryDate(`${yyyy}-${mm}-${dd}`)
                      setErrors((prev) => ({ ...prev, deliveryDate: undefined }))
                    }}
                    className="shrink-0 rounded-md border px-2.5 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-50"
                  >
                    {label}
                  </button>
                ))}
              </div>
              {errors.deliveryDate && <p className="text-xs text-red-500">{errors.deliveryDate}</p>}
            </div>
            <div className="space-y-1">
              <Label>{['3시간이내', '직접입력'].includes(deliveryTimeType) ? '배송시간' : '배송시간 *'}</Label>
              <div className="flex flex-wrap items-center gap-2">
                {!['3시간이내', '직접입력'].includes(deliveryTimeType) && (
                  <>
                    <select
                      value={deliveryHour}
                      onChange={(e) => {
                        const newHour = e.target.value
                        setDeliveryHour(newHour)
                        if (newHour) setErrors((prev) => ({ ...prev, deliveryHour: undefined }))
                        if (deliveryTimeType === '예식' && newHour) {
                          const autoHour = ((Number(newHour) - 1) + 24) % 24
                          setMessageSchedule('scheduled')
                          setScheduledHour(String(autoHour))
                          setScheduledMinute(deliveryMinute)
                        }
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
                      onChange={(e) => {
                        const newMinute = e.target.value
                        setDeliveryMinute(newMinute)
                        if (deliveryTimeType === '예식' && deliveryHour) {
                          setScheduledMinute(newMinute)
                        }
                      }}
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
                  onChange={(e) => {
                    const newType = e.target.value
                    setDeliveryTimeType(newType)
                    if (newType === '예식' && deliveryHour) {
                      const autoHour = ((Number(deliveryHour) - 1) + 24) % 24
                      setMessageSchedule('scheduled')
                      setScheduledHour(String(autoHour))
                      setScheduledMinute(deliveryMinute)
                    }
                  }}
                  className="rounded-md border px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
                >
                  {['까지', '예식', '행사', '3시간이내', '직접입력'].map((opt) => (
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
                {errors.messageSchedule && <p className="mt-1 text-xs text-red-500">{errors.messageSchedule}</p>}
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

      {availablePoint !== null && (
        <div className={cn('rounded-md border px-3 py-2 text-sm', pointInsufficient ? 'border-red-200 bg-red-50 text-red-600' : 'bg-gray-50 text-gray-700')}>
          <div className="flex justify-between">
            <span>보유 포인트</span>
            <span className="font-medium">{availablePoint.toLocaleString()} P</span>
          </div>
          <div className="flex justify-between">
            <span>예치 예정 (상품금액 + 추가비용)</span>
            <span className="font-medium">{requiredPoint.toLocaleString()} P</span>
          </div>
          {pointInsufficient && (
            <p className="mt-1 text-xs">포인트가 부족합니다. 충전 후 발주해주세요.</p>
          )}
        </div>
      )}

      <div className="flex justify-end gap-2">
        <Button variant="outline" onClick={() => router.push(coupangOrderId ? '/coupang/orders' : shopOrderId ? '/shop/orders' : '/orders')}>취소</Button>
        <Button
          className="bg-green-700 hover:bg-green-800"
          onClick={handleSubmit}
          disabled={submitting || pointInsufficient || (!!coupangOrderId && !coupangOrder) || (!!shopOrderId && !shopOrder)}
        >
          {submitting ? '등록 중...' : coupangOrderId || shopOrderId ? '승인 및 발주 등록' : '발주 등록'}
        </Button>
      </div>

      <VendorSelectModal
        open={vendorModalOpen}
        onSelect={(v, product) => {
          setVendorId(v.vendorId)
          setSelectedVendorName(v.companyName)
          setSelectedProductId(product.id)
          setProductName(product.name)
          setUnitPrice(product.price)
          setAmount(String(product.price * (Number(quantity) || 1)))
          setErrors((prev) => ({ ...prev, vendorId: undefined, amount: undefined }))
          setVendorModalOpen(false)
        }}
        onClose={() => setVendorModalOpen(false)}
      />
    </div>
  )
}

'use client'

import { useEffect, useState } from 'react'
import { createPortal } from 'react-dom'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { fetchVendorProducts } from '@/lib/api/productApi'
import { approveShopOrder, type ShopOrder } from '@/lib/api/shopApi'
import type { VendorProduct } from '@/types/product'

const TIME_TYPES = ['까지', '예식', '3시간이내', '직접입력']
const EVENT_TYPES = ['장례식', '결혼식', '기타']

interface Props {
  order: ShopOrder
  onClose: () => void
  onApproved: () => void
}

// 수주사 전용 쇼핑몰: 본인 수주사 앞으로 발주를 만드는 간단 승인 폼 (발주사 지정 없음)
export function VendorShopApproveModal({ order, onClose, onApproved }: Props) {
  const [products, setProducts] = useState<VendorProduct[]>([])
  const [productId, setProductId] = useState('')
  const [deliveryDate, setDeliveryDate] = useState(order.deliveryDate ?? '')
  const [timeType, setTimeType] = useState('까지')
  const [hour, setHour] = useState('')
  const [minute, setMinute] = useState('00')
  const [custom, setCustom] = useState(order.deliveryTime ?? '')
  const [placeName, setPlaceName] = useState(order.placeName ?? '')
  const [eventType, setEventType] = useState(EVENT_TYPES.includes(order.eventType ?? '') ? order.eventType! : '기타')
  const [ribbonLeft, setRibbonLeft] = useState(order.ribbonLeft ?? '')
  const [ribbonRight, setRibbonRight] = useState(order.ribbonRight ?? '')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    fetchVendorProducts('', 0, 200)
      .then((r) => setProducts(r.content.filter((p) => p.enabled)))
      .catch(() => setError('상품 목록을 불러오지 못했습니다.'))
  }, [])

  const fixedTime = timeType === '3시간이내' || timeType === '직접입력'
  const totalQty = order.items.reduce((s, i) => s + i.quantity, 0)

  async function submit() {
    const vendorId = localStorage.getItem('vendorId')
    if (!vendorId) return setError('수주사 계정 정보를 찾을 수 없습니다. 다시 로그인해 주세요.')
    if (!productId) return setError('발주에 사용할 상품을 선택해 주세요.')
    if (!deliveryDate) return setError('배송날짜를 입력해 주세요.')
    if (!fixedTime && !hour) return setError('배송시간을 선택해 주세요.')
    setSubmitting(true)
    setError('')
    try {
      await approveShopOrder(order.id, {
        vendorId: Number(vendorId),
        productId: Number(productId),
        productName: order.items.map((i) => i.name).join(', '),
        quantity: totalQty,
        amount: 0,
        deliveryDate,
        deliveryTimeType: timeType,
        deliveryTimeHour: fixedTime ? '' : hour,
        deliveryTimeMinute: fixedTime ? '' : minute,
        deliveryTimeCustom: custom,
        address: order.address,
        addressDetail: order.addressDetail ?? '',
        eventType,
        placeName,
        roomName: '',
        eventInfo: '',
        ribbonLeft,
        ribbonRight,
        ordererName: order.ordererName,
        ordererPhone: order.ordererPhone,
        recipientName: order.recipientName,
        recipientPhone: order.recipientPhone,
        sendPhotoToOrderer: true,
        sendPhotoToRecipient: false,
        messageSchedule: 'immediate',
        scheduledMessageHour: '9',
        scheduledMessageMinute: '00',
        adminMemo: order.memo ? `[쇼핑몰 요청사항] ${order.memo}` : '',
        vendorMemo: '',
      })
      onApproved()
    } catch (e) {
      setError(e instanceof Error ? e.message : '승인에 실패했습니다.')
    } finally {
      setSubmitting(false)
    }
  }

  return createPortal(
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/40">
      <div className="flex min-h-full items-center justify-center p-4">
        <div className="w-full max-w-lg space-y-3 rounded-lg bg-white p-4 shadow-lg">
          <h2 className="text-base font-bold">쇼핑몰 주문 승인 · 발주 등록</h2>
          <p className="text-xs text-gray-500">
            {order.orderNo} · {order.items.map((i) => `${i.name} × ${i.quantity}`).join(', ')}
          </p>

          <div className="space-y-1.5">
            <Label>발주 상품 *</Label>
            <select
              className="w-full rounded-md border bg-white px-3 py-2 text-sm"
              value={productId}
              onChange={(e) => setProductId(e.target.value)}
            >
              <option value="">선택하세요</option>
              {products.map((p) => (
                <option key={p.id} value={p.id}>{p.name} ({p.price.toLocaleString()}원)</option>
              ))}
            </select>
            <p className="text-xs text-gray-400">쇼핑몰 상품과 별개로, 발주에 사용할 본인 상품을 선택합니다. 수량 {totalQty}개로 등록됩니다.</p>
          </div>

          <div className="grid gap-3 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label>배송날짜 *</Label>
              <Input type="date" value={deliveryDate} onChange={(e) => setDeliveryDate(e.target.value)} />
            </div>
            <div className="space-y-1.5">
              <Label>배송시간 {fixedTime ? '' : '*'}</Label>
              <div className="flex gap-1">
                <select className="rounded-md border bg-white px-2 py-2 text-sm" value={timeType} onChange={(e) => setTimeType(e.target.value)}>
                  {TIME_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
                </select>
                {!fixedTime && (
                  <>
                    <select className="rounded-md border bg-white px-2 py-2 text-sm" value={hour} onChange={(e) => setHour(e.target.value)}>
                      <option value="">시</option>
                      {Array.from({ length: 24 }, (_, i) => <option key={i} value={String(i)}>{i}시</option>)}
                    </select>
                    <select className="rounded-md border bg-white px-2 py-2 text-sm" value={minute} onChange={(e) => setMinute(e.target.value)}>
                      {['00', '10', '20', '30', '40', '50'].map((m) => <option key={m} value={m}>{m}분</option>)}
                    </select>
                  </>
                )}
              </div>
            </div>
          </div>
          {timeType === '직접입력' && (
            <Input placeholder="배송시간 직접 입력" value={custom} onChange={(e) => setCustom(e.target.value)} />
          )}
          {order.deliveryTime && (
            <p className="text-xs text-gray-400">고객 희망 시간: {order.deliveryTime}</p>
          )}

          <div className="grid gap-3 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label>행사 종류</Label>
              <select className="w-full rounded-md border bg-white px-3 py-2 text-sm" value={eventType} onChange={(e) => setEventType(e.target.value)}>
                {EVENT_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
              </select>
            </div>
            <div className="space-y-1.5">
              <Label>장소</Label>
              <Input value={placeName} onChange={(e) => setPlaceName(e.target.value)} />
            </div>
            <div className="space-y-1.5">
              <Label>리본 왼쪽 (보내는 분)</Label>
              <Input value={ribbonLeft} onChange={(e) => setRibbonLeft(e.target.value)} />
            </div>
            <div className="space-y-1.5">
              <Label>리본 오른쪽 (문구)</Label>
              <Input value={ribbonRight} onChange={(e) => setRibbonRight(e.target.value)} />
            </div>
          </div>

          {error && <p className="text-sm text-red-500">{error}</p>}

          <div className="flex justify-end gap-2 pt-1">
            <Button variant="outline" onClick={onClose} disabled={submitting}>닫기</Button>
            <Button onClick={submit} disabled={submitting} className="bg-green-700 hover:bg-green-800">
              {submitting ? '처리 중…' : '승인 및 발주 등록'}
            </Button>
          </div>
        </div>
      </div>
    </div>,
    document.body,
  )
}

'use client'

import { useState, useEffect, useRef, useCallback } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { OrderStatusBadge, DeliveryTimeTypeBadge } from '@/components/common/StatusBadge'
import {
  fetchOrder, updateOrder, updateOrderStatus, deleteOrder,
  fetchOrderHistory, fetchReceiptConfirmation,
  fetchSmsMessages, resendMms,
  OrderHistoryItem, ReceiptConfirmationResult, SmsMessageResult,
} from '@/lib/api/orderApi'
import { PrintButton } from '@/components/common/PrintButton'
import { ReceiptConfirmButton } from '@/components/common/ReceiptConfirmDialog'
import { ReferencePhotoSection } from '@/components/common/ReferencePhotoViewer'
import { Pencil, X, Check, Trash2, GripHorizontal } from 'lucide-react'
import { cn, RIBBON_PHRASES } from '@/lib/utils'
import { Order } from '@/types/order'
import { fetchAllProducts } from '@/lib/api/productApi'
import { Product } from '@/types/product'
import { fetchVendors } from '@/lib/api/vendorApi'
import { Vendor } from '@/types/vendor'

interface Props {
  orderId: string
  zIndex: number
  initialOffset?: number
  onClose: () => void
  onFocus: () => void
  onUpdated?: () => void
}

const ACTION_LABEL: Record<string, string> = {
  CREATED: '발주 등록',
  UPDATED: '발주 수정',
  STATUS_CHANGED: '상태 변경',
}

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="space-y-1">
      <span className="text-xs font-medium text-gray-500">{label}</span>
      {children}
    </div>
  )
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex gap-2">
      <span className="w-24 flex-shrink-0 text-gray-500">{label}</span>
      <span className="break-all">{value}</span>
    </div>
  )
}

export function OrderDetailPopup({ orderId, zIndex, initialOffset = 0, onClose, onFocus, onUpdated }: Props) {
  const [pos, setPos] = useState(() => {
    if (typeof window === 'undefined') return { x: 60 + initialOffset * 32, y: 40 + initialOffset * 32 }
    return {
      x: Math.max(0, Math.round((window.innerWidth - 860) / 2) + initialOffset * 32),
      y: Math.max(0, Math.round(window.innerHeight * 0.05) + initialOffset * 32),
    }
  })
  const posRef = useRef(pos)
  posRef.current = pos
  const dragRef = useRef<{ startX: number; startY: number; origX: number; origY: number } | null>(null)

  const [order, setOrder] = useState<Order | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [isEditing, setIsEditing] = useState(false)
  const [draft, setDraft] = useState<Order | null>(null)
  const [confirmDelete, setConfirmDelete] = useState(false)
  const [deleteError, setDeleteError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)
  const isSuperAdmin = typeof window !== 'undefined' && localStorage.getItem('role') === 'SUPER_ADMIN'
  const [saveError, setSaveError] = useState<string | null>(null)
  const [receiptConfirmation, setReceiptConfirmation] = useState<ReceiptConfirmationResult | null>(null)
  const [history, setHistory] = useState<OrderHistoryItem[]>([])
  const [smsMessages, setSmsMessages] = useState<SmsMessageResult[]>([])
  const [resending, setResending] = useState(false)
  const [completeDialogOpen, setCompleteDialogOpen] = useState(false)
  const [selectedPhotoUrls, setSelectedPhotoUrls] = useState<string[]>([])
  const [products, setProducts] = useState<Product[]>([])
  const [selectedProductId, setSelectedProductId] = useState('')
  const [vendors, setVendors] = useState<Vendor[]>([])

  useEffect(() => {
    fetchAllProducts().then(setProducts).catch(() => setSelectedProductId('__direct__'))
    fetchVendors('ACTIVE').then(setVendors).catch(() => {})
    Promise.all([
      fetchOrder(orderId),
      fetchReceiptConfirmation(orderId).catch(() => null),
      fetchOrderHistory(orderId).catch(() => []),
      fetchSmsMessages(orderId).catch(() => []),
    ]).then(([orderData, receipt, hist, sms]) => {
      setOrder(orderData)
      setDraft(orderData)
      setReceiptConfirmation(receipt)
      setHistory(hist)
      setSmsMessages(sms)
    }).catch(() => setLoadError('발주 정보를 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }, [orderId])

  useEffect(() => {
    function clampOnResize() {
      setPos((prev) => ({ x: prev.x, y: Math.max(0, prev.y) }))
    }
    window.addEventListener('resize', clampOnResize)
    return () => window.removeEventListener('resize', clampOnResize)
  }, [])

  const handleDragStart = useCallback((e: React.MouseEvent) => {
    e.preventDefault()
    dragRef.current = { startX: e.clientX, startY: e.clientY, origX: posRef.current.x, origY: posRef.current.y }

    const onMove = (ev: MouseEvent) => {
      if (!dragRef.current) return
      setPos({
        x: dragRef.current.origX + ev.clientX - dragRef.current.startX,
        y: Math.max(0, dragRef.current.origY + ev.clientY - dragRef.current.startY),
      })
    }
    const onUp = () => {
      dragRef.current = null
      document.removeEventListener('mousemove', onMove)
      document.removeEventListener('mouseup', onUp)
    }
    document.addEventListener('mousemove', onMove)
    document.addEventListener('mouseup', onUp)
  }, [])

  if (!order && !loading && !loadError) return null

  const hasSitePhotos = (receiptConfirmation?.sitePhotoUrls.length ?? 0) > 0
  const hasPhotos = hasSitePhotos || (receiptConfirmation?.storePhotoUrls.length ?? 0) > 0
  const hasSmsFailure = smsMessages.some((m) => m.status === 'FAIL')

  function startEdit() {
    setDraft({ ...order! })
    const pid = order!.productId
    setSelectedProductId(pid ? String(pid) : order!.productName ? '__direct__' : '')
    setIsEditing(true)
    setSaveError(null)
  }

  function cancelEdit() {
    setIsEditing(false)
    setSaveError(null)
  }

  async function saveEdit() {
    if (!draft) return
    if (!draft.vendorId) {
      setSaveError('수주업체를 선택해 주세요.')
      return
    }
    setSaving(true)
    setSaveError(null)
    try {
      const updated = await updateOrder(orderId, {
        vendorId: Number(draft.vendorId),
        ...(selectedProductId && selectedProductId !== '__direct__' ? { productId: Number(selectedProductId) } : {}),
        productName: draft.productName,
        quantity: draft.quantity,
        amount: draft.amount,
        extraCost: draft.extraCost,
        deliveryDate: draft.deliveryAt.slice(0, 10),
        deliveryTimeType: draft.deliveryTimeType ?? '',
        deliveryTimeHour: draft.deliveryTimeHour ?? '',
        deliveryTimeMinute: draft.deliveryTimeMinute ?? '',
        deliveryTimeCustom: draft.deliveryTimeCustom ?? '',
        address: draft.address,
        addressDetail: draft.addressDetail ?? '',
        eventType: draft.eventType ?? '기타',
        placeName: draft.placeName ?? '',
        roomName: draft.roomName ?? '',
        eventInfo: draft.eventInfo ?? '',
        ribbonLeft: draft.ribbonLeft ?? '',
        ribbonRight: draft.ribbonRight ?? '',
        ordererName: draft.ordererName,
        ordererPhone: draft.ordererPhone,
        recipientName: draft.recipientName ?? '',
        recipientPhone: draft.recipientPhone ?? '',
        sendPhotoToOrderer: draft.sendPhotoToOrderer,
        sendPhotoToRecipient: draft.sendPhotoToRecipient,
        messageSchedule: draft.messageSchedule ?? '',
        scheduledMessageHour: draft.scheduledMessageHour ?? '',
        scheduledMessageMinute: draft.scheduledMessageMinute ?? '',
        adminMemo: draft.adminMemo ?? '',
        vendorMemo: draft.vendorMemo ?? '',
      })
      setOrder(updated)
      setDraft(updated)
      setIsEditing(false)
      onUpdated?.()
    } catch (e) {
      setSaveError(e instanceof Error ? e.message : '수정에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  function openCompleteDialog() {
    setSelectedPhotoUrls([])
    setCompleteDialogOpen(true)
  }

  function togglePhotoSelection(url: string) {
    setSelectedPhotoUrls((prev) =>
      prev.includes(url) ? prev.filter((u) => u !== url) : [...prev, url]
    )
  }

  async function handleComplete() {
    setCompleteDialogOpen(false)
    setSaving(true)
    setSaveError(null)
    try {
      const updated = await updateOrderStatus(orderId, 'complete', selectedPhotoUrls)
      setOrder(updated)
      setDraft(updated)
      const sms = await fetchSmsMessages(orderId).catch(() => [])
      setSmsMessages(sms)
      onUpdated?.()
    } catch (e) {
      setSaveError(e instanceof Error ? e.message : '배송완료 처리에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  async function handleAccept() {
    setSaving(true)
    setSaveError(null)
    try {
      const updated = await updateOrderStatus(orderId, 'accept')
      setOrder(updated)
      setDraft(updated)
      onUpdated?.()
    } catch (e) {
      setSaveError(e instanceof Error ? e.message : '수주 승인에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  async function handleReject() {
    setSaving(true)
    setSaveError(null)
    try {
      const updated = await updateOrderStatus(orderId, 'reject')
      setOrder(updated)
      setDraft(updated)
      onUpdated?.()
    } catch (e) {
      setSaveError(e instanceof Error ? e.message : '수주 거절에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  async function handleResend() {
    setResending(true)
    setSaveError(null)
    try {
      await resendMms(orderId)
      const sms = await fetchSmsMessages(orderId).catch(() => [])
      setSmsMessages(sms)
    } catch (e) {
      setSaveError(e instanceof Error ? e.message : '재발송에 실패했습니다.')
    } finally {
      setResending(false)
    }
  }

  async function handleDelete() {
    try {
      await deleteOrder(orderId)
      onUpdated?.()
      onClose()
    } catch {
      setDeleteError('삭제에 실패했습니다. 다시 시도해 주세요.')
    }
  }

  function set<K extends keyof Order>(key: K, value: Order[K]) {
    setDraft((prev) => prev ? { ...prev, [key]: value } : prev)
  }

  return (
    <div
      className="fixed shadow-2xl"
      style={{ left: pos.x, top: pos.y, zIndex, width: 860, maxWidth: 'calc(100vw - 32px)' }}
      onMouseDown={onFocus}
    >
      {/* 타이틀바 */}
      <div
        className="flex select-none cursor-grab items-center justify-between rounded-t-lg bg-[#1e3a5f] px-4 py-2.5 text-white active:cursor-grabbing"
        onMouseDown={handleDragStart}
      >
        <div className="flex items-center gap-2">
          <GripHorizontal className="h-4 w-4 opacity-60" />
          {loading ? (
            <span className="text-sm opacity-70">불러오는 중...</span>
          ) : order ? (
            <>
              <span className="font-mono text-sm font-semibold">{order.orderNumber}</span>
              <OrderStatusBadge status={order.status} />
              <DeliveryTimeTypeBadge type={order.deliveryTimeType} custom={order.deliveryTimeCustom} />
            </>
          ) : (
            <span className="text-sm opacity-70">주문서</span>
          )}
        </div>
        <button
          onMouseDown={(e) => e.stopPropagation()}
          onClick={onClose}
          className="rounded p-1 opacity-70 hover:bg-white/20 hover:opacity-100"
        >
          <X className="h-4 w-4" />
        </button>
      </div>

      {/* 본문 (스크롤) */}
      <div className="max-h-[calc(100vh-120px)] overflow-y-auto rounded-b-lg bg-gray-50">
        {loading ? (
          <div className="flex h-40 items-center justify-center text-sm text-gray-400">불러오는 중...</div>
        ) : loadError ? (
          <div className="flex h-40 items-center justify-center text-sm text-red-500">{loadError}</div>
        ) : !order || !draft ? (
          <div className="flex h-40 items-center justify-center text-sm text-gray-400">주문을 찾을 수 없습니다.</div>
        ) : (
          <div className="space-y-3 p-4">
            {/* 액션 버튼 */}
            <div className="flex flex-wrap items-center gap-2">
              <PrintButton orderId={order.id} />
              {order.status === '발주대기' && !isEditing && (
                <>
                  <Button className="bg-green-700 hover:bg-green-800" onClick={handleAccept} disabled={saving}>수주승인</Button>
                  <Button variant="outline" className="gap-1.5 border-red-300 text-red-600 hover:bg-red-50" onClick={handleReject} disabled={saving}>수주거절</Button>
                </>
              )}
              {order.status === '수주완료' && !isEditing && (
                <Button
                  className={cn('gap-1.5', hasSitePhotos ? 'bg-blue-700 hover:bg-blue-800' : 'cursor-not-allowed bg-gray-300 text-gray-500')}
                  disabled={!hasSitePhotos || saving}
                  onClick={openCompleteDialog}
                  title={!hasSitePhotos ? '현장사진을 1장 이상 업로드해주세요' : undefined}
                >
                  {saving ? '처리 중…' : '배송완료 처리'}
                </Button>
              )}
              {isEditing ? (
                <>
                  <Button variant="outline" className="gap-1.5" onClick={cancelEdit} disabled={saving}>
                    <X className="h-3.5 w-3.5" />취소
                  </Button>
                  <Button className="gap-1.5 bg-green-700 hover:bg-green-800" onClick={saveEdit} disabled={saving}>
                    <Check className="h-3.5 w-3.5" />{saving ? '저장 중...' : '저장'}
                  </Button>
                </>
              ) : (
                <Button variant="outline" className="gap-1.5" onClick={startEdit}>
                  <Pencil className="h-3.5 w-3.5" />수정
                </Button>
              )}
              {isSuperAdmin && !isEditing && (
                <Button
                  variant="outline"
                  className="gap-1.5 border-red-300 text-red-600 hover:bg-red-50"
                  onClick={() => { setConfirmDelete(true); setDeleteError(null) }}
                >
                  <Trash2 className="h-3.5 w-3.5" />삭제
                </Button>
              )}
            </div>

            {saveError && (
              <div className="rounded-md border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-600">{saveError}</div>
            )}

            <div className="grid gap-3 sm:grid-cols-2">
              {/* 기본정보 */}
              <Card>
                <CardHeader><CardTitle className="text-sm">기본정보</CardTitle></CardHeader>
                <CardContent className="space-y-3 text-sm">
                  {isEditing ? (
                    <>
                      <Field label="상품명">
                        <select
                          value={selectedProductId}
                          onChange={(e) => {
                            const productId = e.target.value
                            setSelectedProductId(productId)
                            if (productId !== '__direct__') {
                              const found = products.find((p) => p.id === productId)
                              set('productName', found ? found.name : '')
                            }
                          }}
                          className="flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm shadow-sm focus:outline-none focus:ring-1 focus:ring-ring"
                        >
                          <option value="">상품 선택</option>
                          {products.map((p) => (
                            <option key={p.id} value={p.id}>{p.name}</option>
                          ))}
                          <option value="__direct__">직접 입력</option>
                        </select>
                        {selectedProductId === '__direct__' && (
                          <Input className="mt-1.5" placeholder="상품명 직접 입력" value={draft.productName} onChange={(e) => set('productName', e.target.value)} />
                        )}
                      </Field>
                      <div className="grid grid-cols-2 gap-3">
                        <Field label="수량">
                          <Input type="number" value={draft.quantity} onChange={(e) => set('quantity', Number(e.target.value))} />
                        </Field>
                        <Field label="수주업체">
                          <select
                            value={draft.vendorId ?? ''}
                            onChange={(e) => {
                              const id = e.target.value
                              const found = vendors.find((v) => v.id === id)
                              setDraft((prev) => prev ? { ...prev, vendorId: id, vendorName: found?.name ?? '' } : prev)
                            }}
                            className="flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm shadow-sm focus:outline-none focus:ring-1 focus:ring-ring"
                          >
                            <option value="">업체 선택</option>
                            {vendors.map((v) => (
                              <option key={v.id} value={v.id}>{v.name}</option>
                            ))}
                          </select>
                        </Field>
                      </div>
                      <Field label="발주금액">
                        <Input type="number" value={draft.amount} onChange={(e) => set('amount', Number(e.target.value))} />
                      </Field>
                      <Field label="추가비용">
                        <Input
                          type="number" placeholder="0"
                          value={draft.extraCost ?? ''}
                          onChange={(e) => set('extraCost', e.target.value ? Number(e.target.value) : undefined)}
                        />
                        {draft.extraCost ? (
                          <p className="mt-1 text-xs text-gray-500">
                            최종: {(draft.amount + draft.extraCost).toLocaleString()}원
                          </p>
                        ) : null}
                      </Field>
                    </>
                  ) : (
                    <>
                      <Row label="상품명" value={order.productName} />
                      <Row label="수량" value={`${order.quantity}개`} />
                      <Row label="발주금액" value={order.extraCost ? `${(order.amount + order.extraCost).toLocaleString()}원 (기본 ${order.amount.toLocaleString()} + 추가 ${order.extraCost.toLocaleString()})` : `${order.amount.toLocaleString()}원`} />
                      <Row label="수주업체" value={order.vendorName} />
                      <Row label="발주일시" value={order.orderedAt} />
                    </>
                  )}
                </CardContent>
              </Card>

              {/* 배송정보 */}
              <Card>
                <CardHeader><CardTitle className="text-sm">배송정보</CardTitle></CardHeader>
                <CardContent className="space-y-3 text-sm">
                  {isEditing ? (
                    <>
                      <Field label="배송예정">
                        <Input value={draft.deliveryAt.slice(0, 10)} onChange={(e) => set('deliveryAt', e.target.value)} />
                      </Field>
                      <Field label="주소">
                        <Input value={draft.address} onChange={(e) => set('address', e.target.value)} />
                      </Field>
                      <Field label="상세주소">
                        <Input value={draft.addressDetail} onChange={(e) => set('addressDetail', e.target.value)} />
                      </Field>
                      <Field label="행사유형">
                        <div className="flex flex-wrap gap-1.5 pt-0.5">
                          {(['장례식', '결혼식', '기타'] as const).map((type) => (
                            <button
                              key={type} type="button"
                              onClick={() => set('eventType', type)}
                              className={cn(
                                'rounded-md border px-3 py-1.5 text-sm font-medium transition-colors',
                                draft.eventType === type ? 'border-green-700 bg-green-700 text-white' : 'border-gray-300 text-gray-600 hover:border-gray-400 hover:bg-gray-50',
                              )}
                            >{type}</button>
                          ))}
                        </div>
                      </Field>
                      <div className="grid grid-cols-2 gap-3">
                        <Field label={draft.eventType === '장례식' ? '장례식장' : draft.eventType === '결혼식' ? '예식장' : '장소'}>
                          <Input value={draft.placeName ?? ''} onChange={(e) => set('placeName', e.target.value)} />
                        </Field>
                        <Field label="받는 분">
                          <Input value={draft.recipientName ?? ''} onChange={(e) => set('recipientName', e.target.value || undefined)} />
                        </Field>
                      </div>
                    </>
                  ) : (
                    <>
                      <Row label="배송예정" value={
                        order.deliveryAt && order.deliveryTimeHour && order.deliveryTimeMinute !== undefined && order.deliveryTimeMinute !== ''
                          ? `${order.deliveryAt} ${order.deliveryTimeHour.padStart(2, '0')}:${order.deliveryTimeMinute.padStart(2, '0')}`
                          : order.deliveryTimeType
                            ? `${order.deliveryAt} ${order.deliveryTimeType === '직접입력' ? (order.deliveryTimeCustom ?? order.deliveryTimeType) : order.deliveryTimeType}`
                            : order.deliveryAt
                      } />
                      <Row label="주소" value={`${order.address} ${order.addressDetail ?? ''}`.trim()} />
                      <Row label="행사유형" value={order.eventType} />
                      <Row label={order.eventType === '장례식' ? '장례식장' : order.eventType === '결혼식' ? '예식장' : '장소'} value={[order.placeName, order.roomName].filter(Boolean).join(' · ') || '-'} />
                      {order.recipientName && <Row label="받는 분" value={order.recipientName} />}
                      {order.eventInfo && <Row label="행사정보" value={order.eventInfo} />}
                    </>
                  )}
                </CardContent>
              </Card>

              {/* 리본 문구 */}
              <Card>
                <CardHeader><CardTitle className="text-sm">리본 문구</CardTitle></CardHeader>
                <CardContent className="space-y-3 text-sm">
                  {isEditing ? (
                    <>
                      <Field label="보내는이">
                        <Input value={draft.ribbonLeft} onChange={(e) => set('ribbonLeft', e.target.value)} />
                      </Field>
                      <Field label="경조문구">
                        <Input value={draft.ribbonRight} onChange={(e) => set('ribbonRight', e.target.value)} />
                      </Field>
                      <div className="space-y-1.5">
                        <p className="text-xs text-gray-400">경조문구 선택</p>
                        <div className="flex flex-wrap gap-1.5">
                          {RIBBON_PHRASES.map((phrase) => (
                            <button key={phrase} type="button" onClick={() => set('ribbonRight', phrase)}
                              className="rounded border border-gray-200 bg-gray-50 px-2 py-1 text-xs text-gray-700 hover:border-green-600 hover:bg-green-50 hover:text-green-700">
                              {phrase}
                            </button>
                          ))}
                        </div>
                      </div>
                    </>
                  ) : (
                    <>
                      <Row label="보내는이" value={order.ribbonLeft} />
                      <Row label="경조문구" value={order.ribbonRight} />
                    </>
                  )}
                </CardContent>
              </Card>

              {/* 주문자 / 받는 분 */}
              <Card>
                <CardHeader><CardTitle className="text-sm">주문자 / 받는 분</CardTitle></CardHeader>
                <CardContent className="space-y-3 text-sm">
                  {isEditing ? (
                    <>
                      <div className="grid grid-cols-2 gap-3">
                        <Field label="주문자 성함">
                          <Input value={draft.ordererName} onChange={(e) => set('ordererName', e.target.value)} />
                        </Field>
                        <Field label="주문자 연락처">
                          <Input value={draft.ordererPhone} onChange={(e) => set('ordererPhone', e.target.value)} />
                        </Field>
                      </div>
                      <div className="grid grid-cols-2 gap-3">
                        <Field label="받는 분 성함">
                          <Input placeholder="없음" value={draft.recipientName ?? ''} onChange={(e) => set('recipientName', e.target.value || undefined)} />
                        </Field>
                        <Field label="받는 분 연락처">
                          <Input placeholder="없음" value={draft.recipientPhone ?? ''} onChange={(e) => set('recipientPhone', e.target.value || undefined)} />
                        </Field>
                      </div>
                      <Field label="문자 발송 대상">
                        <div className="flex gap-4 pt-1">
                          <label className="flex items-center gap-1.5 text-sm">
                            <input type="checkbox" checked={draft.sendPhotoToOrderer} onChange={(e) => set('sendPhotoToOrderer', e.target.checked)} />
                            주문자에게 발송
                          </label>
                          <label className="flex items-center gap-1.5 text-sm">
                            <input type="checkbox" checked={draft.sendPhotoToRecipient} onChange={(e) => set('sendPhotoToRecipient', e.target.checked)} />
                            받는 분에게 발송
                          </label>
                        </div>
                      </Field>
                      <Field label="문자 발송 설정">
                        <div className="space-y-1.5 pt-1">
                          <label className="flex cursor-pointer items-center gap-2">
                            <input type="radio" name={`messageSchedule-${orderId}`} checked={draft.messageSchedule !== 'scheduled'}
                              onChange={() => set('messageSchedule', 'immediate')} className="accent-green-700" />
                            <span className="text-sm">자동발송</span>
                            {draft.messageSchedule !== 'scheduled' && <span className="text-xs text-gray-400">배송완료 처리 즉시 발송</span>}
                          </label>
                          <label className="flex cursor-pointer items-center gap-2">
                            <input type="radio" name={`messageSchedule-${orderId}`} checked={draft.messageSchedule === 'scheduled'}
                              onChange={() => {
                                set('messageSchedule', 'scheduled')
                                if (!draft.scheduledMessageHour) set('scheduledMessageHour', '0')
                                if (!draft.scheduledMessageMinute) set('scheduledMessageMinute', '00')
                              }} className="accent-green-700" />
                            <span className="text-sm">예약발송</span>
                            {draft.messageSchedule === 'scheduled' && (
                              <div className="flex items-center gap-2">
                                <select value={draft.scheduledMessageHour ?? ''} onChange={(e) => set('scheduledMessageHour', e.target.value)}
                                  className="rounded-md border px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-green-700">
                                  {Array.from({ length: 24 }, (_, i) => (
                                    <option key={i} value={String(i)}>{i}시</option>
                                  ))}
                                </select>
                                <select value={draft.scheduledMessageMinute ?? ''} onChange={(e) => set('scheduledMessageMinute', e.target.value)}
                                  className="rounded-md border px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-green-700">
                                  {['00', '10', '20', '30', '40', '50'].map((m) => (
                                    <option key={m} value={m}>{m}분</option>
                                  ))}
                                </select>
                                <span className="text-xs text-gray-400">배송날짜 기준</span>
                              </div>
                            )}
                          </label>
                        </div>
                      </Field>
                    </>
                  ) : (
                    <>
                      <Row label="주문자" value={`${order.ordererName} ${order.ordererPhone}`} />
                      {order.recipientName && <Row label="받는 분" value={`${order.recipientName}${order.recipientPhone ? ` ${order.recipientPhone}` : ''}`} />}
                      <Row label="문자 발송" value={[order.sendPhotoToOrderer ? '주문자' : '', order.sendPhotoToRecipient ? '받는 분' : ''].filter(Boolean).join(', ') || '미발송'} />
                      <Row label="발송 예약시간" value={
                        order.messageSchedule === 'scheduled' && order.scheduledMessageHour
                          ? `예약발송 ${order.deliveryAt.slice(0, 10)} ${String(order.scheduledMessageHour).padStart(2, '0')}:${order.scheduledMessageMinute ?? '00'}`
                          : '자동발송 (즉시)'
                      } />
                    </>
                  )}
                </CardContent>
              </Card>

              {/* 메모 */}
              <Card className="sm:col-span-2">
                <CardHeader><CardTitle className="text-sm">메모</CardTitle></CardHeader>
                <CardContent className="space-y-3 text-sm">
                  {isEditing ? (
                    <>
                      <Field label="관리자 메모">
                        <Input placeholder="내부 메모" value={draft.adminMemo ?? ''} onChange={(e) => set('adminMemo', e.target.value || undefined)} />
                      </Field>
                      <Field label="업체 전달사항">
                        <Textarea placeholder="업체에 전달할 내용" className="min-h-[80px]" value={draft.vendorMemo ?? ''} onChange={(e) => set('vendorMemo', e.target.value || undefined)} />
                      </Field>
                    </>
                  ) : (
                    <>
                      {order.adminMemo && <Row label="관리자 메모" value={order.adminMemo} />}
                      {order.vendorMemo && <Row label="업체 전달사항" value={order.vendorMemo} />}
                      {!order.adminMemo && !order.vendorMemo && <p className="text-gray-400">메모 없음</p>}
                    </>
                  )}
                </CardContent>
              </Card>

              {/* 참고 이미지 */}
              <Card>
                <CardHeader><CardTitle className="text-sm">참고 이미지</CardTitle></CardHeader>
                <CardContent>
                  <ReferencePhotoSection orderId={orderId} />
                </CardContent>
              </Card>

              {/* 배송사진 */}
              <Card>
                <CardHeader><CardTitle className="text-sm">배송사진</CardTitle></CardHeader>
                <CardContent className="space-y-4">
                  {!hasPhotos ? (
                    <p className="text-sm text-gray-400">등록된 배송사진이 없습니다.</p>
                  ) : (
                    <>
                      {hasSitePhotos && (
                        <div>
                          <div className="mb-1.5 flex items-center gap-2">
                            <span className="text-xs font-medium text-gray-600">현장사진</span>
                            <span className="rounded-full bg-blue-100 px-1.5 py-0.5 text-[10px] text-blue-700">SMS 발송</span>
                            <span className="text-xs text-gray-400">({receiptConfirmation!.sitePhotoUrls.length}장)</span>
                          </div>
                          <div className="flex flex-wrap gap-2">
                            {receiptConfirmation!.sitePhotoUrls.map((url, idx) => (
                              <div key={idx} className="h-24 w-24 overflow-hidden rounded-lg border">
                                <img src={url} alt="현장사진" className="h-full w-full object-cover" />
                              </div>
                            ))}
                          </div>
                        </div>
                      )}
                      {(receiptConfirmation?.storePhotoUrls.length ?? 0) > 0 && (
                        <div>
                          <div className="mb-1.5 flex items-center gap-2">
                            <span className="text-xs font-medium text-gray-600">매장사진</span>
                            <span className="text-xs text-gray-400">({receiptConfirmation!.storePhotoUrls.length}장)</span>
                          </div>
                          <div className="flex flex-wrap gap-2">
                            {receiptConfirmation!.storePhotoUrls.map((url, idx) => (
                              <div key={idx} className="h-24 w-24 overflow-hidden rounded-lg border">
                                <img src={url} alt="매장사진" className="h-full w-full object-cover" />
                              </div>
                            ))}
                          </div>
                        </div>
                      )}
                    </>
                  )}
                </CardContent>
              </Card>

              {/* 인수확인 */}
              <Card className="sm:col-span-2">
                <CardHeader><CardTitle className="text-sm">인수확인</CardTitle></CardHeader>
                <CardContent className="space-y-3 text-sm">
                  {receiptConfirmation?.receiverName ? (
                    <div className="space-y-2">
                      <Row label="인수자" value={receiptConfirmation.receiverName} />
                      {receiptConfirmation.receiverRelation && <Row label="관계" value={receiptConfirmation.receiverRelation} />}
                      <Row label="배송완료" value={receiptConfirmation.delivered ? '확인' : '미확인'} />
                      {receiptConfirmation.confirmedAt && <Row label="확인일시" value={receiptConfirmation.confirmedAt} />}
                      {receiptConfirmation.memo && <Row label="메모" value={receiptConfirmation.memo} />}
                    </div>
                  ) : (
                    <p className="text-gray-400">인수확인 정보가 없습니다.</p>
                  )}
                  <ReceiptConfirmButton
                    orderId={order.id}
                    orderNumber={order.orderNumber}
                    senderName={order.ribbonLeft}
                    recipientName={order.recipientName}
                    onDeliveryComplete={async () => {
                      try {
                        const [updated, receipt, sms] = await Promise.all([
                          fetchOrder(orderId),
                          fetchReceiptConfirmation(orderId).catch(() => null),
                          fetchSmsMessages(orderId).catch(() => []),
                        ])
                        setOrder(updated)
                        setDraft(updated)
                        setReceiptConfirmation(receipt)
                        setSmsMessages(sms)
                        onUpdated?.()
                      } catch {}
                    }}
                  />
                </CardContent>
              </Card>

              {/* 문자 발송 내역 */}
              <Card className="sm:col-span-2">
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <CardTitle className="text-sm">문자 발송 내역</CardTitle>
                    {hasSmsFailure && (
                      <button
                        onClick={handleResend}
                        disabled={resending}
                        className="rounded bg-red-600 px-3 py-1 text-xs font-medium text-white hover:bg-red-700 disabled:opacity-50"
                      >
                        {resending ? '재발송 중...' : '재발송'}
                      </button>
                    )}
                  </div>
                </CardHeader>
                <CardContent className="text-sm">
                  {smsMessages.length === 0 ? (
                    <p className="text-xs text-gray-400">배송완료 처리 시 자동으로 발송됩니다.</p>
                  ) : (
                    <div className="space-y-2">
                      {smsMessages.map((msg) => (
                        <div key={msg.id} className="rounded-md border bg-gray-50 px-3 py-2 space-y-1.5">
                          <div className="flex items-center justify-between">
                            <span className="text-sm font-medium">{msg.receiver}</span>
                            {msg.status === 'SUCCESS'
                              ? <span className="text-xs text-green-600">✓ 발송성공</span>
                              : <span className="text-xs text-red-500">✕ 발송실패</span>}
                          </div>
                          {msg.sentAt && <p className="text-xs text-gray-400">{msg.sentAt}</p>}
                          {msg.message && <p className="whitespace-pre-wrap border-t pt-1.5 text-sm text-gray-800">{msg.message}</p>}
                          {msg.imageUrl && <img src={msg.imageUrl} alt="발송사진" className="h-20 w-20 rounded border object-cover" />}
                          {msg.failReason && <p className="text-xs text-red-400">{msg.failReason}</p>}
                        </div>
                      ))}
                    </div>
                  )}
                </CardContent>
              </Card>

              {/* 변경 이력 */}
              <Card className="sm:col-span-2">
                <CardHeader><CardTitle className="text-sm">변경 이력</CardTitle></CardHeader>
                <CardContent>
                  {history.length === 0 ? (
                    <p className="text-sm text-gray-400">이력이 없습니다.</p>
                  ) : (
                    <ol className="relative border-l border-gray-200">
                      {history.map((h) => (
                        <li key={h.id} className="mb-4 ml-4">
                          <span className="absolute -left-1.5 mt-1.5 h-3 w-3 rounded-full border border-white bg-green-600" />
                          <p className="text-xs text-gray-400">{h.createdAt}</p>
                          <p className="text-sm font-medium">{ACTION_LABEL[h.action]}</p>
                          <p className="text-xs text-gray-500">{h.detail}</p>
                          {(h.actorCompany || h.actorName) && (
                            <p className="text-xs text-gray-400">{[h.actorCompany, h.actorName].filter(Boolean).join(' / ')}</p>
                          )}
                        </li>
                      ))}
                    </ol>
                  )}
                </CardContent>
              </Card>
            </div>
          </div>
        )}
      </div>

      {/* 배송완료 사진 선택 다이얼로그 */}
      {completeDialogOpen && (
        <div className="absolute inset-0 z-10 flex items-center justify-center rounded-lg bg-black/50">
          <div className="w-full max-w-md rounded-lg bg-white shadow-xl">
            <div className="flex items-center justify-between border-b px-5 py-4">
              <h2 className="font-semibold">발송할 배송사진 선택</h2>
              <button onClick={() => setCompleteDialogOpen(false)} className="text-gray-400 hover:text-gray-600"><X className="h-5 w-5" /></button>
            </div>
            <div className="p-5">
              <p className="mb-3 text-sm text-gray-500">선택한 사진 수만큼 MMS가 각각 발송됩니다. 선택하지 않으면 사진 없이 SMS만 발송됩니다.</p>
              <div className="flex flex-wrap gap-2">
                {receiptConfirmation?.sitePhotoUrls.map((url, idx) => {
                  const selected = selectedPhotoUrls.includes(url)
                  return (
                    <button key={idx} type="button" onClick={() => togglePhotoSelection(url)}
                      className={cn('relative h-24 w-24 overflow-hidden rounded border-2 transition-all', selected ? 'border-blue-500' : 'border-transparent')}>
                      <img src={url} alt="배송사진" className="h-full w-full object-cover" />
                      {selected && <div className="absolute inset-0 flex items-center justify-center bg-blue-500/30"><Check className="h-6 w-6 text-white drop-shadow" /></div>}
                    </button>
                  )
                })}
              </div>
              {selectedPhotoUrls.length > 0 && <p className="mt-2 text-xs text-blue-600">{selectedPhotoUrls.length}장 선택됨 — MMS {selectedPhotoUrls.length}건 발송 예정</p>}
            </div>
            <div className="flex justify-end gap-2 border-t px-5 py-4">
              <Button variant="outline" onClick={() => setCompleteDialogOpen(false)}>취소</Button>
              <Button className="bg-blue-700 hover:bg-blue-800" onClick={handleComplete} disabled={saving}>배송완료 처리</Button>
            </div>
          </div>
        </div>
      )}

      {/* 삭제 확인 다이얼로그 */}
      {confirmDelete && (
        <div className="absolute inset-0 z-10 flex items-center justify-center rounded-lg bg-black/50">
          <div className="w-full max-w-sm rounded-lg bg-white shadow-xl">
            <div className="flex items-center justify-between border-b px-5 py-4">
              <h2 className="font-semibold">주문서 삭제</h2>
              <button onClick={() => { setConfirmDelete(false); setDeleteError(null) }} className="text-gray-400 hover:text-gray-600"><X className="h-5 w-5" /></button>
            </div>
            <div className="p-5">
              <p className="text-sm text-gray-700">정말 삭제하시겠습니까?</p>
              <p className="mt-1 text-xs text-gray-400">삭제된 주문서는 목록에 표시되지 않습니다.</p>
              {deleteError && <p className="mt-2 text-sm text-red-600">{deleteError}</p>}
            </div>
            <div className="flex justify-end gap-2 border-t px-5 py-4">
              <Button variant="outline" onClick={() => { setConfirmDelete(false); setDeleteError(null) }}>취소</Button>
              <Button className="gap-1.5 bg-red-600 hover:bg-red-700" onClick={handleDelete}>
                <Trash2 className="h-3.5 w-3.5" />삭제
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

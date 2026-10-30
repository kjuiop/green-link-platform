'use client'

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { PrintButton } from '@/components/common/PrintButton'
import { ReceiptConfirmButton } from '@/components/common/ReceiptConfirmDialog'
import { ReferencePhotoSection } from '@/components/common/ReferencePhotoViewer'
import { Pencil, X, Check, Trash2 } from 'lucide-react'
import { cn, RIBBON_PHRASES } from '@/lib/utils'
import type { LoadedOrderDetail } from '@/components/common/useOrderDetail'

type D = LoadedOrderDetail
interface SectionProps { d: D; className?: string }

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

export function OrderActionButtons({ d }: { d: D }) {
  const { order, isEditing, saving, hasSitePhotos } = d
  return (
    <>
      <PrintButton orderId={order.id} />
      {order.status === '발주대기' && !isEditing && (
        <>
          <Button className="bg-green-700 hover:bg-green-800" onClick={d.handleAccept} disabled={saving}>수주승인</Button>
          <Button variant="outline" className="gap-1.5 border-red-300 text-red-600 hover:bg-red-50" onClick={d.handleReject} disabled={saving}>수주거절</Button>
        </>
      )}
      {order.status === '수주완료' && !isEditing && (
        <Button
          className={cn('gap-1.5', hasSitePhotos ? 'bg-blue-700 hover:bg-blue-800' : 'cursor-not-allowed bg-gray-300 text-gray-500')}
          disabled={!hasSitePhotos || saving}
          onClick={d.openCompleteDialog}
          title={!hasSitePhotos ? '현장사진을 1장 이상 업로드해주세요' : undefined}
        >
          {saving ? '처리 중…' : '배송완료 처리'}
        </Button>
      )}
      {isEditing ? (
        <>
          <Button variant="outline" className="gap-1.5" onClick={d.cancelEdit} disabled={saving}>
            <X className="h-3.5 w-3.5" />취소
          </Button>
          <Button className="gap-1.5 bg-green-700 hover:bg-green-800" onClick={d.saveEdit} disabled={saving}>
            <Check className="h-3.5 w-3.5" />{saving ? '저장 중...' : '저장'}
          </Button>
        </>
      ) : (
        <Button variant="outline" className="gap-1.5" onClick={d.startEdit}>
          <Pencil className="h-3.5 w-3.5" />수정
        </Button>
      )}
      {d.isSuperAdmin && !isEditing && (
        <Button
          variant="outline"
          className="gap-1.5 border-red-300 text-red-600 hover:bg-red-50"
          onClick={d.openDeleteConfirm}
        >
          <Trash2 className="h-3.5 w-3.5" />삭제
        </Button>
      )}
    </>
  )
}

export function SaveErrorBanner({ d }: { d: D }) {
  if (!d.saveError) return null
  return (
    <div className="rounded-md border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-600">{d.saveError}</div>
  )
}

export function BasicInfoCard({ d, className }: SectionProps) {
  const { order, draft, isEditing, products, vendors, selectedProductId, set } = d
  return (
    <Card className={className}>
      <CardHeader><CardTitle className="text-sm">기본정보</CardTitle></CardHeader>
      <CardContent className="space-y-3 text-sm">
        {isEditing ? (
          <>
            <Field label="상품명">
              <select
                value={selectedProductId}
                onChange={(e) => {
                  const productId = e.target.value
                  d.setSelectedProductId(productId)
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
                    d.setDraft((prev) => prev ? { ...prev, vendorId: id, vendorName: found?.name ?? '' } : prev)
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
  )
}

export function DeliveryInfoCard({ d, className }: SectionProps) {
  const { order, draft, isEditing, set } = d
  return (
    <Card className={className}>
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
  )
}

export function RibbonCard({ d, className }: SectionProps) {
  const { order, draft, isEditing, set } = d
  return (
    <Card className={className}>
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
  )
}

export function ContactCard({ d, className }: SectionProps) {
  const { order, draft, isEditing, orderId, set } = d
  return (
    <Card className={className}>
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
  )
}

export function MemoCard({ d, className }: SectionProps) {
  const { order, draft, isEditing, set } = d
  return (
    <Card className={className}>
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
  )
}

export function ReferenceImageCard({ d, className }: SectionProps) {
  return (
    <Card className={className}>
      <CardHeader><CardTitle className="text-sm">참고 이미지</CardTitle></CardHeader>
      <CardContent>
        <ReferencePhotoSection orderId={d.orderId} />
      </CardContent>
    </Card>
  )
}

export function DeliveryPhotosCard({ d, className }: SectionProps) {
  const { receiptConfirmation, hasPhotos, hasSitePhotos } = d
  return (
    <Card className={className}>
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
  )
}

export function ReceiptConfirmationCard({ d, className }: SectionProps) {
  const { order, receiptConfirmation } = d
  return (
    <Card className={className}>
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
          onDeliveryComplete={d.refreshAfterDeliveryComplete}
        />
      </CardContent>
    </Card>
  )
}

export function SmsHistoryCard({ d, className }: SectionProps) {
  const { smsMessages, hasSmsFailure, resending } = d
  return (
    <Card className={className}>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle className="text-sm">문자 발송 내역</CardTitle>
          {hasSmsFailure && (
            <button
              onClick={d.handleResend}
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
              <div key={msg.id} className="space-y-1.5 rounded-md border bg-gray-50 px-3 py-2">
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
  )
}

export function HistoryCard({ d, className }: SectionProps) {
  const { history } = d
  return (
    <Card className={className}>
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
  )
}

export function CompleteDialog({ d, overlayClassName }: { d: D; overlayClassName: string }) {
  if (!d.completeDialogOpen) return null
  const { receiptConfirmation, selectedPhotoUrls } = d
  return (
    <div className={overlayClassName}>
      <div className="w-full max-w-md rounded-lg bg-white shadow-xl">
        <div className="flex items-center justify-between border-b px-5 py-4">
          <h2 className="font-semibold">발송할 배송사진 선택</h2>
          <button onClick={() => d.setCompleteDialogOpen(false)} className="text-gray-400 hover:text-gray-600"><X className="h-5 w-5" /></button>
        </div>
        <div className="p-5">
          <p className="mb-3 text-sm text-gray-500">선택한 사진 수만큼 MMS가 각각 발송됩니다. 선택하지 않으면 사진 없이 SMS만 발송됩니다.</p>
          <div className="flex flex-wrap gap-2">
            {receiptConfirmation?.sitePhotoUrls.map((url, idx) => {
              const selected = selectedPhotoUrls.includes(url)
              return (
                <button key={idx} type="button" onClick={() => d.togglePhotoSelection(url)}
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
          <Button variant="outline" onClick={() => d.setCompleteDialogOpen(false)}>취소</Button>
          <Button className="bg-blue-700 hover:bg-blue-800" onClick={d.handleComplete} disabled={d.saving}>배송완료 처리</Button>
        </div>
      </div>
    </div>
  )
}

export function DeleteDialog({ d, overlayClassName }: { d: D; overlayClassName: string }) {
  if (!d.confirmDelete) return null
  return (
    <div className={overlayClassName}>
      <div className="w-full max-w-sm rounded-lg bg-white shadow-xl">
        <div className="flex items-center justify-between border-b px-5 py-4">
          <h2 className="font-semibold">주문서 삭제</h2>
          <button onClick={d.closeDeleteConfirm} className="text-gray-400 hover:text-gray-600"><X className="h-5 w-5" /></button>
        </div>
        <div className="p-5">
          <p className="text-sm text-gray-700">정말 삭제하시겠습니까?</p>
          <p className="mt-1 text-xs text-gray-400">삭제된 주문서는 목록에 표시되지 않습니다.</p>
          {d.deleteError && <p className="mt-2 text-sm text-red-600">{d.deleteError}</p>}
        </div>
        <div className="flex justify-end gap-2 border-t px-5 py-4">
          <Button variant="outline" onClick={d.closeDeleteConfirm}>취소</Button>
          <Button className="gap-1.5 bg-red-600 hover:bg-red-700" onClick={d.handleDelete}>
            <Trash2 className="h-3.5 w-3.5" />삭제
          </Button>
        </div>
      </div>
    </div>
  )
}

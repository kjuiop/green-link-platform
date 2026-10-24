'use client'

import { useState, useEffect } from 'react'
import { X, Send, Check } from 'lucide-react'
import { cn } from '@/lib/utils'
import { Dialog, DialogContent, DialogTitle } from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { MessageStatus } from '@/types/order'
import { fetchSmsMessages, resendMms, SmsMessageResult } from '@/lib/api/orderApi'

// ---- 공유 다이얼로그 ----

interface PhotoSendDialogProps {
  open: boolean
  onClose: () => void
  photos: string[]
  orderNumber?: string
  ordererName?: string
  ordererPhone?: string
  recipientName?: string
  recipientPhone?: string
  onSend?: () => Promise<void>
}

function PhotoSendDialog({
  open, onClose, photos, orderNumber,
  ordererName, ordererPhone, recipientName, recipientPhone,
  onSend,
}: PhotoSendDialogProps) {
  const [checkedPhotos, setCheckedPhotos] = useState<Set<number>>(new Set())
  const [sendToOrderer, setSendToOrderer] = useState(!!ordererPhone)
  const [sendToRecipient, setSendToRecipient] = useState(!!recipientPhone)
  const [message, setMessage] = useState('')
  const [sending, setSending] = useState(false)
  const [sent, setSent] = useState(false)
  const [sendError, setSendError] = useState<string | null>(null)

  useEffect(() => {
    if (open) {
      setCheckedPhotos(new Set())
      setSendToOrderer(!!ordererPhone)
      setSendToRecipient(!!recipientPhone)
      setMessage('')
      setSending(false)
      setSent(false)
      setSendError(null)
    }
  }, [open, ordererPhone, recipientPhone])

  function toggleCheck(i: number) {
    setCheckedPhotos((prev) => {
      const next = new Set(prev)
      next.has(i) ? next.delete(i) : next.add(i)
      return next
    })
  }

  const canSend = checkedPhotos.size > 0 && (sendToOrderer || sendToRecipient)

  async function handleSend() {
    if (!canSend) return
    setSending(true)
    setSendError(null)
    try {
      if (onSend) {
        await onSend()
      } else {
        // TODO: 문자 발송 API 호출
        await new Promise((r) => setTimeout(r, 1000))
      }
      setSent(true)
      setTimeout(() => setSent(false), 2500)
    } catch (e) {
      setSendError(e instanceof Error ? e.message : '발송에 실패했습니다.')
    } finally {
      setSending(false)
    }
  }

  return (
    <Dialog open={open} onOpenChange={(next) => { if (!next) onClose() }}>
      <DialogContent className="sm:max-w-3xl max-h-[90vh] overflow-y-auto" showCloseButton={false}>
        <DialogTitle className="pr-6">
          문자 발송
          {orderNumber && <span className="ml-2 text-xs font-normal text-gray-400">{orderNumber}</span>}
        </DialogTitle>

        {/* 발송 대상 */}
        {(ordererName || recipientName) && (
          <div className="rounded-lg border bg-gray-50 p-3">
            <div className="mb-2 text-xs font-medium text-gray-500">발송 대상</div>
            <div className="space-y-2">
              {ordererName && (
                <label className="flex cursor-pointer items-center gap-3">
                  <input
                    type="checkbox"
                    checked={sendToOrderer}
                    onChange={(e) => setSendToOrderer(e.target.checked)}
                    className="accent-green-700"
                  />
                  <span className="w-12 shrink-0 text-xs text-gray-500">주문자</span>
                  <span className="text-sm font-medium">{ordererName}</span>
                  {ordererPhone && <span className="text-sm text-gray-400">{ordererPhone}</span>}
                </label>
              )}
              {recipientName && (
                <label className="flex cursor-pointer items-center gap-3">
                  <input
                    type="checkbox"
                    checked={sendToRecipient}
                    onChange={(e) => setSendToRecipient(e.target.checked)}
                    className="accent-green-700"
                  />
                  <span className="w-12 shrink-0 text-xs text-gray-500">받는분</span>
                  <span className="text-sm font-medium">{recipientName}</span>
                  {recipientPhone && <span className="text-sm text-gray-400">{recipientPhone}</span>}
                </label>
              )}
            </div>
          </div>
        )}

        {/* 문자 내용 + 발송 버튼 */}
        <div className="space-y-2 rounded-lg border bg-gray-50 p-3">
          <textarea
            placeholder="발송할 문자 내용을 입력하세요"
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            rows={2}
            className="w-full resize-none rounded-md border bg-white px-3 py-2 text-sm placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-green-700"
          />
          <div className="flex items-center justify-between">
            <span className="text-xs text-gray-500">
              {checkedPhotos.size > 0 ? `사진 ${checkedPhotos.size}장 선택됨` : '아래 사진을 선택하세요'}
            </span>
            <Button
              size="sm"
              disabled={!canSend || sending}
              onClick={handleSend}
              className={cn(
                'gap-1.5',
                sent ? 'bg-green-600 hover:bg-green-700' : 'bg-green-700 hover:bg-green-800',
              )}
            >
              <Send className="h-3.5 w-3.5" />
              {sent ? '발송 완료' : sending ? '발송 중…' : '문자 발송'}
            </Button>
          </div>
          {sendError && (
            <p className="text-xs text-red-500">{sendError}</p>
          )}
        </div>

        {/* 사진 그리드 */}
        {photos.length > 0 ? (
          <div className="grid grid-cols-2 gap-2 sm:grid-cols-3">
            {photos.map((src, i) => {
              const isChecked = checkedPhotos.has(i)
              return (
                <button
                  key={i}
                  type="button"
                  onClick={() => toggleCheck(i)}
                  className={cn(
                    'relative aspect-square overflow-hidden rounded-lg border-2 transition-all',
                    isChecked
                      ? 'border-green-600 ring-2 ring-green-300'
                      : 'border-transparent hover:border-gray-300',
                  )}
                >
                  <img src={src} alt={`배송사진 ${i + 1}`} className="h-full w-full object-cover" />
                  {isChecked && <div className="absolute inset-0 bg-green-600/20" />}
                  <div
                    className={cn(
                      'absolute right-1.5 top-1.5 flex h-5 w-5 items-center justify-center rounded-full border-2 transition-all',
                      isChecked
                        ? 'border-green-600 bg-green-600 text-white'
                        : 'border-white bg-black/30 text-transparent',
                    )}
                  >
                    <Check className="h-3 w-3" strokeWidth={3} />
                  </div>
                </button>
              )
            })}
          </div>
        ) : (
          <p className="py-2 text-center text-sm text-gray-400">등록된 배송사진이 없습니다.</p>
        )}

        <button
          type="button"
          onClick={onClose}
          className="absolute right-3 top-3 rounded-full p-1 text-gray-500 hover:bg-gray-100"
        >
          <X className="h-4 w-4" />
        </button>
      </DialogContent>
    </Dialog>
  )
}

// ---- 배송사진 썸네일 트리거 ----

interface DeliveryPhotoCellProps {
  photos?: string[]
  orderNumber?: string
  ordererName?: string
  ordererPhone?: string
  recipientName?: string
  recipientPhone?: string
}

export function DeliveryPhotoCell({ photos, orderNumber, ordererName, ordererPhone, recipientName, recipientPhone }: DeliveryPhotoCellProps) {
  const [open, setOpen] = useState(false)

  if (!photos || photos.length === 0) {
    return <span className="text-xs text-gray-300">-</span>
  }

  const extra = photos.length - 1

  return (
    <>
      <button
        type="button"
        onClick={(e) => { e.preventDefault(); e.stopPropagation(); setOpen(true) }}
        className="group flex items-center gap-1.5"
      >
        <div className="relative h-10 w-10 overflow-hidden rounded border bg-gray-100 transition-opacity group-hover:opacity-75">
          <img src={photos[0]} alt="배송사진" className="h-full w-full object-cover" />
        </div>
        {extra > 0 && (
          <span className="text-xs font-medium text-gray-500">+{extra}</span>
        )}
      </button>

      <PhotoSendDialog
        open={open} onClose={() => setOpen(false)}
        photos={photos} orderNumber={orderNumber}
        ordererName={ordererName} ordererPhone={ordererPhone}
        recipientName={recipientName} recipientPhone={recipientPhone}
      />
    </>
  )
}

// ---- 문자발송 상태 배지 (미발송이면 클릭 가능) ----

const messageStatusStyle: Record<MessageStatus, string> = {
  '미발송': 'bg-gray-100 text-gray-600 border-gray-200',
  '발송': 'bg-green-100 text-green-800 border-green-200',
  '실패': 'bg-red-100 text-red-800 border-red-200',
}

// ---- 발송 내역 다이얼로그 ----

function SmsHistoryDialog({ open, onClose, orderId, orderNumber }: {
  open: boolean; onClose: () => void; orderId: string; orderNumber?: string
}) {
  const [messages, setMessages] = useState<SmsMessageResult[]>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (!open) return
    setLoading(true)
    fetchSmsMessages(orderId)
      .then(setMessages)
      .finally(() => setLoading(false))
  }, [open, orderId])

  return (
    <Dialog open={open} onOpenChange={(next) => { if (!next) onClose() }}>
      <DialogContent className="sm:max-w-md max-h-[80vh] overflow-y-auto">
        <DialogTitle>
          문자 발송 내역
          {orderNumber && <span className="ml-2 text-xs font-normal text-gray-400">{orderNumber}</span>}
        </DialogTitle>
        {loading ? (
          <p className="py-6 text-center text-sm text-gray-400">불러오는 중...</p>
        ) : messages.length === 0 ? (
          <p className="py-6 text-center text-sm text-gray-400">발송 내역이 없습니다.</p>
        ) : (
          <div className="space-y-3">
            {messages[0]?.message && (
              <div className="rounded-lg border bg-gray-50 p-3 text-sm">
                <p className="mb-1 text-xs font-medium text-gray-500">발송 메시지</p>
                <p className="text-gray-800 whitespace-pre-wrap">{messages[0].message}</p>
              </div>
            )}
            <ul className="space-y-2">
              {messages.map((m) => (
                <li key={m.id} className="rounded-lg border p-3 text-sm space-y-1.5">
                  <div className="flex items-center justify-between">
                    <span className="font-medium">{m.receiver}</span>
                    <span className={cn(
                      'rounded-full px-2 py-0.5 text-xs font-medium',
                      m.status === 'SUCCESS' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
                    )}>
                      {m.status === 'SUCCESS' ? '성공' : '실패'}
                    </span>
                  </div>
                  {m.sentAt && <p className="text-xs text-gray-400">{m.sentAt}</p>}
                  {m.failReason && <p className="text-xs text-red-500">{m.failReason}</p>}
                  {m.imageUrl && (
                    <img src={m.imageUrl} alt="발송 사진" className="h-24 w-24 rounded border object-cover" />
                  )}
                </li>
              ))}
            </ul>
          </div>
        )}
      </DialogContent>
    </Dialog>
  )
}

// ---- 문자발송 상태 배지 ----

interface ClickableMessageBadgeProps {
  status: MessageStatus
  orderId?: string
  photos?: string[]
  orderNumber?: string
  ordererName?: string
  ordererPhone?: string
  recipientName?: string
  recipientPhone?: string
}

export function ClickableMessageBadge({
  status, orderId, photos, orderNumber,
  ordererName, ordererPhone, recipientName, recipientPhone,
}: ClickableMessageBadgeProps) {
  const [open, setOpen] = useState(false)

  function handleClick(e: React.MouseEvent) {
    e.preventDefault(); e.stopPropagation(); setOpen(true)
  }

  if (status === '발송') {
    return (
      <>
        <button type="button" onClick={handleClick}>
          <Badge variant="outline" className={cn(messageStatusStyle[status], 'cursor-pointer hover:bg-green-200')}>
            {status}
          </Badge>
        </button>
        {orderId && (
          <SmsHistoryDialog open={open} onClose={() => setOpen(false)} orderId={orderId} orderNumber={orderNumber} />
        )}
      </>
    )
  }

  if (status === '실패') {
    return (
      <>
        <button type="button" onClick={handleClick}>
          <Badge variant="outline" className={cn(messageStatusStyle[status], 'cursor-pointer hover:bg-red-200')}>
            {status}
          </Badge>
        </button>
        <PhotoSendDialog
          open={open} onClose={() => setOpen(false)}
          photos={photos ?? []} orderNumber={orderNumber}
          ordererName={ordererName} ordererPhone={ordererPhone}
          recipientName={recipientName} recipientPhone={recipientPhone}
          onSend={orderId ? () => resendMms(orderId) : undefined}
        />
      </>
    )
  }

  return (
    <>
      <button type="button" onClick={handleClick}>
        <Badge variant="outline" className={cn(messageStatusStyle[status], 'cursor-pointer hover:bg-gray-200')}>
          {status}
        </Badge>
      </button>
      <PhotoSendDialog
        open={open} onClose={() => setOpen(false)}
        photos={photos ?? []} orderNumber={orderNumber}
        ordererName={ordererName} ordererPhone={ordererPhone}
        recipientName={recipientName} recipientPhone={recipientPhone}
      />
    </>
  )
}

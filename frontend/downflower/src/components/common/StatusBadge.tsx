'use client'

import { useState } from 'react'
import { Badge } from '@/components/ui/badge'
import { OrderStatus, MessageStatus, DeliveryType, DeliveryTimeType } from '@/types/order'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog'

const orderStatusConfig: Record<OrderStatus, { label: string; className: string }> = {
  '발주대기': { label: '미확인', className: 'bg-red-100 text-red-700 border-red-300 font-bold animate-blink' },
  '수주완료': { label: '수주완료', className: 'bg-blue-100 text-blue-800 border-blue-200' },
  '배송완료': { label: '배송완료', className: 'bg-green-100 text-green-800 border-green-200' },
  '수주거절': { label: '수주거절', className: 'bg-red-100 text-red-800 border-red-200' },
  '주문취소': { label: '주문취소', className: 'bg-gray-100 text-gray-600 border-gray-200' },
}

const messageStatusConfig: Record<MessageStatus, { label: string; className: string }> = {
  '미발송': { label: '미발송', className: 'bg-gray-100 text-gray-600 border-gray-200' },
  '발송': { label: '발송', className: 'bg-green-100 text-green-800 border-green-200' },
  '실패': { label: '실패', className: 'bg-red-100 text-red-800 border-red-200' },
}

export function OrderStatusBadge({ status }: { status: OrderStatus }) {
  const config = orderStatusConfig[status]
  return (
    <Badge variant="outline" className={config.className}>
      {config.label}
    </Badge>
  )
}

const vendorStatusConfig = orderStatusConfig

export function VendorOrderStatusBadge({ status }: { status: OrderStatus }) {
  const config = vendorStatusConfig[status]
  return (
    <Badge variant="outline" className={config.className}>
      {config.label}
    </Badge>
  )
}

export function MessageStatusBadge({ status }: { status: MessageStatus }) {
  const config = messageStatusConfig[status]
  return (
    <Badge variant="outline" className={config.className}>
      {config.label}
    </Badge>
  )
}

export function AcceptOrderBadge({
  status,
  variant = 'admin',
  onAccept,
}: {
  status: OrderStatus
  variant?: 'admin' | 'vendor'
  onAccept: () => Promise<void>
}) {
  const [open, setOpen] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const badge = variant === 'vendor'
    ? <VendorOrderStatusBadge status={status} />
    : <OrderStatusBadge status={status} />

  if (status !== '발주대기') return badge

  return (
    <>
      <button
        type="button"
        onClick={(e) => { e.stopPropagation(); setError(null); setOpen(true) }}
        className="cursor-pointer focus:outline-none"
      >
        {badge}
      </button>
      <AlertDialog open={open} onOpenChange={(v) => { if (!loading) { setOpen(v); if (!v) setError(null) } }}>
        <AlertDialogContent size="sm">
          <AlertDialogHeader>
            <AlertDialogTitle>수주 승인</AlertDialogTitle>
            <AlertDialogDescription>수주를 승인하시겠습니까?</AlertDialogDescription>
          </AlertDialogHeader>
          {error && (
            <p className="px-1 text-sm text-red-500">{error}</p>
          )}
          <AlertDialogFooter>
            <AlertDialogCancel disabled={loading}>취소</AlertDialogCancel>
            <AlertDialogAction
              onClick={async (e) => {
                e.preventDefault()
                setLoading(true)
                setError(null)
                try {
                  await onAccept()
                  setOpen(false)
                } catch {
                  setError('수주 승인에 실패했습니다. 다시 시도해 주세요.')
                } finally {
                  setLoading(false)
                }
              }}
              disabled={loading}
            >
              {loading ? '처리 중…' : '승인'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  )
}

export function DeliveryTypeBadge({ deliveryType }: { deliveryType: DeliveryType }) {
  if (deliveryType === '즉시') {
    return <Badge variant="outline" className="bg-orange-100 text-orange-700 border-orange-200">즉시배송</Badge>
  }
  return <Badge variant="outline" className="bg-gray-100 text-gray-600 border-gray-200">일반배송</Badge>
}

const deliveryTimeTypeConfig: Record<DeliveryTimeType, string> = {
  '까지':     'bg-gray-100 text-gray-700 border-gray-200',
  '예식':     'bg-purple-100 text-purple-700 border-purple-200',
  '행사':     'bg-blue-100 text-blue-700 border-blue-200',
  '3시간이내': 'bg-orange-100 text-orange-800 border-orange-300',
  '직접입력': 'bg-gray-100 text-gray-700 border-gray-200',
}

export function DeliveryTimeTypeBadge({
  type,
  custom,
}: {
  type?: DeliveryTimeType
  custom?: string
}) {
  if (!type) return <span className="text-gray-400">-</span>
  const label = type === '직접입력' && custom ? custom : type
  return (
    <Badge variant="outline" className={deliveryTimeTypeConfig[type]}>
      {label}
    </Badge>
  )
}

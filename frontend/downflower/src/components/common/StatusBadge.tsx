import { Badge } from '@/components/ui/badge'
import { OrderStatus, MessageStatus, DeliveryType, DeliveryTimeType } from '@/types/order'

const orderStatusConfig: Record<OrderStatus, { label: string; className: string }> = {
  '발주대기': { label: '발주대기', className: 'bg-yellow-100 text-yellow-800 border-yellow-200' },
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

// 수주사 화면용 — 발주대기를 "미확인"으로 표시
const vendorStatusConfig: Record<OrderStatus, { label: string; className: string }> = {
  ...orderStatusConfig,
  '발주대기': { label: '미확인', className: 'bg-orange-100 text-orange-700 border-orange-200' },
}

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
  '3시간이내': 'bg-amber-100 text-amber-700 border-amber-200',
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

import { Badge } from '@/components/ui/badge'
import { OrderStatus, MessageStatus } from '@/types/order'

const orderStatusConfig: Record<OrderStatus, { label: string; className: string }> = {
  '발주대기': { label: '발주대기', className: 'bg-yellow-100 text-yellow-800 border-yellow-200' },
  '수주완료': { label: '수주완료', className: 'bg-blue-100 text-blue-800 border-blue-200' },
  '배송완료': { label: '배송완료', className: 'bg-green-100 text-green-800 border-green-200' },
  '수주거절': { label: '수주거절', className: 'bg-red-100 text-red-800 border-red-200' },
  '주문취소': { label: '주문취소', className: 'bg-gray-100 text-gray-600 border-gray-200' },
}

const messageStatusConfig: Record<MessageStatus, { label: string; className: string }> = {
  '미발송': { label: '미발송', className: 'bg-gray-100 text-gray-600 border-gray-200' },
  '발송성공': { label: '발송성공', className: 'bg-green-100 text-green-800 border-green-200' },
  '발송실패': { label: '발송실패', className: 'bg-red-100 text-red-800 border-red-200' },
}

export function OrderStatusBadge({ status }: { status: OrderStatus }) {
  const config = orderStatusConfig[status]
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

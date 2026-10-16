import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { OrderStatusBadge, MessageStatusBadge } from '@/components/common/StatusBadge'
import { mockOrders } from '@/lib/mock/orders'
import Link from 'next/link'

export default function VendorOrdersPage() {
  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">수주리스트</h1>

      <div className="rounded-lg border bg-white">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>주문번호</TableHead>
              <TableHead>발주일시</TableHead>
              <TableHead>배송예정</TableHead>
              <TableHead>상품명</TableHead>
              <TableHead>배송장소</TableHead>
              <TableHead className="text-right">금액</TableHead>
              <TableHead>상태</TableHead>
              <TableHead>문자발송</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {mockOrders.map((order) => (
              <TableRow key={order.id} className="cursor-pointer hover:bg-gray-50">
                <TableCell>
                  <Link
                    href={`/vendor/orders/${order.id}`}
                    className="font-mono text-xs text-blue-600 hover:underline"
                  >
                    {order.orderNumber}
                  </Link>
                </TableCell>
                <TableCell className="text-sm">{order.orderedAt}</TableCell>
                <TableCell className="text-sm">{order.deliveryAt}</TableCell>
                <TableCell className="text-sm">{order.productName}</TableCell>
                <TableCell className="text-sm">
                  {order.placeName} {order.roomName}
                </TableCell>
                <TableCell className="text-right text-sm">
                  {order.amount.toLocaleString()}원
                </TableCell>
                <TableCell>
                  <OrderStatusBadge status={order.status} />
                </TableCell>
                <TableCell>
                  <MessageStatusBadge status={order.messageStatus} />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </div>
  )
}

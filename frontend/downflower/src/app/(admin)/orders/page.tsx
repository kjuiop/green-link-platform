import Link from 'next/link'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { OrderStatusBadge, MessageStatusBadge } from '@/components/common/StatusBadge'
import { mockOrders } from '@/lib/mock/orders'
import { Plus } from 'lucide-react'

export default function OrdersPage() {
  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold">발주 리스트</h1>
        <Link
          href="/orders/new"
          className="inline-flex items-center gap-1.5 rounded-lg bg-green-700 px-2.5 py-1.5 text-sm font-medium text-white hover:bg-green-800"
        >
          <Plus className="h-4 w-4" />
          신규 발주
        </Link>
      </div>

      <div className="rounded-lg border bg-white">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>주문번호</TableHead>
              <TableHead>발주일시</TableHead>
              <TableHead>배송예정</TableHead>
              <TableHead>수주업체</TableHead>
              <TableHead>상품명</TableHead>
              <TableHead>배송장소</TableHead>
              <TableHead className="text-right">발주금액</TableHead>
              <TableHead>주문상태</TableHead>
              <TableHead>문자발송</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {mockOrders.map((order) => (
              <TableRow key={order.id} className="cursor-pointer hover:bg-gray-50">
                <TableCell>
                  <Link href={`/orders/${order.id}`} className="font-mono text-xs text-blue-600 hover:underline">
                    {order.orderNumber}
                  </Link>
                </TableCell>
                <TableCell className="text-sm">{order.orderedAt}</TableCell>
                <TableCell className="text-sm">{order.deliveryAt}</TableCell>
                <TableCell className="text-sm">{order.vendorName}</TableCell>
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

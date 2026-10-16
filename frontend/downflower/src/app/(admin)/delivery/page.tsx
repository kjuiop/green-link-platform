import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { OrderStatusBadge } from '@/components/common/StatusBadge'
import { mockOrders } from '@/lib/mock/orders'
import Link from 'next/link'

export default function DeliveryPage() {
  const activeOrders = mockOrders.filter((o) => o.status === '수주완료')

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">배송현황</h1>

      <div className="rounded-lg border bg-white">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>주문번호</TableHead>
              <TableHead>배송예정</TableHead>
              <TableHead>수주업체</TableHead>
              <TableHead>상품명</TableHead>
              <TableHead>배송장소</TableHead>
              <TableHead>주문자</TableHead>
              <TableHead>상태</TableHead>
              <TableHead>처리</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {activeOrders.map((order) => (
              <TableRow key={order.id}>
                <TableCell>
                  <Link href={`/orders/${order.id}`} className="font-mono text-xs text-blue-600 hover:underline">
                    {order.orderNumber}
                  </Link>
                </TableCell>
                <TableCell className="text-sm">{order.deliveryAt}</TableCell>
                <TableCell className="text-sm">{order.vendorName}</TableCell>
                <TableCell className="text-sm">{order.productName}</TableCell>
                <TableCell className="text-sm">
                  {order.placeName} {order.roomName}
                </TableCell>
                <TableCell className="text-sm">{order.ordererName}</TableCell>
                <TableCell>
                  <OrderStatusBadge status={order.status} />
                </TableCell>
                <TableCell>
                  <Link
                    href={`/orders/${order.id}`}
                    className="text-xs text-blue-600 hover:underline"
                  >
                    상세보기
                  </Link>
                </TableCell>
              </TableRow>
            ))}
            {activeOrders.length === 0 && (
              <TableRow>
                <TableCell colSpan={8} className="py-10 text-center text-gray-400">
                  배송 대기 중인 주문이 없습니다.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  )
}

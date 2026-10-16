import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { MessageStatusBadge } from '@/components/common/StatusBadge'
import { mockOrders } from '@/lib/mock/orders'
import Link from 'next/link'

export default function MessagesPage() {
  const sentOrders = mockOrders.filter((o) => o.messageStatus !== '미발송')

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">문자발송내역</h1>

      <div className="rounded-lg border bg-white">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>주문번호</TableHead>
              <TableHead>배송완료일시</TableHead>
              <TableHead>수주업체</TableHead>
              <TableHead>상품명</TableHead>
              <TableHead>주문자</TableHead>
              <TableHead>발송대상</TableHead>
              <TableHead>발송상태</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {sentOrders.map((order) => (
              <TableRow key={order.id}>
                <TableCell>
                  <Link href={`/orders/${order.id}`} className="font-mono text-xs text-blue-600 hover:underline">
                    {order.orderNumber}
                  </Link>
                </TableCell>
                <TableCell className="text-sm">{order.completedAt ?? '-'}</TableCell>
                <TableCell className="text-sm">{order.vendorName}</TableCell>
                <TableCell className="text-sm">{order.productName}</TableCell>
                <TableCell className="text-sm">{order.ordererName}</TableCell>
                <TableCell className="text-sm">
                  {[
                    order.sendPhotoToOrderer ? '주문자' : '',
                    order.sendPhotoToRecipient ? '받는 분' : '',
                  ]
                    .filter(Boolean)
                    .join(', ') || '-'}
                </TableCell>
                <TableCell>
                  <MessageStatusBadge status={order.messageStatus} />
                </TableCell>
              </TableRow>
            ))}
            {sentOrders.length === 0 && (
              <TableRow>
                <TableCell colSpan={7} className="py-10 text-center text-gray-400">
                  발송 내역이 없습니다.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  )
}

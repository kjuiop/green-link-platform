import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { mockOrders } from '@/lib/mock/orders'
import Link from 'next/link'

export default function VendorSettlementPage() {
  const completed = mockOrders.filter((o) => o.status === '배송완료')
  const total = completed.reduce((sum, o) => sum + o.amount, 0)

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">정산내역</h1>

      <div className="grid grid-cols-2 gap-4">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-500">완료 주문</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">{completed.length}건</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-green-600">총 수주금액</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-green-600">{total.toLocaleString()}원</p>
          </CardContent>
        </Card>
      </div>

      <div className="rounded-lg border bg-white">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>주문번호</TableHead>
              <TableHead>배송완료일시</TableHead>
              <TableHead>상품명</TableHead>
              <TableHead className="text-right">금액</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {completed.map((order) => (
              <TableRow key={order.id}>
                <TableCell>
                  <Link href={`/vendor/orders/${order.id}`} className="font-mono text-xs text-blue-600 hover:underline">
                    {order.orderNumber}
                  </Link>
                </TableCell>
                <TableCell className="text-sm">{order.completedAt ?? '-'}</TableCell>
                <TableCell className="text-sm">{order.productName}</TableCell>
                <TableCell className="text-right text-sm">{order.amount.toLocaleString()}원</TableCell>
              </TableRow>
            ))}
            {completed.length === 0 && (
              <TableRow>
                <TableCell colSpan={4} className="py-10 text-center text-gray-400">
                  정산 내역이 없습니다.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  )
}

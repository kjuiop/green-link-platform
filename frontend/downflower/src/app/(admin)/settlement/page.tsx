import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { mockOrders } from '@/lib/mock/orders'

export default function SettlementPage() {
  const completed = mockOrders.filter((o) => o.status === '배송완료')
  const total = completed.reduce((sum, o) => sum + o.amount, 0)

  const byVendor = completed.reduce<Record<string, { name: string; count: number; amount: number }>>(
    (acc, o) => {
      if (!acc[o.vendorId]) acc[o.vendorId] = { name: o.vendorName, count: 0, amount: 0 }
      acc[o.vendorId].count += 1
      acc[o.vendorId].amount += o.amount
      return acc
    },
    {}
  )

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">정산내역</h1>

      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
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
            <CardTitle className="text-sm font-medium text-green-600">총 정산금액</CardTitle>
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
              <TableHead>업체명</TableHead>
              <TableHead className="text-right">완료 건수</TableHead>
              <TableHead className="text-right">정산금액</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {Object.values(byVendor).map((v) => (
              <TableRow key={v.name}>
                <TableCell>{v.name}</TableCell>
                <TableCell className="text-right">{v.count}건</TableCell>
                <TableCell className="text-right">{v.amount.toLocaleString()}원</TableCell>
              </TableRow>
            ))}
            {Object.keys(byVendor).length === 0 && (
              <TableRow>
                <TableCell colSpan={3} className="py-10 text-center text-gray-400">
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

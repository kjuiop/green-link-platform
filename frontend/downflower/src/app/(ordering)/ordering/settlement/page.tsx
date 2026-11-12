'use client'

import { useState, useMemo } from 'react'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Input } from '@/components/ui/input'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'

interface MockOrder {
  id: string
  orderNumber: string
  vendorName: string
  productName: string
  placeName: string
  ordererName: string
  deliveredAt: string
  orderAmount: number
  vendorAmount: number
}

const MOCK_MY_ORDERS: MockOrder[] = [
  { id: '1',  orderNumber: 'DN-20261101-001', vendorName: '꽃향기',   productName: '축하화환 대',  placeName: '강남구청',      ordererName: '김민준', deliveredAt: '2026-11-01', orderAmount: 150000, vendorAmount: 120000 },
  { id: '2',  orderNumber: 'DN-20261102-002', vendorName: '꽃향기',   productName: '근조화환 중',  placeName: '강남구청',      ordererName: '이서연', deliveredAt: '2026-11-02', orderAmount: 100000, vendorAmount: 80000  },
  { id: '3',  orderNumber: 'DN-20261103-003', vendorName: '한강꽃집', productName: '동양란',        placeName: '서초 웨딩홀',   ordererName: '박도윤', deliveredAt: '2026-11-03', orderAmount: 80000,  vendorAmount: 65000  },
  { id: '4',  orderNumber: 'DN-20261104-004', vendorName: '한강꽃집', productName: '축하화환 소',  placeName: '마포 사무실',   ordererName: '정시우', deliveredAt: '2026-11-04', orderAmount: 90000,  vendorAmount: 72000  },
  { id: '5',  orderNumber: 'DN-20261108-005', vendorName: '남산꽃원', productName: '근조화환 대',  placeName: '용산 장례식장', ordererName: '최지아', deliveredAt: '2026-11-08', orderAmount: 200000, vendorAmount: 160000 },
  { id: '6',  orderNumber: 'DN-20261110-006', vendorName: '남산꽃원', productName: '서양란',        placeName: '여의도 사무실', ordererName: '윤준서', deliveredAt: '2026-11-10', orderAmount: 85000,  vendorAmount: 68000  },
]

function formatKRW(amount: number) {
  return amount.toLocaleString('ko-KR') + '원'
}

function today() {
  return new Date().toISOString().slice(0, 10)
}

function firstDayOfMonth() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-01`
}

export default function OrderingSettlementPage() {
  const [from, setFrom] = useState(firstDayOfMonth())
  const [to, setTo] = useState(today())

  const filteredOrders = useMemo(() => {
    return MOCK_MY_ORDERS.filter((o) => o.deliveredAt >= from && o.deliveredAt <= to)
  }, [from, to])

  const totalCount = filteredOrders.length
  const totalOrderAmount = filteredOrders.reduce((s, o) => s + o.orderAmount, 0)
  const totalVendorAmount = filteredOrders.reduce((s, o) => s + o.vendorAmount, 0)

  const vendorGroups = useMemo(() => {
    const map = new Map<string, { count: number; orderAmount: number; vendorAmount: number }>()
    for (const o of filteredOrders) {
      const g = map.get(o.vendorName) ?? { count: 0, orderAmount: 0, vendorAmount: 0 }
      g.count++
      g.orderAmount += o.orderAmount
      g.vendorAmount += o.vendorAmount
      map.set(o.vendorName, g)
    }
    return Array.from(map.entries())
  }, [filteredOrders])

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">정산내역</h1>

      {/* 기간 필터 */}
      <div className="flex flex-wrap items-end gap-2 rounded-lg border bg-white p-4">
        <div>
          <p className="mb-1 text-xs text-gray-500">시작일</p>
          <Input type="date" value={from} onChange={(e) => setFrom(e.target.value)} className="w-36" />
        </div>
        <span className="mb-2 text-gray-400">~</span>
        <div>
          <p className="mb-1 text-xs text-gray-500">종료일</p>
          <Input type="date" value={to} onChange={(e) => setTo(e.target.value)} className="w-36" />
        </div>
      </div>

      {/* 요약 카드 */}
      <div className="grid grid-cols-3 gap-3">
        <Card>
          <CardHeader className="pb-1 pt-3">
            <CardTitle className="text-xs font-medium text-gray-500">배송완료 건수</CardTitle>
          </CardHeader>
          <CardContent className="pb-3">
            <p className="text-2xl font-bold">{totalCount}건</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-1 pt-3">
            <CardTitle className="text-xs font-medium text-gray-500">발주금액 합계</CardTitle>
          </CardHeader>
          <CardContent className="pb-3">
            <p className="text-xl font-bold text-gray-800">{formatKRW(totalOrderAmount)}</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-1 pt-3">
            <CardTitle className="text-xs font-medium text-blue-600">지급금액 합계</CardTitle>
          </CardHeader>
          <CardContent className="pb-3">
            <p className="text-xl font-bold text-blue-700">{formatKRW(totalVendorAmount)}</p>
          </CardContent>
        </Card>
      </div>

      {/* 수주사별 소계 */}
      {vendorGroups.length > 0 && (
        <div className="rounded-lg border bg-white">
          <div className="border-b px-4 py-2.5 text-sm font-medium text-gray-700">수주사별 소계</div>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>수주사</TableHead>
                <TableHead className="text-right">건수</TableHead>
                <TableHead className="text-right">발주금액</TableHead>
                <TableHead className="text-right">지급금액</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {vendorGroups.map(([vendorName, g]) => (
                <TableRow key={vendorName}>
                  <TableCell className="font-medium">{vendorName}</TableCell>
                  <TableCell className="text-right text-sm">{g.count}건</TableCell>
                  <TableCell className="text-right text-sm">{formatKRW(g.orderAmount)}</TableCell>
                  <TableCell className="text-right text-sm font-medium text-blue-700">{formatKRW(g.vendorAmount)}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}

      {/* 주문 전체 목록 */}
      <div className="rounded-lg border bg-white">
        <div className="border-b px-4 py-2.5 text-sm font-medium text-gray-700">주문 목록</div>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>주문번호</TableHead>
              <TableHead>배송일</TableHead>
              <TableHead>수주사</TableHead>
              <TableHead>상품명</TableHead>
              <TableHead>배달장소</TableHead>
              <TableHead>주문자</TableHead>
              <TableHead className="text-right">발주금액</TableHead>
              <TableHead className="text-right">지급금액</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filteredOrders.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} className="py-10 text-center text-sm text-gray-400">
                  해당 기간에 배송완료된 주문이 없습니다.
                </TableCell>
              </TableRow>
            ) : (
              filteredOrders.map((o) => (
                <TableRow key={o.id}>
                  <TableCell className="font-mono text-xs text-blue-600">{o.orderNumber}</TableCell>
                  <TableCell className="text-sm">{o.deliveredAt}</TableCell>
                  <TableCell className="text-sm">{o.vendorName}</TableCell>
                  <TableCell className="text-sm">{o.productName}</TableCell>
                  <TableCell className="text-sm">{o.placeName}</TableCell>
                  <TableCell className="text-sm">{o.ordererName}</TableCell>
                  <TableCell className="text-right text-sm">{formatKRW(o.orderAmount)}</TableCell>
                  <TableCell className="text-right text-sm font-medium text-blue-700">
                    {formatKRW(o.vendorAmount)}
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
        {filteredOrders.length > 0 && (
          <div className="flex justify-end gap-6 border-t bg-gray-50 px-4 py-3 text-sm">
            <span className="text-gray-600">합계: <strong>{totalCount}건</strong></span>
            <span className="text-gray-600">발주금액: <strong>{formatKRW(totalOrderAmount)}</strong></span>
            <span className="text-blue-700">지급금액: <strong>{formatKRW(totalVendorAmount)}</strong></span>
          </div>
        )}
      </div>
    </div>
  )
}

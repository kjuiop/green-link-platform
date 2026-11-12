'use client'

import { useState, useMemo, useEffect } from 'react'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Input } from '@/components/ui/input'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'

type CompanyType = 'ORDERING' | 'VENDOR' | 'BOTH'
type SettlementTab = 'ordering' | 'vendor'

interface MockOrder {
  id: string
  orderNumber: string
  orderingCompanyName: string
  vendorName: string
  productName: string
  placeName: string
  ordererName: string
  deliveredAt: string
  orderAmount: number
  vendorAmount: number
}

const MOCK_ORDERS: MockOrder[] = [
  { id: '1',  orderNumber: 'DN-20261101-001', orderingCompanyName: '다운플라워', vendorName: '꽃향기',    productName: '축하화환 대',  placeName: '강남구청',      ordererName: '김민준', deliveredAt: '2026-11-01', orderAmount: 150000, vendorAmount: 120000 },
  { id: '2',  orderNumber: 'DN-20261102-002', orderingCompanyName: '다운플라워', vendorName: '꽃향기',    productName: '근조화환 중',  placeName: '강남구청',      ordererName: '이서연', deliveredAt: '2026-11-02', orderAmount: 100000, vendorAmount: 80000  },
  { id: '3',  orderNumber: 'DN-20261103-003', orderingCompanyName: '다운플라워', vendorName: '꽃향기',    productName: '동양란',        placeName: '서초 웨딩홀',   ordererName: '박도윤', deliveredAt: '2026-11-03', orderAmount: 80000,  vendorAmount: 65000  },
  { id: '4',  orderNumber: 'DN-20261104-004', orderingCompanyName: '다운플라워', vendorName: '한강꽃집',  productName: '근조화환 대',  placeName: '용산 장례식장', ordererName: '최지아', deliveredAt: '2026-11-01', orderAmount: 200000, vendorAmount: 160000 },
  { id: '5',  orderNumber: 'DN-20261105-005', orderingCompanyName: '다운플라워', vendorName: '한강꽃집',  productName: '축하화환 중',  placeName: '여의도 증권사', ordererName: '윤준서', deliveredAt: '2026-11-02', orderAmount: 120000, vendorAmount: 96000  },
  { id: '6',  orderNumber: 'DN-20261106-006', orderingCompanyName: '그린플라워', vendorName: '다운플라워', productName: '서양란',       placeName: '마포 사무실',   ordererName: '임채원', deliveredAt: '2026-11-05', orderAmount: 85000,  vendorAmount: 68000  },
  { id: '7',  orderNumber: 'DN-20261107-007', orderingCompanyName: '그린플라워', vendorName: '다운플라워', productName: '근조화환 중',  placeName: '중구 장례식장', ordererName: '한지호', deliveredAt: '2026-11-02', orderAmount: 110000, vendorAmount: 88000  },
  { id: '8',  orderNumber: 'DN-20261108-008', orderingCompanyName: '블룸코리아', vendorName: '다운플라워', productName: '쌀화환',       placeName: '송파 웨딩홀',   ordererName: '오서준', deliveredAt: '2026-11-05', orderAmount: 130000, vendorAmount: 104000 },
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

function SummaryCards({ count, orderAmount, vendorAmount }: { count: number; orderAmount: number; vendorAmount: number }) {
  return (
    <div className="grid grid-cols-3 gap-3">
      <Card>
        <CardHeader className="pb-1 pt-3"><CardTitle className="text-xs font-medium text-gray-500">배송완료 건수</CardTitle></CardHeader>
        <CardContent className="pb-3"><p className="text-2xl font-bold">{count}건</p></CardContent>
      </Card>
      <Card>
        <CardHeader className="pb-1 pt-3"><CardTitle className="text-xs font-medium text-gray-500">발주금액 합계</CardTitle></CardHeader>
        <CardContent className="pb-3"><p className="text-xl font-bold text-gray-800">{formatKRW(orderAmount)}</p></CardContent>
      </Card>
      <Card>
        <CardHeader className="pb-1 pt-3"><CardTitle className="text-xs font-medium text-blue-600">지급금액 합계</CardTitle></CardHeader>
        <CardContent className="pb-3"><p className="text-xl font-bold text-blue-700">{formatKRW(vendorAmount)}</p></CardContent>
      </Card>
    </div>
  )
}

function OrderTable({ orders, showVendor, showOrdering }: { orders: MockOrder[]; showVendor?: boolean; showOrdering?: boolean }) {
  const colSpan = 6 + (showVendor ? 1 : 0) + (showOrdering ? 1 : 0)
  return (
    <div className="rounded-lg border bg-white">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>주문번호</TableHead>
            <TableHead>배송일</TableHead>
            {showVendor && <TableHead>수주사</TableHead>}
            {showOrdering && <TableHead>발주사</TableHead>}
            <TableHead>상품명</TableHead>
            <TableHead>배달장소</TableHead>
            <TableHead>주문자</TableHead>
            <TableHead className="text-right">발주금액</TableHead>
            <TableHead className="text-right">지급금액</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {orders.length === 0 ? (
            <TableRow>
              <TableCell colSpan={colSpan} className="py-10 text-center text-sm text-gray-400">
                해당 기간에 배송완료된 주문이 없습니다.
              </TableCell>
            </TableRow>
          ) : (
            orders.map((o) => (
              <TableRow key={o.id}>
                <TableCell className="font-mono text-xs text-blue-600">{o.orderNumber}</TableCell>
                <TableCell className="text-sm">{o.deliveredAt}</TableCell>
                {showVendor && <TableCell className="text-sm">{o.vendorName}</TableCell>}
                {showOrdering && <TableCell className="text-sm">{o.orderingCompanyName}</TableCell>}
                <TableCell className="text-sm">{o.productName}</TableCell>
                <TableCell className="text-sm">{o.placeName}</TableCell>
                <TableCell className="text-sm">{o.ordererName}</TableCell>
                <TableCell className="text-right text-sm">{formatKRW(o.orderAmount)}</TableCell>
                <TableCell className="text-right text-sm font-medium text-blue-700">{formatKRW(o.vendorAmount)}</TableCell>
              </TableRow>
            ))
          )}
        </TableBody>
      </Table>
      {orders.length > 0 && (
        <div className="flex justify-end gap-6 border-t bg-gray-50 px-4 py-3 text-sm">
          <span className="text-gray-600">합계: <strong>{orders.length}건</strong></span>
          <span className="text-gray-600">발주금액: <strong>{formatKRW(orders.reduce((s, o) => s + o.orderAmount, 0))}</strong></span>
          <span className="text-blue-700">지급금액: <strong>{formatKRW(orders.reduce((s, o) => s + o.vendorAmount, 0))}</strong></span>
        </div>
      )}
    </div>
  )
}

export default function MySettlementPage() {
  const [from, setFrom] = useState(firstDayOfMonth())
  const [to, setTo] = useState(today())
  const [companyType, setCompanyType] = useState<CompanyType>('BOTH')
  const [myCompanyName, setMyCompanyName] = useState('다운플라워')
  const [tab, setTab] = useState<SettlementTab>('ordering')

  useEffect(() => {
    const stored = localStorage.getItem('companyType') as CompanyType | null
    const name = localStorage.getItem('companyName') ?? localStorage.getItem('name') ?? '다운플라워'
    if (stored) setCompanyType(stored)
    setMyCompanyName(name)
    if (stored === 'VENDOR') setTab('vendor')
  }, [])

  const showOrdering = companyType === 'ORDERING' || companyType === 'BOTH'
  const showVendor = companyType === 'VENDOR' || companyType === 'BOTH'

  const orderingOrders = useMemo(() => {
    if (!showOrdering) return []
    return MOCK_ORDERS.filter(
      (o) => o.orderingCompanyName === myCompanyName && o.deliveredAt >= from && o.deliveredAt <= to
    )
  }, [showOrdering, myCompanyName, from, to])

  const vendorOrders = useMemo(() => {
    if (!showVendor) return []
    return MOCK_ORDERS.filter(
      (o) => o.vendorName === myCompanyName && o.deliveredAt >= from && o.deliveredAt <= to
    )
  }, [showVendor, myCompanyName, from, to])

  const currentOrders = tab === 'ordering' ? orderingOrders : vendorOrders
  const totalOrderAmount = currentOrders.reduce((s, o) => s + o.orderAmount, 0)
  const totalVendorAmount = currentOrders.reduce((s, o) => s + o.vendorAmount, 0)

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

      {/* BOTH일 때만 탭 표시 */}
      {companyType === 'BOTH' && (
        <div className="flex gap-1 rounded-lg border bg-white p-1 w-fit">
          <button
            onClick={() => setTab('ordering')}
            className={`rounded-md px-4 py-1.5 text-sm font-medium transition-colors ${
              tab === 'ordering' ? 'bg-green-600 text-white' : 'text-gray-600 hover:bg-gray-100'
            }`}
          >
            발주 정산
          </button>
          <button
            onClick={() => setTab('vendor')}
            className={`rounded-md px-4 py-1.5 text-sm font-medium transition-colors ${
              tab === 'vendor' ? 'bg-green-600 text-white' : 'text-gray-600 hover:bg-gray-100'
            }`}
          >
            수주 정산
          </button>
        </div>
      )}

      <SummaryCards
        count={currentOrders.length}
        orderAmount={totalOrderAmount}
        vendorAmount={totalVendorAmount}
      />

      <OrderTable
        orders={currentOrders}
        showVendor={tab === 'ordering'}
        showOrdering={tab === 'vendor'}
      />
    </div>
  )
}

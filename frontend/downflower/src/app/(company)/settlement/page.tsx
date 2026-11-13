'use client'

import { useState, useEffect, useCallback } from 'react'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Input } from '@/components/ui/input'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { AccountInfoBanner } from '@/components/settlement/AccountInfoBanner'
import { fetchMySettlement, SettlementOrder, totalAmountOf } from '@/lib/api/settlementApi'

type SettlementTab = 'vendor' | 'ordering'

function formatKRW(n: number) {
  return n.toLocaleString('ko-KR') + '원'
}

function today() {
  return new Date().toISOString().slice(0, 10)
}

function firstDayOfMonth() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-01`
}

function DiffCards({
  vendorOrders,
  orderingOrders,
}: {
  vendorOrders: SettlementOrder[]
  orderingOrders: SettlementOrder[]
}) {
  const vendorSum = vendorOrders.reduce((s, o) => s + (o.vendorAmount ?? 0), 0)
  const orderingSum = orderingOrders.reduce((s, o) => s + totalAmountOf(o), 0)
  const diff = vendorSum - orderingSum
  return (
    <div className="grid grid-cols-3 gap-3">
      <Card>
        <CardHeader className="pb-1 pt-3">
          <CardTitle className="text-xs font-medium text-blue-600">수주금액 합계</CardTitle>
        </CardHeader>
        <CardContent className="pb-3">
          <p className="text-xl font-bold text-blue-700">{formatKRW(vendorSum)}</p>
        </CardContent>
      </Card>
      <Card>
        <CardHeader className="pb-1 pt-3">
          <CardTitle className="text-xs font-medium text-gray-500">발주금액 합계</CardTitle>
        </CardHeader>
        <CardContent className="pb-3">
          <p className="text-xl font-bold text-gray-800">{formatKRW(orderingSum)}</p>
        </CardContent>
      </Card>
      <Card className={diff < 0 ? 'border-red-200' : 'border-green-200'}>
        <CardHeader className="pb-1 pt-3">
          <CardTitle className={`text-xs font-medium ${diff < 0 ? 'text-red-600' : 'text-green-600'}`}>
            차액 (수주−발주)
          </CardTitle>
        </CardHeader>
        <CardContent className="pb-3">
          <p className={`text-xl font-bold ${diff < 0 ? 'text-red-600' : 'text-green-700'}`}>
            {diff < 0 ? '-' : '+'}{formatKRW(Math.abs(diff))}
          </p>
        </CardContent>
      </Card>
    </div>
  )
}

function SummaryCards({ count, mainAmount, mainLabel }: { count: number; mainAmount: number; mainLabel: string }) {
  return (
    <div className="grid grid-cols-2 gap-3">
      <Card>
        <CardHeader className="pb-1 pt-3"><CardTitle className="text-xs font-medium text-gray-500">배송완료 건수</CardTitle></CardHeader>
        <CardContent className="pb-3"><p className="text-2xl font-bold">{count}건</p></CardContent>
      </Card>
      <Card>
        <CardHeader className="pb-1 pt-3"><CardTitle className="text-xs font-medium text-blue-600">{mainLabel} 합계</CardTitle></CardHeader>
        <CardContent className="pb-3"><p className="text-xl font-bold text-blue-700">{formatKRW(mainAmount)}</p></CardContent>
      </Card>
    </div>
  )
}

function OrderTable({ orders, isVendor }: { orders: SettlementOrder[]; isVendor: boolean }) {
  return (
    <div className="rounded-lg border bg-white">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>주문번호</TableHead>
            <TableHead>배송일</TableHead>
            {isVendor ? <TableHead>발주사</TableHead> : <TableHead>수주사</TableHead>}
            <TableHead>상품명</TableHead>
            <TableHead>배달장소</TableHead>
            <TableHead>주문자</TableHead>
            {isVendor
              ? <TableHead className="text-right">수주금액</TableHead>
              : <TableHead className="text-right">발주금액</TableHead>
            }
          </TableRow>
        </TableHeader>
        <TableBody>
          {orders.length === 0 ? (
            <TableRow>
              <TableCell colSpan={7} className="py-10 text-center text-sm text-gray-400">
                해당 기간에 배송완료된 주문이 없습니다.
              </TableCell>
            </TableRow>
          ) : (
            orders.map((o) => (
              <TableRow key={o.orderId}>
                <TableCell className="font-mono text-xs text-blue-600">{o.orderNumber}</TableCell>
                <TableCell className="text-sm">{o.deliveredAt?.slice(0, 10)}</TableCell>
                <TableCell className="text-sm">{o.counterpartCompanyName}</TableCell>
                <TableCell className="text-sm">{o.productName}</TableCell>
                <TableCell className="text-sm">{o.placeName}</TableCell>
                <TableCell className="text-sm">{o.ordererName}</TableCell>
                <TableCell className="text-right text-sm font-medium text-blue-700">
                  {isVendor
                    ? (o.vendorAmount != null ? formatKRW(o.vendorAmount) : '-')
                    : formatKRW(totalAmountOf(o))
                  }
                </TableCell>
              </TableRow>
            ))
          )}
        </TableBody>
      </Table>
      {orders.length > 0 && (
        <div className="flex justify-end gap-6 border-t bg-gray-50 px-4 py-3 text-sm">
          <span className="text-gray-600">합계: <strong>{orders.length}건</strong></span>
          {isVendor
            ? <span className="text-blue-700">수주금액: <strong>{formatKRW(orders.reduce((s, o) => s + (o.vendorAmount ?? 0), 0))}</strong></span>
            : <span className="text-blue-700">발주금액: <strong>{formatKRW(orders.reduce((s, o) => s + totalAmountOf(o), 0))}</strong></span>
          }
        </div>
      )}
    </div>
  )
}

export default function MySettlementPage() {
  const [from, setFrom] = useState(firstDayOfMonth())
  const [to, setTo] = useState(today())
  const [companyType, setCompanyType] = useState<'VENDOR' | 'ORDERING' | 'BOTH'>('VENDOR')
  const [tab, setTab] = useState<SettlementTab>('vendor')
  const [vendorOrders, setVendorOrders] = useState<SettlementOrder[]>([])
  const [orderingOrders, setOrderingOrders] = useState<SettlementOrder[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const load = useCallback(() => {
    if (!from || !to) return
    setLoading(true)
    setError('')
    fetchMySettlement(from, to)
      .then((result) => {
        setCompanyType(result.companyType)
        setVendorOrders(result.vendorOrders)
        setOrderingOrders(result.orderingOrders)
        if (result.companyType === 'ORDERING') setTab('ordering')
      })
      .catch(() => setError('정산 내역을 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }, [from, to])

  useEffect(() => { load() }, [load])

  const activeOrders = tab === 'vendor' ? vendorOrders : orderingOrders
  const isVendorTab = tab === 'vendor'
  const mainAmount = isVendorTab
    ? vendorOrders.reduce((s, o) => s + (o.vendorAmount ?? 0), 0)
    : orderingOrders.reduce((s, o) => s + totalAmountOf(o), 0)
  const mainLabel = isVendorTab ? '수주금액' : '발주금액'

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">정산내역</h1>

      <AccountInfoBanner />

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

      {error && <p className="text-sm text-red-500">{error}</p>}

      {/* BOTH일 때 차액 카드 */}
      {!loading && companyType === 'BOTH' && (
        <DiffCards vendorOrders={vendorOrders} orderingOrders={orderingOrders} />
      )}

      {/* BOTH일 때 탭 */}
      {companyType === 'BOTH' && (
        <div className="flex gap-1 rounded-lg border bg-white p-1 w-fit">
          <button
            onClick={() => setTab('vendor')}
            className={`rounded-md px-4 py-1.5 text-sm font-medium transition-colors ${
              tab === 'vendor' ? 'bg-green-600 text-white' : 'text-gray-600 hover:bg-gray-100'
            }`}
          >
            수주 정산
          </button>
          <button
            onClick={() => setTab('ordering')}
            className={`rounded-md px-4 py-1.5 text-sm font-medium transition-colors ${
              tab === 'ordering' ? 'bg-green-600 text-white' : 'text-gray-600 hover:bg-gray-100'
            }`}
          >
            발주 정산
          </button>
        </div>
      )}

      {/* 단일 타입 요약 카드 */}
      {companyType !== 'BOTH' && (
        <SummaryCards
          count={loading ? 0 : activeOrders.length}
          mainAmount={loading ? 0 : mainAmount}
          mainLabel={mainLabel}
        />
      )}

      {loading ? (
        <p className="py-10 text-center text-sm text-gray-400">불러오는 중...</p>
      ) : (
        <OrderTable orders={activeOrders} isVendor={isVendorTab} />
      )}
    </div>
  )
}

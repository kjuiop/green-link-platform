'use client'

import { useState, useEffect, useCallback } from 'react'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Input } from '@/components/ui/input'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { AccountInfoBanner } from '@/components/settlement/AccountInfoBanner'
import { fetchMySettlement, SettlementOrder, SettlementType } from '@/lib/api/settlementApi'

type CompanyType = 'ORDERING' | 'VENDOR' | 'BOTH'
type SettlementTab = 'ordering' | 'vendor'

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

function OrderTable({ orders, type }: { orders: SettlementOrder[]; type: SettlementType }) {
  const isVendor = type === 'VENDOR'
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
                    : formatKRW(o.orderAmount)
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
            : <span className="text-blue-700">발주금액: <strong>{formatKRW(orders.reduce((s, o) => s + o.orderAmount, 0))}</strong></span>
          }
        </div>
      )}
    </div>
  )
}

export default function MySettlementPage() {
  const [from, setFrom] = useState(firstDayOfMonth())
  const [to, setTo] = useState(today())
  const [companyType, setCompanyType] = useState<CompanyType>('VENDOR')
  const [tab, setTab] = useState<SettlementTab>('vendor')
  const [orders, setOrders] = useState<SettlementOrder[]>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    const stored = localStorage.getItem('companyType') as CompanyType | null
    if (stored) {
      setCompanyType(stored)
      if (stored === 'ORDERING') setTab('ordering')
    }
  }, [])

  const activeType: SettlementType = tab === 'ordering' ? 'ORDERING' : 'VENDOR'

  const load = useCallback(() => {
    if (!from || !to) return
    setLoading(true)
    const type: SettlementType = companyType === 'ORDERING' ? 'ORDERING'
      : companyType === 'VENDOR' ? 'VENDOR'
      : activeType
    fetchMySettlement(from, to, type)
      .then(setOrders)
      .catch(() => setOrders([]))
      .finally(() => setLoading(false))
  }, [from, to, companyType, activeType])

  useEffect(() => { load() }, [load])

  const mainAmount = activeType === 'VENDOR'
    ? orders.reduce((s, o) => s + (o.vendorAmount ?? 0), 0)
    : orders.reduce((s, o) => s + o.orderAmount, 0)
  const mainLabel = activeType === 'VENDOR' ? '수주금액' : '발주금액'

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
        count={loading ? 0 : orders.length}
        mainAmount={loading ? 0 : mainAmount}
        mainLabel={mainLabel}
      />

      {loading ? (
        <p className="py-10 text-center text-sm text-gray-400">불러오는 중...</p>
      ) : (
        <OrderTable orders={orders} type={activeType} />
      )}
    </div>
  )
}

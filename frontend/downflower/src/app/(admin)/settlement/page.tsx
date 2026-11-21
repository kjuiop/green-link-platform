'use client'

import { useCallback, useEffect, useState } from 'react'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import {
  fetchMySettlement,
  fetchAdminSettlementDetail,
  getSettlementExcelUrl,
  totalAmountOf,
  type SettlementOrder,
} from '@/lib/api/settlementApi'
import { fetchCompanies, type CompanyInfo, type CompanyType } from '@/lib/api/companyApi'
import { AccountInfoBanner } from '@/components/settlement/AccountInfoBanner'

function formatKRW(n: number) {
  return n.toLocaleString('ko-KR') + '원'
}

function today() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function firstDayOfMonth() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-01`
}

function formatDateTime(v: string | null) {
  return v ? v.replace('T', ' ').slice(0, 16) : '-'
}

function calcVendorSettlementTotal(orders: SettlementOrder[]): number {
  return orders.reduce((s, o) => {
    const gross = totalAmountOf(o)
    const fee = o.vendorFee ?? 0
    return s + (o.vendorAmount ?? (gross - fee))
  }, 0)
}

function calcOrderingSettlementTotal(orders: SettlementOrder[]): number {
  return orders.reduce((s, o) => s + (o.orderingSettlementAmount ?? totalAmountOf(o)), 0)
}

function deriveVendorFeeLabel(orders: SettlementOrder[]): string | null {
  if (orders.length === 0) return null
  const fee = orders[0].vendorFee
  if (!fee || fee === 0) return null
  return `수수료 ${fee.toLocaleString('ko-KR')}원/건`
}

function deriveOrderingDiscountLabel(orders: SettlementOrder[]): string | null {
  if (orders.length === 0) return null
  const first = orders[0]
  if (!first.orderingFeeDiscountType || first.orderingFeeDiscountType === 'NONE') return null
  if (first.orderingFeeDiscountType === 'FIXED') {
    const amt = first.orderingDiscountAmount ?? 0
    return amt > 0 ? `수수료 감면 ${amt.toLocaleString('ko-KR')}원/건` : null
  }
  if (first.orderingFeeDiscountType === 'PERCENT') {
    const fee = first.vendorFee
    const discount = first.orderingDiscountAmount ?? 0
    if (fee && fee > 0) {
      return `수수료 감면 ${Math.round((discount / fee) * 100)}%`
    }
    return '수수료 감면 적용중'
  }
  return null
}

function SectionHeader({ title, label }: { title: string; label?: string | null }) {
  return (
    <div className="flex items-center gap-2 pb-1">
      <h2 className="text-sm font-semibold text-gray-700">{title}</h2>
      {label && (
        <span className="rounded-full bg-gray-100 px-2 py-0.5 text-xs text-gray-500">{label}</span>
      )}
    </div>
  )
}

function BothSummaryCards({
  vendorOrders,
  orderingOrders,
}: {
  vendorOrders: SettlementOrder[]
  orderingOrders: SettlementOrder[]
}) {
  const vendorTotal = calcVendorSettlementTotal(vendorOrders)
  const orderingTotal = calcOrderingSettlementTotal(orderingOrders)
  const net = vendorTotal - orderingTotal
  const isPositive = net >= 0

  return (
    <div className="grid grid-cols-3 gap-3">
      <Card className="border-green-200">
        <CardHeader className="pb-1 pt-3">
          <CardTitle className="text-xs font-medium text-green-600">수주 정산 합계</CardTitle>
        </CardHeader>
        <CardContent className="pb-3">
          <p className="text-xl font-bold text-green-700">+{formatKRW(vendorTotal)}</p>
          <p className="mt-0.5 text-xs text-gray-400">받을 금액</p>
        </CardContent>
      </Card>
      <Card className="border-red-200">
        <CardHeader className="pb-1 pt-3">
          <CardTitle className="text-xs font-medium text-red-600">발주 정산 합계</CardTitle>
        </CardHeader>
        <CardContent className="pb-3">
          <p className="text-xl font-bold text-red-700">-{formatKRW(orderingTotal)}</p>
          <p className="mt-0.5 text-xs text-gray-400">낼 금액</p>
        </CardContent>
      </Card>
      <Card className={isPositive ? 'border-blue-200' : 'border-orange-200'}>
        <CardHeader className="pb-1 pt-3">
          <CardTitle className={`text-xs font-medium ${isPositive ? 'text-blue-600' : 'text-orange-600'}`}>
            순 정산 ({isPositive ? '받을 금액' : '낼 금액'})
          </CardTitle>
        </CardHeader>
        <CardContent className="pb-3">
          <p className={`text-xl font-bold ${isPositive ? 'text-blue-700' : 'text-orange-700'}`}>
            {isPositive ? '+' : '-'}{formatKRW(Math.abs(net))}
          </p>
          <p className="mt-0.5 text-xs text-gray-400">수주 − 발주</p>
        </CardContent>
      </Card>
    </div>
  )
}

function OrderingTable({
  orders,
  showPayBadge = false,
}: {
  orders: SettlementOrder[]
  showPayBadge?: boolean
}) {
  const hasDiscount = orders.some((o) => o.orderingFeeDiscountType && o.orderingFeeDiscountType !== 'NONE')
  const grossTotal = orders.reduce((s, o) => s + totalAmountOf(o), 0)
  const discountTotal = orders.reduce((s, o) => s + (o.orderingDiscountAmount ?? 0), 0)
  const settlementTotal = calcOrderingSettlementTotal(orders)
  const showSettlementCard = hasDiscount || showPayBadge

  return (
    <div className="space-y-3">
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        <Card>
          <CardHeader className="pb-1 pt-3"><CardTitle className="text-xs font-medium text-gray-500">발주 건수</CardTitle></CardHeader>
          <CardContent className="pb-3"><p className="text-2xl font-bold">{orders.length}건</p></CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-1 pt-3"><CardTitle className="text-xs font-medium text-gray-500">발주금액 합계</CardTitle></CardHeader>
          <CardContent className="pb-3"><p className="text-xl font-bold">{formatKRW(grossTotal)}</p></CardContent>
        </Card>
        {hasDiscount && (
          <Card>
            <CardHeader className="pb-1 pt-3"><CardTitle className="text-xs font-medium text-gray-500">수수료 감면 합계</CardTitle></CardHeader>
            <CardContent className="pb-3"><p className="text-xl font-bold text-gray-500">-{formatKRW(discountTotal)}</p></CardContent>
          </Card>
        )}
        {showSettlementCard && (
          <Card className={showPayBadge ? 'border-red-200' : ''}>
            <CardHeader className="pb-1 pt-3">
              <CardTitle className={`flex items-center gap-1.5 text-xs font-medium ${showPayBadge ? 'text-red-600' : 'text-blue-600'}`}>
                정산금액 합계
                {showPayBadge && (
                  <span className="rounded-full bg-red-100 px-1.5 py-0.5 text-xs font-semibold text-red-600">지급</span>
                )}
              </CardTitle>
            </CardHeader>
            <CardContent className="pb-3">
              <p className={`text-xl font-bold ${showPayBadge ? 'text-red-700' : 'text-blue-700'}`}>
                {showPayBadge ? '-' : ''}{formatKRW(settlementTotal)}
              </p>
            </CardContent>
          </Card>
        )}
      </div>
      <div className="rounded-lg border bg-white">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>배송완료일</TableHead>
              <TableHead>주문번호</TableHead>
              <TableHead>수주사</TableHead>
              <TableHead>상품명</TableHead>
              <TableHead>장소</TableHead>
              <TableHead>주문자</TableHead>
              <TableHead className="text-right">발주금액</TableHead>
              {hasDiscount && <TableHead className="text-right">수수료 감면</TableHead>}
              {hasDiscount && <TableHead className="text-right">정산금액</TableHead>}
            </TableRow>
          </TableHeader>
          <TableBody>
            {orders.length === 0 ? (
              <TableRow>
                <TableCell colSpan={hasDiscount ? 9 : 7} className="py-10 text-center text-sm text-gray-400">해당 기간에 발주 내역이 없습니다.</TableCell>
              </TableRow>
            ) : orders.map((o) => (
              <TableRow key={o.orderId}>
                <TableCell className="text-sm">{formatDateTime(o.deliveredAt)}</TableCell>
                <TableCell className="text-sm font-medium">{o.orderNumber}</TableCell>
                <TableCell className="text-sm">{o.counterpartCompanyName || '-'}</TableCell>
                <TableCell className="text-sm">{o.productName}</TableCell>
                <TableCell className="text-sm">{o.placeName}</TableCell>
                <TableCell className="text-sm">{o.ordererName}</TableCell>
                <TableCell className="text-right text-sm font-medium">{formatKRW(totalAmountOf(o))}</TableCell>
                {hasDiscount && <TableCell className="text-right text-sm text-gray-500">-{formatKRW(o.orderingDiscountAmount ?? 0)}</TableCell>}
                {hasDiscount && <TableCell className="text-right text-sm font-medium text-blue-700">{formatKRW(o.orderingSettlementAmount ?? totalAmountOf(o))}</TableCell>}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </div>
  )
}

function VendorTable({
  orders,
  showReceiveBadge = false,
}: {
  orders: SettlementOrder[]
  showReceiveBadge?: boolean
}) {
  const feeTotal = orders.reduce((s, o) => s + (o.vendorFee ?? 0), 0)
  const total = calcVendorSettlementTotal(orders)

  return (
    <div className="space-y-3">
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
        <Card>
          <CardHeader className="pb-1 pt-3"><CardTitle className="text-xs font-medium text-gray-500">수주 건수</CardTitle></CardHeader>
          <CardContent className="pb-3"><p className="text-2xl font-bold">{orders.length}건</p></CardContent>
        </Card>
        <Card className={showReceiveBadge ? 'border-green-200' : ''}>
          <CardHeader className="pb-1 pt-3">
            <CardTitle className={`flex items-center gap-1.5 text-xs font-medium ${showReceiveBadge ? 'text-green-600' : 'text-blue-600'}`}>
              정산금액 합계
              {showReceiveBadge && (
                <span className="rounded-full bg-green-100 px-1.5 py-0.5 text-xs font-semibold text-green-600">수취</span>
              )}
            </CardTitle>
          </CardHeader>
          <CardContent className="pb-3">
            <p className={`text-xl font-bold ${showReceiveBadge ? 'text-green-700' : 'text-blue-700'}`}>
              {showReceiveBadge ? '+' : ''}{formatKRW(total)}
            </p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-1 pt-3"><CardTitle className="text-xs font-medium text-gray-500">수수료 합계</CardTitle></CardHeader>
          <CardContent className="pb-3"><p className="text-xl font-bold text-gray-500">{formatKRW(feeTotal)}</p></CardContent>
        </Card>
      </div>
      <div className="rounded-lg border bg-white">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>배송완료일</TableHead>
              <TableHead>주문번호</TableHead>
              <TableHead>발주사</TableHead>
              <TableHead>상품명</TableHead>
              <TableHead>장소</TableHead>
              <TableHead>주문자</TableHead>
              <TableHead className="text-right">수주금액</TableHead>
              <TableHead className="text-right">수수료</TableHead>
              <TableHead className="text-right">정산금액</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {orders.length === 0 ? (
              <TableRow>
                <TableCell colSpan={9} className="py-10 text-center text-sm text-gray-400">해당 기간에 수주 내역이 없습니다.</TableCell>
              </TableRow>
            ) : orders.map((o) => {
              const gross = totalAmountOf(o)
              const fee = o.vendorFee ?? 0
              const settlement = o.vendorAmount ?? (gross - fee)
              return (
                <TableRow key={o.orderId}>
                  <TableCell className="text-sm">{formatDateTime(o.deliveredAt)}</TableCell>
                  <TableCell className="text-sm font-medium">{o.orderNumber}</TableCell>
                  <TableCell className="text-sm">{o.counterpartCompanyName || '-'}</TableCell>
                  <TableCell className="text-sm">{o.productName}</TableCell>
                  <TableCell className="text-sm">{o.placeName}</TableCell>
                  <TableCell className="text-sm">{o.ordererName}</TableCell>
                  <TableCell className="text-right text-sm">{formatKRW(gross)}</TableCell>
                  <TableCell className="text-right text-sm text-gray-500">-{formatKRW(fee)}</TableCell>
                  <TableCell className="text-right text-sm font-medium text-blue-700">{formatKRW(settlement)}</TableCell>
                </TableRow>
              )
            })}
          </TableBody>
        </Table>
      </div>
    </div>
  )
}

export default function SettlementPage() {
  const [from, setFrom] = useState(firstDayOfMonth())
  const [to, setTo] = useState(today())

  const [initialized, setInitialized] = useState(false)
  const [isSuperAdmin, setIsSuperAdmin] = useState(false)
  const [companies, setCompanies] = useState<CompanyInfo[]>([])
  const [selectedCompanyId, setSelectedCompanyId] = useState<number | null>(null)
  const [companyType, setCompanyType] = useState<CompanyType | null>(null)

  const [orderingOrders, setOrderingOrders] = useState<SettlementOrder[]>([])
  const [vendorOrders, setVendorOrders] = useState<SettlementOrder[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const isBoth = companyType === 'BOTH'
  const isOrderingOnly = companyType === 'ORDERING'
  const isVendorOnly = companyType === 'VENDOR'

  useEffect(() => {
    const role = localStorage.getItem('role') ?? ''
    if (role === 'SUPER_ADMIN') {
      setIsSuperAdmin(true)
      fetchCompanies()
        .then((list) => {
          setCompanies(list)
          if (list.length > 0) {
            setSelectedCompanyId(list[0].id)
            setCompanyType(list[0].type)
          }
        })
        .catch(() => {})
        .finally(() => setInitialized(true))
    } else {
      setInitialized(true)
    }
  }, [])

  const load = useCallback(async () => {
    if (isSuperAdmin && !selectedCompanyId) return
    setLoading(true)
    setError('')
    try {
      if (isSuperAdmin && selectedCompanyId && companyType) {
        const [ord, ven] = await Promise.all([
          (companyType === 'ORDERING' || companyType === 'BOTH')
            ? fetchAdminSettlementDetail(selectedCompanyId, from, to, 'ORDERING')
            : Promise.resolve([]),
          (companyType === 'VENDOR' || companyType === 'BOTH')
            ? fetchAdminSettlementDetail(selectedCompanyId, from, to, 'VENDOR')
            : Promise.resolve([]),
        ])
        setOrderingOrders(ord)
        setVendorOrders(ven)
      } else {
        const result = await fetchMySettlement(from, to)
        setCompanyType(result.companyType)
        setOrderingOrders(result.orderingOrders)
        setVendorOrders(result.vendorOrders)
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : '불러오지 못했습니다.')
    } finally {
      setLoading(false)
    }
  }, [isSuperAdmin, selectedCompanyId, companyType, from, to])

  useEffect(() => {
    if (!initialized) return
    if (isSuperAdmin && !selectedCompanyId) return
    if (!isSuperAdmin) load()
  }, [initialized, isSuperAdmin, selectedCompanyId, load])

  useEffect(() => {
    if (!initialized || !isSuperAdmin || !selectedCompanyId) return
    load()
  }, [initialized, isSuperAdmin, selectedCompanyId, from, to, load])

  function handleCompanyChange(id: number) {
    const c = companies.find((c) => c.id === id)
    if (!c) return
    setSelectedCompanyId(id)
    setCompanyType(c.type)
  }

  const vendorExcelUrl = isSuperAdmin && selectedCompanyId && (isBoth || isVendorOnly)
    ? getSettlementExcelUrl(selectedCompanyId, from, to, 'VENDOR')
    : null
  const orderingExcelUrl = isSuperAdmin && selectedCompanyId && (isBoth || isOrderingOnly)
    ? getSettlementExcelUrl(selectedCompanyId, from, to, 'ORDERING')
    : null

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h1 className="text-lg font-bold">정산 관리</h1>
        {isSuperAdmin && (
          <div className="flex items-center gap-2">
            <span className="text-sm text-gray-500">회사</span>
            <select
              className="rounded-md border bg-white px-2 py-1.5 text-sm"
              value={selectedCompanyId ?? ''}
              onChange={(e) => handleCompanyChange(Number(e.target.value))}
            >
              {companies.map((c) => (
                <option key={c.id} value={c.id}>{c.companyName} ({c.type})</option>
              ))}
            </select>
          </div>
        )}
      </div>

      {!isSuperAdmin && <AccountInfoBanner />}

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
        <Button onClick={load} disabled={loading} className="bg-green-700 hover:bg-green-800">
          {loading ? '조회 중…' : '조회'}
        </Button>
        {!isBoth && orderingExcelUrl && (
          <a href={orderingExcelUrl} download>
            <Button variant="outline">엑셀 다운로드</Button>
          </a>
        )}
        {!isBoth && vendorExcelUrl && (
          <a href={vendorExcelUrl} download>
            <Button variant="outline">엑셀 다운로드</Button>
          </a>
        )}
      </div>

      {error && <p className="text-sm text-red-500">{error}</p>}

      {loading ? (
        <p className="py-8 text-center text-sm text-gray-400">불러오는 중…</p>
      ) : companyType ? (
        <div className="space-y-6">
          {/* 수발주사: 순정산 요약 카드 + 두 섹션 통합 */}
          {isBoth && (
            <BothSummaryCards vendorOrders={vendorOrders} orderingOrders={orderingOrders} />
          )}

          {(isBoth || isVendorOnly) && (
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <SectionHeader title="수주 정산" label={deriveVendorFeeLabel(vendorOrders)} />
                {isBoth && vendorExcelUrl && (
                  <a href={vendorExcelUrl} download>
                    <Button variant="outline" className="h-7 px-2 text-xs">수주 엑셀</Button>
                  </a>
                )}
              </div>
              <VendorTable orders={vendorOrders} showReceiveBadge={isVendorOnly} />
            </div>
          )}

          {(isBoth || isOrderingOnly) && (
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <SectionHeader title="발주 정산" label={deriveOrderingDiscountLabel(orderingOrders)} />
                {isBoth && orderingExcelUrl && (
                  <a href={orderingExcelUrl} download>
                    <Button variant="outline" className="h-7 px-2 text-xs">발주 엑셀</Button>
                  </a>
                )}
              </div>
              <OrderingTable orders={orderingOrders} showPayBadge={isOrderingOnly} />
            </div>
          )}
        </div>
      ) : !isSuperAdmin ? (
        <p className="py-8 text-center text-sm text-gray-400">조회 버튼을 눌러주세요.</p>
      ) : (
        <p className="py-8 text-center text-sm text-gray-400">회사를 선택해주세요.</p>
      )}
    </div>
  )
}

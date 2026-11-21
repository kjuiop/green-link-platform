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

type Tab = 'ordering' | 'vendor'

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

function OrderingTable({ orders }: { orders: SettlementOrder[] }) {
  const total = orders.reduce((s, o) => s + totalAmountOf(o), 0)
  return (
    <div className="space-y-3">
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
        <Card>
          <CardHeader className="pb-1 pt-3"><CardTitle className="text-xs font-medium text-gray-500">발주 건수</CardTitle></CardHeader>
          <CardContent className="pb-3"><p className="text-2xl font-bold">{orders.length}건</p></CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-1 pt-3"><CardTitle className="text-xs font-medium text-gray-500">발주금액 합계</CardTitle></CardHeader>
          <CardContent className="pb-3"><p className="text-xl font-bold">{formatKRW(total)}</p></CardContent>
        </Card>
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
            </TableRow>
          </TableHeader>
          <TableBody>
            {orders.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} className="py-10 text-center text-sm text-gray-400">해당 기간에 발주 내역이 없습니다.</TableCell>
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
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </div>
  )
}

function VendorTable({ orders }: { orders: SettlementOrder[] }) {
  const total = orders.reduce((s, o) => s + (o.vendorAmount ?? 0), 0)
  const feeTotal = orders.reduce((s, o) => s + (totalAmountOf(o) - (o.vendorAmount ?? 0)), 0)
  return (
    <div className="space-y-3">
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
        <Card>
          <CardHeader className="pb-1 pt-3"><CardTitle className="text-xs font-medium text-gray-500">수주 건수</CardTitle></CardHeader>
          <CardContent className="pb-3"><p className="text-2xl font-bold">{orders.length}건</p></CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-1 pt-3"><CardTitle className="text-xs font-medium text-blue-600">정산금액 합계</CardTitle></CardHeader>
          <CardContent className="pb-3"><p className="text-xl font-bold text-blue-700">{formatKRW(total)}</p></CardContent>
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
              <TableHead className="text-right">발주금액</TableHead>
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
              const fee = gross - (o.vendorAmount ?? 0)
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
                  <TableCell className="text-right text-sm font-medium text-blue-700">{formatKRW(o.vendorAmount ?? 0)}</TableCell>
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
  const [activeTab, setActiveTab] = useState<Tab>('ordering')

  const [initialized, setInitialized] = useState(false)
  const [isSuperAdmin, setIsSuperAdmin] = useState(false)
  const [companies, setCompanies] = useState<CompanyInfo[]>([])
  const [selectedCompanyId, setSelectedCompanyId] = useState<number | null>(null)
  const [companyType, setCompanyType] = useState<CompanyType | null>(null)

  const [orderingOrders, setOrderingOrders] = useState<SettlementOrder[]>([])
  const [vendorOrders, setVendorOrders] = useState<SettlementOrder[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const showOrdering = companyType === 'ORDERING' || companyType === 'BOTH'
  const showVendor = companyType === 'VENDOR' || companyType === 'BOTH'

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
        if (result.companyType === 'VENDOR') setActiveTab('vendor')
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
    setActiveTab(c.type === 'VENDOR' ? 'vendor' : 'ordering')
  }

  const excelUrl = isSuperAdmin && selectedCompanyId && companyType
    ? getSettlementExcelUrl(selectedCompanyId, from, to, activeTab === 'ordering' ? 'ORDERING' : 'VENDOR')
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
        {excelUrl && (
          <a href={excelUrl} download>
            <Button variant="outline">엑셀 다운로드</Button>
          </a>
        )}
      </div>

      {error && <p className="text-sm text-red-500">{error}</p>}

      {/* 탭 */}
      {companyType && (
        <div className="flex gap-1 border-b">
          {showOrdering && (
            <button
              onClick={() => setActiveTab('ordering')}
              className={activeTab === 'ordering'
                ? 'border-b-2 border-green-700 px-4 py-2 text-sm font-medium text-green-700'
                : 'px-4 py-2 text-sm text-gray-500 hover:text-gray-900'}
            >
              발주 정산
            </button>
          )}
          {showVendor && (
            <button
              onClick={() => setActiveTab('vendor')}
              className={activeTab === 'vendor'
                ? 'border-b-2 border-green-700 px-4 py-2 text-sm font-medium text-green-700'
                : 'px-4 py-2 text-sm text-gray-500 hover:text-gray-900'}
            >
              수주 정산
            </button>
          )}
        </div>
      )}

      {loading ? (
        <p className="py-8 text-center text-sm text-gray-400">불러오는 중…</p>
      ) : companyType ? (
        <>
          {activeTab === 'ordering' && showOrdering && <OrderingTable orders={orderingOrders} />}
          {activeTab === 'vendor' && showVendor && <VendorTable orders={vendorOrders} />}
        </>
      ) : !isSuperAdmin ? (
        <p className="py-8 text-center text-sm text-gray-400">조회 버튼을 눌러주세요.</p>
      ) : (
        <p className="py-8 text-center text-sm text-gray-400">회사를 선택해주세요.</p>
      )}
    </div>
  )
}

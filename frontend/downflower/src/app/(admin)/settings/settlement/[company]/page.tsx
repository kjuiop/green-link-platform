'use client'

import { useEffect, useState } from 'react'
import { useParams, useSearchParams, useRouter } from 'next/navigation'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { ChevronLeft, Download } from 'lucide-react'
import { AccountInfoBanner } from '@/components/settlement/AccountInfoBanner'
import {
  fetchAdminSettlementDetail,
  getSettlementExcelUrl,
  SettlementOrder,
  SettlementType,
} from '@/lib/api/settlementApi'
import { authBearerHeader } from '@/lib/api/fetchWithAuth'
import { formatKRW } from '../page'

export default function SettlementDetailPage() {
  const params = useParams()
  const searchParams = useSearchParams()
  const router = useRouter()

  const companyId = Number(params.company)
  const from = searchParams.get('from') ?? ''
  const to = searchParams.get('to') ?? ''
  const type = (searchParams.get('type') ?? 'VENDOR') as SettlementType
  const companyName = searchParams.get('name') ?? ''

  const [orders, setOrders] = useState<SettlementOrder[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!companyId || !from || !to) return
    setLoading(true)
    fetchAdminSettlementDetail(companyId, from, to, type)
      .then(setOrders)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [companyId, from, to, type])

  const totalOrderAmount = orders.reduce((s, o) => s + o.orderAmount, 0)
  const totalVendorAmount = orders.reduce((s, o) => s + (o.vendorAmount ?? 0), 0)

  async function downloadExcel() {
    const url = getSettlementExcelUrl(companyId, from, to, type)
    const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null
    const res = await fetch(url, { headers: token ? { Authorization: `Bearer ${token}` } : {} })
    if (!res.ok) return
    const blob = await res.blob()
    const a = document.createElement('a')
    a.href = URL.createObjectURL(blob)
    a.download = `settlement_${from}_${to}.xlsx`
    a.click()
  }

  return (
    <div className="space-y-4">
      {/* 헤더 */}
      <div className="flex items-center gap-3">
        <Button variant="outline" size="sm" onClick={() => router.back()} className="gap-1">
          <ChevronLeft className="h-4 w-4" />
          목록
        </Button>
        <div>
          <h1 className="text-xl font-bold">{companyName}</h1>
          {from && to && (
            <p className="text-sm text-gray-500">{from} ~ {to}</p>
          )}
        </div>
        <Badge variant="outline" className="ml-2 border-gray-200 text-gray-600">
          {type === 'VENDOR' ? '수주사' : '발주사'}
        </Badge>
        <Button size="sm" variant="outline" className="ml-auto gap-1" onClick={downloadExcel}>
          <Download className="h-4 w-4" />
          엑셀
        </Button>
      </div>

      <AccountInfoBanner />

      {/* 요약 */}
      <div className="flex gap-8 rounded-lg border bg-white px-6 py-4 text-sm">
        <div>
          <p className="text-xs text-gray-500">배송완료 건수</p>
          <p className="mt-1 text-2xl font-bold">{orders.length}건</p>
        </div>
        <div className="border-l pl-8">
          <p className="text-xs text-gray-500">발주금액 합계</p>
          <p className="mt-1 text-xl font-bold text-gray-800">{formatKRW(totalOrderAmount)}</p>
        </div>
        <div className="border-l pl-8">
          <p className="text-xs text-blue-600">지급금액 합계</p>
          <p className="mt-1 text-xl font-bold text-blue-700">{formatKRW(totalVendorAmount)}</p>
        </div>
      </div>

      {error && <p className="text-sm text-red-500">{error}</p>}

      {/* 주문 목록 */}
      <div className="rounded-lg border bg-white">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>주문번호</TableHead>
              <TableHead>배송일</TableHead>
              <TableHead>{type === 'VENDOR' ? '발주사' : '수주사'}</TableHead>
              <TableHead>상품명</TableHead>
              <TableHead>배달장소</TableHead>
              <TableHead>주문자</TableHead>
              <TableHead className="text-right">발주금액</TableHead>
              <TableHead className="text-right">지급금액</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={8} className="py-10 text-center text-sm text-gray-400">불러오는 중...</TableCell>
              </TableRow>
            ) : orders.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} className="py-10 text-center text-sm text-gray-400">
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
                  <TableCell className="text-right text-sm">{formatKRW(o.orderAmount)}</TableCell>
                  <TableCell className="text-right text-sm font-medium text-blue-700">
                    {o.vendorAmount != null ? formatKRW(o.vendorAmount) : '-'}
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
        {orders.length > 0 && (
          <div className="flex justify-end gap-6 border-t bg-gray-50 px-4 py-3 text-sm">
            <span className="text-gray-600">합계: <strong>{orders.length}건</strong></span>
            <span className="text-gray-600">발주금액: <strong>{formatKRW(totalOrderAmount)}</strong></span>
            <span className="text-blue-700">지급금액: <strong>{formatKRW(totalVendorAmount)}</strong></span>
          </div>
        )}
      </div>
    </div>
  )
}

'use client'

import { useState, useMemo } from 'react'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { ChevronDown, ChevronUp, Search } from 'lucide-react'

type SettlementStatus = '미정산' | '정산완료'

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

interface VendorSummary {
  vendorId: string
  vendorName: string
  count: number
  orderAmountTotal: number
  vendorAmountTotal: number
  status: SettlementStatus
}

const MOCK_ORDERS: MockOrder[] = [
  { id: '1',  orderNumber: 'DN-20261101-001', vendorName: '꽃향기', productName: '축하화환 대',    placeName: '강남구청',   ordererName: '김민준', deliveredAt: '2026-11-01', orderAmount: 150000, vendorAmount: 120000 },
  { id: '2',  orderNumber: 'DN-20261102-002', vendorName: '꽃향기', productName: '근조화환 중',    placeName: '강남구청',   ordererName: '이서연', deliveredAt: '2026-11-02', orderAmount: 100000, vendorAmount: 80000  },
  { id: '3',  orderNumber: 'DN-20261103-003', vendorName: '꽃향기', productName: '동양란',         placeName: '서초 웨딩홀', ordererName: '박도윤', deliveredAt: '2026-11-03', orderAmount: 80000,  vendorAmount: 65000  },
  { id: '4',  orderNumber: 'DN-20261104-004', vendorName: '꽃향기', productName: '축하화환 소',    placeName: '마포 사무실', ordererName: '정시우', deliveredAt: '2026-11-04', orderAmount: 90000,  vendorAmount: 72000  },
  { id: '5',  orderNumber: 'DN-20261101-005', vendorName: '한강꽃집', productName: '근조화환 대',  placeName: '용산 장례식장', ordererName: '최지아', deliveredAt: '2026-11-01', orderAmount: 200000, vendorAmount: 160000 },
  { id: '6',  orderNumber: 'DN-20261102-006', vendorName: '한강꽃집', productName: '축하화환 중',  placeName: '여의도 증권사', ordererName: '윤준서', deliveredAt: '2026-11-02', orderAmount: 120000, vendorAmount: 96000  },
  { id: '7',  orderNumber: 'DN-20261103-007', vendorName: '한강꽃집', productName: '서양란',       placeName: '마포 사무실', ordererName: '임채원', deliveredAt: '2026-11-05', orderAmount: 85000,  vendorAmount: 68000  },
  { id: '8',  orderNumber: 'DN-20261104-008', vendorName: '남산꽃원', productName: '근조화환 중',  placeName: '중구 장례식장', ordererName: '한지호', deliveredAt: '2026-11-02', orderAmount: 110000, vendorAmount: 88000  },
  { id: '9',  orderNumber: 'DN-20261105-009', vendorName: '남산꽃원', productName: '쌀화환',       placeName: '송파 웨딩홀', ordererName: '오서준', deliveredAt: '2026-11-05', orderAmount: 130000, vendorAmount: 104000 },
  { id: '10', orderNumber: 'DN-20261106-010', vendorName: '남산꽃원', productName: '축하화환 대',  placeName: '강동 사무실', ordererName: '신예은', deliveredAt: '2026-11-06', orderAmount: 150000, vendorAmount: 120000 },
  { id: '11', orderNumber: 'DN-20261101-011', vendorName: '부산꽃향기', productName: '근조화환 대', placeName: '부산 장례식장', ordererName: '권민서', deliveredAt: '2026-11-01', orderAmount: 180000, vendorAmount: 144000 },
  { id: '12', orderNumber: 'DN-20261103-012', vendorName: '부산꽃향기', productName: '축하화환 중', placeName: '해운대 웨딩홀', ordererName: '황지민', deliveredAt: '2026-11-03', orderAmount: 120000, vendorAmount: 96000  },
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

export default function AdminSettlementPage() {
  const [from, setFrom] = useState(firstDayOfMonth())
  const [to, setTo] = useState(today())
  const [search, setSearch] = useState('')
  const [selectedVendor, setSelectedVendor] = useState<VendorSummary | null>(null)
  const [settledIds, setSettledIds] = useState<Set<string>>(new Set())

  const filteredOrders = useMemo(() => {
    return MOCK_ORDERS.filter((o) => o.deliveredAt >= from && o.deliveredAt <= to)
  }, [from, to])

  const vendorSummaries = useMemo((): VendorSummary[] => {
    const map = new Map<string, VendorSummary>()
    for (const o of filteredOrders) {
      const existing = map.get(o.vendorName)
      if (existing) {
        existing.count++
        existing.orderAmountTotal += o.orderAmount
        existing.vendorAmountTotal += o.vendorAmount
      } else {
        map.set(o.vendorName, {
          vendorId: o.vendorName,
          vendorName: o.vendorName,
          count: 1,
          orderAmountTotal: o.orderAmount,
          vendorAmountTotal: o.vendorAmount,
          status: settledIds.has(o.vendorName) ? '정산완료' : '미정산',
        })
      }
    }
    return Array.from(map.values()).map((v) => ({
      ...v,
      status: settledIds.has(v.vendorId) ? '정산완료' : '미정산',
    }))
  }, [filteredOrders, settledIds])

  const displayedSummaries = useMemo(() => {
    if (!search.trim()) return vendorSummaries
    return vendorSummaries.filter((v) => v.vendorName.includes(search.trim()))
  }, [vendorSummaries, search])

  const totalCount = vendorSummaries.reduce((s, v) => s + v.count, 0)
  const totalOrderAmount = vendorSummaries.reduce((s, v) => s + v.orderAmountTotal, 0)
  const totalVendorAmount = vendorSummaries.reduce((s, v) => s + v.vendorAmountTotal, 0)
  const settledCount = vendorSummaries.filter((v) => v.status === '정산완료').length

  const selectedOrders = useMemo(() => {
    if (!selectedVendor) return []
    return filteredOrders.filter((o) => o.vendorName === selectedVendor.vendorName)
  }, [selectedVendor, filteredOrders])

  function toggleSettled(vendorId: string) {
    setSettledIds((prev) => {
      const next = new Set(prev)
      if (next.has(vendorId)) next.delete(vendorId)
      else next.add(vendorId)
      return next
    })
  }

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">정산관리</h1>

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
        <div className="relative">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-gray-400" />
          <Input
            placeholder="업체명 검색"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-40 pl-8"
          />
        </div>
      </div>

      {/* 요약 카드 */}
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        <Card>
          <CardHeader className="pb-1 pt-3">
            <CardTitle className="text-xs font-medium text-gray-500">배송완료 건수</CardTitle>
          </CardHeader>
          <CardContent className="pb-3">
            <p className="text-2xl font-bold">{totalCount}</p>
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
        <Card>
          <CardHeader className="pb-1 pt-3">
            <CardTitle className="text-xs font-medium text-green-600">정산완료 업체</CardTitle>
          </CardHeader>
          <CardContent className="pb-3">
            <p className="text-2xl font-bold text-green-700">
              {settledCount} <span className="text-sm font-normal text-gray-400">/ {vendorSummaries.length}</span>
            </p>
          </CardContent>
        </Card>
      </div>

      {/* 업체별 정산 테이블 */}
      <div className="rounded-lg border bg-white">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>업체명</TableHead>
              <TableHead className="text-right">배송완료</TableHead>
              <TableHead className="text-right">발주금액</TableHead>
              <TableHead className="text-right">지급금액</TableHead>
              <TableHead className="text-center">상태</TableHead>
              <TableHead className="text-center">처리</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {displayedSummaries.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="py-10 text-center text-sm text-gray-400">
                  해당 기간에 배송완료된 주문이 없습니다.
                </TableCell>
              </TableRow>
            ) : (
              displayedSummaries.map((v) => (
                <TableRow key={v.vendorId} className="cursor-pointer hover:bg-gray-50">
                  <TableCell>
                    <button
                      type="button"
                      className="font-medium text-blue-600 hover:underline"
                      onClick={() => setSelectedVendor(v)}
                    >
                      {v.vendorName}
                    </button>
                  </TableCell>
                  <TableCell className="text-right text-sm">{v.count}건</TableCell>
                  <TableCell className="text-right text-sm">{formatKRW(v.orderAmountTotal)}</TableCell>
                  <TableCell className="text-right text-sm font-medium text-blue-700">
                    {formatKRW(v.vendorAmountTotal)}
                  </TableCell>
                  <TableCell className="text-center">
                    <Badge
                      variant="outline"
                      className={
                        v.status === '정산완료'
                          ? 'border-green-200 bg-green-50 text-green-700'
                          : 'border-orange-200 bg-orange-50 text-orange-700'
                      }
                    >
                      {v.status}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-center">
                    <Button
                      size="sm"
                      variant={v.status === '정산완료' ? 'outline' : 'default'}
                      className={
                        v.status === '정산완료'
                          ? 'h-7 border-gray-300 text-xs text-gray-500'
                          : 'h-7 bg-blue-600 text-xs hover:bg-blue-700'
                      }
                      onClick={() => toggleSettled(v.vendorId)}
                    >
                      {v.status === '정산완료' ? '완료취소' : '정산완료'}
                    </Button>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {/* 주문 상세 모달 */}
      <Dialog open={!!selectedVendor} onOpenChange={(o) => !o && setSelectedVendor(null)}>
        <DialogContent className="flex max-h-[90vh] max-w-3xl flex-col p-0">
          <DialogHeader className="px-6 pt-5">
            <DialogTitle>
              {selectedVendor?.vendorName} — 주문 상세{' '}
              <span className="text-sm font-normal text-gray-500">
                ({from} ~ {to})
              </span>
            </DialogTitle>
          </DialogHeader>

          <div className="flex-1 overflow-y-auto px-6 pb-6">
            {selectedVendor && (
              <>
                <div className="mb-4 flex gap-6 rounded-lg bg-gray-50 px-4 py-3 text-sm">
                  <span>
                    건수: <strong>{selectedVendor.count}건</strong>
                  </span>
                  <span>
                    발주금액: <strong>{formatKRW(selectedVendor.orderAmountTotal)}</strong>
                  </span>
                  <span>
                    지급금액:{' '}
                    <strong className="text-blue-700">{formatKRW(selectedVendor.vendorAmountTotal)}</strong>
                  </span>
                </div>

                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>주문번호</TableHead>
                      <TableHead>배송일</TableHead>
                      <TableHead>상품명</TableHead>
                      <TableHead>배달장소</TableHead>
                      <TableHead>주문자</TableHead>
                      <TableHead className="text-right">발주금액</TableHead>
                      <TableHead className="text-right">지급금액</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {selectedOrders.map((o) => (
                      <TableRow key={o.id}>
                        <TableCell className="font-mono text-xs text-blue-600">{o.orderNumber}</TableCell>
                        <TableCell className="text-sm">{o.deliveredAt}</TableCell>
                        <TableCell className="text-sm">{o.productName}</TableCell>
                        <TableCell className="text-sm">{o.placeName}</TableCell>
                        <TableCell className="text-sm">{o.ordererName}</TableCell>
                        <TableCell className="text-right text-sm">{formatKRW(o.orderAmount)}</TableCell>
                        <TableCell className="text-right text-sm font-medium text-blue-700">
                          {formatKRW(o.vendorAmount)}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </>
            )}
          </div>
        </DialogContent>
      </Dialog>
    </div>
  )
}

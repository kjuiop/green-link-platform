'use client'

import { useState, useEffect, useCallback } from 'react'
import { useRouter } from 'next/navigation'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Search } from 'lucide-react'
import {
  fetchAdminSettlements,
  completeSettlement,
  cancelSettlement,
  SettlementSummary,
  SettlementType,
} from '@/lib/api/settlementApi'

export const formatKRW = (n: number) => n.toLocaleString('ko-KR') + '원'

type ViewTab = '전체' | '수주사별' | '발주사별'

function today() {
  return new Date().toISOString().slice(0, 10)
}

function firstDayOfMonth() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-01`
}

function companyTypeLabel(t: string) {
  if (t === 'VENDOR') return '수주사'
  if (t === 'ORDERING') return '발주사'
  if (t === 'BOTH') return '둘다'
  return t
}

export default function AdminSettlementPage() {
  const router = useRouter()
  const [from, setFrom] = useState(firstDayOfMonth())
  const [to, setTo] = useState(today())
  const [search, setSearch] = useState('')
  const [tab, setTab] = useState<ViewTab>('전체')
  const [summaries, setSummaries] = useState<SettlementSummary[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const tabToType: Record<ViewTab, SettlementType> = { '전체': 'ALL', '수주사별': 'VENDOR', '발주사별': 'ORDERING' }
  const settlementType = tabToType[tab]

  const load = useCallback(() => {
    if (!from || !to) return
    setLoading(true)
    setError('')
    fetchAdminSettlements(from, to, settlementType)
      .then(setSummaries)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [from, to, settlementType])

  useEffect(() => { load() }, [load])

  const displayed = search.trim()
    ? summaries.filter((v) => v.companyName.includes(search.trim()))
    : summaries

  const totalCount = summaries.reduce((s, v) => s + v.count, 0)
  const totalOrderAmount = summaries.reduce((s, v) => s + v.orderAmountTotal, 0)
  const totalVendorAmount = summaries.reduce((s, v) => s + v.vendorAmountTotal, 0)
  const settledCount = summaries.filter((v) => v.completed).length

  function goToDetail(s: SettlementSummary) {
    router.push(
      `/settings/settlement/${s.companyId}?from=${from}&to=${to}&type=${settlementType}&name=${encodeURIComponent(s.companyName)}`
    )
  }

  async function toggleSettled(s: SettlementSummary) {
    try {
      if (s.completed) {
        await cancelSettlement(s.companyId, from, to, settlementType)
      } else {
        await completeSettlement(s.companyId, from, to, settlementType)
      }
      load()
    } catch (e) {
      alert(e instanceof Error ? e.message : '처리에 실패했습니다.')
    }
  }

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">정산관리</h1>

      {/* 필터 */}
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

      {/* 탭 */}
      <div className="flex gap-1 rounded-lg border bg-white p-1 w-fit">
        {(['전체', '수주사별', '발주사별'] as ViewTab[]).map((t) => (
          <button
            key={t}
            onClick={() => { setTab(t); setSearch('') }}
            className={`rounded-md px-4 py-1.5 text-sm font-medium transition-colors ${
              tab === t ? 'bg-green-600 text-white' : 'text-gray-600 hover:bg-gray-100'
            }`}
          >
            {t}
          </button>
        ))}
      </div>

      {/* 요약 카드 */}
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        <Card>
          <CardHeader className="pb-1 pt-3"><CardTitle className="text-xs font-medium text-gray-500">배송완료 건수</CardTitle></CardHeader>
          <CardContent className="pb-3"><p className="text-2xl font-bold">{totalCount}</p></CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-1 pt-3"><CardTitle className="text-xs font-medium text-gray-500">발주금액 합계</CardTitle></CardHeader>
          <CardContent className="pb-3"><p className="text-xl font-bold text-gray-800">{formatKRW(totalOrderAmount)}</p></CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-1 pt-3"><CardTitle className="text-xs font-medium text-blue-600">지급금액 합계</CardTitle></CardHeader>
          <CardContent className="pb-3"><p className="text-xl font-bold text-blue-700">{formatKRW(totalVendorAmount)}</p></CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-1 pt-3"><CardTitle className="text-xs font-medium text-green-600">정산완료</CardTitle></CardHeader>
          <CardContent className="pb-3">
            <p className="text-2xl font-bold text-green-700">
              {settledCount} <span className="text-sm font-normal text-gray-400">/ {summaries.length}</span>
            </p>
          </CardContent>
        </Card>
      </div>

      {/* 에러 */}
      {error && <p className="text-sm text-red-500">{error}</p>}

      {/* 테이블 */}
      <div className="rounded-lg border bg-white">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>업체명</TableHead>
              <TableHead>유형</TableHead>
              <TableHead className="text-right">배송완료</TableHead>
              <TableHead className="text-right">발주금액</TableHead>
              <TableHead className="text-right">지급금액</TableHead>
              {tab !== '전체' && <TableHead className="text-center">상태</TableHead>}
              {tab !== '전체' && <TableHead className="text-center">처리</TableHead>}
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={tab !== '전체' ? 7 : 5} className="py-10 text-center text-sm text-gray-400">불러오는 중...</TableCell>
              </TableRow>
            ) : displayed.length === 0 ? (
              <TableRow>
                <TableCell colSpan={tab !== '전체' ? 7 : 5} className="py-10 text-center text-sm text-gray-400">
                  해당 기간에 배송완료된 주문이 없습니다.
                </TableCell>
              </TableRow>
            ) : (
              displayed.map((v) => (
                <TableRow key={v.companyId} className="hover:bg-gray-50">
                  <TableCell>
                    <button
                      type="button"
                      className="font-medium text-blue-600 hover:underline"
                      onClick={() => goToDetail(v)}
                    >
                      {v.companyName}
                    </button>
                  </TableCell>
                  <TableCell className="text-sm text-gray-500">{companyTypeLabel(v.companyType)}</TableCell>
                  <TableCell className="text-right text-sm">{v.count}건</TableCell>
                  <TableCell className="text-right text-sm">{formatKRW(v.orderAmountTotal)}</TableCell>
                  <TableCell className="text-right text-sm font-medium text-blue-700">{formatKRW(v.vendorAmountTotal)}</TableCell>
                  {tab !== '전체' && (
                    <TableCell className="text-center">
                      <Badge
                        variant="outline"
                        className={
                          v.completed
                            ? 'border-green-200 bg-green-50 text-green-700'
                            : 'border-orange-200 bg-orange-50 text-orange-700'
                        }
                      >
                        {v.completed ? '정산완료' : '미정산'}
                      </Badge>
                    </TableCell>
                  )}
                  {tab !== '전체' && (
                    <TableCell className="text-center">
                      <Button
                        size="sm"
                        variant={v.completed ? 'outline' : 'default'}
                        className={
                          v.completed
                            ? 'h-7 border-gray-300 text-xs text-gray-500'
                            : 'h-7 bg-blue-600 text-xs hover:bg-blue-700'
                        }
                        onClick={() => toggleSettled(v)}
                      >
                        {v.completed ? '완료취소' : '정산완료'}
                      </Button>
                    </TableCell>
                  )}
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  )
}

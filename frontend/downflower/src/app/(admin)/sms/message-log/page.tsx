'use client'

import { useState, useEffect } from 'react'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Pagination } from '@/components/ui/Pagination'
import { fetchOrders } from '@/lib/api/orderApi'
import { fetchSmsMessages, resendMms, SmsMessage } from '@/lib/api/smsApi'
import { Order } from '@/types/order'
import Link from 'next/link'
import { Search, RefreshCw } from 'lucide-react'

const PAGE_SIZE = 20

type SmsStatusFilter = 'ALL' | 'SUCCESS' | 'FAIL'

const STATUS_FILTERS: { label: string; value: SmsStatusFilter }[] = [
  { label: '전체', value: 'ALL' },
  { label: '발송성공', value: 'SUCCESS' },
  { label: '발송실패', value: 'FAIL' },
]

type MessageRow = {
  sms: SmsMessage
  order: Order | undefined
}

export default function MessagesPage() {
  const [rows, setRows] = useState<MessageRow[]>([])
  const [loading, setLoading] = useState(true)
  const [query, setQuery] = useState('')
  const [statusFilter, setStatusFilter] = useState<SmsStatusFilter>('ALL')
  const [appliedQuery, setAppliedQuery] = useState('')
  const [appliedStatus, setAppliedStatus] = useState<SmsStatusFilter>('ALL')
  const [resendingIds, setResendingIds] = useState<Set<number>>(new Set())
  const [page, setPage] = useState(0)

  const loadData = () =>
    Promise.all([fetchSmsMessages(), fetchOrders()])
      .then(([messages, orders]) => {
        const orderMap = new Map(orders.map((o) => [Number(o.id), o]))
        setRows(messages.map((sms) => ({ sms, order: orderMap.get(sms.orderId) })))
      })
      .catch(() => {})
      .finally(() => setLoading(false))

  useEffect(() => { loadData() }, [])

  const handleSearch = () => {
    setAppliedQuery(query)
    setAppliedStatus(statusFilter)
    setPage(0)
  }

  const handleReset = () => {
    setQuery(''); setStatusFilter('ALL')
    setAppliedQuery(''); setAppliedStatus('ALL')
    setPage(0)
  }

  const handleResend = async (orderId: number) => {
    setResendingIds((prev) => new Set(prev).add(orderId))
    try {
      await resendMms(orderId)
      await loadData()
    } catch {
      alert('재발송에 실패했습니다.')
    } finally {
      setResendingIds((prev) => { const s = new Set(prev); s.delete(orderId); return s })
    }
  }

  const filtered = rows.filter(({ sms, order }) => {
    const matchStatus = appliedStatus === 'ALL' || sms.status === appliedStatus
    const matchQuery =
      !appliedQuery ||
      order?.orderNumber.includes(appliedQuery) ||
      order?.vendorName.includes(appliedQuery) ||
      order?.ordererName.includes(appliedQuery) ||
      sms.receiver.includes(appliedQuery)
    return matchStatus && matchQuery
  })

  const totalPages = Math.ceil(filtered.length / PAGE_SIZE)
  const paginatedRows = filtered.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE)

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">문자 발송내역</h1>

      <div className="rounded-lg border bg-white p-4">
        <div className="flex flex-wrap gap-3">
          {STATUS_FILTERS.map(({ label, value }) => (
            <label key={value} className="flex cursor-pointer items-center gap-1.5">
              <input
                type="radio"
                name="statusFilter"
                checked={statusFilter === value}
                onChange={() => setStatusFilter(value)}
                className="accent-green-700"
              />
              <span className="text-sm text-gray-700">{label}</span>
            </label>
          ))}
        </div>
        <div className="mt-3 flex gap-2">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
            <input
              type="text"
              placeholder="주문번호, 업체명, 주문자명, 수신번호로 검색"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
              className="w-full rounded-md border px-3 py-2 pl-9 text-sm placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-green-700"
            />
          </div>
          <button
            onClick={handleSearch}
            className="rounded-md bg-green-700 px-4 py-2 text-sm font-medium text-white hover:bg-green-800"
          >
            검색
          </button>
          <button
            onClick={handleReset}
            className="rounded-md border px-4 py-2 text-sm font-medium text-gray-500 hover:bg-gray-50"
          >
            초기화
          </button>
        </div>
      </div>

      {/* 데스크톱 테이블 */}
      <div className="hidden rounded-lg border bg-white md:block">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>주문번호</TableHead>
              <TableHead>발송일시</TableHead>
              <TableHead>수주업체</TableHead>
              <TableHead>상품명</TableHead>
              <TableHead>주문자</TableHead>
              <TableHead>수신번호</TableHead>
              <TableHead>발송상태</TableHead>
              <TableHead>재발송</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={8} className="py-10 text-center text-sm text-gray-400">
                  불러오는 중...
                </TableCell>
              </TableRow>
            ) : paginatedRows.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} className="py-10 text-center text-sm text-gray-400">
                  검색 결과가 없습니다.
                </TableCell>
              </TableRow>
            ) : (
              paginatedRows.map(({ sms, order }) => (
                <TableRow key={sms.id}>
                  <TableCell>
                    {order ? (
                      <Link href={`/ordering/orders/${order.id}`} className="font-mono text-xs text-blue-600 hover:underline">
                        {order.orderNumber}
                      </Link>
                    ) : (
                      <span className="font-mono text-xs text-gray-400">주문 #{sms.orderId}</span>
                    )}
                  </TableCell>
                  <TableCell className="text-sm">{sms.sentAt}</TableCell>
                  <TableCell className="text-sm">{order?.vendorName ?? '-'}</TableCell>
                  <TableCell className="text-sm">{order?.productName ?? '-'}</TableCell>
                  <TableCell className="text-sm">{order?.ordererName ?? '-'}</TableCell>
                  <TableCell className="font-mono text-sm">{sms.receiver}</TableCell>
                  <TableCell>
                    {sms.status === 'SUCCESS' ? (
                      <span className="inline-flex items-center rounded-full bg-blue-100 px-2 py-0.5 text-xs font-medium text-blue-700" title={sms.failReason ?? undefined}>
                        발송성공{sms.failReason && <span className="ml-1 text-amber-600">(즉시전환)</span>}
                      </span>
                    ) : (
                      <span className="inline-flex items-center rounded-full bg-red-100 px-2 py-0.5 text-xs font-medium text-red-700" title={sms.failReason ?? undefined}>
                        발송실패
                      </span>
                    )}
                  </TableCell>
                  <TableCell>
                    {sms.status === 'FAIL' && (
                      <button
                        onClick={() => handleResend(sms.orderId)}
                        disabled={resendingIds.has(sms.orderId)}
                        className="flex items-center gap-1 rounded-md border px-2 py-1 text-xs text-gray-600 hover:bg-gray-50 disabled:opacity-50"
                      >
                        <RefreshCw className="h-3 w-3" />
                        재발송
                      </button>
                    )}
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
        {totalPages > 1 && (
          <div className="border-t px-4 py-3">
            <Pagination
              page={page}
              totalPages={totalPages}
              totalElements={filtered.length}
              size={PAGE_SIZE}
              onPageChange={setPage}
            />
          </div>
        )}
      </div>

      {/* 모바일 카드 */}
      <div className="space-y-3 md:hidden">
        {loading ? (
          <div className="rounded-lg border bg-white py-10 text-center text-sm text-gray-400">불러오는 중...</div>
        ) : paginatedRows.length === 0 ? (
          <div className="rounded-lg border bg-white py-10 text-center text-sm text-gray-400">
            검색 결과가 없습니다.
          </div>
        ) : (
          paginatedRows.map(({ sms, order }) => (
            <div key={sms.id} className="rounded-lg border bg-white p-4">
              <div className="flex items-start justify-between gap-2">
                <div className="min-w-0">
                  {order ? (
                    <Link href={`/ordering/orders/${order.id}`} className="font-mono text-xs text-blue-600 hover:underline">
                      {order.orderNumber}
                    </Link>
                  ) : (
                    <span className="font-mono text-xs text-gray-400">주문 #{sms.orderId}</span>
                  )}
                  <div className="mt-1 truncate text-sm font-medium">{order?.productName ?? '-'}</div>
                  <div className="mt-0.5 text-xs text-gray-500">{order?.vendorName ?? '-'} · {order?.ordererName ?? '-'}</div>
                </div>
                {sms.status === 'SUCCESS' ? (
                  <span className="inline-flex items-center rounded-full bg-blue-100 px-2 py-0.5 text-xs font-medium text-blue-700 whitespace-nowrap">
                    발송성공
                  </span>
                ) : (
                  <span className="inline-flex items-center rounded-full bg-red-100 px-2 py-0.5 text-xs font-medium text-red-700 whitespace-nowrap">
                    발송실패
                  </span>
                )}
              </div>
              <div className="mt-2 flex flex-wrap items-center justify-between gap-x-3 gap-y-0.5 text-xs text-gray-400">
                <span>발송 {sms.sentAt} · {sms.receiver}</span>
                {sms.status === 'FAIL' && (
                  <button
                    onClick={() => handleResend(sms.orderId)}
                    disabled={resendingIds.has(sms.orderId)}
                    className="flex items-center gap-1 rounded-md border px-2 py-1 text-xs text-gray-600 hover:bg-gray-50 disabled:opacity-50"
                  >
                    <RefreshCw className="h-3 w-3" />
                    재발송
                  </button>
                )}
              </div>
            </div>
          ))
        )}
        {totalPages > 1 && (
          <Pagination
            page={page}
            totalPages={totalPages}
            totalElements={filtered.length}
            size={PAGE_SIZE}
            onPageChange={setPage}
          />
        )}
      </div>
    </div>
  )
}

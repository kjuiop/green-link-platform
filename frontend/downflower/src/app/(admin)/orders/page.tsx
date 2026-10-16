'use client'

import { useState } from 'react'
import Link from 'next/link'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { OrderStatusBadge, MessageStatusBadge } from '@/components/common/StatusBadge'
import { mockOrders } from '@/lib/mock/orders'
import { OrderStatus } from '@/types/order'
import { Plus, Search } from 'lucide-react'

const STATUS_FILTERS: { label: string; value: OrderStatus | 'ALL' }[] = [
  { label: '전체', value: 'ALL' },
  { label: '발주대기', value: '발주대기' },
  { label: '수주완료', value: '수주완료' },
  { label: '배송완료', value: '배송완료' },
  { label: '수주거절', value: '수주거절' },
  { label: '주문취소', value: '주문취소' },
]

export default function OrdersPage() {
  const [query, setQuery] = useState('')
  const [statusFilter, setStatusFilter] = useState<OrderStatus | 'ALL'>('ALL')
  const [dateFrom, setDateFrom] = useState('')
  const [dateTo, setDateTo] = useState('')
  const [appliedQuery, setAppliedQuery] = useState('')
  const [appliedStatus, setAppliedStatus] = useState<OrderStatus | 'ALL'>('ALL')
  const [appliedFrom, setAppliedFrom] = useState('')
  const [appliedTo, setAppliedTo] = useState('')

  const handleSearch = () => {
    setAppliedQuery(query)
    setAppliedStatus(statusFilter)
    setAppliedFrom(dateFrom)
    setAppliedTo(dateTo)
  }

  const handleReset = () => {
    setQuery(''); setStatusFilter('ALL'); setDateFrom(''); setDateTo('')
    setAppliedQuery(''); setAppliedStatus('ALL'); setAppliedFrom(''); setAppliedTo('')
  }

  const filtered = mockOrders.filter((o) => {
    const matchStatus = appliedStatus === 'ALL' || o.status === appliedStatus
    const matchQuery =
      !appliedQuery ||
      o.orderNumber.includes(appliedQuery) ||
      o.vendorName.includes(appliedQuery) ||
      o.productName.includes(appliedQuery)
    const deliveryDate = o.deliveryAt.slice(0, 10)
    const matchFrom = !appliedFrom || deliveryDate >= appliedFrom
    const matchTo = !appliedTo || deliveryDate <= appliedTo
    return matchStatus && matchQuery && matchFrom && matchTo
  })

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold">발주 목록</h1>
        <Link
          href="/orders/new"
          className="inline-flex items-center gap-1.5 rounded-lg bg-green-700 px-2.5 py-1.5 text-sm font-medium text-white hover:bg-green-800"
        >
          <Plus className="h-4 w-4" />
          신규 발주
        </Link>
      </div>

      {/* 검색 영역 */}
      <div className="rounded-lg border bg-white p-4">
        <div className="flex flex-wrap gap-4">
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
        <div className="mt-3 flex flex-wrap gap-2">
          <div className="flex items-center gap-1.5">
            <label className="text-sm text-gray-500 whitespace-nowrap">배송마감</label>
            <input
              type="date"
              value={dateFrom}
              onChange={(e) => setDateFrom(e.target.value)}
              className="rounded-md border px-2 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
            />
            <span className="text-sm text-gray-400">~</span>
            <input
              type="date"
              value={dateTo}
              onChange={(e) => setDateTo(e.target.value)}
              className="rounded-md border px-2 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
            />
          </div>
          <div className="relative flex-1 min-w-48">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
            <input
              type="text"
              placeholder="주문번호, 업체명, 상품명으로 검색"
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

      <div className="rounded-lg border bg-white">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>주문번호</TableHead>
              <TableHead>발주일시</TableHead>
              <TableHead>배송예정</TableHead>
              <TableHead>수주업체</TableHead>
              <TableHead>상품명</TableHead>
              <TableHead>배송장소</TableHead>
              <TableHead className="text-right">발주금액</TableHead>
              <TableHead>주문상태</TableHead>
              <TableHead>문자발송</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filtered.length === 0 ? (
              <TableRow>
                <TableCell colSpan={9} className="py-10 text-center text-sm text-gray-400">
                  검색 결과가 없습니다.
                </TableCell>
              </TableRow>
            ) : (
              filtered.map((order) => (
                <TableRow key={order.id} className="cursor-pointer hover:bg-gray-50">
                  <TableCell>
                    <Link href={`/orders/${order.id}`} className="font-mono text-xs text-blue-600 hover:underline">
                      {order.orderNumber}
                    </Link>
                  </TableCell>
                  <TableCell className="text-sm">{order.orderedAt}</TableCell>
                  <TableCell className="text-sm">{order.deliveryAt}</TableCell>
                  <TableCell className="text-sm">{order.vendorName}</TableCell>
                  <TableCell className="text-sm">{order.productName}</TableCell>
                  <TableCell className="text-sm">{order.placeName} {order.roomName}</TableCell>
                  <TableCell className="text-right text-sm">{order.amount.toLocaleString()}원</TableCell>
                  <TableCell><OrderStatusBadge status={order.status} /></TableCell>
                  <TableCell><MessageStatusBadge status={order.messageStatus} /></TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  )
}

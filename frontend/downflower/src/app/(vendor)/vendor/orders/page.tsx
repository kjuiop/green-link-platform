'use client'

import { useState } from 'react'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { OrderStatusBadge, MessageStatusBadge, DeliveryTypeBadge } from '@/components/common/StatusBadge'
import { mockOrders } from '@/lib/mock/orders'
import { OrderStatus } from '@/types/order'
import Link from 'next/link'
import { Search } from 'lucide-react'

const STATUS_FILTERS: { label: string; value: OrderStatus | 'ALL' }[] = [
  { label: '전체', value: 'ALL' },
  { label: '발주대기', value: '발주대기' },
  { label: '수주완료', value: '수주완료' },
  { label: '배송완료', value: '배송완료' },
  { label: '수주거절', value: '수주거절' },
]

export default function VendorOrdersPage() {
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
      o.productName.includes(appliedQuery)
    const deliveryDate = o.deliveryAt.slice(0, 10)
    const matchFrom = !appliedFrom || deliveryDate >= appliedFrom
    const matchTo = !appliedTo || deliveryDate <= appliedTo
    return matchStatus && matchQuery && matchFrom && matchTo
  })

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">수주 목록</h1>

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
        <div className="mt-3 flex flex-wrap gap-2">
          <div className="flex flex-wrap items-center gap-1.5">
            <label className="whitespace-nowrap text-sm text-gray-500">배송마감</label>
            <div className="flex items-center gap-1.5">
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
          </div>
          <div className="relative min-w-48 flex-1">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
            <input
              type="text"
              placeholder="주문번호, 상품명으로 검색"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
              className="w-full rounded-md border px-3 py-2 pl-9 text-sm placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-green-700"
            />
          </div>
          <button onClick={handleSearch} className="rounded-md bg-green-700 px-4 py-2 text-sm font-medium text-white hover:bg-green-800">검색</button>
          <button onClick={handleReset} className="rounded-md border px-4 py-2 text-sm font-medium text-gray-500 hover:bg-gray-50">초기화</button>
        </div>
      </div>

      {/* 데스크톱 테이블 */}
      <div className="hidden rounded-lg border bg-white md:block">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>주문번호</TableHead>
              <TableHead>상품명</TableHead>
              <TableHead>배송장소</TableHead>
              <TableHead>배송예정</TableHead>
              <TableHead>배송마감시간</TableHead>
              <TableHead>배송유형</TableHead>
              <TableHead>주문상태</TableHead>
              <TableHead>문자발송</TableHead>
              <TableHead>발주일시</TableHead>
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
                    <Link href={`/vendor/orders/${order.id}`} className="font-mono text-xs text-blue-600 hover:underline">
                      {order.orderNumber}
                    </Link>
                  </TableCell>
                  <TableCell className="text-sm">{order.productName}</TableCell>
                  <TableCell className="text-sm">{order.placeName} {order.roomName}</TableCell>
                  <TableCell className="text-sm">{order.deliveryAt}</TableCell>
                  <TableCell className="text-sm">{order.deliveryDeadline ?? '-'}</TableCell>
                  <TableCell><DeliveryTypeBadge deliveryType={order.deliveryType} /></TableCell>
                  <TableCell><OrderStatusBadge status={order.status} /></TableCell>
                  <TableCell><MessageStatusBadge status={order.messageStatus} /></TableCell>
                  <TableCell className="text-sm text-gray-400">{order.orderedAt}</TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {/* 모바일 카드 */}
      <div className="space-y-3 md:hidden">
        {filtered.length === 0 ? (
          <div className="rounded-lg border bg-white py-10 text-center text-sm text-gray-400">
            검색 결과가 없습니다.
          </div>
        ) : (
          filtered.map((order) => (
            <Link
              key={order.id}
              href={`/vendor/orders/${order.id}`}
              className="block rounded-lg border bg-white p-4 hover:bg-gray-50"
            >
              <div className="flex items-start justify-between gap-2">
                <div className="min-w-0">
                  <div className="font-mono text-xs text-blue-600">{order.orderNumber}</div>
                  <div className="mt-1 truncate text-sm font-medium">{order.productName}</div>
                  <div className="mt-0.5 text-xs text-gray-500">{order.placeName} {order.roomName}</div>
                </div>
                <div className="flex shrink-0 flex-col items-end gap-1">
                  <OrderStatusBadge status={order.status} />
                  <DeliveryTypeBadge deliveryType={order.deliveryType} />
                </div>
              </div>
              <div className="mt-2 flex flex-wrap gap-x-3 gap-y-0.5 text-xs text-gray-400">
                <span>{order.deliveryAt}</span>
                {order.deliveryDeadline && <span>마감 {order.deliveryDeadline}</span>}
              </div>
              <div className="mt-1.5">
                <MessageStatusBadge status={order.messageStatus} />
              </div>
            </Link>
          ))
        )}
      </div>
    </div>
  )
}

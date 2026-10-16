'use client'

import { useState } from 'react'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { MessageStatusBadge } from '@/components/common/StatusBadge'
import { mockOrders } from '@/lib/mock/orders'
import { MessageStatus } from '@/types/order'
import Link from 'next/link'
import { Search } from 'lucide-react'

const STATUS_FILTERS: { label: string; value: MessageStatus | 'ALL' }[] = [
  { label: '전체', value: 'ALL' },
  { label: '발송성공', value: '발송성공' },
  { label: '발송실패', value: '발송실패' },
]

const BASE_ORDERS = mockOrders.filter((o) => o.messageStatus !== '미발송')

export default function MessagesPage() {
  const [query, setQuery] = useState('')
  const [statusFilter, setStatusFilter] = useState<MessageStatus | 'ALL'>('ALL')
  const [appliedQuery, setAppliedQuery] = useState('')
  const [appliedStatus, setAppliedStatus] = useState<MessageStatus | 'ALL'>('ALL')

  const handleSearch = () => {
    setAppliedQuery(query)
    setAppliedStatus(statusFilter)
  }

  const handleReset = () => {
    setQuery(''); setStatusFilter('ALL')
    setAppliedQuery(''); setAppliedStatus('ALL')
  }

  const filtered = BASE_ORDERS.filter((o) => {
    const matchStatus = appliedStatus === 'ALL' || o.messageStatus === appliedStatus
    const matchQuery =
      !appliedQuery ||
      o.orderNumber.includes(appliedQuery) ||
      o.vendorName.includes(appliedQuery) ||
      o.ordererName.includes(appliedQuery)
    return matchStatus && matchQuery
  })

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">문자 발송내역</h1>

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
        <div className="mt-3 flex gap-2">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
            <input
              type="text"
              placeholder="주문번호, 업체명, 주문자명으로 검색"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
              className="w-full rounded-md border px-3 py-2 pl-9 text-sm placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-green-700"
            />
          </div>
          <button
            onClick={handleSearch}
            className="rounded-md bg-green-700 px-5 py-2.5 text-sm font-medium text-white hover:bg-green-800"
          >
            검색
          </button>
          <button
            onClick={handleReset}
            className="rounded-md border px-5 py-2.5 text-sm font-medium text-gray-500 hover:bg-gray-50"
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
              <TableHead>배송완료일시</TableHead>
              <TableHead>수주업체</TableHead>
              <TableHead>상품명</TableHead>
              <TableHead>주문자</TableHead>
              <TableHead>발송대상</TableHead>
              <TableHead>발송상태</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filtered.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} className="py-10 text-center text-sm text-gray-400">
                  검색 결과가 없습니다.
                </TableCell>
              </TableRow>
            ) : (
              filtered.map((order) => (
                <TableRow key={order.id}>
                  <TableCell>
                    <Link href={`/orders/${order.id}`} className="font-mono text-xs text-blue-600 hover:underline">
                      {order.orderNumber}
                    </Link>
                  </TableCell>
                  <TableCell className="text-sm">{order.completedAt ?? '-'}</TableCell>
                  <TableCell className="text-sm">{order.vendorName}</TableCell>
                  <TableCell className="text-sm">{order.productName}</TableCell>
                  <TableCell className="text-sm">{order.ordererName}</TableCell>
                  <TableCell className="text-sm">
                    {[
                      order.sendPhotoToOrderer ? '주문자' : '',
                      order.sendPhotoToRecipient ? '받는 분' : '',
                    ].filter(Boolean).join(', ') || '-'}
                  </TableCell>
                  <TableCell>
                    <MessageStatusBadge status={order.messageStatus} />
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  )
}

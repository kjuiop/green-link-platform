'use client'

import { useState, useEffect } from 'react'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { VendorOrderStatusBadge, MessageStatusBadge, DeliveryTimeTypeBadge, AcceptOrderBadge } from '@/components/common/StatusBadge'
import { fetchOrders, updateOrderStatus } from '@/lib/api/orderApi'
import { getDeliveryDateRowBg } from '@/lib/utils'
import { Order, OrderStatus } from '@/types/order'
import Link from 'next/link'
import { Search, Printer } from 'lucide-react'
import { ReferencePhotoIcon } from '@/components/common/ReferencePhotoViewer'
import { ReceiptConfirmButton } from '@/components/common/ReceiptConfirmDialog'

const STATUS_FILTERS: { label: string; value: OrderStatus | 'ALL' }[] = [
  { label: '전체', value: 'ALL' },
  { label: '미확인', value: '발주대기' },
  { label: '수주완료', value: '수주완료' },
  { label: '배송완료', value: '배송완료' },
  { label: '수주거절', value: '수주거절' },
]

const MSG_STATUS_FILTERS: { label: string; value: string }[] = [
  { label: '전체', value: 'ALL' },
  { label: '미발송', value: '미발송' },
  { label: '발송', value: '발송' },
  { label: '실패', value: '실패' },
]

export default function VendorOrdersPage() {
  const [orders, setOrders] = useState<Order[]>([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [query, setQuery] = useState('')
  const [statusFilter, setStatusFilter] = useState<OrderStatus | 'ALL'>('ALL')
  const [msgStatusFilter, setMsgStatusFilter] = useState('ALL')
  const [dateFrom, setDateFrom] = useState('')
  const [dateTo, setDateTo] = useState('')
  const [appliedQuery, setAppliedQuery] = useState('')
  const [appliedStatus, setAppliedStatus] = useState<OrderStatus | 'ALL'>('ALL')
  const [appliedMsgStatus, setAppliedMsgStatus] = useState('ALL')
  const [appliedFrom, setAppliedFrom] = useState('')
  const [appliedTo, setAppliedTo] = useState('')

  useEffect(() => {
    const vendorId = typeof window !== 'undefined' ? localStorage.getItem('vendorId') ?? undefined : undefined
    fetchOrders({ vendorId })
      .then(setOrders)
      .catch(() => setLoadError('수주 목록을 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }, [])

  const handleSearch = () => {
    setAppliedQuery(query)
    setAppliedStatus(statusFilter)
    setAppliedMsgStatus(msgStatusFilter)
    setAppliedFrom(dateFrom)
    setAppliedTo(dateTo)
  }

  const handleReset = () => {
    setQuery(''); setStatusFilter('ALL'); setMsgStatusFilter('ALL'); setDateFrom(''); setDateTo('')
    setAppliedQuery(''); setAppliedStatus('ALL'); setAppliedMsgStatus('ALL'); setAppliedFrom(''); setAppliedTo('')
  }

  const filtered = orders.filter((o) => {
    const matchStatus = appliedStatus === 'ALL' || o.status === appliedStatus
    const matchMsgStatus = appliedMsgStatus === 'ALL' || o.messageStatus === appliedMsgStatus
    const matchQuery =
      !appliedQuery ||
      o.orderNumber.includes(appliedQuery) ||
      o.productName.includes(appliedQuery)
    const deliveryDate = o.deliveryAt.slice(0, 10)
    const matchFrom = !appliedFrom || deliveryDate >= appliedFrom
    const matchTo = !appliedTo || deliveryDate <= appliedTo
    return matchStatus && matchMsgStatus && matchQuery && matchFrom && matchTo
  })

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">수주 목록</h1>

      {loadError && (
        <div className="rounded-md border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-600">
          {loadError}
        </div>
      )}

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

        <div className="mt-2 flex flex-wrap items-center gap-3">
          <span className="text-xs text-gray-400">문자현황</span>
          {MSG_STATUS_FILTERS.map(({ label, value }) => (
            <label key={value} className="flex cursor-pointer items-center gap-1.5">
              <input
                type="radio"
                name="msgStatusFilter"
                checked={msgStatusFilter === value}
                onChange={() => setMsgStatusFilter(value)}
                className="accent-green-700"
              />
              <span className="text-sm text-gray-700">{label}</span>
            </label>
          ))}
        </div>

        <div className="mt-3 flex flex-wrap gap-2">
          <div className="flex flex-wrap items-center gap-1.5">
            <label className="whitespace-nowrap text-sm text-gray-500">배송날짜</label>
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
              <TableHead>배송날짜</TableHead>
              <TableHead>배송시간</TableHead>
              <TableHead>배송완료일</TableHead>
              <TableHead>주문상태</TableHead>
              <TableHead>문자발송</TableHead>
              <TableHead>발주일시</TableHead>
              <TableHead>인쇄</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={10} className="py-10 text-center text-sm text-gray-400">
                  불러오는 중...
                </TableCell>
              </TableRow>
            ) : filtered.length === 0 ? (
              <TableRow>
                <TableCell colSpan={10} className="py-10 text-center text-sm text-gray-400">
                  검색 결과가 없습니다.
                </TableCell>
              </TableRow>
            ) : (
              filtered.map((order) => (
                <TableRow key={order.id} className={`cursor-pointer ${getDeliveryDateRowBg(order.deliveryAt)} hover:brightness-95`}>
                  <TableCell>
                    <Link href={`/vendor/orders/${order.id}`} className="font-mono text-xs text-blue-600 hover:underline">
                      {order.orderNumber}
                    </Link>
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-1 text-sm">
                      <span>{order.productName}</span>
                      <ReferencePhotoIcon orderId={order.id} count={order.referencePhotoCount ?? 0} />
                    </div>
                  </TableCell>
                  <TableCell className="text-sm">{order.placeName} {order.roomName}</TableCell>
                  <TableCell className="text-sm">
                    <span>{order.deliveryAt.split(' ')[0]}</span>
                    {order.deliveryTimeHour && (
                      <span className="ml-1 text-red-500">
                        {String(order.deliveryTimeHour).padStart(2, '0')}:{order.deliveryTimeMinute ?? '00'}
                      </span>
                    )}
                  </TableCell>
                  <TableCell><DeliveryTimeTypeBadge type={order.deliveryTimeType} custom={order.deliveryTimeCustom} /></TableCell>
                  <TableCell className="text-sm">{order.completedAt ?? '-'}</TableCell>
                  <TableCell onClick={(e) => e.stopPropagation()}>
                    <AcceptOrderBadge
                      status={order.status}
                      variant="vendor"
                      onAccept={async () => {
                        const updated = await updateOrderStatus(order.id, 'accept')
                        setOrders(prev => prev.map(o => o.id === updated.id ? updated : o))
                      }}
                    />
                  </TableCell>
                  <TableCell><MessageStatusBadge status={order.messageStatus} /></TableCell>
                  <TableCell className="text-sm text-gray-400">{order.orderedAt}</TableCell>
                  <TableCell onClick={(e) => e.stopPropagation()}>
                    <button
                      type="button"
                      title="주문서 인쇄"
                      onClick={() => window.open(`/orders/${order.id}/print`, '_blank')}
                      className="rounded p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-700"
                    >
                      <Printer className="h-4 w-4" />
                    </button>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {/* 모바일 카드 */}
      <div className="space-y-3 md:hidden">
        {loading ? (
          <div className="rounded-lg border bg-white py-10 text-center text-sm text-gray-400">
            불러오는 중...
          </div>
        ) : filtered.length === 0 ? (
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
                  <div className="mt-1 flex items-center gap-1 text-sm font-medium">
                    <span className="truncate">{order.productName}</span>
                    <ReferencePhotoIcon orderId={order.id} count={order.referencePhotoCount ?? 0} />
                  </div>
                  <div className="mt-0.5 text-xs text-gray-500">{order.placeName} {order.roomName}</div>
                </div>
                <div className="flex shrink-0 flex-col items-end gap-1">
                  <div onClick={(e) => { e.preventDefault(); e.stopPropagation() }}>
                    <AcceptOrderBadge
                      status={order.status}
                      variant="vendor"
                      onAccept={async () => {
                        const updated = await updateOrderStatus(order.id, 'accept')
                        setOrders(prev => prev.map(o => o.id === updated.id ? updated : o))
                      }}
                    />
                  </div>
                  <DeliveryTimeTypeBadge type={order.deliveryTimeType} custom={order.deliveryTimeCustom} />
                </div>
              </div>
              <div className="mt-2 flex flex-wrap gap-x-3 gap-y-0.5 text-xs text-gray-400">
                <span>{order.deliveryAt}</span>
                {order.completedAt && <span>완료 {order.completedAt}</span>}
              </div>
              <div className="mt-1.5">
                <MessageStatusBadge status={order.messageStatus} />
              </div>
              <div className="mt-2 flex items-center justify-between" onClick={(e) => { e.preventDefault(); e.stopPropagation() }}>
                <ReceiptConfirmButton
                  orderId={order.id}
                  orderNumber={order.orderNumber}
                  senderName={order.ribbonLeft}
                  recipientName={order.recipientName}
                  initialHasReceipt={order.hasReceipt}
                />
                <button
                  type="button"
                  onClick={() => window.open(`/orders/${order.id}/print`, '_blank')}
                  className="inline-flex items-center gap-1 rounded border px-2 py-1 text-xs text-gray-500 hover:bg-gray-100"
                >
                  <Printer className="h-3.5 w-3.5" />
                  인쇄
                </button>
              </div>
            </Link>
          ))
        )}
      </div>
    </div>
  )
}

'use client'

import { useState, useEffect, useRef } from 'react'
import Link from 'next/link'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { AcceptOrderBadge } from '@/components/common/StatusBadge'
import { ClickableMessageBadge } from '@/components/common/DeliveryPhotoCell'
import { ReceiptInfoCell } from '@/components/common/ReceiptConfirmDialog'
import { fetchOrdersPaged, downloadOrdersExcel, updateOrderStatus, OrderSearchParams } from '@/lib/api/orderApi'
import { getDeliveryDateRowBg, getDeliveryDateTextStyle, formatDeliveryShort } from '@/lib/utils'
import { OrderStatus, Order } from '@/types/order'
import { PageResult } from '@/types/page'
import { Pagination } from '@/components/ui/Pagination'
import { Plus, Download } from 'lucide-react'
import { ReferencePhotoIcon } from '@/components/common/ReferencePhotoViewer'
import { OrderDetailPopup } from '@/components/common/OrderDetailPopup'

type SearchField = '발주사명' | '수주사명' | '주문일' | '배송일' | '주문자' | '받는분' | '배달장소' | '보내는분' | '인수자' | '주문번호'
type DeletedFilter = '정상' | '삭제' | '전체'

const SEARCH_FIELDS: SearchField[] = ['발주사명', '수주사명', '주문일', '배송일', '주문자', '받는분', '배달장소', '보내는분', '인수자', '주문번호']

const STATUS_TO_API: Partial<Record<OrderStatus, string>> = {
  '발주대기': 'PENDING',
  '수주완료': 'ACCEPTED',
  '배송완료': 'DELIVERED',
  '수주거절': 'REJECTED',
  '주문취소': 'CANCELLED',
}

type StringOrderSearchKey = { [K in keyof OrderSearchParams]: OrderSearchParams[K] extends string | undefined ? K : never }[keyof OrderSearchParams]
const SERVER_FIELD_MAP: Partial<Record<SearchField, StringOrderSearchKey>> = {
  '주문번호': 'orderNumber',
  '주문자': 'ordererName',
  '보내는분': 'ribbonLeft',
  '인수자': 'receiverName',
  '배달장소': 'address',
  '받는분': 'recipientName',
  '수주사명': 'vendorName',
  '발주사명': 'orderingCompanyName',
}

const MSG_STATUS_FILTERS: { label: string; value: string }[] = [
  { label: '전체', value: 'ALL' },
  { label: '미발송', value: 'NONE' },
  { label: '발송', value: 'SENT' },
  { label: '실패', value: 'FAIL' },
]

function matchField(o: Order, field: SearchField, value: string): boolean {
  if (!value.trim()) return true
  const v = value.trim()
  switch (field) {
    case '발주사명': return (o.orderingCompanyName ?? '').includes(v)
    case '수주사명': return o.vendorName.includes(v)
    case '주문일':   return o.orderedAt.includes(v)
    case '배송일':   return o.deliveryAt.includes(v)
    case '주문자':   return o.ordererName.includes(v)
    case '받는분':   return (o.recipientName ?? '').includes(v)
    case '배달장소': return `${o.placeName} ${o.roomName} ${o.address}`.includes(v)
    case '보내는분': return o.ribbonLeft.includes(v)
    case '인수자':   return true  // server-side
    case '주문번호': return o.orderNumber.includes(v)
  }
}

const STATUS_FILTERS: { label: string; value: OrderStatus | 'ALL' }[] = [
  { label: '전체', value: 'ALL' },
  { label: '미확인', value: '발주대기' },
  { label: '수주완료', value: '수주완료' },
  { label: '배송완료', value: '배송완료' },
  { label: '수주거절', value: '수주거절' },
]

const PAGE_SIZE_OPTIONS = [10, 50, 100, 200, 300, 500]
const YEAR_OPTIONS = [new Date().getFullYear(), new Date().getFullYear() - 1]

function getMonthRange(year: number, month: number): { from: string; to: string } {
  const m = String(month).padStart(2, '0')
  const lastDay = new Date(year, month, 0).getDate()
  return { from: `${year}-${m}-01`, to: `${year}-${m}-${String(lastDay).padStart(2, '0')}` }
}

export default function OrdersPage() {
  const today = new Date()
  const initYear = today.getFullYear()
  const initMonth = today.getMonth() + 1
  const initRange = getMonthRange(initYear, initMonth)

  const [result, setResult] = useState<PageResult<Order> | null>(null)
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState('')
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set())
  const [excelLoading, setExcelLoading] = useState(false)

  const [statusFilter, setStatusFilter] = useState<OrderStatus | 'ALL'>('ALL')
  const [msgStatusFilter, setMsgStatusFilter] = useState('ALL')
  const [deletedFilter, setDeletedFilter] = useState<DeletedFilter>('정상')
  const [selectedYear, setSelectedYear] = useState(initYear)
  const [selectedMonth, setSelectedMonth] = useState(initMonth)
  const [dateFrom, setDateFrom] = useState(initRange.from)
  const [dateTo, setDateTo] = useState(initRange.to)
  const [search1Field, setSearch1Field] = useState<SearchField>('주문번호')
  const [search1Value, setSearch1Value] = useState('')
  const [search2Field, setSearch2Field] = useState<SearchField>('수주사명')
  const [search2Value, setSearch2Value] = useState('')

  const [appliedDeletedFilter, setAppliedDeletedFilter] = useState<DeletedFilter>('정상')
  const [applied1Field, setApplied1Field] = useState<SearchField>('주문번호')
  const [applied1Value, setApplied1Value] = useState('')
  const [applied2Field, setApplied2Field] = useState<SearchField>('수주사명')
  const [applied2Value, setApplied2Value] = useState('')

  const isSuperAdmin = typeof window !== 'undefined' && localStorage.getItem('role') === 'SUPER_ADMIN'

  const [popups, setPopups] = useState<Array<{ id: string; zIndex: number }>>([])
  const nextZ = useRef(1000)

  function openPopup(orderId: string) {
    setPopups((prev) => {
      const existing = prev.find((p) => p.id === orderId)
      if (existing) {
        return prev.map((p) => p.id === orderId ? { ...p, zIndex: ++nextZ.current } : p)
      }
      return [...prev, { id: orderId, zIndex: ++nextZ.current }]
    })
  }

  function closePopup(orderId: string) {
    setPopups((prev) => prev.filter((p) => p.id !== orderId))
  }

  function focusPopup(orderId: string) {
    setPopups((prev) => prev.map((p) => p.id === orderId ? { ...p, zIndex: ++nextZ.current } : p))
  }

  function buildServerParams(fromDate = dateFrom, toDate = dateTo): OrderSearchParams {
    const params: OrderSearchParams = {}
    if (statusFilter !== 'ALL') params.status = STATUS_TO_API[statusFilter]
    if (msgStatusFilter !== 'ALL') params.messageStatus = msgStatusFilter
    if (fromDate) params.deliveryDateFrom = fromDate
    if (toDate) params.deliveryDateTo = toDate
    const key1 = SERVER_FIELD_MAP[search1Field]
    if (key1 && search1Value) params[key1] = search1Value
    const key2 = SERVER_FIELD_MAP[search2Field]
    if (key2 && search2Value) params[key2] = search2Value
    if (isSuperAdmin && deletedFilter === '삭제') params.deleted = true
    return params
  }

  function loadOrders(params: OrderSearchParams = {}, p = 0, size = pageSize) {
    setLoading(true)
    setLoadError('')
    fetchOrdersPaged(params, p, size)
      .then(setResult)
      .catch((err) => setLoadError(err instanceof Error ? err.message : '목록을 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { loadOrders(buildServerParams(initRange.from, initRange.to)) }, [])

  function selectMonth(year: number, month: number) {
    setSelectedYear(year)
    setSelectedMonth(month)
    const from = month === 0 ? '' : getMonthRange(year, month).from
    const to = month === 0 ? '' : getMonthRange(year, month).to
    setDateFrom(from)
    setDateTo(to)
    setPage(0)
    setSelectedIds(new Set())
    loadOrders(buildServerParams(from, to), 0)
  }

  const handleSearch = () => {
    setAppliedDeletedFilter(deletedFilter)
    setApplied1Field(search1Field)
    setApplied1Value(SERVER_FIELD_MAP[search1Field] ? '' : search1Value)
    setApplied2Field(search2Field)
    setApplied2Value(SERVER_FIELD_MAP[search2Field] ? '' : search2Value)
    setPage(0)
    setSelectedIds(new Set())
    loadOrders(buildServerParams(), 0)
  }

  const handleReset = () => {
    const r = getMonthRange(initYear, initMonth)
    setStatusFilter('ALL'); setMsgStatusFilter('ALL'); setDeletedFilter('정상')
    setSelectedYear(initYear); setSelectedMonth(initMonth)
    setDateFrom(r.from); setDateTo(r.to)
    setSearch1Field('주문번호'); setSearch1Value('')
    setSearch2Field('수주사명'); setSearch2Value('')
    setAppliedDeletedFilter('정상')
    setApplied1Field('주문번호'); setApplied1Value('')
    setApplied2Field('수주사명'); setApplied2Value('')
    setPage(0)
    setSelectedIds(new Set())
    loadOrders({ deliveryDateFrom: r.from, deliveryDateTo: r.to }, 0)
  }

  const handlePageChange = (p: number) => {
    setPage(p)
    setSelectedIds(new Set())
    loadOrders(buildServerParams(), p)
  }

  const handlePageSizeChange = (size: number) => {
    setPageSize(size)
    setPage(0)
    setSelectedIds(new Set())
    loadOrders(buildServerParams(), 0, size)
  }

  const orders = result?.content ?? []
  const filtered = orders.filter((o) => {
    const matchDeleted =
      appliedDeletedFilter === '전체' ||
      (appliedDeletedFilter === '정상' && !o.isDeleted) ||
      (appliedDeletedFilter === '삭제' && !!o.isDeleted)
    const match1 = !SERVER_FIELD_MAP[applied1Field] ? matchField(o, applied1Field, applied1Value) : true
    const match2 = !SERVER_FIELD_MAP[applied2Field] ? matchField(o, applied2Field, applied2Value) : true
    return matchDeleted && match1 && match2
  })

  const allSelected = filtered.length > 0 && filtered.every((o) => selectedIds.has(o.id))
  const toggleAll = () => setSelectedIds((prev) => {
    const next = new Set(prev)
    if (allSelected) filtered.forEach((o) => next.delete(o.id))
    else filtered.forEach((o) => next.add(o.id))
    return next
  })
  const toggleOne = (id: string) => setSelectedIds((prev) => {
    const next = new Set(prev)
    if (next.has(id)) next.delete(id); else next.add(id)
    return next
  })

  const handleExcelDownload = async () => {
    if (selectedIds.size === 0) return
    setExcelLoading(true)
    try { await downloadOrdersExcel(Array.from(selectedIds)) }
    catch { alert('엑셀 다운로드에 실패했습니다. 잠시 후 다시 시도해 주세요.') }
    finally { setExcelLoading(false) }
  }

  return (
    <div className="space-y-4">
      {loadError && (
        <div className="rounded-md border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-600">{loadError}</div>
      )}
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
        <div className="flex flex-wrap items-center gap-3">
          <span className="w-14 shrink-0 text-xs font-medium text-gray-500">주문 상태</span>
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
          <span className="w-14 shrink-0 text-xs font-medium text-gray-500">문자현황</span>
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

        {/* 배송월 */}
        <div className="mt-3 flex flex-wrap items-center gap-2">
          <span className="w-14 shrink-0 text-xs font-medium text-gray-500">배송월</span>
          <select
            value={selectedYear}
            onChange={(e) => selectMonth(Number(e.target.value), selectedMonth)}
            className="rounded-md border px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
          >
            {YEAR_OPTIONS.map((y) => <option key={y} value={y}>{y}년</option>)}
          </select>
          <div className="flex flex-wrap gap-1">
            {[{ label: '전체', value: 0 }, ...Array.from({ length: 12 }, (_, i) => ({ label: `${i + 1}월`, value: i + 1 }))].map(({ label, value }) => (
              <button
                key={value}
                type="button"
                onClick={() => selectMonth(selectedYear, value)}
                className={`rounded px-2.5 py-1 text-xs font-medium transition-colors ${
                  selectedMonth === value ? 'bg-green-700 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                }`}
              >
                {label}
              </button>
            ))}
          </div>
        </div>

        {/* 배송날짜 + 정상/삭제 */}
        <div className="mt-2 flex flex-wrap items-center gap-2">
          <span className="w-14 shrink-0 text-xs font-medium text-gray-500">배송날짜</span>
          <input
            type="date"
            value={dateFrom}
            onChange={(e) => { setDateFrom(e.target.value); setSelectedMonth(0) }}
            className="rounded-md border px-2 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
          />
          <span className="text-sm text-gray-400">~</span>
          <input
            type="date"
            value={dateTo}
            onChange={(e) => { setDateTo(e.target.value); setSelectedMonth(0) }}
            className="rounded-md border px-2 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
          />
          {isSuperAdmin && (
            <select
              value={deletedFilter}
              onChange={(e) => setDeletedFilter(e.target.value as DeletedFilter)}
              className="rounded-md border px-2 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
            >
              <option value="정상">정상</option>
              <option value="삭제">삭제</option>
            </select>
          )}
        </div>

        {/* 검색 조건 2쌍 */}
        <div className="mt-2 flex flex-wrap gap-2">
          <div className="flex min-w-0 flex-1 gap-1.5">
            <select
              value={search1Field}
              onChange={(e) => { setSearch1Field(e.target.value as SearchField); setSearch1Value('') }}
              className="w-28 shrink-0 rounded-md border px-2 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
            >
              {SEARCH_FIELDS.map((f) => <option key={f} value={f}>{f}</option>)}
            </select>
            <input
              type="text"
              placeholder={`${search1Field} 검색`}
              value={search1Value}
              onChange={(e) => setSearch1Value(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
              className="min-w-0 flex-1 rounded-md border px-3 py-2 text-sm placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-green-700"
            />
          </div>
          <div className="flex min-w-0 flex-1 gap-1.5">
            <select
              value={search2Field}
              onChange={(e) => { setSearch2Field(e.target.value as SearchField); setSearch2Value('') }}
              className="w-28 shrink-0 rounded-md border px-2 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
            >
              {SEARCH_FIELDS.map((f) => <option key={f} value={f}>{f}</option>)}
            </select>
            <input
              type="text"
              placeholder={`${search2Field} 검색`}
              value={search2Value}
              onChange={(e) => setSearch2Value(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
              className="min-w-0 flex-1 rounded-md border px-3 py-2 text-sm placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-green-700"
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

      {/* 검색 결과 요약 + 엑셀 다운로드 */}
      <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-center gap-4 text-sm">
          {selectedIds.size > 0 ? (
            <>
              <span className="text-gray-500">건수 <strong className="text-green-700">{selectedIds.size}</strong>건</span>
              <span className="text-gray-300">|</span>
              <span className="text-gray-500">발주금액 <strong className="text-green-700">{filtered.filter((o) => selectedIds.has(o.id)).reduce((sum, o) => sum + o.amount + (o.extraCost ?? 0), 0).toLocaleString()}</strong>원</span>
            </>
          ) : (
            <>
              <span className="text-gray-500">건수 <strong className="text-gray-900">{result?.totalElements ?? 0}</strong>건</span>
              <span className="text-gray-300">|</span>
              <span className="text-gray-500">발주금액 <strong className="text-gray-900">{(result?.totalAmount ?? 0).toLocaleString()}</strong>원</span>
            </>
          )}
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={handleExcelDownload}
            disabled={selectedIds.size === 0 || excelLoading}
            className="hidden sm:inline-flex items-center gap-1.5 rounded-md border border-green-700 px-3 py-1.5 text-sm font-medium text-green-700 hover:bg-green-50 disabled:cursor-not-allowed disabled:opacity-40"
          >
            <Download className="h-3.5 w-3.5" />
            {selectedIds.size > 0 ? `엑셀 다운로드 (${selectedIds.size})` : '엑셀 다운로드'}
          </button>
          <select
            value={pageSize}
            onChange={(e) => handlePageSizeChange(Number(e.target.value))}
            className="rounded-md border px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
          >
            {PAGE_SIZE_OPTIONS.map((s) => (
              <option key={s} value={s}>{s}개씩</option>
            ))}
          </select>
        </div>
      </div>

      {/* 데스크톱 테이블 */}
      <div className="hidden rounded-lg border bg-white md:block">
        <Table>
          <TableHeader>
            <TableRow className="[&>th]:h-auto [&>th]:py-1.5 [&>th]:text-xs">
              <TableHead className="w-8">
                <input
                  type="checkbox"
                  checked={allSelected}
                  onChange={toggleAll}
                  className="accent-green-700"
                  title="전체 선택"
                />
              </TableHead>
              <TableHead>주문번호</TableHead>
              <TableHead><div>배송요구일</div><div className="font-normal text-gray-400">주문접수일</div></TableHead>
              <TableHead className="w-36"><div>발주업체 / 주문자</div><div className="font-normal text-gray-400">수주업체</div></TableHead>
              <TableHead><div>상품명</div><div className="font-normal text-gray-400">받는분</div></TableHead>
              <TableHead className="w-36"><div>보내는이</div><div className="font-normal text-gray-400">배송지</div></TableHead>
              <TableHead>수량</TableHead>
              <TableHead><div>상품명</div><div className="font-normal text-gray-400">발주금액</div></TableHead>
              <TableHead>배송현황</TableHead>
              <TableHead>문자현황</TableHead>
              <TableHead>인수정보</TableHead>
              <TableHead>주문서</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={12} className="py-10 text-center text-sm text-gray-400">
                  불러오는 중...
                </TableCell>
              </TableRow>
            ) : filtered.length === 0 ? (
              <TableRow>
                <TableCell colSpan={12} className="py-10 text-center text-sm text-gray-400">
                  검색 결과가 없습니다.
                </TableCell>
              </TableRow>
            ) : (
              filtered.map((order) => (
                <TableRow
                  key={order.id}
                  className={`[&>td]:py-1 ${order.isDeleted ? 'bg-red-50 opacity-60' : `cursor-pointer ${getDeliveryDateRowBg(order.deliveryAt)} hover:brightness-95`}`}
                >
                  <TableCell onClick={(e) => e.stopPropagation()}>
                    <input
                      type="checkbox"
                      checked={selectedIds.has(order.id)}
                      onChange={() => toggleOne(order.id)}
                      className="accent-green-700"
                    />
                  </TableCell>
                  <TableCell>
                    <button
                      type="button"
                      onClick={() => openPopup(String(order.id))}
                      className="font-mono text-xs text-blue-600 hover:underline"
                    >
                      {order.orderNumber}
                    </button>
                    {order.isDeleted && (
                      <span className="ml-1.5 rounded bg-red-100 px-1 py-0.5 text-[10px] font-medium text-red-600">삭제</span>
                    )}
                  </TableCell>
                  <TableCell>
                    <div className="flex items-baseline gap-1.5">
                      <span className="text-sm" style={getDeliveryDateTextStyle(order.deliveryAt)}>{order.deliveryAt.split(' ')[0]}</span>
                      {order.deliveryTimeHour && (
                        <span className="text-sm font-medium text-red-500">
                          {String(order.deliveryTimeHour).padStart(2, '0')}:{order.deliveryTimeMinute ?? '00'}
                        </span>
                      )}
                      {order.deliveryTimeType && (
                        <span className="text-xs font-bold text-red-500">
                          {order.deliveryTimeType === '직접입력' ? order.deliveryTimeCustom : order.deliveryTimeType}
                        </span>
                      )}
                    </div>
                    <div className="text-sm text-gray-400">{order.orderedAt}</div>
                  </TableCell>
                  <TableCell>
                    <div className="max-w-[9rem] break-all text-sm">{order.orderingCompanyName || '—'}</div>
                    <div className="text-xs text-gray-500">{order.ordererName || '—'}</div>
                    <div className="max-w-[9rem] break-all text-sm text-gray-400">{order.vendorName || '—'}</div>
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-1 text-sm">
                      <span>{order.productName}</span>
                      <ReferencePhotoIcon orderId={order.id} count={order.referencePhotoCount ?? 0} />
                    </div>
                    {order.recipientName && <div className="text-sm text-gray-400">{order.recipientName}</div>}
                  </TableCell>
                  <TableCell>
                    <div className="text-sm">{order.ribbonLeft || '—'}</div>
                    <div className="max-w-[9rem] break-all text-sm text-gray-400">{formatDeliveryShort(order.placeName, order.address)}</div>
                  </TableCell>
                  <TableCell className="text-sm">{order.quantity}</TableCell>
                  <TableCell>
                    <div className="text-xs text-gray-600">{order.productName}</div>
                    <div className="text-xs text-gray-400">{order.amount.toLocaleString()}원</div>
                    <div className="text-sm font-medium text-blue-600">{(order.amount + (order.extraCost ?? 0)).toLocaleString()}원</div>
                  </TableCell>
                  <TableCell onClick={(e) => e.stopPropagation()}>
                    <AcceptOrderBadge
                      status={order.status}
                      variant="admin"
                      onAccept={async () => {
                        await updateOrderStatus(order.id, 'accept')
                        loadOrders(buildServerParams(), page)
                      }}
                    />
                    {order.acceptedAt && (
                      <div className="mt-0.5 text-[10px] text-gray-400">{order.acceptedAt.slice(0, 10)} {order.acceptedAt.slice(11, 16)}</div>
                    )}
                  </TableCell>
                  <TableCell>
                    <ClickableMessageBadge
                      status={order.messageStatus}
                      orderId={order.id}
                      photos={order.deliveryPhotos}
                      orderNumber={order.orderNumber}
                      ordererName={order.ordererName}
                      ordererPhone={order.ordererPhone}
                      recipientName={order.recipientName}
                      recipientPhone={order.recipientPhone}
                    />
                    <div className="mt-0.5 text-[10px] text-gray-400">
                      {order.messageSchedule === 'scheduled' && order.scheduledMessageHour
                        ? `예약 ${String(order.scheduledMessageHour).padStart(2, '0')}:${order.scheduledMessageMinute ?? '00'}`
                        : '자동발송'}
                    </div>
                  </TableCell>
                  <TableCell>
                    <ReceiptInfoCell
                      hasReceipt={order.hasReceipt}
                      receiverName={order.receiptReceiverName}
                      receiverRelation={order.receiptReceiverRelation}
                    />
                  </TableCell>
                  <TableCell onClick={(e) => e.stopPropagation()}>
                    <button
                      type="button"
                      title="주문서 보기"
                      onClick={() => openPopup(String(order.id))}
                      className="rounded px-2 py-1 text-xs font-medium text-blue-600 hover:bg-blue-50"
                    >
                      주문서
                    </button>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
        {result && (
          <div className="border-t px-4 py-3">
            <Pagination
              page={result.page}
              totalPages={result.totalPages}
              totalElements={result.totalElements}
              size={result.size}
              onPageChange={handlePageChange}
            />
          </div>
        )}
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
            <div
              key={order.id}
              className={`rounded-lg border p-4 ${order.isDeleted ? 'bg-red-50 opacity-60' : 'bg-white'}`}
            >
              <button
                type="button"
                className="w-full text-left"
                onClick={() => openPopup(String(order.id))}
              >
                <div className="flex items-start justify-between gap-2">
                  <div className="min-w-0">
                    <div className="flex items-center gap-1.5">
                      <span className="font-mono text-xs text-blue-600">{order.orderNumber}</span>
                      {order.isDeleted && (
                        <span className="rounded bg-red-100 px-1 py-0.5 text-[10px] font-medium text-red-600">삭제</span>
                      )}
                    </div>
                    <div className="mt-1 truncate text-sm font-medium">{order.productName}</div>
                    <div className="mt-0.5 text-xs text-gray-500">
                      {order.ordererName}{order.recipientName ? ` / ${order.recipientName}` : ''}
                    </div>
                    {order.vendorName && (
                      <div className="mt-0.5 text-xs text-gray-400">수주사 {order.vendorName}</div>
                    )}
                  </div>
                  <div className="flex shrink-0 flex-col items-end gap-1">
                    <div onClick={(e) => e.stopPropagation()}>
                      <AcceptOrderBadge
                        status={order.status}
                        variant="admin"
                        onAccept={async () => {
                          await updateOrderStatus(order.id, 'accept')
                          loadOrders(buildServerParams(), page)
                        }}
                      />
                    </div>
                    <div onClick={(e) => e.stopPropagation()}>
                      <ClickableMessageBadge status={order.messageStatus} orderId={order.id} photos={order.deliveryPhotos} orderNumber={order.orderNumber} />
                    </div>
                  </div>
                </div>
                <div className="mt-2 flex flex-wrap gap-x-3 gap-y-0.5 text-xs text-gray-400">
                  <span>
                    {order.deliveryAt.split(' ')[0]}
                    {order.deliveryTimeHour && ` ${String(order.deliveryTimeHour).padStart(2, '0')}:${order.deliveryTimeMinute ?? '00'}`}
                    {order.deliveryTimeType && order.deliveryTimeType !== '직접입력' && ` ${order.deliveryTimeType}`}
                    {order.deliveryTimeType === '직접입력' && order.deliveryTimeCustom && ` ${order.deliveryTimeCustom}`}
                  </span>
                  <span className="break-all">{order.placeName}</span>
                  <span>{(order.amount + (order.extraCost ?? 0)).toLocaleString()}원 · {order.quantity}개</span>
                </div>
              </button>
            </div>
          ))
        )}
        {result && (
          <Pagination
            page={result.page}
            totalPages={result.totalPages}
            totalElements={result.totalElements}
            size={result.size}
            onPageChange={handlePageChange}
          />
        )}
      </div>

      {/* 주문서 팝업 */}
      {popups.map((popup, idx) => (
        <OrderDetailPopup
          key={popup.id}
          orderId={popup.id}
          zIndex={popup.zIndex}
          initialOffset={idx}
          onClose={() => closePopup(popup.id)}
          onFocus={() => focusPopup(popup.id)}
          onUpdated={() => loadOrders(buildServerParams(), page)}
        />
      ))}
    </div>
  )
}

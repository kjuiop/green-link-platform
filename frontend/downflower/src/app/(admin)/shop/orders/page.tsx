'use client'

import Link from 'next/link'
import { useCallback, useEffect, useState } from 'react'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Pagination } from '@/components/ui/Pagination'
import { ShopCompanySelect } from '@/components/shop/ShopCompanySelect'
import { useShopScope } from '@/components/shop/useShopScope'
import { VendorShopApproveModal } from '@/components/shop/VendorShopApproveModal'
import { fetchMemberSummary } from '@/lib/api/memberApi'
import {
  fetchShopOrders,
  holdShopOrder,
  rejectShopOrder,
  releaseShopOrder,
  type ShopOrder,
  type ShopOrderStatus,
} from '@/lib/api/shopApi'
import type { CompanyType } from '@/lib/api/companyApi'
import { PageResult } from '@/types/page'

const PAGE_SIZE = 20

const TABS: { value: ShopOrderStatus; label: string }[] = [
  { value: 'PENDING', label: '승인대기' },
  { value: 'HELD', label: '보류' },
  { value: 'APPROVED', label: '승인됨' },
  { value: 'REJECTED', label: '거절됨' },
  { value: 'CANCELED', label: '취소됨' },
]

function won(n: number) {
  return `${n.toLocaleString('ko-KR')}원`
}

function formatDateTime(v: string | null) {
  return v ? v.replace('T', ' ').slice(0, 16) : '-'
}

export default function ShopOrdersPage() {
  const scope = useShopScope()
  const companyId = scope.selectedId

  const [status, setStatus] = useState<ShopOrderStatus>('PENDING')
  const [page, setPage] = useState(0)
  const [data, setData] = useState<PageResult<ShopOrder> | null>(null)
  const [loading, setLoading] = useState(true)
  const [busyId, setBusyId] = useState<number | null>(null)
  const [message, setMessage] = useState<{ kind: 'ok' | 'error'; text: string } | null>(null)
  const [ownType, setOwnType] = useState<CompanyType | null>(null)
  const [approving, setApproving] = useState<ShopOrder | null>(null)
  const [rejecting, setRejecting] = useState<ShopOrder | null>(null)
  const [rejectReason, setRejectReason] = useState('')

  const companyType: CompanyType | null = scope.isSuperAdmin
    ? (scope.companies.find((c) => c.id === companyId)?.type ?? null)
    : ownType
  const vendorOnly = companyType === 'VENDOR'

  useEffect(() => {
    if (scope.isSuperAdmin) return
    fetchMemberSummary()
      .then((s) => setOwnType(s?.companyType ?? null))
      .catch(() => {})
  }, [scope.isSuperAdmin])

  const load = useCallback(async () => {
    if (scope.isSuperAdmin && !companyId) {
      setLoading(false)
      return
    }
    setLoading(true)
    try {
      setData(await fetchShopOrders(companyId, status, page, PAGE_SIZE))
    } catch (e) {
      setMessage({ kind: 'error', text: e instanceof Error ? e.message : '불러오지 못했습니다.' })
    } finally {
      setLoading(false)
    }
  }, [scope.isSuperAdmin, companyId, status, page])

  useEffect(() => {
    if (!scope.ready) return
    // eslint-disable-next-line react-hooks/set-state-in-effect
    load()
  }, [scope.ready, load])

  async function handleHoldToggle(order: ShopOrder) {
    setBusyId(order.id)
    setMessage(null)
    try {
      if (order.status === 'PENDING') await holdShopOrder(order.id)
      else await releaseShopOrder(order.id)
      await load()
    } catch (e) {
      setMessage({ kind: 'error', text: e instanceof Error ? e.message : '처리에 실패했습니다.' })
    } finally {
      setBusyId(null)
    }
  }

  async function handleReject() {
    if (!rejecting) return
    setBusyId(rejecting.id)
    setMessage(null)
    try {
      await rejectShopOrder(rejecting.id, rejectReason.trim())
      setRejecting(null)
      setRejectReason('')
      await load()
    } catch (e) {
      setMessage({ kind: 'error', text: e instanceof Error ? e.message : '거절 처리에 실패했습니다.' })
    } finally {
      setBusyId(null)
    }
  }

  function renderApprove(o: ShopOrder) {
    if (!vendorOnly) {
      return (
        <Link href={`/orders/new?shopOrderId=${o.id}`}>
          <Button size="sm" className="bg-green-700 hover:bg-green-800">발주서 작성·승인</Button>
        </Link>
      )
    }
    if (scope.isSuperAdmin) {
      return <span className="self-center text-xs text-gray-400">수주사 전용 쇼핑몰은 해당 수주사 계정에서 승인합니다</span>
    }
    return (
      <Button size="sm" className="bg-green-700 hover:bg-green-800" onClick={() => setApproving(o)}>
        승인 및 발주 등록
      </Button>
    )
  }

  return (
    <div className="mx-auto max-w-4xl space-y-4 p-4 md:p-6">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h1 className="text-lg font-bold">쇼핑몰 주문함</h1>
        <ShopCompanySelect scope={scope} />
      </div>

      {message && (
        <p className={message.kind === 'ok' ? 'text-sm text-green-700' : 'text-sm text-red-500'}>{message.text}</p>
      )}

      <div className="flex gap-1 overflow-x-auto border-b">
        {TABS.map((t) => (
          <button
            key={t.value}
            onClick={() => { setStatus(t.value); setPage(0) }}
            className={
              status === t.value
                ? 'shrink-0 border-b-2 border-green-700 px-4 py-2 text-sm font-medium text-green-700'
                : 'shrink-0 px-4 py-2 text-sm text-gray-500 hover:text-gray-900'
            }
          >
            {t.label}
          </button>
        ))}
      </div>

      {loading ? (
        <p className="py-8 text-center text-sm text-gray-400">불러오는 중…</p>
      ) : !data || data.content.length === 0 ? (
        <p className="py-8 text-center text-sm text-gray-400">해당 상태의 쇼핑몰 주문이 없습니다.</p>
      ) : (
        <div className="space-y-3">
          {data.content.map((o) => (
            <Card key={o.id}>
              <CardContent className="space-y-2 pt-4 text-sm">
                <div className="flex flex-wrap items-center justify-between gap-2">
                  <span className="font-medium">주문번호 {o.orderNo}</span>
                  <span className="text-xs text-gray-400">접수 {formatDateTime(o.createdAt)}</span>
                </div>
                <div className="text-gray-700">
                  {o.items.map((i, idx) => (
                    <div key={idx}>{i.name} × {i.quantity} ({won(i.unitPrice)})</div>
                  ))}
                  <div className="font-medium">고객 결제 예정 합계 {won(o.totalPrice)}</div>
                  {o.status === 'APPROVED' && o.margin !== null && o.linkedOrderAmount !== null && (
                    <div className="text-xs text-gray-500">
                      발주 금액 {won(o.linkedOrderAmount)} · 마진 {won(o.margin)}
                    </div>
                  )}
                </div>
                <div className="grid gap-1 text-gray-600 md:grid-cols-2">
                  <div>주문자: {o.ordererName} {o.ordererPhone}</div>
                  <div>받는 분: {o.recipientName} {o.recipientPhone}</div>
                  <div className="md:col-span-2">주소: {o.address} {o.addressDetail}</div>
                  <div>
                    배송 희망: {o.deliveryDate ?? '-'} {o.deliveryTime ?? ''}
                  </div>
                  <div>행사: {o.eventType ?? '-'} {o.placeName ? `· ${o.placeName}` : ''}</div>
                  {(o.ribbonLeft || o.ribbonRight) && (
                    <div className="md:col-span-2">리본: {o.ribbonLeft ?? ''} / {o.ribbonRight ?? ''}</div>
                  )}
                  {o.memo && <div className="md:col-span-2">요청사항: {o.memo}</div>}
                  {o.rejectReason && <div className="text-red-600 md:col-span-2">거절 사유: {o.rejectReason}</div>}
                </div>
                <div className="flex flex-wrap justify-end gap-2 pt-1">
                  {(o.status === 'PENDING' || o.status === 'HELD') && (
                    <>
                      <Button variant="outline" size="sm" disabled={busyId === o.id} onClick={() => handleHoldToggle(o)}>
                        {o.status === 'PENDING' ? '보류' : '보류 해제'}
                      </Button>
                      <Button
                        variant="outline"
                        size="sm"
                        disabled={busyId === o.id}
                        className="text-red-600"
                        onClick={() => { setRejecting(o); setRejectReason('') }}
                      >
                        거절
                      </Button>
                      {renderApprove(o)}
                    </>
                  )}
                  {o.status === 'APPROVED' && o.linkedOrderId && (
                    <Link href={`/orders/${o.linkedOrderId}`} className="text-sm text-green-700 underline">
                      연결된 발주 보기
                    </Link>
                  )}
                </div>
              </CardContent>
            </Card>
          ))}
          <Pagination page={page} totalPages={data.totalPages} totalElements={data.totalElements} size={PAGE_SIZE} onPageChange={setPage} />
        </div>
      )}

      {rejecting && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-black/40">
          <div className="flex min-h-full items-center justify-center p-4">
            <div className="w-full max-w-sm space-y-3 rounded-lg bg-white p-4 shadow-lg">
              <h2 className="text-base font-bold">주문 거절</h2>
              <p className="text-xs text-gray-500">{rejecting.orderNo}</p>
              <textarea
                className="w-full rounded-md border px-3 py-2 text-sm"
                rows={3}
                maxLength={200}
                placeholder="거절 사유"
                value={rejectReason}
                onChange={(e) => setRejectReason(e.target.value)}
              />
              <div className="flex justify-end gap-2">
                <Button variant="outline" onClick={() => setRejecting(null)} disabled={busyId === rejecting.id}>닫기</Button>
                <Button
                  onClick={handleReject}
                  disabled={busyId === rejecting.id || !rejectReason.trim()}
                  className="bg-red-600 hover:bg-red-700"
                >
                  거절하기
                </Button>
              </div>
            </div>
          </div>
        </div>
      )}

      {approving && (
        <VendorShopApproveModal
          order={approving}
          onClose={() => setApproving(null)}
          onApproved={() => {
            setApproving(null)
            setMessage({ kind: 'ok', text: '승인되어 발주가 등록되었습니다.' })
            load()
          }}
        />
      )}
    </div>
  )
}

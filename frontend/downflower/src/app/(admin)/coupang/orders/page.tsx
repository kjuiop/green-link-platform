'use client'

import Link from 'next/link'
import { useCallback, useEffect, useState } from 'react'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Pagination } from '@/components/ui/Pagination'
import { CompanySelect } from '@/components/coupang/CompanySelect'
import { useCoupangScope } from '@/components/coupang/useCoupangScope'
import {
  collectCoupangOrders,
  fetchCoupangCredential,
  fetchCoupangOrders,
  holdCoupangOrder,
  releaseCoupangOrder,
  type CoupangCredential,
  type CoupangOrder,
  type CoupangOrderStatus,
} from '@/lib/api/coupangApi'
import { PageResult } from '@/types/page'

const PAGE_SIZE = 20

const TABS: { value: CoupangOrderStatus; label: string }[] = [
  { value: 'PENDING', label: '승인대기' },
  { value: 'HELD', label: '보류' },
  { value: 'APPROVED', label: '승인됨' },
  { value: 'CANCELED', label: '취소됨' },
]

function won(n: number) {
  return `${n.toLocaleString('ko-KR')}원`
}

function formatDateTime(v: string | null) {
  return v ? v.replace('T', ' ').slice(0, 16) : '-'
}

export default function CoupangOrdersPage() {
  const scope = useCoupangScope()
  const companyId = scope.selectedId

  const [status, setStatus] = useState<CoupangOrderStatus>('PENDING')
  const [page, setPage] = useState(0)
  const [data, setData] = useState<PageResult<CoupangOrder> | null>(null)
  const [credential, setCredential] = useState<CoupangCredential | null>(null)
  const [loading, setLoading] = useState(true)
  const [collecting, setCollecting] = useState(false)
  const [busyId, setBusyId] = useState<number | null>(null)
  const [message, setMessage] = useState<{ kind: 'ok' | 'error'; text: string } | null>(null)

  const load = useCallback(async () => {
    if (scope.isSuperAdmin && !companyId) {
      setLoading(false)
      return
    }
    setLoading(true)
    try {
      const [orders, cred] = await Promise.all([
        fetchCoupangOrders(companyId, status, page, PAGE_SIZE),
        fetchCoupangCredential(companyId).catch(() => null),
      ])
      setData(orders)
      setCredential(cred)
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

  async function handleCollect() {
    setCollecting(true)
    setMessage(null)
    try {
      const result = await collectCoupangOrders(companyId)
      setMessage({ kind: 'ok', text: `쿠팡에서 ${result.fetchedCount}건 조회, 신규 ${result.insertedCount}건을 가져왔습니다.` })
      await load()
    } catch (e) {
      setMessage({ kind: 'error', text: e instanceof Error ? e.message : '불러오지 못했습니다.' })
      await load()
    } finally {
      setCollecting(false)
    }
  }

  async function handleHoldToggle(order: CoupangOrder) {
    setBusyId(order.id)
    setMessage(null)
    try {
      if (order.status === 'PENDING') await holdCoupangOrder(order.id)
      else await releaseCoupangOrder(order.id)
      await load()
    } catch (e) {
      setMessage({ kind: 'error', text: e instanceof Error ? e.message : '처리에 실패했습니다.' })
    } finally {
      setBusyId(null)
    }
  }

  const expiring = credential && !credential.expired && credential.daysUntilExpire !== null && credential.daysUntilExpire <= 14
  const notRegistered = !loading && !credential

  return (
    <div className="mx-auto max-w-4xl space-y-4 p-4 md:p-6">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h1 className="text-lg font-bold">쿠팡 주문 조회</h1>
        <div className="flex items-center gap-2">
          <CompanySelect scope={scope} />
          <Button onClick={handleCollect} disabled={collecting || !credential} className="bg-green-700 hover:bg-green-800">
            {collecting ? '불러오는 중…' : '쿠팡 주문 불러오기'}
          </Button>
        </div>
      </div>

      {notRegistered && (
        <div className="rounded-md border border-amber-200 bg-amber-50 p-3 text-sm text-amber-800">
          쿠팡 연동이 설정되지 않았습니다. <Link href="/coupang/settings" className="underline">쿠팡 연동 설정</Link>에서 키를 등록해 주세요.
        </div>
      )}
      {credential?.expired && (
        <div className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700">
          쿠팡 Open API 키가 만료되어 자동 수집이 중단되었습니다. <Link href="/coupang/settings" className="underline">재등록하기</Link>
        </div>
      )}
      {expiring && credential && (
        <div className="rounded-md border border-amber-200 bg-amber-50 p-3 text-sm text-amber-800">
          쿠팡 Open API 키가 {credential.daysUntilExpire}일 후 만료됩니다. <Link href="/coupang/settings" className="underline">설정 확인</Link>
        </div>
      )}
      {credential?.lastError && (
        <div className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700">
          최근 자동 수집 오류: {credential.lastError}
        </div>
      )}
      {credential && (
        <p className="text-xs text-gray-400">마지막 수집: {formatDateTime(credential.lastCollectedAt)} (약 4분마다 자동 수집)</p>
      )}
      {message && (
        <p className={message.kind === 'ok' ? 'text-sm text-green-700' : 'text-sm text-red-500'}>{message.text}</p>
      )}

      <div className="flex gap-1 border-b">
        {TABS.map((t) => (
          <button
            key={t.value}
            onClick={() => { setStatus(t.value); setPage(0) }}
            className={
              status === t.value
                ? 'border-b-2 border-green-700 px-4 py-2 text-sm font-medium text-green-700'
                : 'px-4 py-2 text-sm text-gray-500 hover:text-gray-900'
            }
          >
            {t.label}
          </button>
        ))}
      </div>

      {loading ? (
        <p className="py-8 text-center text-sm text-gray-400">불러오는 중…</p>
      ) : !data || data.content.length === 0 ? (
        <p className="py-8 text-center text-sm text-gray-400">해당 상태의 쿠팡 주문이 없습니다.</p>
      ) : (
        <div className="space-y-3">
          {data.content.map((o) => (
            <Card key={o.id}>
              <CardContent className="space-y-2 pt-4 text-sm">
                <div className="flex flex-wrap items-center justify-between gap-2">
                  <span className="font-medium">주문번호 {o.orderId}</span>
                  <span className="text-xs text-gray-400">주문 {formatDateTime(o.orderedAt)}</span>
                </div>
                <div className="text-gray-700">
                  {o.items.map((i, idx) => (
                    <div key={idx}>{i.name} × {i.quantity} ({won(i.salesPrice)})</div>
                  ))}
                  <div className="font-medium">합계 {won(o.totalPrice)}</div>
                </div>
                <div className="grid gap-1 text-gray-600 md:grid-cols-2">
                  <div>주문자: {o.ordererName || '-'} {o.ordererPhone || ''}</div>
                  <div>받는 분: {o.receiverName || '-'} {o.receiverPhone || ''}</div>
                  <div className="md:col-span-2">주소: {o.receiverAddress} {o.receiverAddressDetail}</div>
                  {o.deliveryMessage && <div className="md:col-span-2">배송메시지: {o.deliveryMessage}</div>}
                </div>
                <div className="flex justify-end gap-2 pt-1">
                  {(o.status === 'PENDING' || o.status === 'HELD') && (
                    <>
                      <Button
                        variant="outline"
                        size="sm"
                        disabled={busyId === o.id}
                        onClick={() => handleHoldToggle(o)}
                      >
                        {o.status === 'PENDING' ? '보류' : '보류 해제'}
                      </Button>
                      <Link href={`/orders/new?coupangOrderId=${o.id}`}>
                        <Button size="sm" className="bg-green-700 hover:bg-green-800">발주서 작성·승인</Button>
                      </Link>
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
    </div>
  )
}

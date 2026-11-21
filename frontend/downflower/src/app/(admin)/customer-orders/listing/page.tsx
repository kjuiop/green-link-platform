'use client'

import Link from 'next/link'
import { useCallback, useEffect, useState, Suspense } from 'react'
import { useSearchParams } from 'next/navigation'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Pagination } from '@/components/ui/Pagination'
import { CompanySelect } from '@/components/coupang/CompanySelect'
import { useCoupangScope } from '@/components/coupang/useCoupangScope'
import { useShopScope } from '@/components/shop/useShopScope'
import { ShopProductsPanel } from '@/components/shop/ShopProductsPanel'
import {
  fetchCustomerOrders,
  holdCustomerOrder,
  releaseCustomerOrder,
  type CustomerOrder,
  type CustomerOrderStatus,
  type OrderChannel,
} from '@/lib/api/customerOrderApi'
import { collectCoupangOrders, fetchCoupangCredential, type CoupangCredential } from '@/lib/api/coupangApi'
import { PageResult } from '@/types/page'

const PAGE_SIZE = 20

const CHANNEL_TABS: { value: OrderChannel | null; label: string }[] = [
  { value: null, label: '전체' },
  { value: 'COUPANG', label: '쿠팡' },
  { value: 'SHOP', label: '쇼핑몰' },
  { value: 'DIRECT', label: '직접입력' },
]

const STATUS_TABS: { value: CustomerOrderStatus | null; label: string }[] = [
  { value: null, label: '전체' },
  { value: 'PENDING', label: '승인대기' },
  { value: 'HELD', label: '보류' },
  { value: 'APPROVED', label: '승인됨' },
  { value: 'CANCELLED', label: '취소됨' },
]

function won(n: number) {
  return `${n.toLocaleString('ko-KR')}원`
}

function formatDateTime(v: string | null) {
  return v ? v.replace('T', ' ').slice(0, 16) : '-'
}

const CHANNEL_LABEL: Record<OrderChannel, string> = {
  COUPANG: '쿠팡',
  SHOP: '쇼핑몰',
  DIRECT: '직접입력',
}

const STATUS_LABEL: Record<CustomerOrderStatus, string> = {
  PENDING: '승인대기',
  HELD: '보류',
  APPROVED: '승인됨',
  CANCELLED: '취소됨',
}

const STATUS_COLOR: Record<CustomerOrderStatus, string> = {
  PENDING: 'bg-yellow-100 text-yellow-700',
  HELD: 'bg-gray-100 text-gray-600',
  APPROVED: 'bg-green-100 text-green-700',
  CANCELLED: 'bg-red-100 text-red-500',
}

const VALID_CHANNELS: OrderChannel[] = ['COUPANG', 'SHOP', 'DIRECT']

function CustomerOrdersContent() {
  const searchParams = useSearchParams()
  const scope = useCoupangScope()
  const shopScope = useShopScope()
  const companyId = scope.selectedId

  const rawChannel = searchParams.get('channel')
  const [channel, setChannel] = useState<OrderChannel | null>(
    VALID_CHANNELS.includes(rawChannel as OrderChannel) ? (rawChannel as OrderChannel) : null
  )
  const [shopSubTab, setShopSubTab] = useState<'orders' | 'products'>(
    searchParams.get('sub') === 'products' ? 'products' : 'orders'
  )
  const [status, setStatus] = useState<CustomerOrderStatus | null>('PENDING')
  const [page, setPage] = useState(0)
  const [data, setData] = useState<PageResult<CustomerOrder> | null>(null)
  const [credential, setCredential] = useState<CoupangCredential | null>(null)
  const [loading, setLoading] = useState(true)
  const [collecting, setCollecting] = useState(false)
  const [busyId, setBusyId] = useState<number | null>(null)
  const [message, setMessage] = useState<{ kind: 'ok' | 'error'; text: string } | null>(null)

  const load = useCallback(async (targetPage: number) => {
    if (scope.isSuperAdmin && !companyId) {
      setLoading(false)
      return
    }
    setLoading(true)
    try {
      const [orders, cred] = await Promise.all([
        fetchCustomerOrders(companyId, channel, status, targetPage, PAGE_SIZE),
        (channel === null || channel === 'COUPANG')
          ? fetchCoupangCredential(companyId).catch(() => null)
          : Promise.resolve(null),
      ])
      setData(orders)
      setCredential(cred)
    } catch (e) {
      setMessage({ kind: 'error', text: e instanceof Error ? e.message : '불러오지 못했습니다.' })
    } finally {
      setLoading(false)
    }
  }, [scope.isSuperAdmin, companyId, channel, status])

  useEffect(() => {
    if (!scope.ready) return
    setData(null)
    setPage(0)
    load(0)
  }, [scope.ready, load])

  const changePage = (p: number) => {
    setPage(p)
    load(p)
  }

  function changeChannel(c: OrderChannel | null) {
    setChannel(c)
    setShopSubTab('orders')
    setPage(0)
  }

  async function handleCollect() {
    setCollecting(true)
    setMessage(null)
    try {
      const result = await collectCoupangOrders(companyId)
      setMessage({ kind: 'ok', text: `쿠팡에서 ${result.fetchedCount}건 조회, 신규 ${result.insertedCount}건을 가져왔습니다.` })
      await load(page)
    } catch (e) {
      setMessage({ kind: 'error', text: e instanceof Error ? e.message : '수집에 실패했습니다.' })
      await load(page)
    } finally {
      setCollecting(false)
    }
  }

  async function handleHoldToggle(order: CustomerOrder) {
    setBusyId(order.id)
    setMessage(null)
    try {
      if (order.status === 'PENDING') await holdCustomerOrder(order.id)
      else await releaseCustomerOrder(order.id)
      await load(page)
    } catch (e) {
      setMessage({ kind: 'error', text: e instanceof Error ? e.message : '처리에 실패했습니다.' })
    } finally {
      setBusyId(null)
    }
  }

  const showCoupangTools = channel === null || channel === 'COUPANG'
  const expiring = credential && !credential.expired && credential.daysUntilExpire !== null && credential.daysUntilExpire <= 14
  const showShopSubTabs = channel === 'SHOP'
  const showProductsPanel = showShopSubTabs && shopSubTab === 'products'

  if (!scope.ready) return null

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h1 className="text-lg font-bold">주문 관리</h1>
        <div className="flex items-center gap-2">
          <CompanySelect scope={scope} />
          {showCoupangTools && (
            <Button
              onClick={handleCollect}
              disabled={collecting || !credential}
              className="bg-green-700 hover:bg-green-800"
            >
              {collecting ? '불러오는 중…' : '쿠팡 주문 불러오기'}
            </Button>
          )}
        </div>
      </div>

      {scope.isSuperAdmin && !companyId && (
        <p className="rounded-md bg-yellow-50 px-3 py-2 text-sm text-yellow-700">회사를 선택해주세요.</p>
      )}

      {showCoupangTools && credential?.expired && (
        <div className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700">
          쿠팡 Open API 키가 만료되어 자동 수집이 중단되었습니다.{' '}
          <Link href="/customer-orders/integration-settings?market=coupang" className="underline">재등록하기</Link>
        </div>
      )}
      {showCoupangTools && expiring && credential && (
        <div className="rounded-md border border-amber-200 bg-amber-50 p-3 text-sm text-amber-800">
          쿠팡 Open API 키가 {credential.daysUntilExpire}일 후 만료됩니다.{' '}
          <Link href="/customer-orders/integration-settings?market=coupang" className="underline">설정 확인</Link>
        </div>
      )}
      {showCoupangTools && credential?.lastError && (
        <div className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700">
          최근 자동 수집 오류: {credential.lastError}
        </div>
      )}
      {showCoupangTools && credential && (
        <p className="text-xs text-gray-400">마지막 수집: {formatDateTime(credential.lastCollectedAt)} (약 4분마다 자동 수집)</p>
      )}

      {message && (
        <p className={message.kind === 'ok' ? 'text-sm text-green-700' : 'text-sm text-red-500'}>{message.text}</p>
      )}

      {/* 채널 탭 */}
      <div className="flex gap-1 border-b">
        {CHANNEL_TABS.map((t) => (
          <button
            key={t.value ?? 'all'}
            onClick={() => changeChannel(t.value)}
            className={
              channel === t.value
                ? 'border-b-2 border-green-700 px-4 py-2 text-sm font-medium text-green-700'
                : 'px-4 py-2 text-sm text-gray-500 hover:text-gray-900'
            }
          >
            {t.label}
          </button>
        ))}
      </div>

      {/* 쇼핑몰 서브탭 */}
      {showShopSubTabs && (
        <div className="flex gap-1">
          {(['orders', 'products'] as const).map((t) => (
            <button
              key={t}
              onClick={() => setShopSubTab(t)}
              className={
                shopSubTab === t
                  ? 'rounded-full bg-green-700 px-3 py-1 text-xs font-medium text-white'
                  : 'rounded-full bg-gray-100 px-3 py-1 text-xs text-gray-600 hover:bg-gray-200'
              }
            >
              {t === 'orders' ? '주문 목록' : '상품 관리'}
            </button>
          ))}
        </div>
      )}

      {/* 쇼핑몰 상품 패널 */}
      {showProductsPanel ? (
        <ShopProductsPanel scope={shopScope} showHeader={false} />
      ) : (
        <>
          {/* 상태 탭 */}
          <div className="flex gap-1">
            {STATUS_TABS.map((t) => (
              <button
                key={t.value ?? 'all'}
                onClick={() => { setStatus(t.value); setPage(0) }}
                className={
                  status === t.value
                    ? 'rounded-full bg-green-700 px-3 py-1 text-xs font-medium text-white'
                    : 'rounded-full bg-gray-100 px-3 py-1 text-xs text-gray-600 hover:bg-gray-200'
                }
              >
                {t.label}
              </button>
            ))}
          </div>

          {loading ? (
            <p className="py-8 text-center text-sm text-gray-400">불러오는 중…</p>
          ) : !data || data.content.length === 0 ? (
            <p className="py-8 text-center text-sm text-gray-400">해당 조건의 주문이 없습니다.</p>
          ) : (
            <div className="space-y-3">
              {data.content.map((o) => (
                <Card key={o.id}>
                  <CardContent className="space-y-2 pt-4 text-sm">
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <div className="flex items-center gap-2">
                        <span className="rounded-full bg-blue-100 px-2 py-0.5 text-xs text-blue-700">
                          {CHANNEL_LABEL[o.channel]}
                        </span>
                        <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${STATUS_COLOR[o.status]}`}>
                          {STATUS_LABEL[o.status]}
                        </span>
                        {o.coupangMeta && (
                          <span className="font-medium text-gray-700">주문번호 {o.coupangMeta.coupangOrderId}</span>
                        )}
                      </div>
                      <span className="text-xs text-gray-400">주문 {formatDateTime(o.orderedAt)}</span>
                    </div>

                    {o.coupangMeta && o.coupangMeta.items.length > 0 && (
                      <div className="text-gray-700">
                        {o.coupangMeta.items.map((i, idx) => (
                          <div key={idx}>{i.name} × {i.quantity} ({won(i.salesPrice)})</div>
                        ))}
                        <div className="font-medium">합계 {won(o.totalPrice)}</div>
                      </div>
                    )}

                    <div className="grid gap-1 text-gray-600 md:grid-cols-2">
                      <div>주문자: {o.ordererName || '-'} {o.ordererPhone || ''}</div>
                      <div>받는 분: {o.receiverName || '-'} {o.receiverPhone || ''}</div>
                      {(o.receiverAddress || o.receiverAddressDetail) && (
                        <div className="md:col-span-2">주소: {o.receiverAddress} {o.receiverAddressDetail}</div>
                      )}
                      {o.deliveryMessage && (
                        <div className="md:col-span-2">배송메시지: {o.deliveryMessage}</div>
                      )}
                    </div>

                    {(o.status === 'PENDING' || o.status === 'HELD') && (
                      <div className="flex justify-end gap-2 pt-1">
                        <Button
                          variant="outline"
                          size="sm"
                          disabled={busyId === o.id}
                          onClick={() => handleHoldToggle(o)}
                        >
                          {o.status === 'PENDING' ? '보류' : '보류 해제'}
                        </Button>
                        {o.channel === 'COUPANG' && o.coupangMeta && (
                          <Link href={`/orders/new?coupangOrderId=${o.id}`}>
                            <Button size="sm" className="bg-green-700 hover:bg-green-800">발주서 작성·승인</Button>
                          </Link>
                        )}
                      </div>
                    )}
                  </CardContent>
                </Card>
              ))}
              <Pagination
                page={page}
                totalPages={data.totalPages}
                totalElements={data.totalElements}
                size={PAGE_SIZE}
                onPageChange={changePage}
              />
            </div>
          )}
        </>
      )}
    </div>
  )
}

export default function CustomerOrdersPage() {
  return (
    <Suspense>
      <CustomerOrdersContent />
    </Suspense>
  )
}

'use client'

import { useState, useEffect } from 'react'
import { useParams } from 'next/navigation'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { VendorOrderStatusBadge } from '@/components/common/StatusBadge'
import { fetchOrder, updateOrderStatus, fetchReceiptConfirmation, uploadReceiptPhotos, ReceiptConfirmationResult } from '@/lib/api/orderApi'
import { PrintButton } from '@/components/common/PrintButton'
import { ReceiptConfirmButton } from '@/components/common/ReceiptConfirmDialog'
import { Upload } from 'lucide-react'
import { cn } from '@/lib/utils'
import { Order } from '@/types/order'

export default function VendorOrderDetailPage() {
  const { id } = useParams<{ id: string }>()

  const [order, setOrder] = useState<Order | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [acting, setActing] = useState(false)
  const [receiptConfirmation, setReceiptConfirmation] = useState<ReceiptConfirmationResult | null>(null)

  useEffect(() => {
    Promise.all([
      fetchOrder(id),
      fetchReceiptConfirmation(id).catch(() => null),
    ])
      .then(([orderData, receipt]) => {
        setOrder(orderData)
        setReceiptConfirmation(receipt)
      })
      .catch(() => setLoadError('주문 정보를 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }, [id])

  if (loading) return <div className="p-8 text-sm text-gray-400">불러오는 중...</div>
  if (loadError) return <div className="p-8 text-sm text-red-500">{loadError}</div>
  if (!order) return <div className="p-8 text-sm text-gray-400">주문을 찾을 수 없습니다.</div>

  const hasSitePhotos = (receiptConfirmation?.sitePhotoUrls.length ?? 0) > 0
  const hasPhotos = hasSitePhotos || (receiptConfirmation?.storePhotoUrls.length ?? 0) > 0

  async function handlePhotoUpload(e: React.ChangeEvent<HTMLInputElement>, photoType: 'SITE' | 'STORE') {
    const files = Array.from(e.target.files ?? [])
    e.target.value = ''
    if (files.length === 0) return
    try {
      const updated = await uploadReceiptPhotos(id, files, photoType)
      setReceiptConfirmation(updated)
    } catch (err) {
      setActionError(err instanceof Error ? err.message : '사진 업로드에 실패했습니다.')
    }
  }

  async function handleAccept() {
    setActing(true)
    setActionError(null)
    try {
      const updated = await updateOrderStatus(id, 'accept')
      setOrder(updated)
    } catch (e) {
      setActionError(e instanceof Error ? e.message : '수주 확인에 실패했습니다.')
    } finally {
      setActing(false)
    }
  }

  async function handleReject() {
    setActing(true)
    setActionError(null)
    try {
      const updated = await updateOrderStatus(id, 'reject')
      setOrder(updated)
    } catch (e) {
      setActionError(e instanceof Error ? e.message : '수주 거절에 실패했습니다.')
    } finally {
      setActing(false)
    }
  }

  async function handleComplete() {
    setActing(true)
    setActionError(null)
    try {
      const autoPhotos = (receiptConfirmation?.sitePhotoUrls ?? []).slice(0, 2)
      const updated = await updateOrderStatus(id, 'complete', autoPhotos)
      setOrder(updated)
    } catch (e) {
      setActionError(e instanceof Error ? e.message : '배송완료 처리에 실패했습니다.')
    } finally {
      setActing(false)
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h1 className="text-xl font-bold">{order.orderNumber}</h1>
          <div className="mt-1">
            <VendorOrderStatusBadge status={order.status} />
          </div>
        </div>
        <div className="flex shrink-0 flex-wrap gap-2">
          <PrintButton orderId={order.id} />
          {order.status === '발주대기' && (
            <>
              <Button
                variant="outline"
                className="border-red-300 text-red-600 hover:bg-red-50"
                onClick={handleReject}
                disabled={acting}
              >
                수주거절
              </Button>
              <Button className="bg-green-700 hover:bg-green-800" onClick={handleAccept} disabled={acting}>
                주문서 접수
              </Button>
            </>
          )}
          {order.status === '수주완료' && (
            <Button
              className={cn(
                'gap-1.5',
                hasSitePhotos
                  ? 'bg-blue-700 hover:bg-blue-800'
                  : 'cursor-not-allowed bg-gray-300 text-gray-500',
              )}
              disabled={!hasSitePhotos || acting}
              onClick={handleComplete}
              title={!hasSitePhotos ? '현장사진을 1장 이상 업로드해주세요' : undefined}
            >
              {acting ? '처리 중…' : '배송완료 처리'}
            </Button>
          )}
        </div>
      </div>

      {actionError && (
        <div className="rounded-md border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-600">
          {actionError}
        </div>
      )}


      <div className="grid gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-sm">배송정보</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2 text-sm">
            <Row label="상품명" value={order.productName} />
            <Row label="배송예정" value={order.deliveryAt} />
            <Row label="주소" value={`${order.address} ${order.addressDetail}`} />
            <Row label="장소명" value={order.placeName} />
            {order.roomName && <Row label="빈소·호실" value={order.roomName} />}
            {order.eventInfo && <Row label="행사정보" value={order.eventInfo} />}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-sm">리본 문구</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2 text-sm">
            <Row label="리본 왼쪽" value={order.ribbonLeft} />
            <Row label="리본 오른쪽" value={order.ribbonRight} />
          </CardContent>
        </Card>

        {order.vendorMemo && (
          <Card>
            <CardHeader>
              <CardTitle className="text-sm">전달사항</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-sm">{order.vendorMemo}</p>
            </CardContent>
          </Card>
        )}

        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle className="text-sm">인수확인</CardTitle>
          </CardHeader>
          <CardContent>
            <ReceiptConfirmButton
              orderId={order.id}
              orderNumber={order.orderNumber}
              senderName={order.ribbonLeft}
              recipientName={order.recipientName}
            />
          </CardContent>
        </Card>

        {(order.status === '수주완료' || order.status === '배송완료') && (
          <Card className="lg:col-span-2">
            <CardHeader>
              <CardTitle className="text-sm">배송사진</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              {/* 현장사진 */}
              <div>
                <div className="mb-1.5 flex items-center gap-2">
                  <span className="text-xs font-medium text-gray-600">현장사진</span>
                  <span className="rounded-full bg-blue-100 px-1.5 py-0.5 text-[10px] text-blue-700">SMS 발송</span>
                  {(receiptConfirmation?.sitePhotoUrls.length ?? 0) > 0 && (
                    <span className="text-xs text-gray-400">({receiptConfirmation!.sitePhotoUrls.length}장)</span>
                  )}
                </div>
                <div className="flex flex-wrap gap-2">
                  {receiptConfirmation?.sitePhotoUrls.map((url, idx) => (
                    <div key={idx} className="h-24 w-24 overflow-hidden rounded border">
                      <img src={url} alt="현장사진" className="h-full w-full object-cover" />
                    </div>
                  ))}
                  {order.status !== '배송완료' && (
                    <label className="flex h-24 w-24 cursor-pointer flex-col items-center justify-center gap-1 rounded border-2 border-dashed border-blue-200 text-blue-400 hover:border-blue-400 hover:text-blue-600">
                      <Upload className="h-5 w-5" />
                      <span className="text-xs">추가</span>
                      <input type="file" accept="image/*" multiple className="hidden"
                        onChange={(e) => handlePhotoUpload(e, 'SITE')} />
                    </label>
                  )}
                </div>
              </div>

              {/* 매장사진 */}
              <div>
                <div className="mb-1.5 flex items-center gap-2">
                  <span className="text-xs font-medium text-gray-600">매장사진</span>
                  {(receiptConfirmation?.storePhotoUrls.length ?? 0) > 0 && (
                    <span className="text-xs text-gray-400">({receiptConfirmation!.storePhotoUrls.length}장)</span>
                  )}
                </div>
                <div className="flex flex-wrap gap-2">
                  {receiptConfirmation?.storePhotoUrls.map((url, idx) => (
                    <div key={idx} className="h-24 w-24 overflow-hidden rounded border">
                      <img src={url} alt="매장사진" className="h-full w-full object-cover" />
                    </div>
                  ))}
                  {order.status !== '배송완료' && (
                    <label className="flex h-24 w-24 cursor-pointer flex-col items-center justify-center gap-1 rounded border-2 border-dashed border-gray-300 text-gray-400 hover:border-gray-400 hover:text-gray-600">
                      <Upload className="h-5 w-5" />
                      <span className="text-xs">추가</span>
                      <input type="file" accept="image/*" multiple className="hidden"
                        onChange={(e) => handlePhotoUpload(e, 'STORE')} />
                    </label>
                  )}
                </div>
              </div>

              {order.status === '수주완료' && !hasSitePhotos && (
                <p className="text-xs text-amber-600">
                  현장사진을 1장 이상 업로드해야 배송완료 처리가 가능합니다.
                </p>
              )}
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  )
}


function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex gap-2">
      <span className="w-24 flex-shrink-0 text-gray-500">{label}</span>
      <span>{value}</span>
    </div>
  )
}

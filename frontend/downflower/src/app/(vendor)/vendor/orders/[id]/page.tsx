'use client'

import { useState, useEffect, useRef } from 'react'
import { useParams } from 'next/navigation'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { VendorOrderStatusBadge } from '@/components/common/StatusBadge'
import { fetchOrder, updateOrderStatus, uploadPhotos, PhotoResult } from '@/lib/api/orderApi'
import { PrintButton } from '@/components/common/PrintButton'
import { ReceiptConfirmButton } from '@/components/common/ReceiptConfirmDialog'
import { cn } from '@/lib/utils'
import { Upload, X } from 'lucide-react'
import { Order } from '@/types/order'

type PhotoCategory = 'store' | 'site'

interface LocalPhoto {
  id: string
  url: string
  category: PhotoCategory
  file?: File
  uploaded?: boolean
  remoteId?: number
}

export default function VendorOrderDetailPage() {
  const { id } = useParams<{ id: string }>()

  const [order, setOrder] = useState<Order | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [acting, setActing] = useState(false)
  const [localPhotos, setLocalPhotos] = useState<LocalPhoto[]>([])

  const storeInputRef = useRef<HTMLInputElement>(null)
  const siteInputRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    fetchOrder(id)
      .then(setOrder)
      .catch(() => setLoadError('주문 정보를 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }, [id])

  if (loading) return <div className="p-8 text-sm text-gray-400">불러오는 중...</div>
  if (loadError) return <div className="p-8 text-sm text-red-500">{loadError}</div>
  if (!order) return <div className="p-8 text-sm text-gray-400">주문을 찾을 수 없습니다.</div>

  const storePhotos = localPhotos.filter((p) => p.category === 'store')
  const sitePhotos = localPhotos.filter((p) => p.category === 'site')
  const hasSitePhotos = sitePhotos.length > 0

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

  function handleFileChange(e: React.ChangeEvent<HTMLInputElement>, category: PhotoCategory) {
    const files = Array.from(e.target.files ?? [])
    const newPhotos: LocalPhoto[] = files.map((file, i) => ({
      id: `local-${category}-${Date.now()}-${i}`,
      url: URL.createObjectURL(file),
      category,
      file,
      uploaded: false,
    }))
    setLocalPhotos((prev) => [...prev, ...newPhotos])
    e.target.value = ''
  }

  function removePhoto(photoId: string) {
    setLocalPhotos((prev) => prev.filter((p) => p.id !== photoId))
  }

  async function handleComplete() {
    if (!hasSitePhotos) return
    setActing(true)
    setActionError(null)
    try {
      const pendingStore = localPhotos.filter((p) => p.category === 'store' && !p.uploaded && p.file)
      const pendingSite = localPhotos.filter((p) => p.category === 'site' && !p.uploaded && p.file)

      let uploadedResults: PhotoResult[] = []
      if (pendingStore.length > 0) {
        const r = await uploadPhotos(id, pendingStore.map((p) => p.file!), 'STORE')
        uploadedResults = [...uploadedResults, ...r]
      }
      if (pendingSite.length > 0) {
        const r = await uploadPhotos(id, pendingSite.map((p) => p.file!), 'SITE')
        uploadedResults = [...uploadedResults, ...r]
      }

      setLocalPhotos((prev) =>
        prev.map((p) => {
          const matched = uploadedResults.find((r) =>
            (r.photoType === 'STORE' && p.category === 'store') ||
            (r.photoType === 'SITE' && p.category === 'site')
          )
          return matched ? { ...p, uploaded: true, remoteId: matched.id } : p
        })
      )

      const updated = await updateOrderStatus(id, 'complete')
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
              title={!hasSitePhotos ? '현장사진을 먼저 업로드해주세요' : undefined}
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
              <CardTitle className="text-sm">배송사진 업로드</CardTitle>
            </CardHeader>
            <CardContent className="space-y-5">
              <PhotoUploadSection
                label="매장사진"
                description="매장에서 제품을 촬영한 사진"
                photos={storePhotos}
                inputRef={storeInputRef}
                onAdd={() => storeInputRef.current?.click()}
                onRemove={removePhoto}
                onChange={(e) => handleFileChange(e, 'store')}
                disabled={order.status === '배송완료'}
              />

              <hr />

              <PhotoUploadSection
                label="현장사진"
                description="배송 현장에서 촬영한 사진"
                photos={sitePhotos}
                inputRef={siteInputRef}
                onAdd={() => siteInputRef.current?.click()}
                onRemove={removePhoto}
                onChange={(e) => handleFileChange(e, 'site')}
                disabled={order.status === '배송완료'}
              />

              {order.status === '수주완료' && !hasSitePhotos && (
                <p className="text-xs text-amber-600">
                  현장사진을 1장 이상 업로드해야 배송완료 처리 및 문자 발송이 가능합니다.
                </p>
              )}
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  )
}

interface PhotoUploadSectionProps {
  label: string
  description: string
  photos: LocalPhoto[]
  inputRef: React.RefObject<HTMLInputElement | null>
  onAdd: () => void
  onRemove: (id: string) => void
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void
  disabled: boolean
}

function PhotoUploadSection({
  label, description, photos, inputRef, onAdd, onRemove, onChange, disabled,
}: PhotoUploadSectionProps) {
  return (
    <div className="space-y-2">
      <div>
        <p className="text-sm font-medium">{label}</p>
        <p className="text-xs text-gray-400">{description}</p>
      </div>
      <div className="flex flex-wrap gap-2">
        {photos.map((photo) => (
          <div key={photo.id} className="relative h-24 w-24">
            <img
              src={photo.url}
              alt={label}
              className="h-full w-full rounded border object-cover"
            />
            {!disabled && (
              <button
                type="button"
                onClick={() => onRemove(photo.id)}
                className="absolute -right-1.5 -top-1.5 flex h-5 w-5 items-center justify-center rounded-full bg-red-500 text-white hover:bg-red-600"
              >
                <X className="h-3 w-3" />
              </button>
            )}
          </div>
        ))}
        {!disabled && (
          <button
            type="button"
            onClick={onAdd}
            className="flex h-24 w-24 flex-col items-center justify-center gap-1 rounded border-2 border-dashed border-gray-300 text-gray-400 hover:border-gray-400 hover:text-gray-600"
          >
            <Upload className="h-5 w-5" />
            <span className="text-xs">추가</span>
          </button>
        )}
        <input
          ref={inputRef}
          type="file"
          accept="image/*"
          multiple
          className="hidden"
          onChange={onChange}
        />
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

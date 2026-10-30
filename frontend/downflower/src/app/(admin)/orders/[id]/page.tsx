'use client'

import { Suspense } from 'react'
import Link from 'next/link'
import { useParams, useRouter, useSearchParams } from 'next/navigation'
import { ArrowLeft } from 'lucide-react'
import { OrderStatusBadge, DeliveryTimeTypeBadge } from '@/components/common/StatusBadge'
import { useOrderDetail, LoadedOrderDetail } from '@/components/common/useOrderDetail'
import {
  OrderActionButtons, SaveErrorBanner,
  BasicInfoCard, DeliveryInfoCard, RibbonCard, ContactCard, MemoCard,
  ReferenceImageCard, DeliveryPhotosCard, ReceiptConfirmationCard,
  SmsHistoryCard, HistoryCard, CompleteDialog, DeleteDialog,
} from '@/components/common/OrderDetailSections'

const PAGE_OVERLAY = 'fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4'

function OrderDetailContent() {
  const { id } = useParams<{ id: string }>()
  const router = useRouter()
  const fromReceived = useSearchParams().get('from') === 'received'
  const listPath = fromReceived ? '/orders/received' : '/orders'
  const listLabel = fromReceived ? '수주 목록' : '발주 목록'

  const detail = useOrderDetail(id, { onClose: () => router.push(listPath) })
  const { order, draft, loading, loadError } = detail
  const loaded: LoadedOrderDetail | null = order && draft ? { ...detail, order, draft } : null

  return (
    <div className="mx-auto w-full max-w-6xl space-y-4">
      <Link href={listPath} className="inline-flex items-center gap-1 text-sm text-gray-500 hover:text-gray-800">
        <ArrowLeft className="h-4 w-4" />{listLabel}
      </Link>

      {loading ? (
        <div className="flex h-60 items-center justify-center text-sm text-gray-400">불러오는 중...</div>
      ) : loadError ? (
        <div className="flex h-60 items-center justify-center text-sm text-red-500">{loadError}</div>
      ) : !loaded ? (
        <div className="flex h-60 items-center justify-center text-sm text-gray-400">주문을 찾을 수 없습니다.</div>
      ) : (
        <>
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div className="flex flex-wrap items-center gap-2">
              <h1 className="text-xl font-bold">
                주문서 <span className="font-mono">{loaded.order.orderNumber}</span>
              </h1>
              <OrderStatusBadge status={loaded.order.status} />
              <DeliveryTimeTypeBadge type={loaded.order.deliveryTimeType} custom={loaded.order.deliveryTimeCustom} />
            </div>
            <div className="flex flex-wrap items-center gap-2">
              <OrderActionButtons d={loaded} />
            </div>
          </div>

          <SaveErrorBanner d={loaded} />

          <div className="grid items-start gap-4 lg:grid-cols-3">
            <div className="space-y-4 lg:col-span-2">
              <BasicInfoCard d={loaded} />
              <DeliveryInfoCard d={loaded} />
              <RibbonCard d={loaded} />
              <ContactCard d={loaded} />
              <MemoCard d={loaded} />
            </div>
            <div className="space-y-4">
              <ReferenceImageCard d={loaded} />
              <DeliveryPhotosCard d={loaded} />
              <ReceiptConfirmationCard d={loaded} />
              <SmsHistoryCard d={loaded} />
              <HistoryCard d={loaded} />
            </div>
          </div>

          <CompleteDialog d={loaded} overlayClassName={PAGE_OVERLAY} />
          <DeleteDialog d={loaded} overlayClassName={PAGE_OVERLAY} />
        </>
      )}
    </div>
  )
}

export default function OrderDetailPage() {
  return (
    <Suspense>
      <OrderDetailContent />
    </Suspense>
  )
}

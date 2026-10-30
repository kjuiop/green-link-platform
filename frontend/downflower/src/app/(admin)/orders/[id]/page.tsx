'use client'

import Link from 'next/link'
import { useParams, useRouter } from 'next/navigation'
import { ArrowLeft } from 'lucide-react'
import { OrderStatusBadge, DeliveryTimeTypeBadge } from '@/components/common/StatusBadge'
import { useOrderDetail } from '@/components/common/useOrderDetail'
import {
  OrderActionButtons, SaveErrorBanner,
  BasicInfoCard, DeliveryInfoCard, RibbonCard, ContactCard, MemoCard,
  ReferenceImageCard, DeliveryPhotosCard, ReceiptConfirmationCard,
  SmsHistoryCard, HistoryCard, CompleteDialog, DeleteDialog,
} from '@/components/common/OrderDetailSections'

const PAGE_OVERLAY = 'fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4'

export default function OrderDetailPage() {
  const { id } = useParams<{ id: string }>()
  const router = useRouter()
  const detail = useOrderDetail(id, { onClose: () => router.push('/orders') })
  const { order, draft, loading, loadError } = detail

  return (
    <div className="mx-auto w-full max-w-6xl space-y-4">
      <Link href="/orders" className="inline-flex items-center gap-1 text-sm text-gray-500 hover:text-gray-800">
        <ArrowLeft className="h-4 w-4" />발주 목록
      </Link>

      {loading ? (
        <div className="flex h-60 items-center justify-center text-sm text-gray-400">불러오는 중...</div>
      ) : loadError ? (
        <div className="flex h-60 items-center justify-center text-sm text-red-500">{loadError}</div>
      ) : !order || !draft ? (
        <div className="flex h-60 items-center justify-center text-sm text-gray-400">주문을 찾을 수 없습니다.</div>
      ) : (
        (() => {
          const d = { ...detail, order, draft }
          return (
            <>
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div className="flex flex-wrap items-center gap-2">
                  <h1 className="text-xl font-bold">
                    주문서 <span className="font-mono">{order.orderNumber}</span>
                  </h1>
                  <OrderStatusBadge status={order.status} />
                  <DeliveryTimeTypeBadge type={order.deliveryTimeType} custom={order.deliveryTimeCustom} />
                </div>
                <div className="flex flex-wrap items-center gap-2">
                  <OrderActionButtons d={d} />
                </div>
              </div>

              <SaveErrorBanner d={d} />

              <div className="grid items-start gap-4 lg:grid-cols-3">
                <div className="space-y-4 lg:col-span-2">
                  <BasicInfoCard d={d} />
                  <DeliveryInfoCard d={d} />
                  <RibbonCard d={d} />
                  <ContactCard d={d} />
                  <MemoCard d={d} />
                </div>
                <div className="space-y-4">
                  <ReferenceImageCard d={d} />
                  <DeliveryPhotosCard d={d} />
                  <ReceiptConfirmationCard d={d} />
                  <SmsHistoryCard d={d} />
                  <HistoryCard d={d} />
                </div>
              </div>

              <CompleteDialog d={d} overlayClassName={PAGE_OVERLAY} />
              <DeleteDialog d={d} overlayClassName={PAGE_OVERLAY} />
            </>
          )
        })()
      )}
    </div>
  )
}

'use client'

import { useParams } from 'next/navigation'
import { X } from 'lucide-react'
import { OrderStatusBadge, DeliveryTimeTypeBadge } from '@/components/common/StatusBadge'
import { useOrderDetail, LoadedOrderDetail } from '@/components/common/useOrderDetail'
import { notifyOrderUpdated } from '@/components/common/useOrderSync'
import {
  OrderActionButtons, SaveErrorBanner,
  BasicInfoCard, DeliveryInfoCard, RibbonCard, ContactCard, MemoCard,
  ReferenceImageCard, DeliveryPhotosCard, ReceiptConfirmationCard,
  SmsHistoryCard, HistoryCard, CompleteDialog, DeleteDialog,
} from '@/components/common/OrderDetailSections'

const WINDOW_OVERLAY = 'fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4'

export default function OrderPopupWindowPage() {
  const { id } = useParams<{ id: string }>()
  const detail = useOrderDetail(id, { onUpdated: () => notifyOrderUpdated(id), onClose: () => window.close() })
  const { order, draft, loading, loadError } = detail
  const loaded: LoadedOrderDetail | null = order && draft ? { ...detail, order, draft } : null

  return (
    <div className="flex h-screen flex-col bg-gray-50">
      <div className="flex shrink-0 items-center justify-between bg-[#1e3a5f] px-4 py-2.5 text-white">
        <div className="flex items-center gap-2">
          {loading ? (
            <span className="text-sm opacity-70">불러오는 중...</span>
          ) : order ? (
            <>
              <span className="font-mono text-sm font-semibold">{order.orderNumber}</span>
              <OrderStatusBadge status={order.status} />
              <DeliveryTimeTypeBadge type={order.deliveryTimeType} custom={order.deliveryTimeCustom} />
            </>
          ) : (
            <span className="text-sm opacity-70">주문서</span>
          )}
        </div>
        <button
          onClick={() => window.close()}
          className="rounded p-1 opacity-70 hover:bg-white/20 hover:opacity-100"
        >
          <X className="h-4 w-4" />
        </button>
      </div>

      <div className="flex-1 overflow-y-auto">
        {loading ? (
          <div className="flex h-40 items-center justify-center text-sm text-gray-400">불러오는 중...</div>
        ) : loadError ? (
          <div className="flex h-40 items-center justify-center text-sm text-red-500">{loadError}</div>
        ) : !loaded ? (
          <div className="flex h-40 items-center justify-center text-sm text-gray-400">주문을 찾을 수 없습니다.</div>
        ) : (
          <div className="space-y-3 p-4">
            <div className="flex flex-wrap items-center gap-2">
              <OrderActionButtons d={loaded} />
            </div>

            <SaveErrorBanner d={loaded} />

            <div className="grid gap-3 sm:grid-cols-2">
              <BasicInfoCard d={loaded} />
              <DeliveryInfoCard d={loaded} />
              <RibbonCard d={loaded} />
              <ContactCard d={loaded} />
              <MemoCard d={loaded} className="sm:col-span-2" />
              <ReferenceImageCard d={loaded} />
              <DeliveryPhotosCard d={loaded} />
              <ReceiptConfirmationCard d={loaded} className="sm:col-span-2" />
              <SmsHistoryCard d={loaded} className="sm:col-span-2" />
              <HistoryCard d={loaded} className="sm:col-span-2" />
            </div>
          </div>
        )}
      </div>

      {loaded && (
        <>
          <CompleteDialog d={loaded} overlayClassName={WINDOW_OVERLAY} />
          <DeleteDialog d={loaded} overlayClassName={WINDOW_OVERLAY} />
        </>
      )}
    </div>
  )
}

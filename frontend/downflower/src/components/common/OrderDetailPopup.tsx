'use client'

import { useState, useEffect, useRef, useCallback } from 'react'
import { OrderStatusBadge, DeliveryTimeTypeBadge } from '@/components/common/StatusBadge'
import { X, GripHorizontal } from 'lucide-react'
import { useOrderDetail, LoadedOrderDetail } from '@/components/common/useOrderDetail'
import {
  OrderActionButtons, SaveErrorBanner,
  BasicInfoCard, DeliveryInfoCard, RibbonCard, ContactCard, MemoCard,
  ReferenceImageCard, DeliveryPhotosCard, ReceiptConfirmationCard,
  SmsHistoryCard, HistoryCard, CompleteDialog, DeleteDialog,
} from '@/components/common/OrderDetailSections'

interface Props {
  orderId: string
  zIndex: number
  initialOffset?: number
  onClose: () => void
  onFocus: () => void
  onUpdated?: () => void
}

const POPUP_OVERLAY = 'absolute inset-0 z-10 flex items-center justify-center rounded-lg bg-black/50'

export function OrderDetailPopup({ orderId, zIndex, initialOffset = 0, onClose, onFocus, onUpdated }: Props) {
  const [pos, setPos] = useState(() => {
    if (typeof window === 'undefined') return { x: 60 + initialOffset * 32, y: 40 + initialOffset * 32 }
    return {
      x: Math.max(0, Math.round((window.innerWidth - 860) / 2) + initialOffset * 32),
      y: Math.max(0, Math.round(window.innerHeight * 0.05) + initialOffset * 32),
    }
  })
  const posRef = useRef(pos)
  posRef.current = pos
  const dragRef = useRef<{ startX: number; startY: number; origX: number; origY: number } | null>(null)

  const detail = useOrderDetail(orderId, { onUpdated, onClose })
  const { order, draft, loading, loadError } = detail

  useEffect(() => {
    function clampOnResize() {
      setPos((prev) => ({ x: prev.x, y: Math.max(0, prev.y) }))
    }
    window.addEventListener('resize', clampOnResize)
    return () => window.removeEventListener('resize', clampOnResize)
  }, [])

  const handleDragStart = useCallback((e: React.MouseEvent) => {
    e.preventDefault()
    dragRef.current = { startX: e.clientX, startY: e.clientY, origX: posRef.current.x, origY: posRef.current.y }

    const onMove = (ev: MouseEvent) => {
      if (!dragRef.current) return
      setPos({
        x: dragRef.current.origX + ev.clientX - dragRef.current.startX,
        y: Math.max(0, dragRef.current.origY + ev.clientY - dragRef.current.startY),
      })
    }
    const onUp = () => {
      dragRef.current = null
      document.removeEventListener('mousemove', onMove)
      document.removeEventListener('mouseup', onUp)
    }
    document.addEventListener('mousemove', onMove)
    document.addEventListener('mouseup', onUp)
  }, [])

  if (!order && !loading && !loadError) return null

  const loaded: LoadedOrderDetail | null = order && draft ? { ...detail, order, draft } : null

  return (
    <div
      className="fixed shadow-2xl"
      style={{ left: pos.x, top: pos.y, zIndex, width: 860, maxWidth: 'calc(100vw - 32px)' }}
      onMouseDown={onFocus}
    >
      {/* 타이틀바 */}
      <div
        className="flex cursor-grab select-none items-center justify-between rounded-t-lg bg-[#1e3a5f] px-4 py-2.5 text-white active:cursor-grabbing"
        onMouseDown={handleDragStart}
      >
        <div className="flex items-center gap-2">
          <GripHorizontal className="h-4 w-4 opacity-60" />
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
          onMouseDown={(e) => e.stopPropagation()}
          onClick={onClose}
          className="rounded p-1 opacity-70 hover:bg-white/20 hover:opacity-100"
        >
          <X className="h-4 w-4" />
        </button>
      </div>

      {/* 본문 (스크롤) */}
      <div className="max-h-[calc(100vh-120px)] overflow-y-auto rounded-b-lg bg-gray-50">
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
          <CompleteDialog d={loaded} overlayClassName={POPUP_OVERLAY} />
          <DeleteDialog d={loaded} overlayClassName={POPUP_OVERLAY} />
        </>
      )}
    </div>
  )
}

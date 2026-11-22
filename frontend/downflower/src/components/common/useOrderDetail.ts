'use client'

import { useState, useEffect } from 'react'
import {
  fetchOrder, updateOrder, updateOrderStatus, deleteOrder, requestRefund, approveRefund, rejectRefund,
  fetchOrderHistory, fetchReceiptConfirmation,
  fetchSmsMessages, fetchAlimtalkMessages, resendMms,
  OrderHistoryItem, ReceiptConfirmationResult, SmsMessageResult,
} from '@/lib/api/orderApi'
import { Order } from '@/types/order'

interface Options {
  onUpdated?: () => void
  onClose: () => void
}

export function useOrderDetail(orderId: string, { onUpdated, onClose }: Options) {
  const [order, setOrder] = useState<Order | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [isEditing, setIsEditing] = useState(false)
  const [draft, setDraft] = useState<Order | null>(null)
  const [refundDialog, setRefundDialog] = useState<'request' | 'approve' | 'reject' | null>(null)
  const [refundReason, setRefundReason] = useState('')
  const [refundError, setRefundError] = useState<string | null>(null)
  const [confirmDelete, setConfirmDelete] = useState(false)
  const [deleteError, setDeleteError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)
  const isSuperAdmin = typeof window !== 'undefined' && localStorage.getItem('role') === 'SUPER_ADMIN'
  const role = typeof window === 'undefined' ? null : localStorage.getItem('role')
  const canEdit = role !== 'VENDOR'
  const canRequestRefund = role === 'SUPER_ADMIN' || role === 'ORDERING'
  const canDecideRefund = role === 'SUPER_ADMIN' || role === 'VENDOR'
  const [saveError, setSaveError] = useState<string | null>(null)
  const [receiptConfirmation, setReceiptConfirmation] = useState<ReceiptConfirmationResult | null>(null)
  const [history, setHistory] = useState<OrderHistoryItem[]>([])
  const [smsMessages, setSmsMessages] = useState<SmsMessageResult[]>([])
  const [alimtalkMessages, setAlimtalkMessages] = useState<SmsMessageResult[]>([])
  const [resending, setResending] = useState(false)
  const [completeDialogOpen, setCompleteDialogOpen] = useState(false)
  const [selectedPhotoUrls, setSelectedPhotoUrls] = useState<string[]>([])
  const [selectedProductId, setSelectedProductId] = useState('')

  useEffect(() => {
    Promise.all([
      fetchOrder(orderId),
      fetchReceiptConfirmation(orderId).catch(() => null),
      fetchOrderHistory(orderId).catch(() => []),
      fetchSmsMessages(orderId).catch(() => []),
      fetchAlimtalkMessages(orderId).catch(() => []),
    ]).then(([orderData, receipt, hist, sms, alimtalk]) => {
      setOrder(orderData)
      setDraft(orderData)
      setReceiptConfirmation(receipt)
      setHistory(hist)
      setSmsMessages(sms)
      setAlimtalkMessages(alimtalk)
    }).catch(() => setLoadError('발주 정보를 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }, [orderId])

  const hasSitePhotos = (receiptConfirmation?.sitePhotoUrls.length ?? 0) > 0
  const hasPhotos = hasSitePhotos || (receiptConfirmation?.storePhotoUrls.length ?? 0) > 0
  const hasSmsFailure = smsMessages.some((m) => m.status === 'FAIL')
  const hasAlimtalkFailure = alimtalkMessages.some((m) => m.status === 'FAIL')

  function startEdit() {
    setDraft({ ...order! })
    const pid = order!.productId
    setSelectedProductId(pid ? String(pid) : '')
    setIsEditing(true)
    setSaveError(null)
  }

  function cancelEdit() {
    setIsEditing(false)
    setSaveError(null)
  }

  async function saveEdit() {
    if (!draft) return
    if (!draft.vendorId) {
      setSaveError('수주업체를 선택해 주세요.')
      return
    }
    if (draft.eventType === '결혼식' && draft.messageSchedule !== 'scheduled') {
      setSaveError('결혼식 주문은 사진 예약발송 시각을 반드시 설정해주세요.')
      return
    }
    setSaving(true)
    setSaveError(null)
    try {
      const updated = await updateOrder(orderId, {
        vendorId: Number(draft.vendorId),
        ...(selectedProductId ? { productId: Number(selectedProductId) } : {}),
        productName: draft.productName,
        quantity: draft.quantity,
        amount: draft.amount,
        vendorAmount: draft.vendorAmount,
        extraCost: draft.extraCost,
        deliveryDate: draft.deliveryAt.slice(0, 10),
        deliveryTimeType: draft.deliveryTimeType ?? '',
        deliveryTimeHour: draft.deliveryTimeHour ?? '',
        deliveryTimeMinute: draft.deliveryTimeMinute ?? '',
        deliveryTimeCustom: draft.deliveryTimeCustom ?? '',
        address: draft.address,
        addressDetail: draft.addressDetail ?? '',
        eventType: draft.eventType ?? '기타',
        placeName: draft.placeName ?? '',
        roomName: draft.roomName ?? '',
        eventInfo: draft.eventInfo ?? '',
        ribbonLeft: draft.ribbonLeft ?? '',
        ribbonRight: draft.ribbonRight ?? '',
        ribbonExtras: draft.ribbonExtras ? JSON.stringify(draft.ribbonExtras) : undefined,
        ordererName: draft.ordererName,
        ordererPhone: draft.ordererPhone,
        recipientName: draft.recipientName ?? '',
        recipientPhone: draft.recipientPhone ?? '',
        sendPhotoToOrderer: draft.sendPhotoToOrderer,
        sendPhotoToRecipient: draft.sendPhotoToRecipient,
        messageSchedule: draft.messageSchedule ?? '',
        scheduledMessageHour: draft.scheduledMessageHour ?? '',
        scheduledMessageMinute: draft.scheduledMessageMinute ?? '',
        adminMemo: draft.adminMemo ?? '',
        vendorMemo: draft.vendorMemo ?? '',
      })
      setOrder(updated)
      setDraft(updated)
      setIsEditing(false)
      onUpdated?.()
    } catch (e) {
      setSaveError(e instanceof Error ? e.message : '수정에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  function openCompleteDialog() {
    setSelectedPhotoUrls([])
    setCompleteDialogOpen(true)
  }

  function togglePhotoSelection(url: string) {
    setSelectedPhotoUrls((prev) =>
      prev.includes(url) ? prev.filter((u) => u !== url) : [...prev, url]
    )
  }

  async function handleComplete() {
    setCompleteDialogOpen(false)
    setSaving(true)
    setSaveError(null)
    try {
      const updated = await updateOrderStatus(orderId, 'complete', selectedPhotoUrls)
      setOrder(updated)
      setDraft(updated)
      const [sms, alimtalk] = await Promise.all([
        fetchSmsMessages(orderId).catch(() => []),
        fetchAlimtalkMessages(orderId).catch(() => []),
      ])
      setSmsMessages(sms)
      setAlimtalkMessages(alimtalk)
      onUpdated?.()
    } catch (e) {
      setSaveError(e instanceof Error ? e.message : '배송완료 처리에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  async function handleAccept() {
    setSaving(true)
    setSaveError(null)
    try {
      const updated = await updateOrderStatus(orderId, 'accept')
      setOrder(updated)
      setDraft(updated)
      onUpdated?.()
    } catch (e) {
      setSaveError(e instanceof Error ? e.message : '수주 승인에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  async function handleReject() {
    setSaving(true)
    setSaveError(null)
    try {
      const updated = await updateOrderStatus(orderId, 'reject')
      setOrder(updated)
      setDraft(updated)
      onUpdated?.()
    } catch (e) {
      setSaveError(e instanceof Error ? e.message : '수주 거절에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  async function handleResend() {
    setResending(true)
    setSaveError(null)
    try {
      await resendMms(orderId)
      const [sms, alimtalk] = await Promise.all([
        fetchSmsMessages(orderId).catch(() => []),
        fetchAlimtalkMessages(orderId).catch(() => []),
      ])
      setSmsMessages(sms)
      setAlimtalkMessages(alimtalk)
    } catch (e) {
      setSaveError(e instanceof Error ? e.message : '재발송에 실패했습니다.')
    } finally {
      setResending(false)
    }
  }

  function openRefundDialog(kind: 'request' | 'approve' | 'reject') {
    setRefundReason('')
    setRefundError(null)
    setRefundDialog(kind)
  }

  function closeRefundDialog() {
    setRefundDialog(null)
    setRefundError(null)
  }

  async function submitRefund() {
    if (!refundDialog) return
    const reason = refundReason.trim()
    if (refundDialog !== 'approve' && !reason) {
      setRefundError('사유를 입력해 주세요.')
      return
    }
    setSaving(true)
    setRefundError(null)
    try {
      const updated = refundDialog === 'request' ? await requestRefund(orderId, reason)
        : refundDialog === 'approve' ? await approveRefund(orderId)
        : await rejectRefund(orderId, reason)
      setOrder(updated)
      setDraft(updated)
      setHistory(await fetchOrderHistory(orderId).catch(() => []))
      setRefundDialog(null)
      onUpdated?.()
    } catch (e) {
      setRefundError(e instanceof Error ? e.message : '처리에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  function openDeleteConfirm() {
    setConfirmDelete(true)
    setDeleteError(null)
  }

  function closeDeleteConfirm() {
    setConfirmDelete(false)
    setDeleteError(null)
  }

  async function handleDelete() {
    setSaving(true)
    setDeleteError(null)
    try {
      await deleteOrder(orderId)
      onUpdated?.()
      onClose()
    } catch (e) {
      setDeleteError(e instanceof Error ? e.message : '삭제에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  async function refreshAfterDeliveryComplete() {
    try {
      const [updated, receipt, sms, alimtalk] = await Promise.all([
        fetchOrder(orderId),
        fetchReceiptConfirmation(orderId).catch(() => null),
        fetchSmsMessages(orderId).catch(() => []),
        fetchAlimtalkMessages(orderId).catch(() => []),
      ])
      setOrder(updated)
      setDraft(updated)
      setReceiptConfirmation(receipt)
      setSmsMessages(sms)
      setAlimtalkMessages(alimtalk)
      onUpdated?.()
    } catch {}
  }

  function set<K extends keyof Order>(key: K, value: Order[K]) {
    setDraft((prev) => prev ? { ...prev, [key]: value } : prev)
  }

  return {
    orderId,
    order, draft, setDraft, loading, loadError,
    isEditing, saving, saveError, isSuperAdmin,
    canEdit: canEdit && order?.status !== '환불요청' && order?.status !== '환불완료',
    canRequestRefund, canDecideRefund,
    receiptConfirmation, history, smsMessages, alimtalkMessages, resending,
    selectedProductId, setSelectedProductId,
    completeDialogOpen, setCompleteDialogOpen, selectedPhotoUrls,
    refundDialog, refundReason, setRefundReason, refundError,
    confirmDelete, deleteError,
    hasSitePhotos, hasPhotos, hasSmsFailure, hasAlimtalkFailure,
    startEdit, cancelEdit, saveEdit,
    openCompleteDialog, togglePhotoSelection, handleComplete,
    handleAccept, handleReject, handleResend,
    openRefundDialog, closeRefundDialog, submitRefund,
    openDeleteConfirm, closeDeleteConfirm, handleDelete, refreshAfterDeliveryComplete,
    set,
  }
}

export type OrderDetail = ReturnType<typeof useOrderDetail>
export type LoadedOrderDetail = Omit<OrderDetail, 'order' | 'draft'> & { order: Order; draft: Order }

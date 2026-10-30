'use client'

import { useState, useEffect } from 'react'
import {
  fetchOrder, updateOrder, updateOrderStatus, deleteOrder,
  fetchOrderHistory, fetchReceiptConfirmation,
  fetchSmsMessages, resendMms,
  OrderHistoryItem, ReceiptConfirmationResult, SmsMessageResult,
} from '@/lib/api/orderApi'
import { Order } from '@/types/order'
import { fetchAllProducts } from '@/lib/api/productApi'
import { Product } from '@/types/product'
import { fetchVendors } from '@/lib/api/vendorApi'
import { Vendor } from '@/types/vendor'

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
  const [confirmDelete, setConfirmDelete] = useState(false)
  const [deleteError, setDeleteError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)
  const isSuperAdmin = typeof window !== 'undefined' && localStorage.getItem('role') === 'SUPER_ADMIN'
  const canEdit = typeof window === 'undefined' || localStorage.getItem('role') !== 'VENDOR'
  const [saveError, setSaveError] = useState<string | null>(null)
  const [receiptConfirmation, setReceiptConfirmation] = useState<ReceiptConfirmationResult | null>(null)
  const [history, setHistory] = useState<OrderHistoryItem[]>([])
  const [smsMessages, setSmsMessages] = useState<SmsMessageResult[]>([])
  const [resending, setResending] = useState(false)
  const [completeDialogOpen, setCompleteDialogOpen] = useState(false)
  const [selectedPhotoUrls, setSelectedPhotoUrls] = useState<string[]>([])
  const [products, setProducts] = useState<Product[]>([])
  const [selectedProductId, setSelectedProductId] = useState('')
  const [vendors, setVendors] = useState<Vendor[]>([])

  useEffect(() => {
    fetchAllProducts().then(setProducts).catch(() => setSelectedProductId('__direct__'))
    fetchVendors('ACTIVE').then(setVendors).catch(() => {})
    Promise.all([
      fetchOrder(orderId),
      fetchReceiptConfirmation(orderId).catch(() => null),
      fetchOrderHistory(orderId).catch(() => []),
      fetchSmsMessages(orderId).catch(() => []),
    ]).then(([orderData, receipt, hist, sms]) => {
      setOrder(orderData)
      setDraft(orderData)
      setReceiptConfirmation(receipt)
      setHistory(hist)
      setSmsMessages(sms)
    }).catch(() => setLoadError('발주 정보를 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }, [orderId])

  const hasSitePhotos = (receiptConfirmation?.sitePhotoUrls.length ?? 0) > 0
  const hasPhotos = hasSitePhotos || (receiptConfirmation?.storePhotoUrls.length ?? 0) > 0
  const hasSmsFailure = smsMessages.some((m) => m.status === 'FAIL')

  function startEdit() {
    setDraft({ ...order! })
    const pid = order!.productId
    setSelectedProductId(pid ? String(pid) : order!.productName ? '__direct__' : '')
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
    setSaving(true)
    setSaveError(null)
    try {
      const updated = await updateOrder(orderId, {
        vendorId: Number(draft.vendorId),
        ...(selectedProductId && selectedProductId !== '__direct__' ? { productId: Number(selectedProductId) } : {}),
        productName: draft.productName,
        quantity: draft.quantity,
        amount: draft.amount,
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
      const sms = await fetchSmsMessages(orderId).catch(() => [])
      setSmsMessages(sms)
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
      const sms = await fetchSmsMessages(orderId).catch(() => [])
      setSmsMessages(sms)
    } catch (e) {
      setSaveError(e instanceof Error ? e.message : '재발송에 실패했습니다.')
    } finally {
      setResending(false)
    }
  }

  async function handleDelete() {
    try {
      await deleteOrder(orderId)
      onUpdated?.()
      onClose()
    } catch {
      setDeleteError('삭제에 실패했습니다. 다시 시도해 주세요.')
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

  async function refreshAfterDeliveryComplete() {
    try {
      const [updated, receipt, sms] = await Promise.all([
        fetchOrder(orderId),
        fetchReceiptConfirmation(orderId).catch(() => null),
        fetchSmsMessages(orderId).catch(() => []),
      ])
      setOrder(updated)
      setDraft(updated)
      setReceiptConfirmation(receipt)
      setSmsMessages(sms)
      onUpdated?.()
    } catch {}
  }

  function set<K extends keyof Order>(key: K, value: Order[K]) {
    setDraft((prev) => prev ? { ...prev, [key]: value } : prev)
  }

  return {
    orderId,
    order, draft, setDraft, loading, loadError,
    isEditing, saving, saveError, isSuperAdmin, canEdit,
    receiptConfirmation, history, smsMessages, resending,
    products, vendors, selectedProductId, setSelectedProductId,
    completeDialogOpen, setCompleteDialogOpen, selectedPhotoUrls,
    confirmDelete, deleteError,
    hasSitePhotos, hasPhotos, hasSmsFailure,
    startEdit, cancelEdit, saveEdit,
    openCompleteDialog, togglePhotoSelection, handleComplete,
    handleAccept, handleReject, handleResend, handleDelete,
    openDeleteConfirm, closeDeleteConfirm, refreshAfterDeliveryComplete,
    set,
  }
}

export type OrderDetail = ReturnType<typeof useOrderDetail>
export type LoadedOrderDetail = Omit<OrderDetail, 'order' | 'draft'> & { order: Order; draft: Order }

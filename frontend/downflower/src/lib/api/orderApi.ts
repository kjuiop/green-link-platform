import { Order, OrderStatus } from '@/types/order'
import { PageResult } from '@/types/page'

const STATUS_MAP: Record<string, OrderStatus> = {
  PENDING: '발주대기',
  ACCEPTED: '수주완료',
  DELIVERED: '배송완료',
  REJECTED: '수주거절',
  CANCELLED: '주문취소',
}

type BackendOrder = {
  id: number
  orderNumber: string
  referencePhotoCount?: number
  vendorId: number
  vendorName: string | null
  vendorPhone: string | null
  orderingCompanyId: number | null
  orderingCompanyName: string | null
  orderingCompanyPhone: string | null
  status: string
  productId: number | null
  productName: string
  quantity: number
  amount: number
  extraCost: number | null
  deliveryDate: string
  deliveryTimeType: string | null
  deliveryTimeHour: string | null
  deliveryTimeMinute: string | null
  deliveryTimeCustom: string | null
  address: string
  addressDetail: string | null
  eventType: string | null
  placeName: string | null
  roomName: string | null
  eventInfo: string | null
  ribbonLeft: string | null
  ribbonRight: string | null
  ordererName: string
  ordererPhone: string
  recipientName: string | null
  recipientPhone: string | null
  sendPhotoToOrderer: boolean
  sendPhotoToRecipient: boolean
  messageSchedule: string | null
  scheduledMessageHour: string | null
  scheduledMessageMinute: string | null
  adminMemo: string | null
  vendorMemo: string | null
  completedAt: string | null
  createdAt: string
  messageStatus: string | null
  hasReceipt: boolean
  receiptReceiverName: string | null
  receiptReceiverRelation: string | null
  isDeleted: boolean
}

export type CreateOrderPayload = {
  vendorId: number
  orderingCompanyId?: number
  productId?: number
  productName: string
  quantity: number
  amount: number
  extraCost?: number
  deliveryDate: string
  deliveryTimeType: string
  deliveryTimeHour: string
  deliveryTimeMinute: string
  deliveryTimeCustom: string
  address: string
  addressDetail: string
  eventType: string
  placeName: string
  roomName: string
  eventInfo: string
  ribbonLeft: string
  ribbonRight: string
  ordererName: string
  ordererPhone: string
  recipientName: string
  recipientPhone: string
  sendPhotoToOrderer: boolean
  sendPhotoToRecipient: boolean
  messageSchedule: string
  scheduledMessageHour: string
  scheduledMessageMinute: string
  adminMemo: string
  vendorMemo: string
}

export type UpdateOrderPayload = CreateOrderPayload

function authHeaders(): HeadersInit {
  const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  }
}

function toFrontend(o: BackendOrder): Order {
  return {
    id: String(o.id),
    orderNumber: o.orderNumber,
    orderedAt: o.createdAt ?? '',
    vendorId: String(o.vendorId),
    vendorName: o.vendorName ?? '',
    vendorPhone: o.vendorPhone ?? undefined,
    orderingCompanyId: o.orderingCompanyId != null ? String(o.orderingCompanyId) : undefined,
    orderingCompanyName: o.orderingCompanyName ?? undefined,
    orderingCompanyPhone: o.orderingCompanyPhone ?? undefined,
    status: STATUS_MAP[o.status] ?? '발주대기',
    productId: o.productId ?? undefined,
    productName: o.productName,
    quantity: o.quantity,
    amount: o.amount,
    extraCost: o.extraCost ?? undefined,
    deliveryType: '일반',
    deliveryAt: o.deliveryDate ?? '',
    deliveryTimeType: (o.deliveryTimeType as Order['deliveryTimeType']) ?? undefined,
    deliveryTimeHour: o.deliveryTimeHour ?? undefined,
    deliveryTimeMinute: o.deliveryTimeMinute ?? undefined,
    deliveryTimeCustom: o.deliveryTimeCustom ?? undefined,
    address: o.address ?? '',
    addressDetail: o.addressDetail ?? '',
    eventType: (o.eventType as Order['eventType']) ?? '기타',
    placeName: o.placeName ?? '',
    roomName: o.roomName ?? '',
    eventInfo: o.eventInfo ?? undefined,
    ribbonLeft: o.ribbonLeft ?? '',
    ribbonRight: o.ribbonRight ?? '',
    ordererName: o.ordererName,
    ordererPhone: o.ordererPhone,
    recipientName: o.recipientName ?? undefined,
    recipientPhone: o.recipientPhone ?? undefined,
    sendPhotoToOrderer: o.sendPhotoToOrderer,
    sendPhotoToRecipient: o.sendPhotoToRecipient,
    messageSchedule: o.messageSchedule ?? undefined,
    scheduledMessageHour: o.scheduledMessageHour ?? undefined,
    scheduledMessageMinute: o.scheduledMessageMinute ?? undefined,
    adminMemo: o.adminMemo ?? undefined,
    vendorMemo: o.vendorMemo ?? undefined,
    completedAt: o.completedAt ?? undefined,
    messageStatus: o.messageStatus === 'SENT' ? '발송' : o.messageStatus === 'FAIL' ? '실패' : '미발송',
    isDeleted: o.isDeleted ?? false,
    referencePhotoCount: o.referencePhotoCount ?? 0,
    hasReceipt: o.hasReceipt,
    receiptReceiverName: o.receiptReceiverName ?? undefined,
    receiptReceiverRelation: o.receiptReceiverRelation ?? undefined,
  }
}

export type OrderingCompany = {
  id: number
  companyName: string
}

export async function fetchOrderingCompanies(): Promise<OrderingCompany[]> {
  const res = await fetch('/api/companies?type=ORDERING', { headers: authHeaders() })
  if (!res.ok) return []
  const data: Array<{ id: number; companyName: string }> = await res.json()
  return data
}

export type OrderSearchParams = {
  status?: string
  vendorId?: string
  orderingCompanyId?: string
  orderNumber?: string
  productName?: string
  ordererName?: string
  deliveryDateFrom?: string
  deliveryDateTo?: string
  receiverName?: string
  messageStatus?: string
  ribbonLeft?: string
  deleted?: boolean
}

function buildOrderQuery(params?: OrderSearchParams, page?: number, size?: number): string {
  const query = new URLSearchParams()
  if (params?.status) query.set('status', params.status)
  if (params?.vendorId) query.set('vendorId', params.vendorId)
  if (params?.orderingCompanyId) query.set('orderingCompanyId', params.orderingCompanyId)
  if (params?.orderNumber) query.set('orderNumber', params.orderNumber)
  if (params?.productName) query.set('productName', params.productName)
  if (params?.ordererName) query.set('ordererName', params.ordererName)
  if (params?.deliveryDateFrom) query.set('deliveryDateFrom', params.deliveryDateFrom)
  if (params?.deliveryDateTo) query.set('deliveryDateTo', params.deliveryDateTo)
  if (params?.receiverName) query.set('receiverName', params.receiverName)
  if (params?.messageStatus) query.set('messageStatus', params.messageStatus)
  if (params?.ribbonLeft) query.set('ribbonLeft', params.ribbonLeft)
  if (params?.deleted !== undefined) query.set('deleted', String(params.deleted))
  if (page !== undefined) query.set('page', String(page))
  if (size !== undefined) query.set('size', String(size))
  return query.toString()
}

export async function fetchOrders(params?: OrderSearchParams): Promise<Order[]> {
  const qs = buildOrderQuery(params, 0, 10000)
  const res = await fetch(`/api/orders${qs ? `?${qs}` : ''}`, { headers: authHeaders() })
  if (!res.ok) throw new Error('발주 목록을 불러오지 못했습니다.')
  const data: PageResult<BackendOrder> = await res.json()
  return data.content.map(toFrontend)
}

export async function fetchOrdersPaged(params?: OrderSearchParams, page = 0, size = 20): Promise<PageResult<Order>> {
  const qs = buildOrderQuery(params, page, size)
  const res = await fetch(`/api/orders${qs ? `?${qs}` : ''}`, { headers: authHeaders() })
  if (!res.ok) throw new Error('발주 목록을 불러오지 못했습니다.')
  const data: PageResult<BackendOrder> = await res.json()
  return { ...data, content: data.content.map(toFrontend) }
}

export async function fetchOrder(id: string): Promise<Order> {
  const res = await fetch(`/api/orders/${id}`, { headers: authHeaders() })
  if (!res.ok) throw new Error('발주 정보를 불러오지 못했습니다.')
  return toFrontend(await res.json())
}

export async function createOrder(payload: CreateOrderPayload): Promise<Order> {
  const res = await fetch('/api/orders', {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    const data = await res.json().catch(() => ({}))
    throw new Error(data.message ?? '발주 등록에 실패했습니다.')
  }
  return toFrontend(await res.json())
}

export async function updateOrder(id: string, payload: UpdateOrderPayload): Promise<Order> {
  const res = await fetch(`/api/orders/${id}`, {
    method: 'PUT',
    headers: authHeaders(),
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    const data = await res.json().catch(() => ({}))
    throw new Error(data.message ?? '발주 수정에 실패했습니다.')
  }
  return toFrontend(await res.json())
}

export type PhotoResult = {
  id: number
  orderId: number
  photoUrl: string
  photoType: 'STORE' | 'SITE'
  isSendToCustomer: boolean
  uploadedAt: string | null
}

export async function fetchAllPhotos(): Promise<PhotoResult[]> {
  const res = await fetch('/api/photos', { headers: authHeaders() })
  if (!res.ok) throw new Error('사진 목록을 불러오지 못했습니다.')
  return res.json()
}

export async function fetchPhotos(orderId: string): Promise<PhotoResult[]> {
  const res = await fetch(`/api/orders/${orderId}/photos`, { headers: authHeaders() })
  if (!res.ok) throw new Error('사진 목록을 불러오지 못했습니다.')
  return res.json()
}

export async function uploadPhotos(orderId: string, files: File[], photoType: 'STORE' | 'SITE'): Promise<PhotoResult[]> {
  const formData = new FormData()
  files.forEach((f) => formData.append('files', f))
  formData.append('photoType', photoType)
  const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null
  const res = await fetch(`/api/orders/${orderId}/photos`, {
    method: 'POST',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    body: formData,
  })
  if (!res.ok) throw new Error('사진 업로드에 실패했습니다.')
  return res.json()
}

export async function selectPhoto(orderId: string, photoId: number, selected: boolean): Promise<PhotoResult> {
  const res = await fetch(`/api/orders/${orderId}/photos/${photoId}/select`, {
    method: 'PUT',
    headers: authHeaders(),
    body: JSON.stringify({ selected }),
  })
  if (!res.ok) throw new Error('사진 선택 변경에 실패했습니다.')
  return res.json()
}

export type OrderHistoryItem = {
  id: number
  orderId: number
  action: 'CREATED' | 'UPDATED' | 'STATUS_CHANGED'
  detail: string
  actorCompany: string | null
  actorName: string | null
  createdAt: string
}

export async function fetchOrderHistory(orderId: string): Promise<OrderHistoryItem[]> {
  const res = await fetch(`/api/orders/${orderId}/history`, { headers: authHeaders() })
  if (!res.ok) throw new Error('이력을 불러오지 못했습니다.')
  return res.json()
}

export type ReceiptConfirmationResult = {
  id: number
  orderId: number
  receiverName: string | null
  receiverRelation: string | null
  delivered: boolean
  memo: string | null
  confirmedAt: string | null
  sitePhotoUrls: string[]
  storePhotoUrls: string[]
}

export async function fetchReceiptConfirmation(orderId: string): Promise<ReceiptConfirmationResult | null> {
  const res = await fetch(`/api/orders/${orderId}/receipt`, { headers: authHeaders() })
  if (res.status === 204) return null
  if (!res.ok) throw new Error('인수확인 정보를 불러오지 못했습니다.')
  return res.json()
}

export async function saveReceiptConfirmation(
  orderId: string,
  data: { receiverName: string; receiverRelation: string; delivered: boolean; memo: string; confirmedAt: string },
  siteFiles: File[],
  storeFiles: File[]
): Promise<ReceiptConfirmationResult> {
  const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null
  const formData = new FormData()
  if (data.receiverName) formData.append('receiverName', data.receiverName)
  if (data.receiverRelation) formData.append('receiverRelation', data.receiverRelation)
  formData.append('delivered', String(data.delivered))
  if (data.memo) formData.append('memo', data.memo)
  if (data.confirmedAt) formData.append('confirmedAt', data.confirmedAt)
  siteFiles.forEach((f) => formData.append('siteFiles', f))
  storeFiles.forEach((f) => formData.append('storeFiles', f))
  const res = await fetch(`/api/orders/${orderId}/receipt`, {
    method: 'POST',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    body: formData,
  })
  if (!res.ok) throw new Error('인수확인 저장에 실패했습니다.')
  return res.json()
}

export async function updateReceiptInfo(
  orderId: string,
  data: { receiverName: string; receiverRelation: string; delivered: boolean; memo: string; confirmedAt?: string }
): Promise<ReceiptConfirmationResult> {
  const params = new URLSearchParams()
  if (data.receiverName) params.append('receiverName', data.receiverName)
  if (data.receiverRelation) params.append('receiverRelation', data.receiverRelation)
  params.append('delivered', String(data.delivered))
  if (data.memo) params.append('memo', data.memo)
  if (data.confirmedAt) params.append('confirmedAt', data.confirmedAt)
  const res = await fetch(`/api/orders/${orderId}/receipt/info?${params.toString()}`, {
    method: 'PATCH',
    headers: authHeaders(),
  })
  if (!res.ok) throw new Error('인수확인 정보 수정에 실패했습니다.')
  return res.json()
}

export async function uploadReceiptPhotos(orderId: string, files: File[], photoType: 'SITE' | 'STORE' = 'SITE'): Promise<ReceiptConfirmationResult> {
  const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null
  const formData = new FormData()
  files.forEach((f) => formData.append('files', f))
  formData.append('photoType', photoType)
  const res = await fetch(`/api/orders/${orderId}/receipt/photos`, {
    method: 'POST',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    body: formData,
  })
  if (!res.ok) throw new Error('배송사진 업로드에 실패했습니다.')
  return res.json()
}

export type SmsMessageResult = {
  id: number
  orderId: number
  receiver: string
  message: string | null
  status: 'SUCCESS' | 'FAIL'
  failReason: string | null
  aligoMsgId: string | null
  sentAt: string | null
  imageUrl: string | null
}

export async function fetchSmsMessages(orderId: string): Promise<SmsMessageResult[]> {
  const res = await fetch(`/api/messages/order/${orderId}`, { headers: authHeaders() })
  if (!res.ok) throw new Error('발송 내역을 불러오지 못했습니다.')
  return res.json()
}

export type ReferencePhotoResult = {
  id: number
  orderId: number
  photoUrl: string
  uploadedAt: string | null
}

export async function fetchReferencePhotos(orderId: string): Promise<ReferencePhotoResult[]> {
  const res = await fetch(`/api/orders/${orderId}/reference-photos`, { headers: authHeaders() })
  if (!res.ok) throw new Error('참고 이미지를 불러오지 못했습니다.')
  return res.json()
}

export async function uploadReferencePhotos(orderId: string, files: File[]): Promise<ReferencePhotoResult[]> {
  const formData = new FormData()
  files.forEach((f) => formData.append('files', f))
  const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null
  const res = await fetch(`/api/orders/${orderId}/reference-photos`, {
    method: 'POST',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    body: formData,
  })
  if (!res.ok) throw new Error('참고 이미지 업로드에 실패했습니다.')
  return res.json()
}

export async function resendMms(orderId: string): Promise<void> {
  const res = await fetch(`/api/orders/${orderId}/resend`, {
    method: 'POST',
    headers: authHeaders(),
  })
  if (!res.ok) throw new Error('재발송에 실패했습니다.')
}

export async function downloadOrdersExcel(orderIds: string[]): Promise<void> {
  const res = await fetch('/api/orders/export/excel', {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ orderIds: orderIds.map(Number) }),
  })
  if (!res.ok) throw new Error('엑셀 다운로드에 실패했습니다.')
  const blob = await res.blob()
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  const today = new Date().toISOString().slice(0, 10).replace(/-/g, '')
  a.href = url
  a.download = `orders_${today}.xlsx`
  a.click()
  URL.revokeObjectURL(url)
}

export async function deleteOrder(id: string): Promise<void> {
  const res = await fetch(`/api/orders/${id}`, {
    method: 'DELETE',
    headers: authHeaders(),
  })
  if (!res.ok) throw new Error('주문서 삭제에 실패했습니다.')
}

export async function updateOrderStatus(id: string, action: string, photoUrls?: string[]): Promise<Order> {
  const res = await fetch(`/api/orders/${id}/status`, {
    method: 'PUT',
    headers: authHeaders(),
    body: JSON.stringify({ action, photoUrls: photoUrls ?? [] }),
  })
  if (!res.ok) {
    const data = await res.json().catch(() => ({}))
    throw new Error(data.message ?? '상태 변경에 실패했습니다.')
  }
  return toFrontend(await res.json())
}

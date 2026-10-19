import { Order, OrderStatus } from '@/types/order'

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
  vendorId: number
  vendorName: string | null
  status: string
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
}

export type CreateOrderPayload = {
  vendorId: number
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
    status: STATUS_MAP[o.status] ?? '발주대기',
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
    messageStatus: '미발송',
    isDeleted: false,
  }
}

export type OrderSearchParams = {
  status?: string
  vendorId?: string
  orderNumber?: string
  productName?: string
  ordererName?: string
  deliveryDateFrom?: string
  deliveryDateTo?: string
}

export async function fetchOrders(params?: OrderSearchParams): Promise<Order[]> {
  const query = new URLSearchParams()
  if (params?.status) query.set('status', params.status)
  if (params?.vendorId) query.set('vendorId', params.vendorId)
  if (params?.orderNumber) query.set('orderNumber', params.orderNumber)
  if (params?.productName) query.set('productName', params.productName)
  if (params?.ordererName) query.set('ordererName', params.ordererName)
  if (params?.deliveryDateFrom) query.set('deliveryDateFrom', params.deliveryDateFrom)
  if (params?.deliveryDateTo) query.set('deliveryDateTo', params.deliveryDateTo)
  const qs = query.toString()
  const res = await fetch(`/api/orders${qs ? `?${qs}` : ''}`, { headers: authHeaders() })
  if (!res.ok) throw new Error('발주 목록을 불러오지 못했습니다.')
  const data: BackendOrder[] = await res.json()
  return data.map(toFrontend)
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

export async function updateOrderStatus(id: string, action: string): Promise<Order> {
  const res = await fetch(`/api/orders/${id}/status`, {
    method: 'PUT',
    headers: authHeaders(),
    body: JSON.stringify({ action }),
  })
  if (!res.ok) {
    const data = await res.json().catch(() => ({}))
    throw new Error(data.message ?? '상태 변경에 실패했습니다.')
  }
  return toFrontend(await res.json())
}

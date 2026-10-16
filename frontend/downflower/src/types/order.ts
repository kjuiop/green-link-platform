export type OrderStatus = '발주대기' | '수주완료' | '배송완료' | '수주거절' | '주문취소'
export type MessageStatus = '미발송' | '발송성공' | '발송실패'
export type DeliveryType = '일반' | '즉시'
export type EventType = '장례식' | '결혼식' | '기타'

export interface Order {
  id: string
  orderNumber: string
  orderedAt: string
  vendorId: string
  vendorName: string
  productName: string
  quantity: number
  amount: number
  deliveryType: DeliveryType
  deliveryDeadline?: string
  deliveryAt: string
  address: string
  addressDetail: string
  eventType: EventType
  placeName: string
  roomName: string
  eventInfo?: string
  ribbonLeft: string
  ribbonRight: string
  ordererName: string
  ordererPhone: string
  recipientName?: string
  recipientPhone?: string
  sendPhotoToOrderer: boolean
  sendPhotoToRecipient: boolean
  adminMemo?: string
  vendorMemo?: string
  status: OrderStatus
  messageStatus: MessageStatus
  completedAt?: string
}

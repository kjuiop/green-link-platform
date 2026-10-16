export type OrderStatus = '발주대기' | '수주완료' | '배송완료' | '수주거절' | '주문취소'
export type MessageStatus = '미발송' | '발송성공' | '발송실패'

export interface Order {
  id: string
  orderNumber: string
  orderedAt: string
  vendorId: string
  vendorName: string
  productName: string
  quantity: number
  amount: number
  deliveryAt: string
  address: string
  addressDetail: string
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

export interface PublicProductView {
  id: number
  category: string
  categoryLabel: string
  name: string
  description: string | null
  price: number
  imageUrl: string | null
}

export interface PublicShopView {
  slug: string
  displayName: string
  logoUrl: string | null
  phone: string | null
  themeColor: string | null
  bannerUrl: string | null
  bannerTitle: string | null
  bannerSubtitle: string | null
  notice: string | null
  deliveryGuide: string | null
  businessInfo: string | null
  products: PublicProductView[]
}

export interface PlacedShopOrderResult {
  orderNo: string
  status: string
}

export interface PublicOrderStatusView {
  orderNo: string
  status: string
  items: { name: string; unitPrice: number; quantity: number }[]
  totalPrice: number
  deliveryDate: string | null
  createdAt: string | null
}

export interface PlaceShopOrderRequest {
  items: { productId: number; quantity: number }[]
  ordererName: string
  ordererPhone: string
  recipientName: string
  recipientPhone: string
  address: string
  addressDetail: string
  deliveryDate: string
  deliveryTime: string
  eventType: string
  placeName: string
  ribbonLeft: string
  ribbonRight: string
  memo: string
  website: string
}

export interface LookupShopOrderRequest {
  orderNo: string
  ordererPhone: string
}

export interface ErrorBody {
  code?: string
  message?: string
}

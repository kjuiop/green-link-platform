export type PhotoType = 'store' | 'site'

export interface OrderPhoto {
  id: string
  orderId: string
  url: string
  photoType: PhotoType
  isSendToCustomer: boolean
  uploadedAt: string
}

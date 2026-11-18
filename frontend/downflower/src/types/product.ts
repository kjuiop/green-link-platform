export interface Product {
  id: string
  name: string
  price: number
  memo?: string
  createdAt: string
  sortOrder: number
}

export interface ProductRequest {
  name: string
  price: number
  memo?: string
  sortOrder: number
}

export interface VendorProduct {
  id: string
  name: string
  price: number
  memo?: string
  sortOrder: number
  enabled: boolean
  baseProductId: string | null
  basePrice: number | null
  createdAt: string
}

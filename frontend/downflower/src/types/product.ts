import type { ProductCategory } from '@/lib/constants/productCategory'

export interface Product {
  id: string
  name: string
  price: number
  memo?: string
  createdAt: string
  sortOrder: number
  category: ProductCategory | null
}

export interface ProductRequest {
  name: string
  price: number
  memo?: string
  sortOrder: number
  category: ProductCategory
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
  category: ProductCategory | null
}

export interface VendorSearchResult {
  vendorId: string
  companyName: string
  phone: string | null
  businessStatus: string | null
  regions: { sido: string; sigungu: string }[]
  capabilities: string[]
  matchedProductCount: number
  minPrice: number
}

export interface VendorSearchParams {
  category?: ProductCategory
  baseProductId?: string
  keyword?: string
  sido?: string
  sigungu?: string
}

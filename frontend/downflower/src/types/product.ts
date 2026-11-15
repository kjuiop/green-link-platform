export interface Product {
  id: string
  name: string
  price: number
  memo?: string
  createdAt: string
  sortOrder: number
}

export interface CreateProductRequest {
  name: string
  price: number
  memo?: string
  sortOrder: number
}

export interface UpdateProductRequest {
  name: string
  price: number
  memo?: string
  sortOrder: number
}

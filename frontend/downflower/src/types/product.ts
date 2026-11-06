export interface Product {
  id: string
  name: string
  price: number
  memo?: string
  createdAt: string
}

export interface CreateProductRequest {
  name: string
  price: number
  memo?: string
}

export interface UpdateProductRequest {
  name: string
  price: number
  memo?: string
}

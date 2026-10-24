export interface Product {
  id: string
  name: string
  price: number
  memo?: string
  createdAt: string
  orderingCompanyId?: number
}

export interface CreateProductRequest {
  name: string
  price: number
  memo?: string
  orderingCompanyId?: number
}

export interface UpdateProductRequest {
  name: string
  price: number
  memo?: string
}

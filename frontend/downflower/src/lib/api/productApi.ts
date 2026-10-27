import { Product, CreateProductRequest, UpdateProductRequest } from '@/types/product'
import { PageResult } from '@/types/page'
import { authHeaders, fetchWithAuth } from './fetchWithAuth'

type BackendProduct = {
  id: number
  name: string
  price: number
  memo: string | null
  createdAt: string
  orderingCompanyId: number | null
}

function toFrontend(p: BackendProduct): Product {
  return {
    id: String(p.id),
    name: p.name,
    price: p.price,
    memo: p.memo ?? undefined,
    createdAt: p.createdAt ?? '',
    orderingCompanyId: p.orderingCompanyId ?? undefined,
  }
}

export async function fetchProducts(page = 0, size = 20, orderingCompanyId?: number): Promise<PageResult<Product>> {
  const qs = new URLSearchParams({ page: String(page), size: String(size) })
  if (orderingCompanyId != null) qs.set('orderingCompanyId', String(orderingCompanyId))
  const res = await fetchWithAuth(`/api/products?${qs}`, { headers: authHeaders() })
  if (!res.ok) throw new Error('상품 목록을 불러오지 못했습니다.')
  const data: PageResult<BackendProduct> = await res.json()
  return { ...data, content: data.content.map(toFrontend) }
}

export async function fetchAllProducts(orderingCompanyId?: number): Promise<Product[]> {
  const qs = orderingCompanyId != null ? `?orderingCompanyId=${orderingCompanyId}` : ''
  const res = await fetchWithAuth(`/api/products/all${qs}`, { headers: authHeaders() })
  if (!res.ok) throw new Error('상품 목록을 불러오지 못했습니다.')
  const data: BackendProduct[] = await res.json()
  return data.map(toFrontend)
}

export async function createProduct(req: CreateProductRequest): Promise<Product> {
  const res = await fetchWithAuth('/api/products', {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ name: req.name, price: req.price, memo: req.memo, orderingCompanyId: req.orderingCompanyId }),
  })
  if (!res.ok) {
    const data = await res.json().catch(() => ({}))
    throw new Error(data.message ?? '상품 등록에 실패했습니다.')
  }
  return toFrontend(await res.json())
}

export async function updateProduct(id: string, req: UpdateProductRequest): Promise<Product> {
  const res = await fetchWithAuth(`/api/products/${id}`, {
    method: 'PUT',
    headers: authHeaders(),
    body: JSON.stringify({ name: req.name, price: req.price, memo: req.memo }),
  })
  if (!res.ok) {
    const data = await res.json().catch(() => ({}))
    throw new Error(data.message ?? '상품 수정에 실패했습니다.')
  }
  return toFrontend(await res.json())
}

export async function deleteProduct(id: string): Promise<void> {
  const res = await fetchWithAuth(`/api/products/${id}`, {
    method: 'DELETE',
    headers: authHeaders(),
  })
  if (!res.ok) {
    const data = await res.json().catch(() => ({}))
    throw new Error(data.message ?? '상품 삭제에 실패했습니다.')
  }
}

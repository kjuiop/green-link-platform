import { Product, ProductRequest, VendorProduct } from '@/types/product'
import { PageResult } from '@/types/page'
import { authHeaders, fetchWithAuth } from './fetchWithAuth'

type BackendProduct = {
  id: number
  name: string
  price: number
  memo: string | null
  createdAt: string
  sortOrder: number
}

type BackendVendorProduct = BackendProduct & {
  enabled: boolean
  baseProductId: number | null
  basePrice: number | null
}

function toFrontend(p: BackendProduct): Product {
  return {
    id: String(p.id),
    name: p.name,
    price: p.price,
    memo: p.memo ?? undefined,
    createdAt: p.createdAt ?? '',
    sortOrder: p.sortOrder ?? 0,
  }
}

function toVendorProduct(p: BackendVendorProduct): VendorProduct {
  return {
    id: String(p.id),
    name: p.name,
    price: p.price,
    memo: p.memo ?? undefined,
    sortOrder: p.sortOrder ?? 0,
    enabled: p.enabled,
    baseProductId: p.baseProductId == null ? null : String(p.baseProductId),
    basePrice: p.basePrice ?? null,
    createdAt: p.createdAt ?? '',
  }
}

async function errorMessage(res: Response, fallback: string): Promise<string> {
  const data = await res.json().catch(() => ({}))
  return data.message ?? fallback
}

function body(req: ProductRequest): string {
  return JSON.stringify({ name: req.name, price: req.price, memo: req.memo, sortOrder: req.sortOrder })
}

// 발주 폼용 전체 목록 (기본상품)
export async function fetchAllProducts(): Promise<Product[]> {
  const res = await fetchWithAuth('/api/products/all', { headers: authHeaders() })
  if (!res.ok) throw new Error('상품 목록을 불러오지 못했습니다.')
  const data: BackendProduct[] = await res.json()
  return data.map(toFrontend)
}

// ── 기본상품 (슈퍼관리자) ────────────────────────────────────────────────

export async function fetchBaseProducts(page = 0, size = 20): Promise<PageResult<Product>> {
  const qs = new URLSearchParams({ page: String(page), size: String(size) })
  const res = await fetchWithAuth(`/api/base-products?${qs}`, { headers: authHeaders() })
  if (!res.ok) throw new Error('기본상품 목록을 불러오지 못했습니다.')
  const data: PageResult<BackendProduct> = await res.json()
  return { ...data, content: data.content.map(toFrontend) }
}

export async function createBaseProduct(req: ProductRequest): Promise<Product> {
  const res = await fetchWithAuth('/api/base-products', { method: 'POST', headers: authHeaders(), body: body(req) })
  if (!res.ok) throw new Error(await errorMessage(res, '기본상품 등록에 실패했습니다.'))
  return toFrontend(await res.json())
}

export async function updateBaseProduct(id: string, req: ProductRequest): Promise<Product> {
  const res = await fetchWithAuth(`/api/base-products/${id}`, { method: 'PUT', headers: authHeaders(), body: body(req) })
  if (!res.ok) throw new Error(await errorMessage(res, '기본상품 수정에 실패했습니다.'))
  return toFrontend(await res.json())
}

export async function deleteBaseProduct(id: string): Promise<void> {
  const res = await fetchWithAuth(`/api/base-products/${id}`, { method: 'DELETE', headers: authHeaders() })
  if (!res.ok) throw new Error(await errorMessage(res, '기본상품 삭제에 실패했습니다.'))
}

// ── 수주사 상품 ──────────────────────────────────────────────────────────

export async function fetchVendorProducts(keyword: string, page = 0, size = 20): Promise<PageResult<VendorProduct>> {
  const qs = new URLSearchParams({ page: String(page), size: String(size) })
  if (keyword.trim()) qs.set('keyword', keyword.trim())
  const res = await fetchWithAuth(`/api/vendor/products?${qs}`, { headers: authHeaders() })
  if (!res.ok) throw new Error(await errorMessage(res, '상품 목록을 불러오지 못했습니다.'))
  const data: PageResult<BackendVendorProduct> = await res.json()
  return { ...data, content: data.content.map(toVendorProduct) }
}

export async function createVendorProduct(req: ProductRequest): Promise<VendorProduct> {
  const res = await fetchWithAuth('/api/vendor/products', { method: 'POST', headers: authHeaders(), body: body(req) })
  if (!res.ok) throw new Error(await errorMessage(res, '상품 등록에 실패했습니다.'))
  return toVendorProduct(await res.json())
}

export async function updateVendorProduct(id: string, req: ProductRequest): Promise<VendorProduct> {
  const res = await fetchWithAuth(`/api/vendor/products/${id}`, { method: 'PUT', headers: authHeaders(), body: body(req) })
  if (!res.ok) throw new Error(await errorMessage(res, '상품 수정에 실패했습니다.'))
  return toVendorProduct(await res.json())
}

export async function changeVendorProductSetting(id: string, price: number, enabled: boolean): Promise<VendorProduct> {
  const res = await fetchWithAuth(`/api/vendor/products/${id}/setting`, {
    method: 'PATCH',
    headers: authHeaders(),
    body: JSON.stringify({ price, enabled }),
  })
  if (!res.ok) throw new Error(await errorMessage(res, '상품 설정 변경에 실패했습니다.'))
  return toVendorProduct(await res.json())
}

export async function deleteVendorProduct(id: string): Promise<void> {
  const res = await fetchWithAuth(`/api/vendor/products/${id}`, { method: 'DELETE', headers: authHeaders() })
  if (!res.ok) throw new Error(await errorMessage(res, '상품 삭제에 실패했습니다.'))
}

import { Product, ProductRequest, VendorProduct, VendorSearchParams, VendorSearchResult } from '@/types/product'
import type { ProductCategory } from '@/lib/constants/productCategory'
import { PageResult } from '@/types/page'
import { authHeaders, fetchWithAuth } from './fetchWithAuth'

type BackendProduct = {
  id: number
  name: string
  price: number
  memo: string | null
  createdAt: string
  sortOrder: number
  category: ProductCategory | null
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
    category: p.category ?? null,
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
    category: p.category ?? null,
  }
}

async function errorMessage(res: Response, fallback: string): Promise<string> {
  const data = await res.json().catch(() => ({}))
  return data.message ?? fallback
}

function body(req: ProductRequest): string {
  return JSON.stringify({ name: req.name, price: req.price, memo: req.memo, sortOrder: req.sortOrder, category: req.category })
}

// 발주 폼용 전체 목록 (기본상품)
export async function fetchAllProducts(): Promise<Product[]> {
  const res = await fetchWithAuth('/api/products/all', { headers: authHeaders() })
  if (!res.ok) throw new Error('상품 목록을 불러오지 못했습니다.')
  const data: BackendProduct[] = await res.json()
  return data.map(toFrontend)
}

// 상품 조건에 맞는 수주업체 검색 (업체당 1행)
export async function searchVendorsByProduct(params: VendorSearchParams): Promise<VendorSearchResult[]> {
  const qs = new URLSearchParams()
  if (params.category) qs.set('category', params.category)
  if (params.baseProductId) qs.set('baseProductId', params.baseProductId)
  if (params.keyword?.trim()) qs.set('keyword', params.keyword.trim())
  if (params.sido) qs.set('sido', params.sido)
  if (params.sigungu) qs.set('sigungu', params.sigungu)
  const res = await fetchWithAuth(`/api/products/vendors?${qs}`, { headers: authHeaders() })
  if (!res.ok) throw new Error(await errorMessage(res, '수주업체를 불러오지 못했습니다.'))
  const data: (Omit<VendorSearchResult, 'vendorId'> & { vendorId: number })[] = await res.json()
  return data.map((v) => ({ ...v, vendorId: String(v.vendorId) }))
}

// 수주업체의 발주 가능(ON) 상품 목록
export async function fetchSellableVendorProducts(vendorId: string): Promise<VendorProduct[]> {
  const qs = new URLSearchParams({ vendorId, page: '0', size: '100' })
  const res = await fetchWithAuth(`/api/products/search?${qs}`, { headers: authHeaders() })
  if (!res.ok) throw new Error(await errorMessage(res, '상품 목록을 불러오지 못했습니다.'))
  const data: PageResult<BackendVendorProduct> = await res.json()
  return data.content.map(toVendorProduct)
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

export async function fetchVendorProducts(keyword: string, page = 0, size = 20, companyId?: number | null): Promise<PageResult<VendorProduct>> {
  const qs = new URLSearchParams({ page: String(page), size: String(size) })
  if (keyword.trim()) qs.set('keyword', keyword.trim())
  if (companyId) qs.set('companyId', String(companyId))
  const res = await fetchWithAuth(`/api/vendor/products?${qs}`, { headers: authHeaders() })
  if (!res.ok) throw new Error(await errorMessage(res, '상품 목록을 불러오지 못했습니다.'))
  const data: PageResult<BackendVendorProduct> = await res.json()
  return { ...data, content: data.content.map(toVendorProduct) }
}

export async function createVendorProduct(req: ProductRequest, companyId?: number | null): Promise<VendorProduct> {
  const qs = companyId ? `?companyId=${companyId}` : ''
  const res = await fetchWithAuth(`/api/vendor/products${qs}`, { method: 'POST', headers: authHeaders(), body: body(req) })
  if (!res.ok) throw new Error(await errorMessage(res, '상품 등록에 실패했습니다.'))
  return toVendorProduct(await res.json())
}

export async function updateVendorProduct(id: string, req: ProductRequest, companyId?: number | null): Promise<VendorProduct> {
  const qs = companyId ? `?companyId=${companyId}` : ''
  const res = await fetchWithAuth(`/api/vendor/products/${id}${qs}`, { method: 'PUT', headers: authHeaders(), body: body(req) })
  if (!res.ok) throw new Error(await errorMessage(res, '상품 수정에 실패했습니다.'))
  return toVendorProduct(await res.json())
}

export async function changeVendorProductSetting(id: string, price: number, enabled: boolean, companyId?: number | null): Promise<VendorProduct> {
  const qs = companyId ? `?companyId=${companyId}` : ''
  const res = await fetchWithAuth(`/api/vendor/products/${id}/setting${qs}`, {
    method: 'PATCH',
    headers: authHeaders(),
    body: JSON.stringify({ price, enabled }),
  })
  if (!res.ok) throw new Error(await errorMessage(res, '상품 설정 변경에 실패했습니다.'))
  return toVendorProduct(await res.json())
}

export async function deleteVendorProduct(id: string, companyId?: number | null): Promise<void> {
  const qs = companyId ? `?companyId=${companyId}` : ''
  const res = await fetchWithAuth(`/api/vendor/products/${id}${qs}`, { method: 'DELETE', headers: authHeaders() })
  if (!res.ok) throw new Error(await errorMessage(res, '상품 삭제에 실패했습니다.'))
}

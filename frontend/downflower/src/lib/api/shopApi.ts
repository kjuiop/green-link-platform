import { authBearerHeader, authHeaders, fetchWithAuth } from './fetchWithAuth'
import { PageResult } from '@/types/page'

export type ShopOrderStatus = 'PENDING' | 'HELD' | 'APPROVED' | 'REJECTED' | 'CANCELED'

export const SHOP_CATEGORIES: { value: string; label: string }[] = [
  { value: 'FUNERAL', label: '근조' },
  { value: 'CELEBRATION', label: '축하' },
  { value: 'ORCHID', label: '난' },
  { value: 'FLOWER', label: '꽃' },
  { value: 'PLANT', label: '관엽' },
  { value: 'RICE_WREATH', label: '쌀화환' },
  { value: 'ETC', label: '기타' },
]

export interface ShopSettings {
  exists: boolean
  id: number | null
  slug: string | null
  published: boolean
  displayName: string | null
  logoUrl: string | null
  phone: string | null
  themeColor: string | null
  bannerUrl: string | null
  bannerTitle: string | null
  bannerSubtitle: string | null
  notice: string | null
  deliveryGuide: string | null
  businessInfo: string | null
}

export interface SaveShopSettingsData {
  slug: string
  displayName: string
  logoUrl: string
  phone: string
  themeColor: string
  bannerUrl: string
  bannerTitle: string
  bannerSubtitle: string
  notice: string
  deliveryGuide: string
  businessInfo: string
}

export interface ShopProduct {
  id: number
  category: string
  categoryLabel: string
  name: string
  description: string | null
  price: number
  imageUrl: string | null
  visible: boolean
  sortOrder: number
}

export interface SaveShopProductData {
  category: string
  name: string
  description: string
  price: number
  imageUrl: string
  visible: boolean
}

export interface ShopOrderItem {
  productId: number
  name: string
  unitPrice: number
  quantity: number
}

export interface ShopOrder {
  id: number
  companyId: number
  orderNo: string
  status: ShopOrderStatus
  items: ShopOrderItem[]
  totalPrice: number
  ordererName: string
  ordererPhone: string
  recipientName: string
  recipientPhone: string
  address: string
  addressDetail: string | null
  deliveryDate: string | null
  deliveryTime: string | null
  eventType: string | null
  placeName: string | null
  ribbonLeft: string | null
  ribbonRight: string | null
  memo: string | null
  rejectReason: string | null
  linkedOrderId: number | null
  linkedOrderAmount: number | null
  margin: number | null
  approvedAt: string | null
  createdAt: string | null
}

async function errorMessage(res: Response, fallback: string): Promise<string> {
  try {
    const body = await res.json()
    return body.message || fallback
  } catch {
    return fallback
  }
}

function withCompany(path: string, companyId?: number | null, extra?: Record<string, string>): string {
  const params = new URLSearchParams(extra)
  if (companyId) params.set('companyId', String(companyId))
  const qs = params.toString()
  return qs ? `${path}?${qs}` : path
}

export async function fetchShopSettings(companyId?: number | null): Promise<ShopSettings> {
  const res = await fetchWithAuth(withCompany('/api/shop/settings', companyId), { headers: authHeaders() })
  if (!res.ok) throw new Error(await errorMessage(res, '쇼핑몰 설정을 불러오지 못했습니다.'))
  return res.json()
}

export async function saveShopSettings(
  companyId: number | null | undefined,
  exists: boolean,
  data: SaveShopSettingsData,
): Promise<ShopSettings> {
  const res = await fetchWithAuth(withCompany('/api/shop/settings', companyId), {
    method: exists ? 'PUT' : 'POST',
    headers: authHeaders(),
    body: JSON.stringify(data),
  })
  if (!res.ok) throw new Error(await errorMessage(res, '쇼핑몰 설정 저장에 실패했습니다.'))
  return res.json()
}

export async function setShopPublished(companyId: number | null | undefined, published: boolean): Promise<ShopSettings> {
  const res = await fetchWithAuth(withCompany('/api/shop/settings/published', companyId), {
    method: 'PUT',
    headers: authHeaders(),
    body: JSON.stringify({ published }),
  })
  if (!res.ok) throw new Error(await errorMessage(res, '공개 상태 변경에 실패했습니다.'))
  return res.json()
}

export async function checkShopSlug(companyId: number | null | undefined, slug: string): Promise<boolean> {
  const res = await fetchWithAuth(withCompany('/api/shop/settings/slug-check', companyId, { slug }), {
    headers: authHeaders(),
  })
  if (!res.ok) throw new Error(await errorMessage(res, '주소 확인에 실패했습니다.'))
  const body = await res.json()
  return !!body.available
}

export async function uploadShopImage(companyId: number | null | undefined, file: File): Promise<string> {
  const form = new FormData()
  form.append('file', file)
  const res = await fetchWithAuth(withCompany('/api/shop/images', companyId), {
    method: 'POST',
    headers: authBearerHeader(),
    body: form,
  })
  if (!res.ok) throw new Error(await errorMessage(res, '이미지 업로드에 실패했습니다.'))
  const body = await res.json()
  return body.url
}

export async function fetchShopProducts(companyId?: number | null): Promise<ShopProduct[]> {
  const res = await fetchWithAuth(withCompany('/api/shop/products', companyId), { headers: authHeaders() })
  if (!res.ok) throw new Error(await errorMessage(res, '상품 목록을 불러오지 못했습니다.'))
  return res.json()
}

export async function createShopProduct(companyId: number | null | undefined, data: SaveShopProductData): Promise<ShopProduct> {
  const res = await fetchWithAuth(withCompany('/api/shop/products', companyId), {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify(data),
  })
  if (!res.ok) throw new Error(await errorMessage(res, '상품 등록에 실패했습니다.'))
  return res.json()
}

export async function updateShopProduct(
  companyId: number | null | undefined,
  id: number,
  data: SaveShopProductData,
): Promise<ShopProduct> {
  const res = await fetchWithAuth(withCompany(`/api/shop/products/${id}`, companyId), {
    method: 'PUT',
    headers: authHeaders(),
    body: JSON.stringify(data),
  })
  if (!res.ok) throw new Error(await errorMessage(res, '상품 수정에 실패했습니다.'))
  return res.json()
}

export async function deleteShopProduct(companyId: number | null | undefined, id: number): Promise<void> {
  const res = await fetchWithAuth(withCompany(`/api/shop/products/${id}`, companyId), {
    method: 'DELETE',
    headers: authHeaders(),
  })
  if (!res.ok) throw new Error(await errorMessage(res, '상품 삭제에 실패했습니다.'))
}

export async function reorderShopProducts(companyId: number | null | undefined, orderedIds: number[]): Promise<void> {
  const res = await fetchWithAuth(withCompany('/api/shop/products/reorder', companyId), {
    method: 'PUT',
    headers: authHeaders(),
    body: JSON.stringify(orderedIds),
  })
  if (!res.ok) throw new Error(await errorMessage(res, '순서 변경에 실패했습니다.'))
}

export async function fetchShopOrders(
  companyId: number | null | undefined,
  status: ShopOrderStatus,
  page = 0,
  size = 20,
): Promise<PageResult<ShopOrder>> {
  const res = await fetchWithAuth(
    withCompany('/api/shop/orders', companyId, { status, page: String(page), size: String(size) }),
    { headers: authHeaders() },
  )
  if (!res.ok) throw new Error(await errorMessage(res, '쇼핑몰 주문을 불러오지 못했습니다.'))
  return res.json()
}

export async function fetchShopOrder(id: number): Promise<ShopOrder> {
  const res = await fetchWithAuth(`/api/shop/orders/${id}`, { headers: authHeaders() })
  if (!res.ok) throw new Error(await errorMessage(res, '쇼핑몰 주문을 불러오지 못했습니다.'))
  return res.json()
}

export async function fetchShopPendingCount(companyId?: number | null): Promise<number> {
  const res = await fetchWithAuth(withCompany('/api/shop/orders/pending-count', companyId), { headers: authHeaders() })
  if (!res.ok) return 0
  const body = await res.json()
  return body.count ?? 0
}

export async function holdShopOrder(id: number): Promise<ShopOrder> {
  const res = await fetchWithAuth(`/api/shop/orders/${id}/hold`, { method: 'POST', headers: authHeaders() })
  if (!res.ok) throw new Error(await errorMessage(res, '보류 처리에 실패했습니다.'))
  return res.json()
}

export async function releaseShopOrder(id: number): Promise<ShopOrder> {
  const res = await fetchWithAuth(`/api/shop/orders/${id}/release`, { method: 'POST', headers: authHeaders() })
  if (!res.ok) throw new Error(await errorMessage(res, '보류 해제에 실패했습니다.'))
  return res.json()
}

export async function rejectShopOrder(id: number, reason: string): Promise<ShopOrder> {
  const res = await fetchWithAuth(`/api/shop/orders/${id}/reject`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ reason }),
  })
  if (!res.ok) throw new Error(await errorMessage(res, '거절 처리에 실패했습니다.'))
  return res.json()
}

export async function approveShopOrder(id: number, body: unknown): Promise<{ id: number; orderNumber: string }> {
  const res = await fetchWithAuth(`/api/shop/orders/${id}/approve`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify(body),
  })
  if (!res.ok) throw new Error(await errorMessage(res, '승인(발주) 처리에 실패했습니다.'))
  return res.json()
}

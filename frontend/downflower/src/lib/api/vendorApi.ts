import { Vendor } from '@/types/vendor'
import { PageResult } from '@/types/page'
import { authHeaders, fetchWithAuth } from './fetchWithAuth'

type BackendVendor = {
  id: number
  companyName: string
  businessNo?: string
  faxNumber?: string
  managerName: string
  phone: string
  address: string
  loginId: string
  status: string
  createdAt?: string
  companyType?: string
}

type CreateVendorPayload = {
  companyName: string
  businessNo?: string
  faxNumber?: string
  address: string
  managerName: string
  phone: string
  loginId: string
  password: string
  companyType?: string
}

type UpdateVendorPayload = {
  companyName: string
  businessNo?: string
  faxNumber?: string
  address: string
  managerName: string
  phone: string
  companyType?: string
}

function toFrontend(v: BackendVendor): Vendor {
  return {
    id: String(v.id),
    name: v.companyName,
    businessNumber: v.businessNo ?? undefined,
    faxNumber: v.faxNumber ?? undefined,
    managerName: v.managerName,
    phone: v.phone,
    address: v.address,
    loginId: v.loginId,
    status: v.status as Vendor['status'],
    createdAt: v.createdAt ?? undefined,
    companyType: (v.companyType as Vendor['companyType']) ?? undefined,
  }
}

export async function fetchVendors(status?: string): Promise<Vendor[]> {
  const query = new URLSearchParams({ page: '0', size: '1000' })
  if (status && status !== 'ALL') query.set('status', status)
  const res = await fetchWithAuth(`/api/vendors?${query}`, { headers: authHeaders() })
  if (!res.ok) throw new Error('업체 목록을 불러오지 못했습니다.')
  const data: PageResult<BackendVendor> = await res.json()
  return data.content.map(toFrontend)
}

export async function fetchVendorsPaged(status?: string, page = 0, size = 20): Promise<PageResult<Vendor>> {
  const query = new URLSearchParams({ page: String(page), size: String(size) })
  if (status && status !== 'ALL') query.set('status', status)
  const res = await fetchWithAuth(`/api/vendors?${query}`, { headers: authHeaders() })
  if (!res.ok) throw new Error('업체 목록을 불러오지 못했습니다.')
  const data: PageResult<BackendVendor> = await res.json()
  return { ...data, content: data.content.map(toFrontend) }
}

export async function fetchVendor(id: string): Promise<Vendor> {
  const res = await fetchWithAuth(`/api/vendors/${id}`, { headers: authHeaders() })
  if (!res.ok) throw new Error('업체 정보를 불러오지 못했습니다.')
  return toFrontend(await res.json())
}

export async function createVendor(payload: CreateVendorPayload): Promise<Vendor> {
  const res = await fetchWithAuth('/api/vendors', {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    const data = await res.json().catch(() => ({}))
    throw new Error(data.message ?? '업체 등록에 실패했습니다.')
  }
  return toFrontend(await res.json())
}

export async function updateVendor(id: string, payload: UpdateVendorPayload): Promise<Vendor> {
  const res = await fetchWithAuth(`/api/vendors/${id}`, {
    method: 'PUT',
    headers: authHeaders(),
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    const data = await res.json().catch(() => ({}))
    throw new Error(data.message ?? '업체 수정에 실패했습니다.')
  }
  return toFrontend(await res.json())
}

export async function updateVendorStatus(id: string, action: 'activate' | 'suspend'): Promise<Vendor> {
  const res = await fetchWithAuth(`/api/vendors/${id}/status`, {
    method: 'PUT',
    headers: authHeaders(),
    body: JSON.stringify({ action }),
  })
  if (!res.ok) {
    const data = await res.json().catch(() => ({}))
    throw new Error(data.message ?? '상태 변경에 실패했습니다.')
  }
  return toFrontend(await res.json())
}

export async function deleteVendor(id: string): Promise<void> {
  const res = await fetchWithAuth(`/api/vendors/${id}`, {
    method: 'DELETE',
    headers: authHeaders(),
  })
  if (!res.ok) {
    const data = await res.json().catch(() => ({}))
    throw new Error(data.message ?? '업체 삭제에 실패했습니다.')
  }
}

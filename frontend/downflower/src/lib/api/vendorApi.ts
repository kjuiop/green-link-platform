import { Vendor } from '@/types/vendor'

type BackendVendor = {
  id: number
  companyName: string
  businessNo?: string
  managerName: string
  phone: string
  address: string
  loginId: string
  status: string
  createdAt?: string
}

type CreateVendorPayload = {
  companyName: string
  businessNo?: string
  address: string
  managerName: string
  phone: string
  loginId: string
  password: string
}

type UpdateVendorPayload = {
  companyName: string
  businessNo?: string
  address: string
  managerName: string
  phone: string
}

function authHeaders(): HeadersInit {
  const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  }
}

function toFrontend(v: BackendVendor): Vendor {
  return {
    id: String(v.id),
    name: v.companyName,
    businessNumber: v.businessNo ?? undefined,
    managerName: v.managerName,
    phone: v.phone,
    address: v.address,
    loginId: v.loginId,
    status: v.status as Vendor['status'],
    createdAt: v.createdAt ?? undefined,
  }
}

export async function fetchVendors(status?: string): Promise<Vendor[]> {
  const url = status && status !== 'ALL' ? `/api/vendors?status=${status}` : '/api/vendors'
  const res = await fetch(url, { headers: authHeaders() })
  if (!res.ok) throw new Error('업체 목록을 불러오지 못했습니다.')
  const data: BackendVendor[] = await res.json()
  return data.map(toFrontend)
}

export async function fetchVendor(id: string): Promise<Vendor> {
  const res = await fetch(`/api/vendors/${id}`, { headers: authHeaders() })
  if (!res.ok) throw new Error('업체 정보를 불러오지 못했습니다.')
  return toFrontend(await res.json())
}

export async function createVendor(payload: CreateVendorPayload): Promise<Vendor> {
  const res = await fetch('/api/vendors', {
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
  const res = await fetch(`/api/vendors/${id}`, {
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
  const res = await fetch(`/api/vendors/${id}/status`, {
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

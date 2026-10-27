import { MenuItem, UserRole } from '@/types/menu'
import { authHeaders, fetchWithAuth } from './fetchWithAuth'

type CreateMenuPayload = {
  name: string
  url: string
  sortOrder: number
  parentId: number | null
  roles: UserRole[]
}

type UpdateMenuPayload = {
  name: string
  url: string
  sortOrder: number
  roles: UserRole[]
}

export async function fetchAllMenus(): Promise<MenuItem[]> {
  const res = await fetchWithAuth('/api/menus', { headers: authHeaders() })
  if (!res.ok) throw new Error('메뉴 목록을 불러오지 못했습니다.')
  return res.json()
}

export async function fetchMenusByRole(): Promise<MenuItem[]> {
  const res = await fetchWithAuth('/api/menus/by-role', { headers: authHeaders() })
  if (!res.ok) throw new Error('메뉴 목록을 불러오지 못했습니다.')
  return res.json()
}

export async function createMenu(payload: CreateMenuPayload): Promise<MenuItem> {
  const res = await fetchWithAuth('/api/menus', {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    const data = await res.json().catch(() => ({}))
    throw new Error(data.message ?? '메뉴 생성에 실패했습니다.')
  }
  return res.json()
}

export async function updateMenu(id: number, payload: UpdateMenuPayload): Promise<MenuItem> {
  const res = await fetchWithAuth(`/api/menus/${id}`, {
    method: 'PUT',
    headers: authHeaders(),
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    const data = await res.json().catch(() => ({}))
    throw new Error(data.message ?? '메뉴 수정에 실패했습니다.')
  }
  return res.json()
}

export async function deleteMenu(id: number): Promise<void> {
  const res = await fetchWithAuth(`/api/menus/${id}`, {
    method: 'DELETE',
    headers: authHeaders(),
  })
  if (!res.ok) {
    const data = await res.json().catch(() => ({}))
    throw new Error(data.message ?? '메뉴 삭제에 실패했습니다.')
  }
}

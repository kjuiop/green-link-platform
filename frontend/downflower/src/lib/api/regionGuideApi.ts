import { authHeaders, fetchWithAuth } from './fetchWithAuth'
import type { RegionGuideCategory } from '@/lib/constants/regionGuideCategory'

export interface RegionGuide {
  id: number
  sido: string
  sigungu: string | null
  placeName: string | null
  category: RegionGuideCategory
  content: string
  aliases: string | null
  sortOrder: number
  active: boolean
  updatedBy: number | null
  updatedAt: string | null
}

export interface SaveRegionGuideInput {
  sido: string
  sigungu: string
  placeName: string
  category: RegionGuideCategory
  content: string
  aliases: string
  sortOrder: number
}

async function errorMessage(res: Response, fallback: string): Promise<string> {
  try {
    const body = await res.json()
    return body.message || fallback
  } catch {
    return fallback
  }
}

const jsonHeaders = () => ({ ...authHeaders(), 'Content-Type': 'application/json' })

export async function fetchActiveRegionGuides(): Promise<RegionGuide[]> {
  const res = await fetchWithAuth('/api/region-guides', { headers: authHeaders() })
  if (!res.ok) throw new Error(await errorMessage(res, '반입안내 조회에 실패했습니다.'))
  return res.json()
}

export async function fetchRegionGuidesBySido(sido: string): Promise<RegionGuide[]> {
  const res = await fetchWithAuth(`/api/admin/region-guides?${new URLSearchParams({ sido })}`, { headers: authHeaders() })
  if (!res.ok) throw new Error(await errorMessage(res, '안내문구 목록 조회에 실패했습니다.'))
  return res.json()
}

export async function createRegionGuide(input: SaveRegionGuideInput): Promise<RegionGuide> {
  const res = await fetchWithAuth('/api/admin/region-guides', {
    method: 'POST',
    headers: jsonHeaders(),
    body: JSON.stringify(input),
  })
  if (!res.ok) throw new Error(await errorMessage(res, '안내문구 등록에 실패했습니다.'))
  return res.json()
}

export async function updateRegionGuide(id: number, input: SaveRegionGuideInput): Promise<RegionGuide> {
  const res = await fetchWithAuth(`/api/admin/region-guides/${id}`, {
    method: 'PUT',
    headers: jsonHeaders(),
    body: JSON.stringify(input),
  })
  if (!res.ok) throw new Error(await errorMessage(res, '안내문구 수정에 실패했습니다.'))
  return res.json()
}

export async function changeRegionGuideActive(id: number, active: boolean): Promise<RegionGuide> {
  const res = await fetchWithAuth(`/api/admin/region-guides/${id}/active`, {
    method: 'PATCH',
    headers: jsonHeaders(),
    body: JSON.stringify({ active }),
  })
  if (!res.ok) throw new Error(await errorMessage(res, '노출 상태 변경에 실패했습니다.'))
  return res.json()
}

export async function deleteRegionGuide(id: number): Promise<void> {
  const res = await fetchWithAuth(`/api/admin/region-guides/${id}`, { method: 'DELETE', headers: authHeaders() })
  if (!res.ok) throw new Error(await errorMessage(res, '안내문구 삭제에 실패했습니다.'))
}

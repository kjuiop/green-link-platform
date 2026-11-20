import { authBearerHeader, fetchWithAuth } from './fetchWithAuth'
import type { SmsProvider } from './companyApi'

export type SmsSettingsResult = {
  id: number | null
  senderNumber: string
  senderName: string
  templateFuneral: string
  templateWedding: string
  templateOthers: string
  provider: SmsProvider
  apiKey: string | null
  userId: string | null
  corpNum: string | null
}

export async function fetchSmsSettings(): Promise<SmsSettingsResult> {
  const res = await fetchWithAuth('/api/settings/sms', { headers: authBearerHeader() })
  if (!res.ok) throw new Error('문자 설정을 불러오지 못했습니다.')
  return res.json()
}

export async function saveSmsSettings(data: Omit<SmsSettingsResult, 'id'>): Promise<SmsSettingsResult> {
  const res = await fetchWithAuth('/api/settings/sms', {
    method: 'PUT',
    headers: { ...authBearerHeader(), 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
  if (!res.ok) throw new Error('문자 설정 저장에 실패했습니다.')
  return res.json()
}

export type BarobillQueryResult = { code: number; message: string }

export async function checkBarobillSender(companyId?: number): Promise<BarobillQueryResult> {
  const query = companyId ? `?companyId=${companyId}` : ''
  const res = await fetchWithAuth(`/api/admin/sms/barobill/sender-check${query}`, { headers: authBearerHeader() })
  if (!res.ok) {
    const data = await res.json().catch(() => ({}))
    throw new Error(data.message ?? '발신번호 확인에 실패했습니다.')
  }
  return res.json()
}

export type KakaoChannel = { channelId: string; channelName: string; status: number }
export type KakaoTemplate = { templateName: string; title: string | null; content: string | null; status: number }
export type KakaoUrlType = 'CHANNEL' | 'TEMPLATE' | 'HISTORY'

async function getKakao<T>(path: string, fallback: string): Promise<T> {
  const res = await fetchWithAuth(`/api/admin/sms/barobill/kakao/${path}`, { headers: authBearerHeader() })
  if (!res.ok) {
    const data = await res.json().catch(() => ({}))
    throw new Error(data.message ?? fallback)
  }
  return res.json()
}

export function fetchBarobillKakaoChannels(companyId: number) {
  return getKakao<{ code: number; message: string | null; channels: KakaoChannel[] }>(
    `channels?companyId=${companyId}`, '채널 목록을 불러오지 못했습니다.')
}

export function fetchBarobillKakaoTemplates(companyId: number, channelId: string) {
  return getKakao<{ code: number; message: string | null; templates: KakaoTemplate[] }>(
    `templates?companyId=${companyId}&channelId=${encodeURIComponent(channelId)}`, '템플릿 목록을 불러오지 못했습니다.')
}

export function fetchBarobillKakaoManagementUrl(companyId: number, type: KakaoUrlType) {
  return getKakao<{ code: number; message: string | null; url: string | null }>(
    `management-url?companyId=${companyId}&type=${type}`, '관리 페이지 주소를 불러오지 못했습니다.')
}

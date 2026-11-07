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

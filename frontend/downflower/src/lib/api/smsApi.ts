import { PageResult } from '@/types/page'

export type SmsMessage = {
  id: number
  orderId: number
  receiver: string
  status: 'SUCCESS' | 'FAIL'
  failReason: string | null
  aligoMsgId: string | null
  sentAt: string
}

function authHeaders(): HeadersInit {
  const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  }
}

export async function fetchSmsMessages(): Promise<SmsMessage[]> {
  const res = await fetch('/api/messages?page=0&size=10000', { headers: authHeaders() })
  if (!res.ok) throw new Error('발송내역을 불러오지 못했습니다.')
  const data: PageResult<SmsMessage> = await res.json()
  return data.content
}

export async function fetchSmsMessagesPaged(page = 0, size = 20): Promise<PageResult<SmsMessage>> {
  const res = await fetch(`/api/messages?page=${page}&size=${size}`, { headers: authHeaders() })
  if (!res.ok) throw new Error('발송내역을 불러오지 못했습니다.')
  return res.json()
}

export async function fetchSmsMessagesByOrder(orderId: number): Promise<SmsMessage[]> {
  const res = await fetch(`/api/messages/order/${orderId}`, { headers: authHeaders() })
  if (!res.ok) throw new Error('발송내역을 불러오지 못했습니다.')
  return res.json()
}

export async function resendMms(orderId: number): Promise<void> {
  const res = await fetch(`/api/orders/${orderId}/resend`, {
    method: 'POST',
    headers: authHeaders(),
  })
  if (!res.ok) {
    const data = await res.json().catch(() => ({}))
    throw new Error(data.message ?? '재발송에 실패했습니다.')
  }
}

export function won(n: number): string {
  return `${n.toLocaleString('ko-KR')}원`
}

export function formatDateTime(v: string | null): string {
  return v ? v.replace('T', ' ').slice(0, 16) : '-'
}

export function todayKst(): string {
  return new Date(Date.now() + 9 * 60 * 60 * 1000).toISOString().slice(0, 10)
}

const HEX_COLOR = /^#[0-9a-fA-F]{6}$/
export function safeThemeColor(v: string | null): string {
  return v && HEX_COLOR.test(v) ? v : '#15803d'
}

export const ORDER_NO_PATTERN = /^SO-\d{8}-[A-Z0-9]{6}$/

export const ORDER_STATUS_LABEL: Record<string, string> = {
  PENDING: '접수됨 (승인 대기)',
  HELD: '보류 중',
  APPROVED: '승인됨 (진행 중)',
  REJECTED: '거절됨',
  CANCELED: '취소됨',
}

export function digitsOnly(v: string): string {
  return v.replace(/\D/g, '')
}

export function validPhone(v: string): boolean {
  const d = digitsOnly(v)
  return d.length >= 9 && d.length <= 11
}

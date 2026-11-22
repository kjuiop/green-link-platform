import type { AccessStatus } from '@/types/auth'
import type { MyAccess, ShopAdmin, ShopAdminStatus } from '@/types/shopAdmin'
import { ACCOUNTS } from './accounts'
import { delay } from './delay'

const KEY = 'shopAdminMockAdmins'

const SEED: ShopAdmin[] = [
  {
    id: 1, loginId: 'shop', name: '김쇼핑', companyName: '꽃피는날', role: 'SHOP_ADMIN',
    status: 'APPROVED', requestedAt: '2026-11-10 10:05', processedAt: '2026-11-10 14:30', rejectReason: null,
  },
  {
    id: 2, loginId: 'pending', name: '이대기', companyName: '푸른화원', role: 'SHOP_ADMIN',
    status: 'PENDING', requestedAt: '2026-11-20 09:12', processedAt: null, rejectReason: null,
  },
  {
    id: 3, loginId: 'green01', name: '최초록', companyName: '그린플라워', role: 'SHOP_ADMIN',
    status: 'PENDING', requestedAt: '2026-11-21 16:40', processedAt: null, rejectReason: null,
  },
  {
    id: 4, loginId: 'rejected', name: '정거절', companyName: '하늘꽃집', role: 'SHOP_ADMIN',
    status: 'REJECTED', requestedAt: '2026-11-18 11:20', processedAt: '2026-11-18 17:02',
    rejectReason: '소속 확인이 되지 않습니다.',
  },
]

function save(rows: ShopAdmin[]) {
  localStorage.setItem(KEY, JSON.stringify(rows))
}

const STATUSES: ShopAdminStatus[] = ['PENDING', 'APPROVED', 'REJECTED']

function isShopAdmin(value: unknown): value is ShopAdmin {
  if (!value || typeof value !== 'object') return false
  const row = value as Partial<ShopAdmin>
  return typeof row.id === 'number'
    && typeof row.loginId === 'string'
    && typeof row.name === 'string'
    && !!row.status
    && STATUSES.includes(row.status)
}

function load(): ShopAdmin[] {
  try {
    const raw = localStorage.getItem(KEY)
    if (raw) {
      const parsed: unknown = JSON.parse(raw)
      if (Array.isArray(parsed) && parsed.every(isShopAdmin)) return parsed
    }
  } catch {
    // 손상된 값은 시드로 복구한다
  }
  const seed = SEED.map((row) => ({ ...row }))
  save(seed)
  return seed
}

function formatNow(): string {
  const d = new Date()
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}

export function resolveAccessStatus(loginId: string): AccessStatus {
  if (ACCOUNTS[loginId]?.role === 'SUPER_ADMIN') return 'APPROVED'
  return load().find((row) => row.loginId === loginId)?.status ?? 'NONE'
}

export async function mockMyAccess(loginId: string): Promise<MyAccess> {
  await delay(150)
  const row = load().find((r) => r.loginId === loginId)
  return {
    accessStatus: resolveAccessStatus(loginId),
    rejectReason: row?.status === 'REJECTED' ? row.rejectReason : null,
  }
}

export async function mockSubmitRequest(loginId: string): Promise<void> {
  await delay()
  const account = ACCOUNTS[loginId]
  if (!account) throw new Error('계정 정보를 찾을 수 없습니다.')
  const rows = load()
  const row = rows.find((r) => r.loginId === loginId)
  const now = formatNow()
  if (row) {
    if (row.status !== 'REJECTED') throw new Error('이미 신청되었거나 승인된 계정입니다.')
    row.status = 'PENDING'
    row.requestedAt = now
    row.processedAt = null
    row.rejectReason = null
  } else {
    rows.push({
      id: Math.max(0, ...rows.map((r) => r.id)) + 1,
      loginId,
      name: account.name,
      companyName: account.companyName,
      role: account.role,
      status: 'PENDING',
      requestedAt: now,
      processedAt: null,
      rejectReason: null,
    })
  }
  save(rows)
}

export async function mockListAdmins(status?: ShopAdminStatus): Promise<ShopAdmin[]> {
  await delay()
  return load()
    .filter((row) => !status || row.status === status)
    .sort((a, b) => b.id - a.id)
}

function findPending(rows: ShopAdmin[], id: number): ShopAdmin {
  const row = rows.find((r) => r.id === id)
  if (!row) throw new Error('대상을 찾을 수 없습니다.')
  if (row.status !== 'PENDING') throw new Error('대기 중인 신청만 처리할 수 있습니다.')
  return row
}

export async function mockApprove(id: number): Promise<void> {
  await delay()
  const rows = load()
  const row = findPending(rows, id)
  row.status = 'APPROVED'
  row.processedAt = formatNow()
  row.rejectReason = null
  save(rows)
}

export async function mockReject(id: number, reason: string): Promise<void> {
  await delay()
  const trimmed = reason.trim()
  if (!trimmed) throw new Error('거절 사유를 입력하세요.')
  const rows = load()
  const row = findPending(rows, id)
  row.status = 'REJECTED'
  row.processedAt = formatNow()
  row.rejectReason = trimmed
  save(rows)
}

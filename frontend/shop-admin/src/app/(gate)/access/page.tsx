'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { fetchMyAccess, submitAccessRequest } from '@/lib/api/accessApi'
import { refreshSession } from '@/lib/api/authApi'
import { clearSession, setSession } from '@/lib/session'
import { useSession } from '@/lib/useSession'
import { markTokenFresh, useTokenRefresh } from '@/lib/useTokenRefresh'
import type { AccessRequestType, AccessStatus, ShopRole } from '@/types/auth'

type GateStatus = Exclude<AccessStatus, 'APPROVED'>

const MESSAGE: Record<GateStatus, { title: string; body: string }> = {
  NONE: {
    title: '쇼핑몰 관리 권한이 없습니다',
    body: '쇼핑몰 연동을 신청하면 최고관리자의 승인 후 이용할 수 있습니다.',
  },
  PENDING: {
    title: '승인 대기 중',
    body: '신청이 접수되었습니다. 승인이 완료되면 이용할 수 있습니다.',
  },
  REJECTED: {
    title: '연동 신청이 거절되었습니다',
    body: '거절 사유를 확인하고 필요하면 다시 신청할 수 있습니다.',
  },
  SITE_DISABLED: {
    title: '쇼핑몰 서비스를 이용할 수 없습니다',
    body: '소속 회사의 쇼핑몰 서비스가 현재 비활성화되어 있습니다. 관리자에게 문의해주세요.',
  },
}

const NONE_BODY: Record<AccessRequestType, string> = {
  COMPANY: '소속 회사의 쇼핑몰 연동을 신청하면 최고관리자의 승인 후 파트너 관리자로 이용할 수 있습니다.',
  JOIN: '이미 등록된 회사에 합류하려면 역할을 선택해 신청하세요. 승인 후 이용할 수 있습니다.',
}

const ROLE_OPTIONS: { value: ShopRole; label: string }[] = [
  { value: 'BRAND_ADMIN', label: '브랜드 관리자' },
  { value: 'PARTNER_ADMIN', label: '파트너 관리자' },
]

export default function AccessPage() {
  const router = useRouter()
  const { session, loading } = useSession()
  useTokenRefresh(!!session)
  const [rejectReason, setRejectReason] = useState<string | null>(null)
  const [requestType, setRequestType] = useState<AccessRequestType | null>(null)
  const [role, setRole] = useState<ShopRole>('BRAND_ADMIN')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [checking, setChecking] = useState(false)
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    if (loading) return
    if (!session) router.replace('/login')
    else if (session.accessStatus === 'APPROVED') router.replace('/dashboard')
  }, [loading, session, router])

  useEffect(() => {
    if (!session || session.accessStatus === 'APPROVED') return
    let cancelled = false
    fetchMyAccess()
      .then(async (result) => {
        if (cancelled) return
        setRejectReason(result.rejectReason)
        setRequestType(result.requestType)
        if (result.requestedRole && result.requestedRole !== 'SUPER_ADMIN') setRole(result.requestedRole)
        setError('')
        if (result.accessStatus === 'APPROVED') {
          // 승인 직후 기존 LIMITED 토큰을 FULL 토큰으로 교체해야 메뉴 등 전체 API를 호출할 수 있다.
          const fresh = await refreshSession()
          markTokenFresh()
          if (!cancelled) setSession(fresh)
        } else if (result.accessStatus !== session.accessStatus) {
          setSession({ ...session, accessStatus: result.accessStatus })
        }
      })
      .catch(() => { if (!cancelled) setError('신청 상태를 확인하지 못했습니다.') })
      .finally(() => { if (!cancelled) setChecking(false) })
    return () => { cancelled = true }
  }, [session, reloadKey])

  if (loading || !session) return null
  const status = session.accessStatus
  if (status === 'APPROVED') return null

  const currentSession = session
  const message = MESSAGE[status]
  const body = status === 'NONE' && requestType ? NONE_BODY[requestType] : message.body
  const canApply = (status === 'NONE' || status === 'REJECTED') && requestType !== null

  async function handleSubmit() {
    if (submitting) return
    setError('')
    setSubmitting(true)
    try {
      await submitAccessRequest(requestType === 'JOIN' ? role : undefined)
      setSession({ ...currentSession, accessStatus: 'PENDING' })
    } catch (e) {
      setError(e instanceof Error ? e.message : '신청에 실패했습니다.')
    } finally {
      setSubmitting(false)
    }
  }

  function handleRefresh() {
    if (checking) return
    setChecking(true)
    setReloadKey((key) => key + 1)
  }

  function handleLogout() {
    clearSession()
    router.replace('/login')
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 p-4">
      <div className="w-full max-w-sm space-y-4">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-green-700">🛍️ 쇼핑몰 관리</h1>
          <p className="mt-1 text-sm text-gray-500">{session.name} ({session.loginId})</p>
        </div>
        <Card>
          <CardHeader>
            <CardTitle className="text-base">{message.title}</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <p className="text-sm text-gray-600">{body}</p>
            {status === 'REJECTED' && rejectReason && (
              <p className="rounded-md bg-red-50 px-3 py-2 text-xs text-red-600">
                거절 사유: {rejectReason}
              </p>
            )}
            {error && <p className="text-xs text-red-500">{error}</p>}
            {canApply && requestType === 'JOIN' && (
              <select
                aria-label="신청할 역할"
                className="h-9 w-full rounded-md border border-gray-200 bg-white px-3 text-sm"
                value={role}
                onChange={(e) => setRole(e.target.value as ShopRole)}
              >
                {ROLE_OPTIONS.map((o) => (
                  <option key={o.value} value={o.value}>{o.label}</option>
                ))}
              </select>
            )}
            {canApply && (
              <Button
                className="w-full bg-green-700 hover:bg-green-800"
                onClick={handleSubmit}
                disabled={submitting}
              >
                {submitting ? '신청 중...' : status === 'REJECTED' ? '다시 신청' : '연동 신청'}
              </Button>
            )}
            {(status === 'PENDING' || status === 'SITE_DISABLED') && (
              <Button className="w-full" variant="outline" onClick={handleRefresh} disabled={checking}>
                {checking ? '확인 중...' : '상태 새로고침'}
              </Button>
            )}
            <Button variant="ghost" className="w-full" onClick={handleLogout}>
              로그아웃
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

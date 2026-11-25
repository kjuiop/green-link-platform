'use client'

import { Fragment, useEffect, useState } from 'react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { approveShopAdmin, fetchShopAdmins, rejectShopAdmin } from '@/lib/api/shopAdminApi'
import { cn } from '@/lib/utils'
import type { ShopRole } from '@/types/auth'
import type { ShopAdmin, ShopAdminStatus } from '@/types/shopAdmin'

type Filter = ShopAdminStatus | 'ALL'

const FILTERS: { value: Filter; label: string }[] = [
  { value: 'PENDING', label: '대기' },
  { value: 'APPROVED', label: '승인' },
  { value: 'REJECTED', label: '거절' },
  { value: 'ALL', label: '전체' },
]

const STATUS_LABEL: Record<ShopAdminStatus, string> = {
  PENDING: '대기',
  APPROVED: '승인',
  REJECTED: '거절',
}

const STATUS_STYLE: Record<ShopAdminStatus, string> = {
  PENDING: 'bg-yellow-100 text-yellow-700',
  APPROVED: 'bg-green-100 text-green-700',
  REJECTED: 'bg-red-100 text-red-600',
}

const ROLE_LABEL: Record<ShopRole, string> = {
  SUPER_ADMIN: '슈퍼관리자',
  PARTNER_ADMIN: '파트너 관리자',
  BRAND_ADMIN: '브랜드 관리자',
}

const EMPTY_TEXT: Record<Filter, string> = {
  PENDING: '대기 중인 신청이 없습니다.',
  APPROVED: '승인된 관리자가 없습니다.',
  REJECTED: '거절된 신청이 없습니다.',
  ALL: '등록된 관리자가 없습니다.',
}

function formatDateTime(value: string): string {
  return value.replace('T', ' ').slice(0, 16)
}

export default function AdminsPage() {
  const [filter, setFilter] = useState<Filter>('PENDING')
  const [admins, setAdmins] = useState<ShopAdmin[] | null>(null)
  const [loadError, setLoadError] = useState(false)
  const [reloadKey, setReloadKey] = useState(0)
  const [busyId, setBusyId] = useState<number | null>(null)
  const [rejectingId, setRejectingId] = useState<number | null>(null)
  const [reason, setReason] = useState('')
  const [actionError, setActionError] = useState('')
  const [grantRoles, setGrantRoles] = useState<Record<number, ShopRole>>({})

  useEffect(() => {
    let cancelled = false
    fetchShopAdmins(filter === 'ALL' ? undefined : filter)
      .then((result) => {
        if (cancelled) return
        setAdmins(result)
        setLoadError(false)
      })
      .catch(() => { if (!cancelled) setLoadError(true) })
    return () => { cancelled = true }
  }, [filter, reloadKey])

  function changeFilter(next: Filter) {
    if (next === filter) return
    setFilter(next)
    setAdmins(null)
    setLoadError(false)
    setRejectingId(null)
    setReason('')
    setActionError('')
  }

  async function handleApprove(admin: ShopAdmin) {
    if (busyId !== null) return
    setActionError('')
    setBusyId(admin.id)
    try {
      await approveShopAdmin(admin.id, grantRoles[admin.id] ?? admin.requestedRole)
      setReloadKey((key) => key + 1)
    } catch (e) {
      setActionError(e instanceof Error ? e.message : '승인에 실패했습니다.')
    } finally {
      setBusyId(null)
    }
  }

  async function handleReject(id: number) {
    if (busyId !== null) return
    if (!reason.trim()) {
      setActionError('거절 사유를 입력하세요.')
      return
    }
    setActionError('')
    setBusyId(id)
    try {
      await rejectShopAdmin(id, reason)
      setRejectingId(null)
      setReason('')
      setReloadKey((key) => key + 1)
    } catch (e) {
      setActionError(e instanceof Error ? e.message : '거절에 실패했습니다.')
    } finally {
      setBusyId(null)
    }
  }

  function startReject(id: number) {
    setRejectingId(id)
    setReason('')
    setActionError('')
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h1 className="text-xl font-bold">관리자 관리</h1>
        <div className="flex gap-1">
          {FILTERS.map((f) => (
            <Button
              key={f.value}
              size="sm"
              variant={filter === f.value ? 'default' : 'outline'}
              aria-pressed={filter === f.value}
              className={cn(filter === f.value && 'bg-green-700 hover:bg-green-800')}
              onClick={() => changeFilter(f.value)}
            >
              {f.label}
            </Button>
          ))}
        </div>
      </div>

      {actionError && <p className="text-xs text-red-500">{actionError}</p>}

      <Card>
        <CardContent>
          {loadError ? (
            <p className="py-8 text-center text-sm text-red-500">
              관리자 목록을 불러오지 못했습니다.
            </p>
          ) : admins === null ? (
            <div className="space-y-2">
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-full" />
            </div>
          ) : admins.length === 0 ? (
            <p className="py-8 text-center text-sm text-gray-400">{EMPTY_TEXT[filter]}</p>
          ) : (
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>이름 / 아이디</TableHead>
                    <TableHead>소속</TableHead>
                    <TableHead>역할</TableHead>
                    <TableHead>신청일시</TableHead>
                    <TableHead>상태</TableHead>
                    <TableHead>처리일시</TableHead>
                    <TableHead className="text-right">동작</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {admins.map((admin) => (
                    <Fragment key={admin.id}>
                      <TableRow>
                        <TableCell className="whitespace-nowrap">
                          <p className="font-medium">{admin.name ?? '-'}</p>
                          <p className="font-mono text-xs text-gray-500">{admin.loginId ?? `#${admin.adminId}`}</p>
                        </TableCell>
                        <TableCell className="whitespace-nowrap">{admin.companyName ?? `#${admin.companyId}`}</TableCell>
                        <TableCell className="whitespace-nowrap">{ROLE_LABEL[admin.role ?? admin.requestedRole]}{admin.role === null && <span className="text-xs text-gray-400"> (신청)</span>}</TableCell>
                        <TableCell className="whitespace-nowrap text-xs text-gray-500">{formatDateTime(admin.requestedAt)}</TableCell>
                        <TableCell>
                          <Badge className={cn('whitespace-nowrap', STATUS_STYLE[admin.status])}>
                            {STATUS_LABEL[admin.status]}
                          </Badge>
                          {admin.status === 'REJECTED' && admin.rejectReason && (
                            <p className="mt-1 max-w-48 text-xs text-gray-500">{admin.rejectReason}</p>
                          )}
                        </TableCell>
                        <TableCell className="whitespace-nowrap text-xs text-gray-500">{admin.processedAt ? formatDateTime(admin.processedAt) : '-'}</TableCell>
                        <TableCell className="whitespace-nowrap text-right">
                          {admin.status === 'PENDING' && (
                            <div className="flex items-center justify-end gap-1">
                              {admin.requestedRole === 'PARTNER_ADMIN' && (
                                <select
                                  aria-label="부여할 역할"
                                  className="h-8 rounded-md border border-gray-200 bg-white px-2 text-xs"
                                  disabled={busyId !== null}
                                  value={grantRoles[admin.id] ?? admin.requestedRole}
                                  onChange={(e) => setGrantRoles((prev) => ({ ...prev, [admin.id]: e.target.value as ShopRole }))}
                                >
                                  <option value="PARTNER_ADMIN">{ROLE_LABEL.PARTNER_ADMIN}</option>
                                  <option value="BRAND_ADMIN">{ROLE_LABEL.BRAND_ADMIN}</option>
                                </select>
                              )}
                              <Button
                                size="sm"
                                className="bg-green-700 hover:bg-green-800"
                                disabled={busyId !== null}
                                onClick={() => handleApprove(admin)}
                              >
                                승인
                              </Button>
                              <Button
                                size="sm"
                                variant="outline"
                                disabled={busyId !== null}
                                onClick={() => startReject(admin.id)}
                              >
                                거절
                              </Button>
                            </div>
                          )}
                        </TableCell>
                      </TableRow>
                      {rejectingId === admin.id && (
                        <TableRow>
                          <TableCell colSpan={7} className="bg-gray-50">
                            <div className="flex flex-wrap items-center gap-2">
                              <Input
                                className="min-w-48 flex-1 bg-white"
                                placeholder="거절 사유를 입력하세요"
                                aria-label="거절 사유"
                                maxLength={200}
                                value={reason}
                                onChange={(e) => { setReason(e.target.value); setActionError('') }}
                                onKeyDown={(e) => e.key === 'Enter' && handleReject(admin.id)}
                              />
                              <Button
                                size="sm"
                                disabled={busyId !== null}
                                onClick={() => handleReject(admin.id)}
                              >
                                {busyId === admin.id ? '처리 중...' : '거절 확인'}
                              </Button>
                              <Button
                                size="sm"
                                variant="ghost"
                                disabled={busyId !== null}
                                onClick={() => { setRejectingId(null); setReason(''); setActionError('') }}
                              >
                                취소
                              </Button>
                            </div>
                          </TableCell>
                        </TableRow>
                      )}
                    </Fragment>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}

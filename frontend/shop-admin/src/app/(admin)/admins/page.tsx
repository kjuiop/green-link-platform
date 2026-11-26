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
import { fetchBrands } from '@/lib/api/brandApi'
import {
  approveShopAdmin,
  assignShopAdminBrands,
  changeShopAdminRole,
  createShopAdmin,
  fetchCompanies,
  fetchShopAdmins,
  reissuePasswordSetup,
  rejectShopAdmin,
  removeShopAdmin,
} from '@/lib/api/shopAdminApi'
import { useSession } from '@/lib/useSession'
import { cn } from '@/lib/utils'
import type { ShopRole } from '@/types/auth'
import type { Brand, CompanyOption } from '@/types/brand'
import type { ShopAdmin, ShopAdminStatus } from '@/types/shopAdmin'

type Filter = ShopAdminStatus | 'ALL'
type Panel = { id: number; kind: 'reject' | 'assign' | 'role' }

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
  REMOVED: '제거',
}

const STATUS_STYLE: Record<ShopAdminStatus, string> = {
  PENDING: 'bg-yellow-100 text-yellow-700',
  APPROVED: 'bg-green-100 text-green-700',
  REJECTED: 'bg-red-100 text-red-600',
  REMOVED: 'bg-gray-100 text-gray-500',
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
  REMOVED: '제거된 관리자가 없습니다.',
  ALL: '등록된 관리자가 없습니다.',
}

const EMPTY_FORM = { loginId: '', name: '', phone: '', email: '', role: 'BRAND_ADMIN' as CreateRole, company: '' }

type CreateRole = 'PARTNER_ADMIN' | 'BRAND_ADMIN'

function validateCreate(f: typeof EMPTY_FORM, isSuper: boolean, brandCount: number): string {
  if (isSuper && !f.company) return '회사를 선택하세요.'
  if (!/^[A-Za-z0-9_.-]{4,30}$/.test(f.loginId)) return '아이디는 영문·숫자·._- 4~30자로 입력하세요.'
  if (!f.name.trim() || f.name.length > 50) return '이름을 입력하세요 (50자 이내).'
  if (!/^[0-9-]{9,20}$/.test(f.phone)) return '연락처를 숫자와 하이픈으로 입력하세요.'
  if (f.email.length > 100) return '이메일은 100자 이내로 입력하세요.'
  if (f.role === 'BRAND_ADMIN' && brandCount === 0) return '브랜드 관리자는 브랜드를 1개 이상 선택해야 합니다.'
  return ''
}

const SELECT_CLASS = 'h-8 rounded-md border border-gray-200 bg-white px-2 text-xs'
const COLUMN_COUNT = 8

function formatDateTime(value: string): string {
  return value.replace('T', ' ').slice(0, 16)
}

function toggle(list: number[], id: number): number[] {
  return list.includes(id) ? list.filter((v) => v !== id) : [...list, id]
}

export default function AdminsPage() {
  const { session } = useSession()
  const isSuper = session?.role === 'SUPER_ADMIN'

  const [filter, setFilter] = useState<Filter>('PENDING')
  const [companyFilter, setCompanyFilter] = useState('')
  const [companies, setCompanies] = useState<CompanyOption[]>([])
  const [brands, setBrands] = useState<Brand[]>([])
  const [admins, setAdmins] = useState<ShopAdmin[] | null>(null)
  const [loadError, setLoadError] = useState(false)
  const [reloadKey, setReloadKey] = useState(0)
  const [busyId, setBusyId] = useState<number | null>(null)
  const [panel, setPanel] = useState<Panel | null>(null)
  const [reason, setReason] = useState('')
  const [actionError, setActionError] = useState('')
  const [grantRoles, setGrantRoles] = useState<Record<number, ShopRole>>({})
  const [brandSelections, setBrandSelections] = useState<Record<number, number[]>>({})
  const [roleDrafts, setRoleDrafts] = useState<Record<number, ShopRole>>({})
  const [notice, setNotice] = useState('')
  const [showCreate, setShowCreate] = useState(false)
  const [form, setForm] = useState(EMPTY_FORM)
  const [createBrandIds, setCreateBrandIds] = useState<number[]>([])
  const [createBrands, setCreateBrands] = useState<Brand[]>([])

  const companyId = isSuper && companyFilter ? Number(companyFilter) : undefined

  useEffect(() => {
    if (!isSuper) return
    let cancelled = false
    fetchCompanies()
      .then((result) => { if (!cancelled) setCompanies(result) })
      .catch(() => { if (!cancelled) setCompanies([]) })
    return () => { cancelled = true }
  }, [isSuper])

  useEffect(() => {
    if (!session) return
    let cancelled = false
    fetchBrands(companyId)
      .then((result) => { if (!cancelled) setBrands(result) })
      .catch(() => { if (!cancelled) setBrands([]) })
    return () => { cancelled = true }
  }, [session, companyId, reloadKey])

  useEffect(() => {
    if (!session) return
    let cancelled = false
    fetchShopAdmins(filter === 'ALL' ? undefined : filter, companyId)
      .then((result) => {
        if (cancelled) return
        setAdmins(result)
        setLoadError(false)
      })
      .catch(() => { if (!cancelled) setLoadError(true) })
    return () => { cancelled = true }
  }, [session, filter, companyId, reloadKey])

  const createCompanyId = isSuper ? (form.company ? Number(form.company) : undefined) : undefined
  const createBrandOptions = createBrands.filter((b) => !isSuper || b.companyId === createCompanyId)

  useEffect(() => {
    if (!showCreate || !session || (isSuper && createCompanyId === undefined)) return
    let cancelled = false
    fetchBrands(createCompanyId)
      .then((result) => { if (!cancelled) setCreateBrands(result) })
      .catch(() => { if (!cancelled) setCreateBrands([]) })
    return () => { cancelled = true }
  }, [showCreate, session, isSuper, createCompanyId])

  function resetView() {
    setAdmins(null)
    setLoadError(false)
    setPanel(null)
    setReason('')
    setActionError('')
  }

  function changeFilter(next: Filter) {
    if (next === filter) return
    setFilter(next)
    resetView()
  }

  function changeCompanyFilter(next: string) {
    if (next === companyFilter) return
    setCompanyFilter(next)
    resetView()
  }

  const brandsOf = (admin: ShopAdmin) => brands.filter((b) => b.companyId === admin.companyId)
  const grantedRoleOf = (admin: ShopAdmin) => grantRoles[admin.id] ?? admin.requestedRole
  const selectionOf = (admin: ShopAdmin) => brandSelections[admin.id] ?? admin.brandIds
  const brandNames = (admin: ShopAdmin) =>
    admin.brandIds.map((id) => brands.find((b) => b.id === id)?.name ?? `#${id}`).join(', ')

  async function run(id: number, action: () => Promise<void>, fallback: string): Promise<boolean> {
    if (busyId !== null) return false
    setActionError('')
    setNotice('')
    setBusyId(id)
    try {
      await action()
      setReloadKey((key) => key + 1)
      return true
    } catch (e) {
      setActionError(e instanceof Error ? e.message : fallback)
      return false
    } finally {
      setBusyId(null)
    }
  }

  function updateForm(patch: Partial<typeof EMPTY_FORM>) {
    setForm((prev) => ({ ...prev, ...patch }))
    setActionError('')
  }

  function closeCreate() {
    setShowCreate(false)
    setForm(EMPTY_FORM)
    setCreateBrandIds([])
    setActionError('')
  }

  async function handleCreate() {
    const brandIds = form.role === 'BRAND_ADMIN' ? createBrandIds : []
    const message = validateCreate(form, isSuper, brandIds.length)
    if (message) {
      setActionError(message)
      return
    }
    const ok = await run(0, async () => {
      await createShopAdmin({
        loginId: form.loginId,
        name: form.name.trim(),
        phone: form.phone,
        email: form.email.trim() || undefined,
        role: form.role,
        companyId: isSuper ? Number(form.company) : undefined,
        brandIds: form.role === 'BRAND_ADMIN' ? brandIds : undefined,
      })
    }, '계정을 만들지 못했습니다.')
    if (ok) {
      closeCreate()
      setNotice('계정을 만들었습니다. 해당 담당자가 아이디와 등록한 연락처로 비밀번호를 설정하면 로그인할 수 있습니다 (7일 이내).')
      if (filter !== 'APPROVED' && filter !== 'ALL') changeFilter('APPROVED')
    }
  }

  async function handleReissue(admin: ShopAdmin) {
    const ok = await run(admin.id, () => reissuePasswordSetup(admin.id), '재발급에 실패했습니다.')
    if (ok) setNotice('비밀번호 설정 기간을 7일로 다시 열었습니다. 담당자에게 안내하세요.')
  }

  function handleApprove(admin: ShopAdmin) {
    const role = grantedRoleOf(admin)
    const brandIds = role === 'BRAND_ADMIN' ? brandSelections[admin.id] ?? [] : undefined
    return run(admin.id, () => approveShopAdmin(admin.id, role, brandIds), '승인에 실패했습니다.')
  }

  async function handleReject(id: number) {
    if (!reason.trim()) {
      setActionError('거절 사유를 입력하세요.')
      return
    }
    const ok = await run(id, () => rejectShopAdmin(id, reason), '거절에 실패했습니다.')
    if (ok) {
      setPanel(null)
      setReason('')
    }
  }

  async function handleAssign(admin: ShopAdmin) {
    const ok = await run(
      admin.id,
      () => assignShopAdminBrands(admin.id, selectionOf(admin)),
      '브랜드 배정에 실패했습니다.',
    )
    if (ok) setPanel(null)
  }

  async function handleChangeRole(admin: ShopAdmin, role: ShopRole) {
    const ok = await run(admin.id, () => changeShopAdminRole(admin.id, role), '역할 변경에 실패했습니다.')
    if (ok) setPanel(null)
  }

  function handleRemove(admin: ShopAdmin) {
    const label = admin.name ?? admin.loginId ?? `#${admin.adminId}`
    if (!window.confirm(`'${label}' 님을 관리자에서 제거할까요?`)) return
    return run(admin.id, () => removeShopAdmin(admin.id), '제거에 실패했습니다.')
  }

  function openPanel(next: Panel, admin?: ShopAdmin) {
    setActionError('')
    setReason('')
    if (admin && next.kind === 'assign') {
      setBrandSelections((prev) => ({ ...prev, [admin.id]: admin.brandIds }))
    }
    if (admin && next.kind === 'role') {
      setRoleDrafts((prev) => ({ ...prev, [admin.id]: admin.role ?? admin.requestedRole }))
    }
    setPanel(next)
  }

  function closePanel() {
    setPanel(null)
    setReason('')
    setActionError('')
  }

  function renderBrandChecks(admin: ShopAdmin, selected: number[], onToggle: (id: number) => void) {
    const options = brandsOf(admin)
    if (options.length === 0) {
      return <p className="text-xs text-gray-500">이 회사에 등록된 브랜드가 없습니다. 브랜드 관리에서 먼저 추가하세요.</p>
    }
    return (
      <div className="flex flex-wrap gap-x-4 gap-y-1">
        {options.map((b) => (
          <label key={b.id} className="flex items-center gap-1 text-sm">
            <input
              type="checkbox"
              checked={selected.includes(b.id)}
              disabled={busyId !== null}
              onChange={() => onToggle(b.id)}
            />
            {b.name}
          </label>
        ))}
      </div>
    )
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h1 className="text-xl font-bold">관리자 관리</h1>
        <div className="flex flex-wrap items-center gap-2">
          {isSuper && (
            <select
              aria-label="회사 필터"
              className="h-8 rounded-md border border-gray-200 bg-white px-2 text-sm"
              value={companyFilter}
              onChange={(e) => changeCompanyFilter(e.target.value)}
            >
              <option value="">전체 회사</option>
              {companies.map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
          )}
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
      </div>

      {notice && <p className="text-xs text-green-700">{notice}</p>}

      {showCreate ? (
        <Card>
          <CardContent className="space-y-3">
            <p className="text-sm font-medium">관리자 계정 추가</p>
            <div className="grid gap-2 sm:grid-cols-2">
              {isSuper && (
                <select
                  aria-label="소속 회사"
                  className="h-9 rounded-md border border-gray-200 bg-white px-2 text-sm"
                  value={form.company}
                  disabled={busyId !== null}
                  onChange={(e) => { updateForm({ company: e.target.value }); setCreateBrandIds([]) }}
                >
                  <option value="">회사 선택</option>
                  {companies.map((c) => (
                    <option key={c.id} value={c.id}>{c.name}</option>
                  ))}
                </select>
              )}
              <select
                aria-label="역할"
                className="h-9 rounded-md border border-gray-200 bg-white px-2 text-sm"
                value={form.role}
                disabled={busyId !== null}
                onChange={(e) => updateForm({ role: e.target.value as CreateRole })}
              >
                <option value="BRAND_ADMIN">{ROLE_LABEL.BRAND_ADMIN}</option>
                <option value="PARTNER_ADMIN">{ROLE_LABEL.PARTNER_ADMIN}</option>
              </select>
              <Input
                placeholder="아이디 (영문·숫자 4~30자)"
                aria-label="아이디"
                maxLength={30}
                value={form.loginId}
                disabled={busyId !== null}
                onChange={(e) => updateForm({ loginId: e.target.value })}
              />
              <Input
                placeholder="이름"
                aria-label="이름"
                maxLength={50}
                value={form.name}
                disabled={busyId !== null}
                onChange={(e) => updateForm({ name: e.target.value })}
              />
              <Input
                type="tel"
                placeholder="연락처 (비밀번호 설정 시 본인 확인에 사용)"
                aria-label="연락처"
                maxLength={20}
                value={form.phone}
                disabled={busyId !== null}
                onChange={(e) => updateForm({ phone: e.target.value })}
              />
              <Input
                type="email"
                placeholder="이메일 (선택)"
                aria-label="이메일"
                maxLength={100}
                value={form.email}
                disabled={busyId !== null}
                onChange={(e) => updateForm({ email: e.target.value })}
              />
            </div>
            {form.role === 'BRAND_ADMIN' && (
              <div>
                <p className="mb-1 text-xs font-medium text-gray-600">담당 브랜드 (1개 이상 선택)</p>
                {isSuper && !form.company ? (
                  <p className="text-xs text-gray-500">회사를 먼저 선택하세요.</p>
                ) : createBrandOptions.length === 0 ? (
                  <p className="text-xs text-gray-500">이 회사에 등록된 브랜드가 없습니다. 브랜드 관리에서 먼저 추가하세요.</p>
                ) : (
                  <div className="flex flex-wrap gap-x-4 gap-y-1">
                    {createBrandOptions.map((b) => (
                      <label key={b.id} className="flex items-center gap-1 text-sm">
                        <input
                          type="checkbox"
                          checked={createBrandIds.includes(b.id)}
                          disabled={busyId !== null}
                          onChange={() => setCreateBrandIds((prev) => toggle(prev, b.id))}
                        />
                        {b.name}
                      </label>
                    ))}
                  </div>
                )}
              </div>
            )}
            <div className="flex items-center gap-2">
              <Button size="sm" className="bg-green-700 hover:bg-green-800" disabled={busyId !== null} onClick={handleCreate}>
                {busyId === 0 ? '만드는 중...' : '계정 만들기'}
              </Button>
              <Button size="sm" variant="ghost" disabled={busyId !== null} onClick={closeCreate}>취소</Button>
              <span className="text-xs text-gray-500">비밀번호는 담당자가 처음 로그인할 때 직접 설정합니다.</span>
            </div>
          </CardContent>
        </Card>
      ) : (
        <div>
          <Button size="sm" variant="outline" onClick={() => { setNotice(''); setActionError(''); setShowCreate(true) }}>
            계정 추가
          </Button>
        </div>
      )}

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
                    <TableHead>브랜드</TableHead>
                    <TableHead>신청일시</TableHead>
                    <TableHead>상태</TableHead>
                    <TableHead>처리일시</TableHead>
                    <TableHead className="text-right">동작</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {admins.map((admin) => {
                    const isPending = admin.status === 'PENDING'
                    const isApproved = admin.status === 'APPROVED'
                    const isBrandAdmin = admin.role === 'BRAND_ADMIN'
                    const granted = grantedRoleOf(admin)
                    const pendingSelection = brandSelections[admin.id] ?? []
                    const approveBlocked = isPending && granted === 'BRAND_ADMIN' && pendingSelection.length === 0
                    const draftRole = roleDrafts[admin.id] ?? admin.role ?? admin.requestedRole

                    return (
                      <Fragment key={admin.id}>
                        <TableRow>
                          <TableCell className="whitespace-nowrap">
                            <p className="font-medium">{admin.name ?? '-'}</p>
                            <p className="font-mono text-xs text-gray-500">{admin.loginId ?? `#${admin.adminId}`}</p>
                          </TableCell>
                          <TableCell className="whitespace-nowrap">{admin.companyName ?? `#${admin.companyId}`}</TableCell>
                          <TableCell className="whitespace-nowrap">{ROLE_LABEL[admin.role ?? admin.requestedRole]}{admin.role === null && <span className="text-xs text-gray-400"> (신청)</span>}</TableCell>
                          <TableCell className="max-w-48 text-xs text-gray-600">
                            {isApproved && isBrandAdmin ? (admin.brandIds.length > 0 ? brandNames(admin) : <span className="text-gray-400">미배정</span>) : '-'}
                          </TableCell>
                          <TableCell className="whitespace-nowrap text-xs text-gray-500">{formatDateTime(admin.requestedAt)}</TableCell>
                          <TableCell>
                            <Badge className={cn('whitespace-nowrap', STATUS_STYLE[admin.status])}>
                              {STATUS_LABEL[admin.status]}
                            </Badge>
                            {isApproved && admin.passwordSetupPending && (
                              <Badge className="mt-1 whitespace-nowrap bg-orange-100 text-orange-700">비밀번호 미설정</Badge>
                            )}
                            {admin.status === 'REJECTED' && admin.rejectReason && (
                              <p className="mt-1 max-w-48 text-xs text-gray-500">{admin.rejectReason}</p>
                            )}
                          </TableCell>
                          <TableCell className="whitespace-nowrap text-xs text-gray-500">{admin.processedAt ? formatDateTime(admin.processedAt) : '-'}</TableCell>
                          <TableCell className="whitespace-nowrap text-right">
                            {isPending && (
                              <div className="flex items-center justify-end gap-1">
                                {admin.requestedRole === 'PARTNER_ADMIN' && (
                                  <select
                                    aria-label="부여할 역할"
                                    className={SELECT_CLASS}
                                    disabled={busyId !== null}
                                    value={granted}
                                    onChange={(e) => setGrantRoles((prev) => ({ ...prev, [admin.id]: e.target.value as ShopRole }))}
                                  >
                                    <option value="PARTNER_ADMIN">{ROLE_LABEL.PARTNER_ADMIN}</option>
                                    <option value="BRAND_ADMIN">{ROLE_LABEL.BRAND_ADMIN}</option>
                                  </select>
                                )}
                                <Button
                                  size="sm"
                                  className="bg-green-700 hover:bg-green-800"
                                  disabled={busyId !== null || approveBlocked}
                                  title={approveBlocked ? '브랜드 관리자는 브랜드를 1개 이상 선택해야 합니다.' : undefined}
                                  onClick={() => handleApprove(admin)}
                                >
                                  승인
                                </Button>
                                <Button
                                  size="sm"
                                  variant="outline"
                                  disabled={busyId !== null}
                                  onClick={() => openPanel({ id: admin.id, kind: 'reject' })}
                                >
                                  거절
                                </Button>
                              </div>
                            )}
                            {isApproved && (
                              <div className="flex items-center justify-end gap-1">
                                {admin.passwordSetupPending && (
                                  <Button
                                    size="sm"
                                    variant="outline"
                                    disabled={busyId !== null}
                                    onClick={() => handleReissue(admin)}
                                  >
                                    설정 재발급
                                  </Button>
                                )}
                                {isBrandAdmin && (
                                  <Button
                                    size="sm"
                                    variant="outline"
                                    disabled={busyId !== null}
                                    onClick={() => openPanel({ id: admin.id, kind: 'assign' }, admin)}
                                  >
                                    브랜드 배정
                                  </Button>
                                )}
                                <Button
                                  size="sm"
                                  variant="outline"
                                  disabled={busyId !== null}
                                  onClick={() => openPanel({ id: admin.id, kind: 'role' }, admin)}
                                >
                                  역할 변경
                                </Button>
                                <Button
                                  size="sm"
                                  variant="outline"
                                  disabled={busyId !== null}
                                  onClick={() => handleRemove(admin)}
                                >
                                  제거
                                </Button>
                              </div>
                            )}
                          </TableCell>
                        </TableRow>

                        {isPending && granted === 'BRAND_ADMIN' && (
                          <TableRow>
                            <TableCell colSpan={COLUMN_COUNT} className="bg-gray-50">
                              <p className="mb-1 text-xs font-medium text-gray-600">담당 브랜드 (1개 이상 선택)</p>
                              {renderBrandChecks(admin, pendingSelection, (id) =>
                                setBrandSelections((prev) => ({ ...prev, [admin.id]: toggle(prev[admin.id] ?? [], id) })))}
                            </TableCell>
                          </TableRow>
                        )}

                        {panel?.id === admin.id && panel.kind === 'reject' && (
                          <TableRow>
                            <TableCell colSpan={COLUMN_COUNT} className="bg-gray-50">
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
                                <Button size="sm" disabled={busyId !== null} onClick={() => handleReject(admin.id)}>
                                  {busyId === admin.id ? '처리 중...' : '거절 확인'}
                                </Button>
                                <Button size="sm" variant="ghost" disabled={busyId !== null} onClick={closePanel}>
                                  취소
                                </Button>
                              </div>
                            </TableCell>
                          </TableRow>
                        )}

                        {panel?.id === admin.id && panel.kind === 'assign' && (
                          <TableRow>
                            <TableCell colSpan={COLUMN_COUNT} className="bg-gray-50">
                              <p className="mb-1 text-xs font-medium text-gray-600">담당 브랜드 (선택 없음 = 배정 해제)</p>
                              <div className="flex flex-wrap items-center gap-3">
                                {renderBrandChecks(admin, selectionOf(admin), (id) =>
                                  setBrandSelections((prev) => ({ ...prev, [admin.id]: toggle(prev[admin.id] ?? admin.brandIds, id) })))}
                                <Button size="sm" disabled={busyId !== null} onClick={() => handleAssign(admin)}>
                                  {busyId === admin.id ? '처리 중...' : '저장'}
                                </Button>
                                <Button size="sm" variant="ghost" disabled={busyId !== null} onClick={closePanel}>
                                  취소
                                </Button>
                              </div>
                            </TableCell>
                          </TableRow>
                        )}

                        {panel?.id === admin.id && panel.kind === 'role' && (
                          <TableRow>
                            <TableCell colSpan={COLUMN_COUNT} className="bg-gray-50">
                              <div className="flex flex-wrap items-center gap-2">
                                <select
                                  aria-label="변경할 역할"
                                  className={SELECT_CLASS}
                                  disabled={busyId !== null}
                                  value={draftRole}
                                  onChange={(e) => setRoleDrafts((prev) => ({ ...prev, [admin.id]: e.target.value as ShopRole }))}
                                >
                                  <option value="PARTNER_ADMIN">{ROLE_LABEL.PARTNER_ADMIN}</option>
                                  <option value="BRAND_ADMIN">{ROLE_LABEL.BRAND_ADMIN}</option>
                                </select>
                                <Button
                                  size="sm"
                                  disabled={busyId !== null || draftRole === admin.role}
                                  onClick={() => handleChangeRole(admin, draftRole)}
                                >
                                  {busyId === admin.id ? '처리 중...' : '변경'}
                                </Button>
                                <Button size="sm" variant="ghost" disabled={busyId !== null} onClick={closePanel}>
                                  취소
                                </Button>
                                <span className="text-xs text-gray-500">역할을 바꾸면 기존 브랜드 배정은 해제됩니다.</span>
                              </div>
                            </TableCell>
                          </TableRow>
                        )}
                      </Fragment>
                    )
                  })}
                </TableBody>
              </Table>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}

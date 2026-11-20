'use client'

import { useEffect, useState } from 'react'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog'
import { Pagination } from '@/components/ui/Pagination'
import { Search } from 'lucide-react'
import { cn } from '@/lib/utils'
import {
  fetchFaxMessages,
  fetchFaxVendors,
  resendFax,
  updateFaxVendor,
  type FaxMessage,
  type FaxStatus,
  type FaxVendor,
} from '@/lib/api/faxApi'
import { PageResult } from '@/types/page'

const PAGE_SIZE = 20

type Tab = 'vendors' | 'messages'

const STATUS_LABEL: Record<FaxStatus, string> = {
  SENDING: '발송 중',
  SUBMITTED: '전송 접수',
  DELIVERED: '전송 완료',
  FAILED: '실패',
}

const STATUS_STYLE: Record<FaxStatus, string> = {
  SENDING: 'bg-gray-100 text-gray-600',
  SUBMITTED: 'bg-blue-50 text-blue-700',
  DELIVERED: 'bg-green-50 text-green-700',
  FAILED: 'bg-red-50 text-red-600',
}

function formatDateTime(v: string | null) {
  if (!v) return '-'
  return v.replace('T', ' ').slice(0, 16)
}

interface Loaded<T> {
  key: string
  data: PageResult<T> | null
  error: string
}

export default function FaxManagementPage() {
  const [tab, setTab] = useState<Tab>('vendors')

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-xl font-bold">팩스관리</h1>
        <p className="mt-1 text-sm text-gray-500">
          수주사가 수주를 승인하면 연동된 수주사의 팩스번호로 주문서가 자동 발송됩니다. 발송 비용은 시스템 바로빌 계정에서 차감됩니다.
        </p>
      </div>

      <div className="flex gap-1 rounded-lg border bg-gray-50 p-1 sm:w-80">
        {([['vendors', '업체 연동 설정'], ['messages', '발송 내역']] as const).map(([value, label]) => (
          <button
            key={value}
            type="button"
            onClick={() => setTab(value)}
            className={cn(
              'flex-1 rounded-md py-1.5 text-sm font-medium transition-colors',
              tab === value ? 'bg-white text-green-700 shadow-sm' : 'text-gray-500 hover:text-gray-700',
            )}
          >
            {label}
          </button>
        ))}
      </div>

      {tab === 'vendors' ? <VendorTab /> : <MessageTab />}
    </div>
  )
}

function VendorTab() {
  const [input, setInput] = useState('')
  const [keyword, setKeyword] = useState('')
  const [page, setPage] = useState(0)
  const [reloadToken, setReloadToken] = useState(0)
  const [loaded, setLoaded] = useState<Loaded<FaxVendor> | null>(null)

  const [editTarget, setEditTarget] = useState<FaxVendor | null>(null)
  const [enabled, setEnabled] = useState(false)
  const [faxNumber, setFaxNumber] = useState('')
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState('')

  const requestKey = `${keyword}|${page}|${reloadToken}`

  useEffect(() => {
    let cancelled = false
    fetchFaxVendors(keyword, page, PAGE_SIZE)
      .then((data) => { if (!cancelled) setLoaded({ key: requestKey, data, error: '' }) })
      .catch((e) => {
        if (!cancelled) setLoaded({ key: requestKey, data: null, error: e instanceof Error ? e.message : '목록을 불러오지 못했습니다.' })
      })
    return () => { cancelled = true }
  }, [keyword, page, requestKey])

  const loading = loaded?.key !== requestKey
  const rows = loaded?.data?.content ?? []
  const error = loading ? '' : loaded?.error ?? ''

  function submitSearch() {
    setPage(0)
    setKeyword(input)
  }

  function openEdit(v: FaxVendor) {
    setEditTarget(v)
    setEnabled(v.faxEnabled)
    setFaxNumber(v.faxNumber ?? '')
    setSaveError('')
  }

  const digits = faxNumber.replace(/[^\d]/g, '')
  const numberValid = !enabled || (digits.length >= 8 && digits.length <= 12)

  async function submit() {
    if (!editTarget || !numberValid) return
    setSaving(true)
    setSaveError('')
    try {
      await updateFaxVendor(editTarget.companyId, enabled, faxNumber.trim())
      setEditTarget(null)
      setReloadToken((t) => t + 1)
    } catch (e) {
      setSaveError(e instanceof Error ? e.message : '저장에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <>
      <div className="flex flex-wrap items-end gap-2 rounded-lg border bg-white p-4">
        <div className="relative">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-gray-400" />
          <Input
            placeholder="업체명 검색"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => { if (e.key === 'Enter') submitSearch() }}
            className="w-48 pl-8"
          />
        </div>
        <Button variant="outline" onClick={submitSearch}>검색</Button>
      </div>

      {error && <p className="text-sm text-red-500">{error}</p>}

      <div className="rounded-lg border bg-white">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>업체명</TableHead>
              <TableHead>사업자번호</TableHead>
              <TableHead>팩스번호</TableHead>
              <TableHead className="text-center">연동</TableHead>
              <TableHead className="text-center">처리</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={5} className="py-10 text-center text-sm text-gray-400">불러오는 중...</TableCell>
              </TableRow>
            ) : rows.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} className="py-10 text-center text-sm text-gray-400">수주사가 없습니다.</TableCell>
              </TableRow>
            ) : (
              rows.map((v) => (
                <TableRow key={v.companyId} className="hover:bg-gray-50">
                  <TableCell className="font-medium">{v.companyName}</TableCell>
                  <TableCell className="text-sm text-gray-500">{v.businessNo ?? '-'}</TableCell>
                  <TableCell className="text-sm text-gray-500">{v.faxNumber || '-'}</TableCell>
                  <TableCell className="text-center">
                    <span className={cn(
                      'rounded px-2 py-0.5 text-xs font-medium',
                      v.faxEnabled ? 'bg-green-50 text-green-700' : 'bg-gray-100 text-gray-500',
                    )}>
                      {v.faxEnabled ? '연동' : '미연동'}
                    </span>
                  </TableCell>
                  <TableCell className="text-center">
                    <Button size="sm" variant="outline" className="h-7 text-xs" onClick={() => openEdit(v)}>
                      수정
                    </Button>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {loaded?.data && (
        <Pagination
          page={page}
          totalPages={loaded.data.totalPages}
          totalElements={loaded.data.totalElements}
          size={PAGE_SIZE}
          onPageChange={setPage}
        />
      )}

      <Dialog open={editTarget !== null} onOpenChange={(open) => { if (!open) setEditTarget(null) }}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>팩스 연동 설정 - {editTarget?.companyName}</DialogTitle>
          </DialogHeader>
          <div className="space-y-3">
            <div className="space-y-1.5">
              <Label>연동 여부</Label>
              <div className="flex gap-4">
                {[true, false].map((v) => (
                  <label key={String(v)} className="flex cursor-pointer items-center gap-2">
                    <input
                      type="radio"
                      name="fax-enabled"
                      checked={enabled === v}
                      onChange={() => setEnabled(v)}
                      className="accent-green-700"
                    />
                    <span className="text-sm">{v ? '연동' : '미연동'}</span>
                  </label>
                ))}
              </div>
            </div>
            <div className="space-y-1">
              <Label htmlFor="fax-number">팩스번호</Label>
              <Input
                id="fax-number"
                placeholder="02-000-0000"
                value={faxNumber}
                onChange={(e) => setFaxNumber(e.target.value)}
              />
              {!numberValid && <p className="text-xs text-red-500">연동하려면 올바른 팩스번호를 입력해 주세요.</p>}
              <p className="text-xs text-gray-500">
                연동 상태에서 수주사가 수주를 승인하면 이 번호로 주문서가 자동 발송됩니다. 이미 수주 완료된 주문에는 소급 발송되지 않습니다.
              </p>
            </div>
            {saveError && <p className="text-sm text-red-500">{saveError}</p>}
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setEditTarget(null)} disabled={saving}>취소</Button>
            <Button className="bg-green-600 hover:bg-green-700" onClick={submit} disabled={!numberValid || saving}>
              {saving ? '저장 중...' : '저장'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  )
}

const STATUS_FILTERS: { value: FaxStatus | ''; label: string }[] = [
  { value: '', label: '전체' },
  { value: 'SENDING', label: STATUS_LABEL.SENDING },
  { value: 'SUBMITTED', label: STATUS_LABEL.SUBMITTED },
  { value: 'DELIVERED', label: STATUS_LABEL.DELIVERED },
  { value: 'FAILED', label: STATUS_LABEL.FAILED },
]

function MessageTab() {
  const [status, setStatus] = useState<FaxStatus | ''>('')
  const [input, setInput] = useState('')
  const [keyword, setKeyword] = useState('')
  const [page, setPage] = useState(0)
  const [reloadToken, setReloadToken] = useState(0)
  const [loaded, setLoaded] = useState<Loaded<FaxMessage> | null>(null)

  const [resendTarget, setResendTarget] = useState<FaxMessage | null>(null)
  const [resending, setResending] = useState(false)
  const [resendError, setResendError] = useState('')

  const requestKey = `${status}|${keyword}|${page}|${reloadToken}`

  useEffect(() => {
    let cancelled = false
    fetchFaxMessages(status, keyword, page, PAGE_SIZE)
      .then((data) => { if (!cancelled) setLoaded({ key: requestKey, data, error: '' }) })
      .catch((e) => {
        if (!cancelled) setLoaded({ key: requestKey, data: null, error: e instanceof Error ? e.message : '내역을 불러오지 못했습니다.' })
      })
    return () => { cancelled = true }
  }, [status, keyword, page, requestKey])

  const loading = loaded?.key !== requestKey
  const rows = loaded?.data?.content ?? []
  const error = loading ? '' : loaded?.error ?? ''

  function submitSearch() {
    setPage(0)
    setKeyword(input)
  }

  function changeStatus(s: FaxStatus | '') {
    setPage(0)
    setStatus(s)
  }

  async function confirmResend() {
    if (!resendTarget) return
    setResending(true)
    setResendError('')
    try {
      await resendFax(resendTarget.orderId)
      setResendTarget(null)
      setReloadToken((t) => t + 1)
    } catch (e) {
      setResendError(e instanceof Error ? e.message : '재발송에 실패했습니다.')
    } finally {
      setResending(false)
    }
  }

  return (
    <>
      <div className="flex flex-wrap items-end gap-2 rounded-lg border bg-white p-4">
        <div className="flex gap-1">
          {STATUS_FILTERS.map(({ value, label }) => (
            <Button
              key={value || 'all'}
              size="sm"
              variant={status === value ? 'default' : 'outline'}
              className={cn('h-8 text-xs', status === value && 'bg-green-700 hover:bg-green-800')}
              onClick={() => changeStatus(value)}
            >
              {label}
            </Button>
          ))}
        </div>
        <div className="relative">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-gray-400" />
          <Input
            placeholder="주문번호·업체명 검색"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => { if (e.key === 'Enter') submitSearch() }}
            className="w-56 pl-8"
          />
        </div>
        <Button variant="outline" onClick={submitSearch}>검색</Button>
      </div>

      {error && <p className="text-sm text-red-500">{error}</p>}

      <div className="overflow-x-auto rounded-lg border bg-white">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>주문번호</TableHead>
              <TableHead>수주사</TableHead>
              <TableHead>수신번호</TableHead>
              <TableHead className="text-center">회차</TableHead>
              <TableHead className="text-center">상태</TableHead>
              <TableHead>요청 시각</TableHead>
              <TableHead>완료 시각</TableHead>
              <TableHead>비고</TableHead>
              <TableHead className="text-center">처리</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={9} className="py-10 text-center text-sm text-gray-400">불러오는 중...</TableCell>
              </TableRow>
            ) : rows.length === 0 ? (
              <TableRow>
                <TableCell colSpan={9} className="py-10 text-center text-sm text-gray-400">발송 내역이 없습니다.</TableCell>
              </TableRow>
            ) : (
              rows.map((m) => (
                <TableRow key={m.id} className="hover:bg-gray-50">
                  <TableCell className="font-medium">{m.orderNumber ?? `#${m.orderId}`}</TableCell>
                  <TableCell className="text-sm">{m.vendorCompanyName ?? '-'}</TableCell>
                  <TableCell className="text-sm text-gray-500">{m.toNumber}</TableCell>
                  <TableCell className="text-center text-sm text-gray-500">
                    {m.sequence}{m.manual ? ' (수동)' : ''}
                  </TableCell>
                  <TableCell className="text-center">
                    <span className={cn('rounded px-2 py-0.5 text-xs font-medium', STATUS_STYLE[m.status])}>
                      {STATUS_LABEL[m.status]}
                    </span>
                  </TableCell>
                  <TableCell className="text-sm text-gray-500">{formatDateTime(m.requestedAt)}</TableCell>
                  <TableCell className="text-sm text-gray-500">{formatDateTime(m.completedAt)}</TableCell>
                  <TableCell className="max-w-48 truncate text-xs text-red-500" title={m.failReason ?? undefined}>
                    {m.failReason ?? ''}
                  </TableCell>
                  <TableCell className="text-center">
                    <Button
                      size="sm"
                      variant="outline"
                      className="h-7 text-xs"
                      onClick={() => { setResendTarget(m); setResendError('') }}
                    >
                      재발송
                    </Button>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {loaded?.data && (
        <Pagination
          page={page}
          totalPages={loaded.data.totalPages}
          totalElements={loaded.data.totalElements}
          size={PAGE_SIZE}
          onPageChange={setPage}
        />
      )}

      <Dialog open={resendTarget !== null} onOpenChange={(open) => { if (!open) setResendTarget(null) }}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>팩스 재발송</DialogTitle>
          </DialogHeader>
          <div className="space-y-2 text-sm">
            <p>
              <span className="font-medium">{resendTarget?.orderNumber ?? `#${resendTarget?.orderId}`}</span> 주문서를
              현재 배정된 수주사의 팩스번호로 다시 발송합니다.
            </p>
            <p className="text-red-600">재발송 시 발송 비용이 바로빌 계정에서 다시 차감됩니다.</p>
            {resendError && <p className="text-red-500">{resendError}</p>}
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setResendTarget(null)} disabled={resending}>취소</Button>
            <Button className="bg-green-600 hover:bg-green-700" onClick={confirmResend} disabled={resending}>
              {resending ? '발송 중...' : '재발송'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  )
}

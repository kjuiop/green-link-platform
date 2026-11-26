'use client'

import { useState, useEffect } from 'react'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog'
import { Pagination } from '@/components/ui/Pagination'
import {
  approveOnurimApplication, fetchOnurimApplications, rejectOnurimApplication,
  OnurimApplication, OnurimApplicationStatus, OnurimCompanyType,
} from '@/lib/api/onurimApplicationApi'
import { PageResult } from '@/types/page'

const PAGE_SIZE = 20
const MAX_REASON = 500

const TYPE_OPTIONS: { value: OnurimCompanyType; label: string }[] = [
  { value: 'ORDERING', label: '발주사' },
  { value: 'VENDOR', label: '수주사' },
  { value: 'BOTH', label: '수발주사' },
]

const STATUS_TABS: { value: OnurimApplicationStatus; label: string }[] = [
  { value: 'PENDING', label: '심사 대기' },
  { value: 'APPROVED', label: '승인' },
  { value: 'REJECTED', label: '거절' },
]

function typeLabel(t: OnurimCompanyType | null) {
  return TYPE_OPTIONS.find((o) => o.value === t)?.label ?? '-'
}

function dateLabel(iso: string | null) {
  return iso ? iso.slice(0, 16).replace('T', ' ') : '-'
}

function includesVendor(t: OnurimCompanyType) {
  return t === 'VENDOR' || t === 'BOTH'
}

interface Loaded {
  key: string
  data: PageResult<OnurimApplication> | null
  error: string
}

function Field({ label, value }: { label: string; value: string | null | undefined }) {
  return (
    <div className="flex gap-2 text-sm">
      <span className="w-28 shrink-0 text-gray-500">{label}</span>
      <span className="break-all">{value || '-'}</span>
    </div>
  )
}

export default function OnurimApplicationsPage() {
  const [status, setStatus] = useState<OnurimApplicationStatus>('PENDING')
  const [page, setPage] = useState(0)
  const [reloadToken, setReloadToken] = useState(0)
  const [loaded, setLoaded] = useState<Loaded | null>(null)

  const [target, setTarget] = useState<OnurimApplication | null>(null)
  const [approveType, setApproveType] = useState<OnurimCompanyType>('ORDERING')
  const [rejecting, setRejecting] = useState(false)
  const [reason, setReason] = useState('')
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState('')

  const requestKey = `${status}|${page}|${reloadToken}`

  useEffect(() => {
    let cancelled = false
    fetchOnurimApplications(status, page, PAGE_SIZE)
      .then((data) => { if (!cancelled) setLoaded({ key: requestKey, data, error: '' }) })
      .catch((e) => {
        if (!cancelled) setLoaded({ key: requestKey, data: null, error: e instanceof Error ? e.message : '목록을 불러오지 못했습니다.' })
      })
    return () => { cancelled = true }
  }, [status, page, requestKey])

  const loading = loaded?.key !== requestKey
  const rows = loaded?.data?.content ?? []
  const error = loading ? '' : loaded?.error ?? ''

  function changeStatus(next: OnurimApplicationStatus) {
    setPage(0)
    setStatus(next)
  }

  function open(a: OnurimApplication) {
    setTarget(a)
    setApproveType(a.desiredType)
    setRejecting(false)
    setReason('')
    setSaveError('')
  }

  async function run(action: () => Promise<unknown>, failMessage: string) {
    setSaving(true)
    setSaveError('')
    try {
      await action()
      setTarget(null)
      setReloadToken((t) => t + 1)
    } catch (e) {
      setSaveError(e instanceof Error ? e.message : failMessage)
    } finally {
      setSaving(false)
    }
  }

  const reasonValid = reason.trim().length > 0 && reason.trim().length <= MAX_REASON

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-xl font-bold">온우림 이용 신청</h1>
        <p className="mt-1 text-sm text-gray-500">
          쇼핑몰 회사가 신청한 온우림(발주·수주) 이용을 승인하거나 거절합니다. 승인하면 선택한 유형으로 회사가 활성화되고, 신청한 계정만 온우림에 로그인할 수 있습니다.
        </p>
      </div>

      <div className="flex gap-2">
        {STATUS_TABS.map((t) => (
          <Button
            key={t.value}
            size="sm"
            variant={status === t.value ? 'default' : 'outline'}
            className={status === t.value ? 'bg-green-600 hover:bg-green-700' : ''}
            onClick={() => changeStatus(t.value)}
          >
            {t.label}
          </Button>
        ))}
      </div>

      {error && <p className="text-sm text-red-500">{error}</p>}

      <div className="rounded-lg border bg-white">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>회사</TableHead>
              <TableHead>신청자</TableHead>
              <TableHead>희망 유형</TableHead>
              <TableHead>신청일</TableHead>
              <TableHead>{status === 'PENDING' ? '상태' : '처리 결과'}</TableHead>
              <TableHead className="text-center">상세</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={6} className="py-10 text-center text-sm text-gray-400">불러오는 중...</TableCell>
              </TableRow>
            ) : rows.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="py-10 text-center text-sm text-gray-400">신청 내역이 없습니다.</TableCell>
              </TableRow>
            ) : (
              rows.map((a) => (
                <TableRow key={a.id} className="hover:bg-gray-50">
                  <TableCell className="font-medium">{a.companyName ?? `#${a.companyId}`}</TableCell>
                  <TableCell className="text-sm text-gray-500">{a.applicantName ?? '-'}</TableCell>
                  <TableCell className="text-sm text-gray-500">{typeLabel(a.desiredType)}</TableCell>
                  <TableCell className="text-sm text-gray-500">{dateLabel(a.createdAt)}</TableCell>
                  <TableCell className="text-sm text-gray-500">
                    {a.status === 'PENDING' && '심사 대기'}
                    {a.status === 'APPROVED' && `승인 (${typeLabel(a.approvedType)})`}
                    {a.status === 'REJECTED' && '거절'}
                  </TableCell>
                  <TableCell className="text-center">
                    <Button size="sm" variant="outline" className="h-7 text-xs" onClick={() => open(a)}>
                      {a.status === 'PENDING' ? '심사' : '보기'}
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

      <Dialog open={target !== null} onOpenChange={(o) => { if (!o && !saving) setTarget(null) }}>
        <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-lg">
          <DialogHeader>
            <DialogTitle>온우림 이용 신청 - {target?.companyName}</DialogTitle>
          </DialogHeader>
          {target && (
            <div className="space-y-2">
              <Field label="신청자" value={target.applicantName} />
              <Field label="희망 유형" value={typeLabel(target.desiredType)} />
              <Field label="회사 전화" value={target.companyPhone} />
              <Field label="주소" value={[target.address, target.detailAddress].filter(Boolean).join(' ')} />
              <Field label="세금계산서 이메일" value={target.taxInvoiceEmail} />
              <Field label="메모" value={target.memo} />
              {includesVendor(target.desiredType) && (
                <>
                  <Field label="팩스" value={target.faxNumber} />
                  <Field
                    label="정산 계좌"
                    value={[target.bankName, target.accountNumber, target.accountHolder].filter(Boolean).join(' ')}
                  />
                  <Field
                    label="활동 지역"
                    value={target.regions.map((r) => [r.sido, r.sigungu].filter(Boolean).join(' ')).join(', ')}
                  />
                </>
              )}
              {target.status === 'REJECTED' && <Field label="거절 사유" value={target.rejectReason} />}
              {target.status !== 'PENDING' && <Field label="처리 시각" value={dateLabel(target.processedAt)} />}

              {target.status === 'PENDING' && !rejecting && (
                <div className="space-y-1 pt-2">
                  <Label>승인할 이용 유형</Label>
                  <div className="flex gap-2">
                    {TYPE_OPTIONS.map((o) => (
                      <Button
                        key={o.value}
                        size="sm"
                        variant={approveType === o.value ? 'default' : 'outline'}
                        className={approveType === o.value ? 'bg-green-600 hover:bg-green-700' : ''}
                        onClick={() => setApproveType(o.value)}
                        disabled={saving}
                      >
                        {o.label}
                      </Button>
                    ))}
                  </div>
                  <p className="text-xs text-gray-500">
                    희망 유형은 참고용이며 여기서 선택한 유형이 최종 적용됩니다. 발주사로 승인하면 팩스·계좌·지역 정보는 반영되지 않습니다.
                  </p>
                </div>
              )}

              {target.status === 'PENDING' && rejecting && (
                <div className="space-y-1 pt-2">
                  <Label htmlFor="reject-reason">거절 사유</Label>
                  <Textarea
                    id="reject-reason"
                    value={reason}
                    maxLength={MAX_REASON}
                    onChange={(e) => setReason(e.target.value)}
                    placeholder="신청자에게 표시됩니다."
                  />
                </div>
              )}

              {saveError && <p className="text-sm text-red-500">{saveError}</p>}
            </div>
          )}
          {target?.status === 'PENDING' && (
            <DialogFooter>
              {rejecting ? (
                <>
                  <Button variant="outline" onClick={() => setRejecting(false)} disabled={saving}>뒤로</Button>
                  <Button
                    variant="destructive"
                    disabled={!reasonValid || saving}
                    onClick={() => run(() => rejectOnurimApplication(target.id, reason.trim()), '거절에 실패했습니다.')}
                  >
                    {saving ? '처리 중...' : '거절'}
                  </Button>
                </>
              ) : (
                <>
                  <Button variant="outline" onClick={() => setRejecting(true)} disabled={saving}>거절</Button>
                  <Button
                    className="bg-green-600 hover:bg-green-700"
                    disabled={saving}
                    onClick={() => run(() => approveOnurimApplication(target.id, approveType), '승인에 실패했습니다.')}
                  >
                    {saving ? '처리 중...' : '승인'}
                  </Button>
                </>
              )}
            </DialogFooter>
          )}
        </DialogContent>
      </Dialog>
    </div>
  )
}

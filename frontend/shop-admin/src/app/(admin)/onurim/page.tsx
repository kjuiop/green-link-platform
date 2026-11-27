'use client'

import { useEffect, useState } from 'react'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import { applyOnurim, fetchOnurimApplication, fetchOnurimDocumentRequirement } from '@/lib/api/onurimApi'
import { compressImage } from '@/lib/imageCompress'
import type { OnurimApplication, OnurimApplyDocuments, OnurimApplyForm, OnurimCompanyType, OnurimDocumentRequirement } from '@/types/onurim'

const TYPE_LABEL: Record<OnurimCompanyType, string> = {
  ORDERING: '발주',
  VENDOR: '수주',
  BOTH: '발주 + 수주',
}

const TYPE_HINT: Record<OnurimCompanyType, string> = {
  ORDERING: '주문을 올리고 수주업체에 발주합니다.',
  VENDOR: '배정된 주문을 수주하고 배송합니다.',
  BOTH: '발주와 수주를 모두 이용합니다.',
}

const MAX_REGIONS = 20
const MAX_TOTAL_BYTES = 4 * 1024 * 1024
const MAX_FILE_BYTES = 10 * 1024 * 1024
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

const EMPTY_FORM: OnurimApplyForm = {
  desiredType: 'ORDERING',
  companyPhone: '',
  address: '',
  detailAddress: '',
  taxInvoiceEmail: '',
  memo: '',
  faxNumber: '',
  bankName: '',
  accountNumber: '',
  accountHolder: '',
  regions: [],
}

const NO_DOCS: OnurimApplyDocuments = { businessDoc: null, bankDoc: null }

function formatSize(bytes: number): string {
  return bytes >= 1024 * 1024 ? `${(bytes / 1024 / 1024).toFixed(1)}MB` : `${Math.ceil(bytes / 1024)}KB`
}

function formatDate(value: string | null): string {
  return value ? value.replace('T', ' ').slice(0, 16) : '-'
}

function Field({ label, required, children }: { label: string; required?: boolean; children: React.ReactNode }) {
  return (
    <label className="block space-y-1">
      <span className="text-xs font-medium text-gray-600">
        {label}
        {required && <span className="ml-0.5 text-red-500">*</span>}
      </span>
      {children}
    </label>
  )
}

export default function OnurimApplicationPage() {
  const [application, setApplication] = useState<OnurimApplication | null | undefined>(undefined)
  const [loadError, setLoadError] = useState(false)
  const [reloadKey, setReloadKey] = useState(0)
  const [reapplying, setReapplying] = useState(false)

  const [form, setForm] = useState<OnurimApplyForm>(EMPTY_FORM)
  const [docReq, setDocReq] = useState<OnurimDocumentRequirement | null | undefined>(undefined)
  const [docs, setDocs] = useState<OnurimApplyDocuments>(NO_DOCS)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false
    fetchOnurimApplication()
      .then((result) => {
        if (cancelled) return
        setApplication(result ?? null)
        setLoadError(false)
      })
      .catch(() => { if (!cancelled) setLoadError(true) })
    return () => { cancelled = true }
  }, [reloadKey])

  const showForm = application === null || (application?.status === 'REJECTED' && reapplying)

  useEffect(() => {
    if (!showForm) return
    let cancelled = false
    fetchOnurimDocumentRequirement()
      .then((result) => { if (!cancelled) setDocReq(result) })
      .catch(() => { if (!cancelled) setDocReq(null) })
    return () => { cancelled = true }
  }, [showForm, reloadKey])

  const includesVendor = form.desiredType === 'VENDOR' || form.desiredType === 'BOTH'

  function update<K extends keyof OnurimApplyForm>(key: K, value: OnurimApplyForm[K]) {
    setForm((prev) => ({ ...prev, [key]: value }))
    setError('')
  }

  async function pickDoc(key: keyof OnurimApplyDocuments, file: File | null) {
    setError('')
    if (!file) return setDocs((prev) => ({ ...prev, [key]: null }))
    if (!file.type.startsWith('image/') && file.type !== 'application/pdf') {
      return setError('이미지 또는 PDF 파일만 첨부할 수 있습니다.')
    }
    if (file.size > MAX_FILE_BYTES * 5) return setError('파일이 너무 큽니다. 10MB 이하 파일을 선택하세요.')
    const prepared = await compressImage(file)
    if (prepared.size > MAX_FILE_BYTES) return setError('파일은 10MB 이하만 첨부할 수 있습니다.')
    setDocs((prev) => ({ ...prev, [key]: prepared }))
  }

  function updateRegion(index: number, key: 'sido' | 'sigungu', value: string) {
    update('regions', form.regions.map((r, i) => (i === index ? { ...r, [key]: value } : r)))
  }

  async function handleSubmit() {
    if (busy) return
    if (!form.companyPhone.trim()) return setError('회사 전화번호를 입력하세요.')
    if (!form.address.trim()) return setError('주소를 입력하세요.')
    if (form.taxInvoiceEmail.trim() && !EMAIL_PATTERN.test(form.taxInvoiceEmail.trim())) {
      return setError('세금계산서 이메일 형식을 확인하세요.')
    }

    if (docReq === undefined || docReq === null) return setError('서류 정보를 불러온 뒤 다시 시도하세요.')
    if (docReq.needsBusinessReg && !docs.businessDoc) return setError('사업자등록증 사본을 첨부하세요.')
    if (docReq.needsBankBook && !docs.bankDoc) return setError('통장 사본을 첨부하세요.')
    const attached: OnurimApplyDocuments = {
      businessDoc: docReq.needsBusinessReg ? docs.businessDoc : null,
      bankDoc: docReq.needsBankBook ? docs.bankDoc : null,
    }
    const totalBytes = (attached.businessDoc?.size ?? 0) + (attached.bankDoc?.size ?? 0)
    if (totalBytes > MAX_TOTAL_BYTES) {
      return setError(`첨부 파일 합계가 ${formatSize(totalBytes)}입니다. 합계 4MB 이하로 줄여 첨부하세요.`)
    }

    const payload: OnurimApplyForm = {
      ...form,
      taxInvoiceEmail: form.taxInvoiceEmail.trim(),
      regions: includesVendor ? form.regions.filter((r) => r.sido.trim()) : [],
      ...(includesVendor ? {} : { faxNumber: '', bankName: '', accountNumber: '', accountHolder: '' }),
    }
    setBusy(true)
    setError('')
    try {
      await applyOnurim(payload, attached)
      setReapplying(false)
      setForm(EMPTY_FORM)
      setDocs(NO_DOCS)
      setDocReq(undefined)
      setApplication(undefined)
      setReloadKey((key) => key + 1)
    } catch (e) {
      setError(e instanceof Error ? e.message : '신청하지 못했습니다.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="max-w-2xl space-y-4">
      <h1 className="text-xl font-bold">온우림 이용 신청</h1>
      <p className="text-sm text-gray-500">
        온우림(발주·수주 시스템)을 이용하려면 신청 후 운영자의 승인이 필요합니다. 승인되면 신청한 계정으로 온우림에 로그인할 수 있습니다.
      </p>

      {loadError ? (
        <Card>
          <CardContent>
            <p className="py-6 text-center text-sm text-red-500">신청 현황을 불러오지 못했습니다.</p>
          </CardContent>
        </Card>
      ) : application === undefined ? (
        <Skeleton className="h-32 w-full" />
      ) : (
        <>
          {application?.status === 'PENDING' && (
            <Card>
              <CardContent className="space-y-1">
                <p className="font-semibold text-yellow-700">심사 중입니다.</p>
                <p className="text-sm text-gray-600">
                  희망 유형: {TYPE_LABEL[application.desiredType]} · 신청일 {formatDate(application.createdAt)}
                </p>
                <p className="text-xs text-gray-400">승인되면 다시 로그인했을 때 온우림을 이용할 수 있습니다.</p>
              </CardContent>
            </Card>
          )}

          {application?.status === 'APPROVED' && (
            <Card>
              <CardContent className="space-y-1">
                <p className="font-semibold text-green-700">승인되었습니다. 온우림을 이용할 수 있습니다.</p>
                <p className="text-sm text-gray-600">
                  이용 유형: {TYPE_LABEL[application.approvedType ?? application.desiredType]} · 승인일 {formatDate(application.processedAt)}
                </p>
              </CardContent>
            </Card>
          )}

          {application?.status === 'REJECTED' && (
            <Card>
              <CardContent className="space-y-2">
                <p className="font-semibold text-red-600">신청이 거절되었습니다.</p>
                <p className="text-sm text-gray-600">거절 사유: {application.rejectReason ?? '-'}</p>
                <p className="text-xs text-gray-400">처리일 {formatDate(application.processedAt)}</p>
                {!reapplying && (
                  <Button size="sm" className="bg-green-700 hover:bg-green-800" onClick={() => setReapplying(true)}>
                    다시 신청하기
                  </Button>
                )}
              </CardContent>
            </Card>
          )}

          {showForm && (
            <Card>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <span className="text-xs font-medium text-gray-600">
                    희망 이용 유형<span className="ml-0.5 text-red-500">*</span>
                  </span>
                  <div className="grid gap-2 sm:grid-cols-3">
                    {(Object.keys(TYPE_LABEL) as OnurimCompanyType[]).map((type) => (
                      <label
                        key={type}
                        className={`cursor-pointer rounded-lg border p-3 text-sm ${form.desiredType === type ? 'border-green-600 bg-green-50' : 'border-gray-200'}`}
                      >
                        <input
                          type="radio"
                          name="desiredType"
                          className="mr-2"
                          checked={form.desiredType === type}
                          disabled={busy}
                          onChange={() => update('desiredType', type)}
                        />
                        <span className="font-medium">{TYPE_LABEL[type]}</span>
                        <span className="mt-1 block text-xs text-gray-500">{TYPE_HINT[type]}</span>
                      </label>
                    ))}
                  </div>
                  <p className="text-xs text-gray-400">희망 유형은 참고용이며, 최종 유형은 승인 시 운영자가 정합니다.</p>
                </div>

                <div className="grid gap-3 sm:grid-cols-2">
                  <Field label="회사 전화번호" required>
                    <Input maxLength={30} value={form.companyPhone} disabled={busy} onChange={(e) => update('companyPhone', e.target.value)} />
                  </Field>
                  <Field label="세금계산서 이메일">
                    <Input type="email" maxLength={255} value={form.taxInvoiceEmail} disabled={busy} onChange={(e) => update('taxInvoiceEmail', e.target.value)} />
                  </Field>
                  <Field label="주소" required>
                    <Input maxLength={255} value={form.address} disabled={busy} onChange={(e) => update('address', e.target.value)} />
                  </Field>
                  <Field label="상세 주소">
                    <Input maxLength={255} value={form.detailAddress} disabled={busy} onChange={(e) => update('detailAddress', e.target.value)} />
                  </Field>
                </div>

                {includesVendor && (
                  <div className="space-y-3 rounded-lg border border-gray-100 bg-gray-50 p-3">
                    <p className="text-sm font-semibold text-gray-700">수주 정보</p>
                    <div className="grid gap-3 sm:grid-cols-2">
                      <Field label="팩스 번호">
                        <Input maxLength={30} value={form.faxNumber} disabled={busy} onChange={(e) => update('faxNumber', e.target.value)} />
                      </Field>
                      <Field label="은행">
                        <Input maxLength={50} value={form.bankName} disabled={busy} onChange={(e) => update('bankName', e.target.value)} />
                      </Field>
                      <Field label="계좌번호">
                        <Input maxLength={50} value={form.accountNumber} disabled={busy} onChange={(e) => update('accountNumber', e.target.value)} />
                      </Field>
                      <Field label="예금주">
                        <Input maxLength={50} value={form.accountHolder} disabled={busy} onChange={(e) => update('accountHolder', e.target.value)} />
                      </Field>
                    </div>

                    <div className="space-y-2">
                      <div className="flex items-center justify-between">
                        <span className="text-xs font-medium text-gray-600">활동 지역</span>
                        <Button
                          type="button"
                          size="sm"
                          variant="outline"
                          disabled={busy || form.regions.length >= MAX_REGIONS}
                          onClick={() => update('regions', [...form.regions, { sido: '', sigungu: '' }])}
                        >
                          지역 추가
                        </Button>
                      </div>
                      {form.regions.map((region, index) => (
                        <div key={index} className="flex items-center gap-2">
                          <Input
                            placeholder="시/도 (예: 서울)"
                            aria-label={`시/도 ${index + 1}`}
                            maxLength={30}
                            value={region.sido}
                            disabled={busy}
                            onChange={(e) => updateRegion(index, 'sido', e.target.value.replace(/[,|]/g, ''))}
                          />
                          <Input
                            placeholder="시/군/구 (선택)"
                            aria-label={`시/군/구 ${index + 1}`}
                            maxLength={30}
                            value={region.sigungu}
                            disabled={busy}
                            onChange={(e) => updateRegion(index, 'sigungu', e.target.value.replace(/[,|]/g, ''))}
                          />
                          <Button
                            type="button"
                            size="sm"
                            variant="outline"
                            disabled={busy}
                            onClick={() => update('regions', form.regions.filter((_, i) => i !== index))}
                          >
                            삭제
                          </Button>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {docReq === null && (
                  <p className="text-xs text-red-500">서류 정보를 불러오지 못했습니다. 페이지를 새로고침한 뒤 다시 시도하세요.</p>
                )}
                {docReq && (docReq.needsBusinessReg || docReq.needsBankBook) && (
                  <div className="space-y-3 rounded-lg border border-gray-100 bg-gray-50 p-3">
                    <p className="text-sm font-semibold text-gray-700">서류 첨부</p>
                    <p className="text-xs text-gray-500">
                      회사에 아직 등록되지 않은 서류만 첨부합니다. 이미지 또는 PDF, 첨부 파일 합계 4MB 이하(큰 사진은 자동으로 줄여집니다).
                    </p>
                    {docReq.needsBusinessReg && (
                      <Field label="사업자등록증 사본" required>
                        <Input
                          type="file"
                          accept="image/*,application/pdf"
                          disabled={busy}
                          onChange={(e) => { void pickDoc('businessDoc', e.target.files?.[0] ?? null); e.target.value = '' }}
                        />
                        {docs.businessDoc && <span className="text-xs text-gray-500">{docs.businessDoc.name} ({formatSize(docs.businessDoc.size)})</span>}
                      </Field>
                    )}
                    {docReq.needsBankBook && (
                      <Field label="통장 사본" required>
                        <Input
                          type="file"
                          accept="image/*,application/pdf"
                          disabled={busy}
                          onChange={(e) => { void pickDoc('bankDoc', e.target.files?.[0] ?? null); e.target.value = '' }}
                        />
                        {docs.bankDoc && <span className="text-xs text-gray-500">{docs.bankDoc.name} ({formatSize(docs.bankDoc.size)})</span>}
                      </Field>
                    )}
                  </div>
                )}

                <Field label="메모">
                  <textarea
                    className="min-h-20 w-full rounded-lg border border-input bg-transparent px-2.5 py-1.5 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50"
                    maxLength={500}
                    value={form.memo}
                    disabled={busy}
                    onChange={(e) => update('memo', e.target.value)}
                  />
                </Field>

                {error && <p className="text-xs text-red-500">{error}</p>}

                <div className="flex justify-end gap-2">
                  {reapplying && (
                    <Button variant="outline" disabled={busy} onClick={() => { setReapplying(false); setDocReq(undefined); setDocs(NO_DOCS); setError('') }}>
                      취소
                    </Button>
                  )}
                  <Button className="bg-green-700 hover:bg-green-800" disabled={busy || !docReq} onClick={handleSubmit}>
                    {busy ? '신청 중...' : '신청하기'}
                  </Button>
                </div>
              </CardContent>
            </Card>
          )}
        </>
      )}
    </div>
  )
}

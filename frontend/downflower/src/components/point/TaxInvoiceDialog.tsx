'use client'

import { useEffect, useState } from 'react'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  issueTaxInvoice,
  previewTaxInvoice,
  TaxInvoicePreview,
  TaxInvoiceParty,
  TaxInvoiceResult,
} from '@/lib/api/taxInvoiceApi'

const EMAIL_PATTERN = /^[^@\s]+@[^@\s]+\.[^@\s]+$/

function PartyBox({ title, party }: { title: string; party: TaxInvoiceParty }) {
  const rows: [string, string | null][] = [
    ['사업자번호', party.corpNum],
    ['상호', party.companyName],
    ['대표자', party.ceoName],
    ['주소', party.address],
  ]
  return (
    <div className="space-y-1 rounded-md border p-3">
      <p className="text-xs font-semibold text-gray-500">{title}</p>
      {rows.map(([label, value]) => (
        <p key={label} className="flex gap-2 text-sm">
          <span className="w-16 shrink-0 text-gray-400">{label}</span>
          <span className={value ? '' : 'text-red-500'}>{value || '미등록'}</span>
        </p>
      ))}
    </div>
  )
}

export function TaxInvoiceDialog({
  transactionId,
  onClose,
  onDone,
}: {
  transactionId: number
  onClose: () => void
  onDone: (result: TaxInvoiceResult) => void
}) {
  const [preview, setPreview] = useState<TaxInvoicePreview | null>(null)
  const [email, setEmail] = useState('')
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    previewTaxInvoice(transactionId)
      .then((p) => {
        setPreview(p)
        setEmail(p.recipient.email ?? '')
      })
      .catch((e) => setError(e instanceof Error ? e.message : '미리보기를 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }, [transactionId])

  const emailValid = EMAIL_PATTERN.test(email.trim())
  const canSubmit = !!preview && preview.missing.length === 0 && emailValid && !submitting

  async function submit() {
    if (!canSubmit) return
    setSubmitting(true)
    setError('')
    try {
      const result = await issueTaxInvoice(transactionId, email.trim())
      onDone(result)
    } catch (e) {
      setError(e instanceof Error ? e.message : '계산서 발행에 실패했습니다.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Dialog open onOpenChange={(open) => { if (!open && !submitting) onClose() }}>
      <DialogContent className="max-w-xl">
        <DialogHeader>
          <DialogTitle>전자계산서 발행 (면세)</DialogTitle>
        </DialogHeader>
        {loading && <p className="py-6 text-center text-sm text-gray-400">불러오는 중...</p>}
        {preview && (
          <div className="space-y-3">
            <div className="grid gap-3 sm:grid-cols-2">
              <PartyBox title="공급자" party={preview.supplier} />
              <PartyBox title="공급받는자" party={preview.recipient} />
            </div>
            <div className="rounded-md border bg-gray-50 p-3 text-sm">
              <p className="flex justify-between"><span className="text-gray-500">품목</span><span>{preview.itemName}</span></p>
              <p className="flex justify-between"><span className="text-gray-500">공급가액</span><span>{preview.supplyAmount.toLocaleString('ko-KR')}원</span></p>
              <p className="flex justify-between"><span className="text-gray-500">세액</span><span>{preview.taxAmount.toLocaleString('ko-KR')}원 (면세)</span></p>
              <p className="flex justify-between font-semibold"><span>합계</span><span>{preview.totalAmount.toLocaleString('ko-KR')}원</span></p>
            </div>
            <div className="space-y-1">
              <Label htmlFor="tax-invoice-email">수신 이메일</Label>
              <Input
                id="tax-invoice-email"
                type="email"
                placeholder="billing@example.com"
                maxLength={255}
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
              {email !== '' && !emailValid && <p className="text-xs text-red-500">이메일 형식이 올바르지 않습니다.</p>}
              <p className="text-xs text-gray-400">입력한 이메일은 해당 발주사의 세금계산서 수신 이메일로 저장됩니다.</p>
            </div>
            {preview.missing.length > 0 && (
              <p className="text-sm text-red-500">발행 전 확인이 필요합니다: {preview.missing.join(', ')}</p>
            )}
            {preview.status === 'FAILED' && preview.failReason && (
              <p className="text-sm text-red-500">이전 발행 실패: {preview.failReason}</p>
            )}
          </div>
        )}
        {error && <p className="text-sm text-red-500">{error}</p>}
        <DialogFooter>
          <Button variant="outline" onClick={onClose} disabled={submitting}>취소</Button>
          <Button className="bg-green-600 hover:bg-green-700" onClick={submit} disabled={!canSubmit}>
            {submitting ? '발행 중...' : preview?.status === 'FAILED' ? '재시도 발행' : '발행'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

'use client'

import { useRouter } from 'next/navigation'
import { useState } from 'react'
import { digitsOnly, validPhone, won } from '@/lib/format'
import type { ErrorBody, PlacedShopOrderResult } from '@/lib/types'

interface Line {
  productId: number
  name: string
  unitPrice: number
  quantity: number
}

const EVENT_TYPES = ['장례식', '결혼식', '기타']

const input = 'w-full rounded-md border bg-white px-3 py-2 text-sm'

function Field({ label, required, children }: { label: string; required?: boolean; children: React.ReactNode }) {
  return (
    <label className="block space-y-1">
      <span className="text-xs font-medium text-gray-600">
        {label} {required && <span className="text-red-500">*</span>}
      </span>
      {children}
    </label>
  )
}

export function OrderForm({ slug, lines, minDate }: { slug: string; lines: Line[]; minDate: string }) {
  const router = useRouter()
  const [v, setV] = useState({
    ordererName: '', ordererPhone: '', recipientName: '', recipientPhone: '',
    address: '', addressDetail: '', deliveryDate: '', deliveryTime: '',
    eventType: '기타', placeName: '', ribbonLeft: '', ribbonRight: '', memo: '', website: '',
  })
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  const total = lines.reduce((s, l) => s + l.unitPrice * l.quantity, 0)
  const set = (k: keyof typeof v) => (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) =>
    setV((prev) => ({ ...prev, [k]: e.target.value }))
  const setPhone = (k: 'ordererPhone' | 'recipientPhone') => (e: React.ChangeEvent<HTMLInputElement>) =>
    setV((prev) => ({ ...prev, [k]: e.target.value.replace(/[^\d-]/g, '') }))

  async function submit(e: React.FormEvent) {
    e.preventDefault()
    if (submitting) return
    if (!v.ordererName.trim() || !v.recipientName.trim() || !v.address.trim() || !v.deliveryDate) {
      return setError('필수 항목을 모두 입력해 주세요.')
    }
    if (!validPhone(v.ordererPhone) || !validPhone(v.recipientPhone)) {
      return setError('연락처는 숫자 9~11자리로 입력해 주세요.')
    }
    setSubmitting(true)
    setError('')
    try {
      const res = await fetch(`/api/shop/${encodeURIComponent(slug)}/orders`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          items: lines.map((l) => ({ productId: l.productId, quantity: l.quantity })),
          ...v,
          ordererPhone: digitsOnly(v.ordererPhone),
          recipientPhone: digitsOnly(v.recipientPhone),
        }),
      })
      if (res.status === 201) {
        const body: PlacedShopOrderResult = await res.json()
        router.push(`/s/${slug}/order/complete?no=${encodeURIComponent(body.orderNo)}`)
        return
      }
      const body: ErrorBody = await res.json().catch(() => ({}))
      setError(
        res.status === 429
          ? '요청이 많아 잠시 후 다시 시도해 주세요.'
          : body.message || '주문 접수에 실패했습니다. 입력 내용을 확인해 주세요.',
      )
    } catch {
      setError('네트워크 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form onSubmit={submit} className="space-y-6">
      <section className="space-y-2 rounded-lg border bg-white p-4">
        <h2 className="text-sm font-bold">주문 상품</h2>
        {lines.map((l) => (
          <div key={l.productId} className="flex justify-between text-sm">
            <span>{l.name} × {l.quantity}</span>
            <span>{won(l.unitPrice * l.quantity)}</span>
          </div>
        ))}
        <div className="flex justify-between border-t pt-2 text-sm font-bold">
          <span>합계</span>
          <span className="text-shop">{won(total)}</span>
        </div>
        <p className="text-xs text-gray-400">최종 금액은 접수 후 업체에서 확인하여 안내드립니다.</p>
      </section>

      <section className="space-y-3 rounded-lg border bg-white p-4">
        <h2 className="text-sm font-bold">주문자</h2>
        <Field label="이름" required><input className={input} maxLength={50} value={v.ordererName} onChange={set('ordererName')} /></Field>
        <Field label="연락처" required><input className={input} inputMode="tel" maxLength={30} value={v.ordererPhone} onChange={setPhone('ordererPhone')} placeholder="01012345678" /></Field>
      </section>

      <section className="space-y-3 rounded-lg border bg-white p-4">
        <h2 className="text-sm font-bold">받는 분 · 배송</h2>
        <Field label="받는 분 이름" required><input className={input} maxLength={50} value={v.recipientName} onChange={set('recipientName')} /></Field>
        <Field label="받는 분 연락처" required><input className={input} inputMode="tel" maxLength={30} value={v.recipientPhone} onChange={setPhone('recipientPhone')} placeholder="01012345678" /></Field>
        <Field label="배송지 주소" required><input className={input} maxLength={255} value={v.address} onChange={set('address')} /></Field>
        <Field label="상세 주소"><input className={input} maxLength={255} value={v.addressDetail} onChange={set('addressDetail')} /></Field>
        <div className="grid gap-3 sm:grid-cols-2">
          <Field label="배송 희망일" required><input type="date" className={input} min={minDate} value={v.deliveryDate} onChange={set('deliveryDate')} /></Field>
          <Field label="배송 희망 시간"><input className={input} maxLength={50} value={v.deliveryTime} onChange={set('deliveryTime')} placeholder="예: 오전 10시까지" /></Field>
        </div>
      </section>

      <section className="space-y-3 rounded-lg border bg-white p-4">
        <h2 className="text-sm font-bold">행사 · 리본</h2>
        <div className="grid gap-3 sm:grid-cols-2">
          <Field label="행사 종류">
            <select className={input} value={v.eventType} onChange={set('eventType')}>
              {EVENT_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
            </select>
          </Field>
          <Field label="장소"><input className={input} maxLength={100} value={v.placeName} onChange={set('placeName')} /></Field>
          <Field label="리본 왼쪽 (보내는 분)"><input className={input} maxLength={100} value={v.ribbonLeft} onChange={set('ribbonLeft')} /></Field>
          <Field label="리본 오른쪽 (문구)"><input className={input} maxLength={200} value={v.ribbonRight} onChange={set('ribbonRight')} /></Field>
        </div>
        <Field label="요청사항"><textarea className={input} rows={3} maxLength={500} value={v.memo} onChange={set('memo')} /></Field>
      </section>

      <div aria-hidden className="absolute -left-[9999px] h-0 w-0 overflow-hidden">
        <label>
          웹사이트
          <input tabIndex={-1} autoComplete="off" maxLength={100} value={v.website} onChange={set('website')} />
        </label>
      </div>

      {error && <p role="alert" className="text-sm text-red-500">{error}</p>}

      <button
        type="submit"
        disabled={submitting}
        className="w-full rounded-md bg-shop px-4 py-3 text-sm font-bold text-white disabled:opacity-50"
      >
        {submitting ? '접수 중…' : '주문 접수하기'}
      </button>
    </form>
  )
}

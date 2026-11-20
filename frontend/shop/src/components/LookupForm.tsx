'use client'

import { useState } from 'react'
import { digitsOnly, formatDateTime, ORDER_STATUS_LABEL, validPhone, won } from '@/lib/format'
import type { ErrorBody, PublicOrderStatusView } from '@/lib/types'

const input = 'w-full rounded-md border bg-white px-3 py-2 text-sm'

export function LookupForm({ slug, initialOrderNo }: { slug: string; initialOrderNo: string }) {
  const [orderNo, setOrderNo] = useState(initialOrderNo)
  const [phone, setPhone] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [result, setResult] = useState<PublicOrderStatusView | null>(null)

  async function submit(e: React.FormEvent) {
    e.preventDefault()
    if (loading) return
    if (!orderNo.trim() || !validPhone(phone)) return setError('주문번호와 연락처(숫자 9~11자리)를 입력해 주세요.')
    setLoading(true)
    setError('')
    setResult(null)
    try {
      const res = await fetch(`/api/shop/${encodeURIComponent(slug)}/orders/lookup`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ orderNo: orderNo.trim(), ordererPhone: digitsOnly(phone) }),
      })
      if (res.ok) {
        setResult(await res.json())
        return
      }
      const body: ErrorBody = await res.json().catch(() => ({}))
      setError(
        res.status === 404
          ? '일치하는 주문이 없습니다. 주문번호와 연락처를 확인해 주세요.'
          : res.status === 429
            ? '요청이 많아 잠시 후 다시 시도해 주세요.'
            : body.message || '조회에 실패했습니다.',
      )
    } catch {
      setError('네트워크 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="space-y-6">
      <form onSubmit={submit} className="space-y-3 rounded-lg border bg-white p-4">
        <label className="block space-y-1">
          <span className="text-xs font-medium text-gray-600">주문번호</span>
          <input className={input} maxLength={40} value={orderNo} onChange={(e) => setOrderNo(e.target.value)} placeholder="SO-20260101-ABC234" />
        </label>
        <label className="block space-y-1">
          <span className="text-xs font-medium text-gray-600">주문자 연락처</span>
          <input
            className={input}
            inputMode="tel"
            maxLength={30}
            value={phone}
            onChange={(e) => setPhone(e.target.value.replace(/[^\d-]/g, ''))}
            placeholder="01012345678"
          />
        </label>
        {error && <p role="alert" className="text-sm text-red-500">{error}</p>}
        <button type="submit" disabled={loading} className="w-full rounded-md bg-shop px-4 py-3 text-sm font-bold text-white disabled:opacity-50">
          {loading ? '조회 중…' : '주문 조회'}
        </button>
      </form>

      {result && (
        <section className="space-y-2 rounded-lg border bg-white p-4 text-sm">
          <div className="flex items-center justify-between">
            <span className="font-medium">{result.orderNo}</span>
            <span className="rounded-full bg-gray-100 px-2 py-0.5 text-xs">{ORDER_STATUS_LABEL[result.status] ?? result.status}</span>
          </div>
          <div className="space-y-1 text-gray-700">
            {result.items.map((i, idx) => (
              <div key={idx} className="flex justify-between">
                <span>{i.name} × {i.quantity}</span>
                <span>{won(i.unitPrice * i.quantity)}</span>
              </div>
            ))}
          </div>
          <div className="flex justify-between border-t pt-2 font-bold">
            <span>합계</span>
            <span className="text-shop">{won(result.totalPrice)}</span>
          </div>
          <p className="text-xs text-gray-500">배송 희망일 {result.deliveryDate ?? '-'} · 접수 {formatDateTime(result.createdAt)}</p>
        </section>
      )}
    </div>
  )
}

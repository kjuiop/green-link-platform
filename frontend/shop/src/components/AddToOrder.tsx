'use client'

import { useRouter } from 'next/navigation'
import { useState } from 'react'
import { won } from '@/lib/format'

export function AddToOrder({ slug, productId, price }: { slug: string; productId: number; price: number }) {
  const router = useRouter()
  const [qty, setQty] = useState(1)

  const step = 'flex h-9 w-9 items-center justify-center rounded-md border text-lg disabled:opacity-30'
  return (
    <div className="sticky bottom-0 -mx-4 flex items-center justify-between gap-3 border-t bg-white px-4 py-3">
      <div className="flex items-center gap-2">
        <button type="button" className={step} disabled={qty <= 1} onClick={() => setQty(qty - 1)} aria-label="수량 감소">−</button>
        <span className="w-8 text-center text-sm font-medium">{qty}</span>
        <button type="button" className={step} disabled={qty >= 99} onClick={() => setQty(qty + 1)} aria-label="수량 증가">+</button>
      </div>
      <button
        type="button"
        onClick={() => router.push(`/s/${slug}/order?items=${productId}:${qty}`)}
        className="flex-1 rounded-md bg-shop px-4 py-3 text-sm font-bold text-white"
      >
        {won(price * qty)} 주문하기
      </button>
    </div>
  )
}

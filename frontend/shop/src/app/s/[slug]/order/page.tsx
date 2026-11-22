import type { Metadata } from 'next'
import { notFound, redirect } from 'next/navigation'
import { OrderForm } from '@/components/OrderForm'
import { todayKst } from '@/lib/format'
import { getShop } from '@/lib/shopApi'

export const metadata: Metadata = { title: '주문하기', robots: { index: false } }

const MAX_LINES = 10

function parseItems(raw: string | undefined): { productId: number; quantity: number }[] | null {
  if (!raw) return null
  const lines: { productId: number; quantity: number }[] = []
  for (const part of raw.split(',')) {
    const m = /^(\d{1,18}):(\d{1,2})$/.exec(part)
    if (!m) return null
    const quantity = Number(m[2])
    if (quantity < 1 || quantity > 99) return null
    lines.push({ productId: Number(m[1]), quantity })
  }
  return lines.length > 0 && lines.length <= MAX_LINES ? lines : null
}

export default async function OrderPage({
  params,
  searchParams,
}: {
  params: Promise<{ slug: string }>
  searchParams: Promise<{ items?: string }>
}) {
  const { slug } = await params
  const { items } = await searchParams
  const shop = await getShop(slug)
  if (!shop) notFound()

  const parsed = parseItems(items)
  if (!parsed) redirect(`/s/${slug}`)
  const lines = parsed.map((p) => {
    const product = shop.products.find((x) => x.id === p.productId)
    return product ? { productId: product.id, name: product.name, unitPrice: product.price, quantity: p.quantity } : null
  })
  if (lines.some((l) => l === null)) redirect(`/s/${slug}`)

  return (
    <div className="mx-auto max-w-xl space-y-4">
      <h1 className="text-lg font-bold">주문하기</h1>
      <OrderForm slug={slug} lines={lines.filter((l) => l !== null)} minDate={todayKst()} />
    </div>
  )
}

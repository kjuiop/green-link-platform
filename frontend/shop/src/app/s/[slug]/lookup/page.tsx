import type { Metadata } from 'next'
import { LookupForm } from '@/components/LookupForm'
import { ORDER_NO_PATTERN } from '@/lib/format'

export const metadata: Metadata = { title: '주문 조회', robots: { index: false } }

export default async function LookupPage({
  params,
  searchParams,
}: {
  params: Promise<{ slug: string }>
  searchParams: Promise<{ no?: string }>
}) {
  const { slug } = await params
  const { no } = await searchParams
  return (
    <div className="mx-auto max-w-md space-y-4">
      <h1 className="text-lg font-bold">주문 조회</h1>
      <LookupForm slug={slug} initialOrderNo={no && ORDER_NO_PATTERN.test(no) ? no : ''} />
    </div>
  )
}

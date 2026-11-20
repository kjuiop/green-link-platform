import type { Metadata } from 'next'
import Link from 'next/link'
import { redirect } from 'next/navigation'
import { CopyButton } from '@/components/CopyButton'
import { ORDER_NO_PATTERN } from '@/lib/format'

export const metadata: Metadata = { title: '주문 접수 완료', robots: { index: false } }

export default async function OrderCompletePage({
  params,
  searchParams,
}: {
  params: Promise<{ slug: string }>
  searchParams: Promise<{ no?: string }>
}) {
  const { slug } = await params
  const { no } = await searchParams
  if (!no || !ORDER_NO_PATTERN.test(no)) redirect(`/s/${slug}`)

  return (
    <div className="mx-auto max-w-md space-y-5 py-8 text-center">
      <h1 className="text-lg font-bold">주문이 접수되었습니다</h1>
      <div className="space-y-2 rounded-lg border bg-white p-5">
        <p className="text-xs text-gray-500">주문번호</p>
        <p className="text-xl font-bold tracking-wide text-shop">{no}</p>
        <CopyButton text={no} />
      </div>
      <p className="text-sm text-gray-600">
        업체 확인 후 승인되면 진행됩니다. 주문 조회에는 주문번호와 주문자 연락처가 필요하니 주문번호를 꼭 보관해 주세요.
      </p>
      <div className="flex justify-center gap-2">
        <Link href={`/s/${slug}/lookup?no=${encodeURIComponent(no)}`} className="rounded-md bg-shop px-4 py-2 text-sm font-medium text-white">
          주문 조회
        </Link>
        <Link href={`/s/${slug}`} className="rounded-md border bg-white px-4 py-2 text-sm text-gray-700">
          쇼핑몰로 돌아가기
        </Link>
      </div>
    </div>
  )
}

import type { Metadata } from 'next'
import Link from 'next/link'
import { notFound } from 'next/navigation'
import { ShopImage } from '@/components/ShopImage'
import { safeThemeColor } from '@/lib/format'
import { getShop } from '@/lib/shopApi'

export async function generateMetadata({ params }: { params: Promise<{ slug: string }> }): Promise<Metadata> {
  const { slug } = await params
  const shop = await getShop(slug)
  if (!shop) return { title: '쇼핑몰', robots: { index: false } }
  const image = shop.bannerUrl ?? shop.logoUrl
  const description = shop.bannerSubtitle ?? shop.notice ?? undefined
  return {
    title: shop.displayName,
    description,
    openGraph: { title: shop.displayName, description, images: image ? [image] : undefined },
  }
}

export default async function ShopLayout({
  children,
  params,
}: {
  children: React.ReactNode
  params: Promise<{ slug: string }>
}) {
  const { slug } = await params
  const shop = await getShop(slug)
  if (!shop) notFound()

  return (
    <div style={{ '--shop-color': safeThemeColor(shop.themeColor) } as React.CSSProperties} className="mx-auto flex min-h-screen max-w-5xl flex-col bg-gray-50">
      <header className="flex items-center justify-between gap-3 border-b bg-white px-4 py-3">
        <Link href={`/s/${slug}`} className="flex min-w-0 items-center gap-2">
          {shop.logoUrl && (
            <span className="relative h-8 w-8 shrink-0 overflow-hidden rounded-full">
              <ShopImage src={shop.logoUrl} alt="" sizes="32px" />
            </span>
          )}
          <span className="truncate text-base font-bold">{shop.displayName}</span>
        </Link>
        <div className="flex shrink-0 items-center gap-3 text-sm">
          <Link href={`/s/${slug}/lookup`} className="text-gray-600">주문조회</Link>
          {shop.phone && (
            <a href={`tel:${shop.phone.replace(/[^\d+]/g, '')}`} className="rounded-md bg-shop px-3 py-1.5 text-xs font-medium text-white">
              전화
            </a>
          )}
        </div>
      </header>

      <main className="flex-1 px-4 py-4">{children}</main>

      <footer className="space-y-1 border-t bg-white px-4 py-6 text-xs text-gray-500">
        <p className="font-medium text-gray-700">{shop.displayName}</p>
        {shop.phone && <p>고객센터 {shop.phone}</p>}
        {shop.businessInfo && <p className="whitespace-pre-line">{shop.businessInfo}</p>}
      </footer>
    </div>
  )
}

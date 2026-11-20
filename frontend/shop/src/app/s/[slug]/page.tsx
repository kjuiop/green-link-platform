import { notFound } from 'next/navigation'
import { CategoryTabs } from '@/components/CategoryTabs'
import { ProductGrid } from '@/components/ProductGrid'
import { ShopImage } from '@/components/ShopImage'
import { getShop } from '@/lib/shopApi'

export default async function ShopPage({
  params,
  searchParams,
}: {
  params: Promise<{ slug: string }>
  searchParams: Promise<{ category?: string }>
}) {
  const { slug } = await params
  const { category } = await searchParams
  const shop = await getShop(slug)
  if (!shop) notFound()

  const categories: { value: string; label: string }[] = []
  for (const p of shop.products) {
    if (!categories.some((c) => c.value === p.category)) categories.push({ value: p.category, label: p.categoryLabel })
  }
  const active = categories.some((c) => c.value === category) ? category! : null
  const products = active ? shop.products.filter((p) => p.category === active) : shop.products

  return (
    <div className="space-y-5">
      <section className="relative flex min-h-40 items-end overflow-hidden rounded-xl bg-shop md:min-h-60">
        {shop.bannerUrl && <ShopImage src={shop.bannerUrl} alt="" sizes="(min-width: 1024px) 1024px, 100vw" priority />}
        {(shop.bannerTitle || shop.bannerSubtitle) && (
          <div className="relative w-full bg-gradient-to-t from-black/60 to-transparent p-4 text-white md:p-6">
            {shop.bannerTitle && <h1 className="text-xl font-bold md:text-3xl">{shop.bannerTitle}</h1>}
            {shop.bannerSubtitle && <p className="mt-1 text-sm md:text-base">{shop.bannerSubtitle}</p>}
          </div>
        )}
      </section>

      {shop.notice && (
        <section className="rounded-lg border bg-white p-4 text-sm">
          <h2 className="mb-1 text-xs font-bold text-shop">공지</h2>
          <p className="whitespace-pre-line text-gray-700">{shop.notice}</p>
        </section>
      )}

      <CategoryTabs slug={slug} categories={categories} active={active} />
      <ProductGrid slug={slug} products={products} />

      {shop.deliveryGuide && (
        <section className="rounded-lg border bg-white p-4 text-sm">
          <h2 className="mb-1 text-xs font-bold text-shop">배송 안내</h2>
          <p className="whitespace-pre-line text-gray-700">{shop.deliveryGuide}</p>
        </section>
      )}
    </div>
  )
}

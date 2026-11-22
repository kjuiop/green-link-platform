import type { Metadata } from 'next'
import Link from 'next/link'
import { notFound } from 'next/navigation'
import { AddToOrder } from '@/components/AddToOrder'
import { ShopImage } from '@/components/ShopImage'
import { won } from '@/lib/format'
import { getProduct } from '@/lib/shopApi'

type Params = Promise<{ slug: string; id: string }>

export async function generateMetadata({ params }: { params: Params }): Promise<Metadata> {
  const { slug, id } = await params
  const product = await getProduct(slug, id)
  if (!product) return {}
  return {
    title: product.name,
    description: product.description ?? undefined,
    openGraph: { images: product.imageUrl ? [product.imageUrl] : undefined },
  }
}

export default async function ProductPage({ params }: { params: Params }) {
  const { slug, id } = await params
  if (!/^\d+$/.test(id)) notFound()
  const product = await getProduct(slug, id)
  if (!product) notFound()

  return (
    <div className="space-y-4">
      <Link href={`/s/${slug}`} className="text-sm text-gray-500">← 목록으로</Link>
      <div className="grid gap-4 md:grid-cols-2">
        <div className="relative aspect-square overflow-hidden rounded-xl bg-gray-100">
          <ShopImage src={product.imageUrl} alt={product.name} sizes="(min-width: 768px) 50vw, 100vw" priority />
        </div>
        <div className="space-y-2">
          <p className="text-xs text-gray-400">{product.categoryLabel}</p>
          <h1 className="text-lg font-bold">{product.name}</h1>
          <p className="text-xl font-bold text-shop">{won(product.price)}</p>
          {product.description && <p className="whitespace-pre-line text-sm text-gray-700">{product.description}</p>}
        </div>
      </div>
      <AddToOrder slug={slug} productId={product.id} price={product.price} />
    </div>
  )
}

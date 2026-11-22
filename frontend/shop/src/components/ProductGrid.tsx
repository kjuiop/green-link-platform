import Link from 'next/link'
import { won } from '@/lib/format'
import type { PublicProductView } from '@/lib/types'
import { ShopImage } from './ShopImage'

export function ProductGrid({ slug, products }: { slug: string; products: PublicProductView[] }) {
  if (products.length === 0) {
    return <p className="py-16 text-center text-sm text-gray-400">등록된 상품이 없습니다.</p>
  }
  return (
    <ul className="grid grid-cols-2 gap-3 md:grid-cols-4 md:gap-4">
      {products.map((p) => (
        <li key={p.id}>
          <Link href={`/s/${slug}/products/${p.id}`} className="block overflow-hidden rounded-lg border bg-white transition hover:shadow-md">
            <div className="relative aspect-square">
              <ShopImage src={p.imageUrl} alt={p.name} sizes="(min-width: 768px) 25vw, 50vw" />
            </div>
            <div className="space-y-1 p-3">
              <p className="text-xs text-gray-400">{p.categoryLabel}</p>
              <p className="line-clamp-2 text-sm font-medium">{p.name}</p>
              <p className="text-sm font-bold text-shop">{won(p.price)}</p>
            </div>
          </Link>
        </li>
      ))}
    </ul>
  )
}

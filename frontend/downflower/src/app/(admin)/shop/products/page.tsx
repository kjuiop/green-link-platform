'use client'

import { useShopScope } from '@/components/shop/useShopScope'
import { ShopProductsPanel } from '@/components/shop/ShopProductsPanel'

export default function ShopProductsPage() {
  const scope = useShopScope()
  return (
    <div className="mx-auto max-w-3xl space-y-4 p-4 md:p-6">
      <ShopProductsPanel scope={scope} showHeader />
    </div>
  )
}

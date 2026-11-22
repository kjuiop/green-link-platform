'use client'

import { useShopScope } from '@/components/shop/useShopScope'
import { ShopSettingsPanel } from '@/components/shop/ShopSettingsPanel'

export default function ShopSettingsPage() {
  const scope = useShopScope()
  return (
    <div className="mx-auto max-w-3xl space-y-4 p-4 md:p-6">
      <h1 className="text-lg font-bold">쇼핑몰 설정</h1>
      <ShopSettingsPanel scope={scope} />
    </div>
  )
}

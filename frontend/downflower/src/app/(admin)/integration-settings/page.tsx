'use client'

import { useSearchParams, useRouter } from 'next/navigation'
import { Suspense } from 'react'
import { useCoupangScope } from '@/components/coupang/useCoupangScope'
import { CoupangSettingsPanel } from '@/components/coupang/CoupangSettingsPanel'
import { useShopScope } from '@/components/shop/useShopScope'
import { ShopSettingsPanel } from '@/components/shop/ShopSettingsPanel'

type Market = 'coupang' | 'shop'
const MARKETS: { value: Market; label: string }[] = [
  { value: 'coupang', label: '쿠팡' },
  { value: 'shop', label: '쇼핑몰' },
]

function IntegrationSettingsContent() {
  const searchParams = useSearchParams()
  const router = useRouter()
  const market = (searchParams.get('market') as Market) ?? 'coupang'

  const coupangScope = useCoupangScope()
  const shopScope = useShopScope()

  function switchMarket(m: Market) {
    router.replace(`/integration-settings?market=${m}`)
  }

  return (
    <div className="space-y-4">
      <h1 className="text-lg font-bold">연동 설정</h1>

      <div className="flex gap-1 border-b">
        {MARKETS.map((m) => (
          <button
            key={m.value}
            onClick={() => switchMarket(m.value)}
            className={
              market === m.value
                ? 'border-b-2 border-green-700 px-4 py-2 text-sm font-medium text-green-700'
                : 'px-4 py-2 text-sm text-gray-500 hover:text-gray-900'
            }
          >
            {m.label}
          </button>
        ))}
      </div>

      {market === 'coupang' && <CoupangSettingsPanel scope={coupangScope} />}
      {market === 'shop' && <ShopSettingsPanel scope={shopScope} />}
    </div>
  )
}

export default function IntegrationSettingsPage() {
  return (
    <Suspense>
      <IntegrationSettingsContent />
    </Suspense>
  )
}

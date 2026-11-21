'use client'

import { useCoupangScope } from '@/components/coupang/useCoupangScope'
import { CoupangSettingsPanel } from '@/components/coupang/CoupangSettingsPanel'

export default function CoupangSettingsPage() {
  const scope = useCoupangScope()
  return (
    <div className="space-y-4">
      <h1 className="text-lg font-bold">쿠팡 연동 설정</h1>
      <CoupangSettingsPanel scope={scope} />
    </div>
  )
}

'use client'

import { useState } from 'react'
import { Button } from '@/components/ui/button'
import { checkBarobillSender } from '@/lib/api/settingsApi'

type Props = {
  companyId?: number
  disabled?: boolean
}

export function BarobillSenderCheck({ companyId, disabled }: Props) {
  const [checking, setChecking] = useState(false)
  const [result, setResult] = useState<{ ok: boolean; message: string } | null>(null)

  async function handleCheck() {
    setChecking(true)
    setResult(null)
    try {
      const data = await checkBarobillSender(companyId)
      setResult({ ok: data.code === 1, message: data.message })
    } catch (e) {
      setResult({ ok: false, message: e instanceof Error ? e.message : '발신번호 확인에 실패했습니다.' })
    } finally {
      setChecking(false)
    }
  }

  return (
    <div className="space-y-1.5">
      <div className="flex items-center gap-3">
        <Button type="button" variant="outline" size="sm" onClick={handleCheck} disabled={checking || disabled}>
          {checking ? '확인 중…' : '발신번호 등록 확인'}
        </Button>
        {result && (
          <span className={result.ok ? 'text-sm text-green-700' : 'text-sm text-red-500'}>{result.message}</span>
        )}
      </div>
      <p className="text-xs text-gray-400">
        저장된 설정 기준으로 바로빌에 발신번호가 등록되어 있는지 확인합니다. 먼저 저장한 뒤 확인하세요.
      </p>
    </div>
  )
}

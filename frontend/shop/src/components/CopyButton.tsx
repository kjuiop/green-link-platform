'use client'

import { useState } from 'react'

export function CopyButton({ text }: { text: string }) {
  const [copied, setCopied] = useState(false)
  async function copy() {
    try {
      await navigator.clipboard.writeText(text)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    } catch {
      // 복사 권한이 없는 환경에서는 사용자가 직접 선택해 복사한다.
    }
  }
  return (
    <button type="button" onClick={copy} className="rounded-md border px-3 py-1.5 text-xs text-gray-600">
      {copied ? '복사됨' : '복사'}
    </button>
  )
}

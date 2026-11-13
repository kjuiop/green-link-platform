'use client'

import { useState, useEffect } from 'react'
import { fetchMyAccount, CompanyAccount } from '@/lib/api/memberApi'

export function AccountInfoBanner() {
  const [account, setAccount] = useState<CompanyAccount | null>(null)

  useEffect(() => {
    fetchMyAccount().then(setAccount).catch(() => {})
  }, [])

  const hasAccount = account?.bankName && account?.accountNumber

  return (
    <div className="flex items-center gap-4 rounded-lg border border-blue-100 bg-blue-50 px-5 py-3">
      <div className="text-xs font-semibold text-blue-700">회원계좌</div>
      <div className="h-4 w-px bg-blue-200" />
      {hasAccount ? (
        <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-sm">
          <span className="font-medium text-gray-800">{account!.bankName}</span>
          <span className="font-mono font-semibold tracking-wider text-gray-900">{account!.accountNumber}</span>
          {account!.accountHolder && <span className="text-gray-600">{account!.accountHolder}</span>}
        </div>
      ) : (
        <span className="text-sm text-gray-400">등록된 회원계좌가 없습니다.</span>
      )}
    </div>
  )
}

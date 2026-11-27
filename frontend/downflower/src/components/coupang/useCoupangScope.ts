'use client'

import { useEffect, useState } from 'react'
import { fetchCompanies, type CompanyInfo } from '@/lib/api/companyApi'
import { isSuperAdmin as sessionIsSuperAdmin } from '@/lib/session'

export interface CoupangScope {
  ready: boolean
  isSuperAdmin: boolean
  companies: CompanyInfo[]
  selectedId: number | null
  setSelectedId: (id: number | null) => void
}

// 발주사 계정은 서버가 본인 회사로 고정하므로 selectedId는 슈퍼관리자일 때만 의미가 있다.
export function useCoupangScope(): CoupangScope {
  const [ready, setReady] = useState(false)
  const [isSuperAdmin, setIsSuperAdmin] = useState(false)
  const [companies, setCompanies] = useState<CompanyInfo[]>([])
  const [selectedId, setSelectedId] = useState<number | null>(null)

  useEffect(() => {
    if (!sessionIsSuperAdmin()) {
      setReady(true)
      return
    }
    setIsSuperAdmin(true)
    fetchCompanies()
      .then((list) => {
        const ordering = list.filter((c) => c.type === 'ORDERING' || c.type === 'BOTH')
        setCompanies(ordering)
        if (ordering.length > 0) setSelectedId(ordering[0].id)
      })
      .catch(() => {})
      .finally(() => setReady(true))
  }, [])

  return { ready, isSuperAdmin, companies, selectedId, setSelectedId }
}

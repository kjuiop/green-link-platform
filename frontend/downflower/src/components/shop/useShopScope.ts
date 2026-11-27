'use client'

import { useEffect, useState } from 'react'
import { fetchCompanies, type CompanyInfo } from '@/lib/api/companyApi'
import { isSuperAdmin as sessionIsSuperAdmin } from '@/lib/session'

export interface ShopScope {
  ready: boolean
  isSuperAdmin: boolean
  companies: CompanyInfo[]
  selectedId: number | null
  setSelectedId: (id: number | null) => void
}

// 일반 계정은 서버가 본인 회사로 고정하므로 selectedId는 슈퍼관리자일 때만 의미가 있다.
export function useShopScope(): ShopScope {
  const [ready, setReady] = useState(false)
  const [isSuperAdmin, setIsSuperAdmin] = useState(false)
  const [companies, setCompanies] = useState<CompanyInfo[]>([])
  const [selectedId, setSelectedId] = useState<number | null>(null)

  useEffect(() => {
    if (!sessionIsSuperAdmin()) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setReady(true)
      return
    }
    setIsSuperAdmin(true)
    fetchCompanies()
      .then((list) => {
        setCompanies(list)
        if (list.length > 0) setSelectedId(list[0].id)
      })
      .catch(() => {})
      .finally(() => setReady(true))
  }, [])

  return { ready, isSuperAdmin, companies, selectedId, setSelectedId }
}

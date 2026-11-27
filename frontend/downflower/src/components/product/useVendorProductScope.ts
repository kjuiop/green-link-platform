'use client'

import { useEffect, useState } from 'react'
import { fetchCompanies, type CompanyInfo } from '@/lib/api/companyApi'
import { isSuperAdmin as sessionIsSuperAdmin } from '@/lib/session'

export interface VendorProductScope {
  ready: boolean
  isSuperAdmin: boolean
  companies: CompanyInfo[]
  selectedId: number | null
  setSelectedId: (id: number | null) => void
}

export function useVendorProductScope(): VendorProductScope {
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
        const vendors = list.filter((c) => c.type === 'VENDOR' || c.type === 'BOTH')
        setCompanies(vendors)
        if (vendors.length > 0) setSelectedId(vendors[0].id)
      })
      .catch(() => {})
      .finally(() => setReady(true))
  }, [])

  return { ready, isSuperAdmin, companies, selectedId, setSelectedId }
}

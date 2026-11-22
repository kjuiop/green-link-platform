'use client'

import { useEffect, useState } from 'react'
import { fetchCompanies, type CompanyInfo } from '@/lib/api/companyApi'

export interface PointScope {
  ready: boolean
  isSuperAdmin: boolean
  companies: CompanyInfo[]
  selectedId: number | null
  setSelectedId: (id: number | null) => void
}

export function usePointScope(): PointScope {
  const [ready, setReady] = useState(false)
  const [isSuperAdmin, setIsSuperAdmin] = useState(false)
  const [companies, setCompanies] = useState<CompanyInfo[]>([])
  const [selectedId, setSelectedId] = useState<number | null>(null)

  useEffect(() => {
    const role = localStorage.getItem('role') ?? ''
    if (role !== 'SUPER_ADMIN') {
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

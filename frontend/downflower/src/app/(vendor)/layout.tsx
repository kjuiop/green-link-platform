'use client'

import { useState, useEffect } from 'react'
import { VendorSidebar } from '@/components/layout/VendorSidebar'
import { Header } from '@/components/layout/Header'

export default function VendorLayout({ children }: { children: React.ReactNode }) {
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [userName, setUserName] = useState('')
  const [companyName, setCompanyName] = useState('')

  useEffect(() => {
    setUserName(localStorage.getItem('name') ?? '')
    setCompanyName(localStorage.getItem('companyName') ?? '')
  }, [])

  return (
    <div className="flex h-screen overflow-hidden">
      <VendorSidebar mobileOpen={drawerOpen} onMobileClose={() => setDrawerOpen(false)} companyName={companyName} />
      <div className="flex flex-1 flex-col overflow-hidden">
        <Header userName={userName} onMenuOpen={() => setDrawerOpen(true)} accountHref="/vendor/account" />
        <main className="flex-1 overflow-y-auto">
          <div className="p-4 md:p-6">{children}</div>
        </main>
      </div>
    </div>
  )
}

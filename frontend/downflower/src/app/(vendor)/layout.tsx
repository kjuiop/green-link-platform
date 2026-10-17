'use client'

import { useState } from 'react'
import { VendorSidebar } from '@/components/layout/VendorSidebar'
import { Header } from '@/components/layout/Header'

export default function VendorLayout({ children }: { children: React.ReactNode }) {
  const [drawerOpen, setDrawerOpen] = useState(false)

  return (
    <div className="flex h-screen overflow-hidden">
      <VendorSidebar mobileOpen={drawerOpen} onMobileClose={() => setDrawerOpen(false)} />
      <div className="flex flex-1 flex-col overflow-hidden">
        <Header userName="서울꽃배달" onMenuOpen={() => setDrawerOpen(true)} />
        <main className="flex-1 overflow-y-auto">
          <div className="p-4 md:p-6">{children}</div>
        </main>
      </div>
    </div>
  )
}

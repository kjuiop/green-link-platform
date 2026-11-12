'use client'

import { useState } from 'react'
import { AdminSidebar } from '@/components/layout/AdminSidebar'
import { Header } from '@/components/layout/Header'

export default function VendorLayout({ children }: { children: React.ReactNode }) {
  const [drawerOpen, setDrawerOpen] = useState(false)

  return (
    <div className="flex h-screen overflow-hidden">
      <AdminSidebar mobileOpen={drawerOpen} onMobileClose={() => setDrawerOpen(false)} />
      <div className="flex flex-1 flex-col overflow-hidden">
        <Header onMenuOpen={() => setDrawerOpen(true)} accountHref="/vendor/account" />
        <main className="flex-1 overflow-y-auto">
          <div className="p-4 md:p-6">{children}</div>
        </main>
      </div>
    </div>
  )
}

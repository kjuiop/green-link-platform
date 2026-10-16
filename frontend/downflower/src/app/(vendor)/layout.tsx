import { VendorSidebar } from '@/components/layout/VendorSidebar'

export const dynamic = 'force-dynamic'

export default function VendorLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex h-screen overflow-hidden">
      <VendorSidebar />
      <main className="flex-1 overflow-y-auto">
        <div className="p-6">{children}</div>
      </main>
    </div>
  )
}

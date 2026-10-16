import { VendorSidebar } from '@/components/layout/VendorSidebar'
import { Header } from '@/components/layout/Header'

export const dynamic = 'force-dynamic'

export default function VendorLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex h-screen overflow-hidden">
      <VendorSidebar />
      <div className="flex flex-1 flex-col overflow-hidden">
        <Header userName="서울꽃배달" />
        <main className="flex-1 overflow-y-auto">
          <div className="p-6">{children}</div>
        </main>
      </div>
    </div>
  )
}

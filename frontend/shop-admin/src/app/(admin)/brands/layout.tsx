import { ManagerGuard } from '@/components/layout/ManagerGuard'

export default function BrandsLayout({ children }: { children: React.ReactNode }) {
  return <ManagerGuard>{children}</ManagerGuard>
}

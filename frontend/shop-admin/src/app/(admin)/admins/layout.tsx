import { ManagerGuard } from '@/components/layout/ManagerGuard'

export default function AdminsLayout({ children }: { children: React.ReactNode }) {
  return <ManagerGuard>{children}</ManagerGuard>
}

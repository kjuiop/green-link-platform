import { PartnerGuard } from '@/components/layout/PartnerGuard'

export default function OnurimLayout({ children }: { children: React.ReactNode }) {
  return <PartnerGuard>{children}</PartnerGuard>
}

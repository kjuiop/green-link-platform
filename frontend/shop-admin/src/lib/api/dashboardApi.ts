import type { Session } from '@/types/auth'
import type { DashboardSummary } from '@/types/dashboard'
import { mockDashboardSummary } from '@/lib/mock/dashboard'

export async function fetchDashboardSummary(session: Session): Promise<DashboardSummary> {
  return mockDashboardSummary(session)
}

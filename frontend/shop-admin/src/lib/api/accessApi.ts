import type { Session } from '@/types/auth'
import type { MyAccess } from '@/types/shopAdmin'
import { mockMyAccess, mockSubmitRequest } from '@/lib/mock/shopAdmins'

export function fetchMyAccess(session: Session): Promise<MyAccess> {
  return mockMyAccess(session.loginId)
}

export function submitAccessRequest(session: Session): Promise<void> {
  return mockSubmitRequest(session.loginId)
}

export type AdminRole = 'SUPER_ADMIN' | 'PARTNER_ADMIN' | 'BRAND_ADMIN'
export type CompanyType = 'ORDERING' | 'VENDOR' | 'BOTH'

const ROLE_KEY = 'adminRole'
const COMPANY_TYPE_KEY = 'companyType'

function read(key: string): string | null {
  return typeof window === 'undefined' ? null : localStorage.getItem(key)
}

export function saveSession(role: string | null | undefined, companyType: string | null | undefined): void {
  localStorage.removeItem('role')
  if (role) localStorage.setItem(ROLE_KEY, role)
  else localStorage.removeItem(ROLE_KEY)
  if (companyType) localStorage.setItem(COMPANY_TYPE_KEY, companyType)
  else localStorage.removeItem(COMPANY_TYPE_KEY)
}

export function clearSession(): void {
  localStorage.removeItem('role')
  localStorage.removeItem(ROLE_KEY)
  localStorage.removeItem(COMPANY_TYPE_KEY)
}

export function getAdminRole(): AdminRole | null {
  return read(ROLE_KEY) as AdminRole | null
}

export function getCompanyType(): CompanyType | null {
  return read(COMPANY_TYPE_KEY) as CompanyType | null
}

export function isSuperAdmin(): boolean {
  return getAdminRole() === 'SUPER_ADMIN'
}

/** 발주 능력: 슈퍼관리자이거나 파트너관리자의 회사 유형이 ORDERING/BOTH */
export function canOrder(): boolean {
  if (isSuperAdmin()) return true
  if (getAdminRole() !== 'PARTNER_ADMIN') return false
  const type = getCompanyType()
  return type === 'ORDERING' || type === 'BOTH'
}

/** 수주 능력: 파트너관리자의 회사 유형이 VENDOR/BOTH (슈퍼관리자는 수주 관점이 아님) */
export function canReceive(): boolean {
  if (getAdminRole() !== 'PARTNER_ADMIN') return false
  const type = getCompanyType()
  return type === 'VENDOR' || type === 'BOTH'
}

/** 수주 능력만 있는 사용자 (발주 불가) */
export function isVendorOnly(): boolean {
  return canReceive() && !canOrder()
}

/** 로그인한 회사 ID (로그인 응답의 vendorId 필드에 저장됨) */
export function getMyCompanyId(): string | null {
  return read('vendorId')
}

/**
 * 이 주문을 수주 관점으로 보는지 여부. 수주 전용 사용자는 항상 수주 관점이고,
 * 수발주(BOTH) 사용자는 자기 회사가 배정된 수주사인 주문만 수주 관점이다. (백엔드 OrderController.isVendorFor와 동일)
 */
export function isVendorSideOf(order: { vendorId?: string | null }): boolean {
  if (!canReceive()) return false
  if (!canOrder()) return true
  const mine = getMyCompanyId()
  return mine !== null && order.vendorId != null && String(order.vendorId) === mine
}

/** 설정 화면 등 발주사 단위 기능에서 쓰는 구분: 슈퍼관리자 / 발주 가능 파트너 / 수주 전용 파트너 */
export function getViewRole(): 'SUPER_ADMIN' | 'ORDERING' | 'VENDOR' | '' {
  if (isSuperAdmin()) return 'SUPER_ADMIN'
  if (canOrder()) return 'ORDERING'
  if (canReceive()) return 'VENDOR'
  return ''
}

/** 이전 버전 토큰으로 로그인된 세션(adminRole 없음)은 재로그인이 필요하다. */
export function hasCurrentSession(): boolean {
  return read('accessToken') !== null && read(ROLE_KEY) !== null
}

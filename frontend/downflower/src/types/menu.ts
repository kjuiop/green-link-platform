export type UserRole = 'SUPER_ADMIN' | 'ORDERING' | 'VENDOR'

export const ROLE_LABELS: Record<UserRole, string> = {
  SUPER_ADMIN: '슈퍼관리자',
  ORDERING: '발주업체',
  VENDOR: '수주업체',
}

export const ALL_ROLES: UserRole[] = ['SUPER_ADMIN', 'ORDERING', 'VENDOR']

export type MenuItem = {
  id: number
  name: string
  url: string
  sortOrder: number
  roles: UserRole[]
  children?: MenuItem[]
}

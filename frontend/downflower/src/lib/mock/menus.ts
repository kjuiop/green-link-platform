import { MenuItem, UserRole } from '@/types/menu'

export const mockMenus: MenuItem[] = [
  {
    id: 1,
    name: '대시보드',
    url: '/dashboard',
    sortOrder: 1,
    roles: ['SUPER_ADMIN', 'ORDERING', 'VENDOR'],
  },
  {
    id: 2,
    name: '신규 발주',
    url: '/orders/new',
    sortOrder: 2,
    roles: ['SUPER_ADMIN', 'ORDERING'],
  },
  {
    id: 3,
    name: '발주 목록',
    url: '/orders',
    sortOrder: 3,
    roles: ['SUPER_ADMIN', 'ORDERING'],
  },
  {
    id: 4,
    name: '수주 목록',
    url: '/orders/received',
    sortOrder: 4,
    roles: ['SUPER_ADMIN', 'ORDERING', 'VENDOR'],
  },
  {
    id: 5,
    name: '정산관리',
    url: '/settlement',
    sortOrder: 5,
    roles: ['SUPER_ADMIN', 'ORDERING', 'VENDOR'],
  },
  {
    id: 6,
    name: '설정',
    url: '/settings',
    sortOrder: 6,
    roles: ['SUPER_ADMIN', 'ORDERING', 'VENDOR'],
    children: [
      {
        id: 61,
        name: '문자 발송내역',
        url: '/messages',
        sortOrder: 1,
        roles: ['SUPER_ADMIN', 'ORDERING'],
      },
      {
        id: 62,
        name: '문자 설정',
        url: '/settings/messages',
        sortOrder: 2,
        roles: ['SUPER_ADMIN', 'ORDERING'],
      },
      {
        id: 63,
        name: '업체 계정관리',
        url: '/vendors',
        sortOrder: 3,
        roles: ['SUPER_ADMIN'],
      },
      {
        id: 64,
        name: '메뉴 관리',
        url: '/settings/menus',
        sortOrder: 4,
        roles: ['SUPER_ADMIN'],
      },
    ],
  },
]

export function getMenusByRole(role: UserRole): MenuItem[] {
  return mockMenus
    .filter((m) => m.roles.includes(role))
    .map((m) => ({
      ...m,
      children: m.children
        ?.filter((c) => c.roles.includes(role))
        .sort((a, b) => a.sortOrder - b.sortOrder),
    }))
    .sort((a, b) => a.sortOrder - b.sortOrder)
}

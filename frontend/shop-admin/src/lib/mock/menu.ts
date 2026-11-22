import type { ShopRole } from '@/types/auth'
import type { MenuItem } from '@/types/menu'
import { delay } from './delay'

export async function mockMenusByRole(role: ShopRole): Promise<MenuItem[]> {
  await delay(150)
  const menus: MenuItem[] = [
    { id: 1, name: '대시보드', url: '/dashboard', icon: 'dashboard' },
    { id: 2, name: '주문 관리', url: '/orders', icon: 'orders' },
    { id: 3, name: '상품 관리', url: '/products', icon: 'products' },
    { id: 4, name: '쇼핑몰 설정', url: '/shop-settings', icon: 'shop' },
  ]
  if (role === 'SUPER_ADMIN') {
    menus.push({ id: 5, name: '설정', url: '/settings', icon: 'settings' })
  }
  return menus
}

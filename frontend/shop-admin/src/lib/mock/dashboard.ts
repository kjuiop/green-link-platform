import type { Session } from '@/types/auth'
import type { DashboardSummary, RecentOrder } from '@/types/dashboard'
import { delay } from './delay'

const RECENT_ORDERS: RecentOrder[] = [
  { id: 5, orderNo: 'S-20261122-0005', customerName: '김민준', productName: '축하 꽃바구니 (대)', amount: 68000, status: '신규', orderedAt: '2026-11-22 09:12' },
  { id: 4, orderNo: 'S-20261122-0004', customerName: '이서연', productName: '근조 3단 화환', amount: 95000, status: '신규', orderedAt: '2026-11-22 08:41' },
  { id: 3, orderNo: 'S-20261121-0003', customerName: '박지후', productName: '축하 3단 화환', amount: 88000, status: '발주대기', orderedAt: '2026-11-21 17:30' },
  { id: 2, orderNo: 'S-20261121-0002', customerName: '최하윤', productName: '개업 화분 (중)', amount: 45000, status: '배송중', orderedAt: '2026-11-21 14:05' },
  { id: 1, orderNo: 'S-20261120-0001', customerName: '정도윤', productName: '꽃다발 (혼합)', amount: 52000, status: '완료', orderedAt: '2026-11-20 11:20' },
]

export async function mockDashboardSummary(session: Session): Promise<DashboardSummary> {
  await delay()
  if (session.role === 'SUPER_ADMIN') {
    return {
      scopeLabel: '전체',
      shopOpen: true,
      newOrders: 12,
      pendingOrders: 5,
      productCount: 128,
      recentOrders: RECENT_ORDERS,
    }
  }
  return {
    scopeLabel: session.companyName ?? '내 쇼핑몰',
    shopOpen: true,
    newOrders: 3,
    pendingOrders: 1,
    productCount: 24,
    recentOrders: RECENT_ORDERS.slice(0, 3),
  }
}

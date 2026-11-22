export type RecentOrderStatus = '신규' | '발주대기' | '배송중' | '완료' | '취소'

export interface RecentOrder {
  id: number
  orderNo: string
  customerName: string
  productName: string
  amount: number
  status: RecentOrderStatus
  orderedAt: string
}

export interface DashboardSummary {
  scopeLabel: string
  shopOpen: boolean
  newOrders: number
  pendingOrders: number
  productCount: number
  recentOrders: RecentOrder[]
}

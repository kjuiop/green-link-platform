import type { Order } from '@/types/order'

/** 발주금액(합계) = 상품금액 + 추가비용 + 배송완료 후 조정금액 */
export function orderTotalOf(o: Pick<Order, 'amount' | 'extraCost' | 'adjustAmount'>): number {
  return o.amount + (o.extraCost ?? 0) + (o.adjustAmount ?? 0)
}

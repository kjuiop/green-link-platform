export { cn } from "cn"

export function getDeliveryDateColor(deliveryAt: string): string {
  const datePart = deliveryAt.split(' ')[0]
  if (!datePart) return 'text-gray-900'
  const todaySeoul = new Date().toLocaleDateString('en-CA', { timeZone: 'Asia/Seoul' })
  const diffDays = Math.round(
    (new Date(datePart).getTime() - new Date(todaySeoul).getTime()) / 86400000,
  )
  if (diffDays < 0) return 'text-red-700'
  if (diffDays === 0) return 'text-orange-600'
  if (diffDays === 1) return 'text-blue-800'
  return 'text-gray-900'
}

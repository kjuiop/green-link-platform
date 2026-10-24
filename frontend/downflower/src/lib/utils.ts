export { cn } from "cn"

export function getDeliveryDateRowBg(deliveryAt: string): string {
  const datePart = deliveryAt.split(' ')[0]
  if (!datePart) return ''
  const todaySeoul = new Date().toLocaleDateString('en-CA', { timeZone: 'Asia/Seoul' })
  const diff = Math.round(
    (new Date(datePart).getTime() - new Date(todaySeoul).getTime()) / 86400000,
  )
  if (diff < 0) return 'bg-red-50'
  if (diff === 0) return 'bg-orange-50'
  if (diff === 1) return 'bg-blue-50'
  return ''
}

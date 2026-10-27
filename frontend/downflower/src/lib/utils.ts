export { cn } from "cn"

export function formatDeliveryShort(placeName?: string | null, address?: string | null): string {
  const regionPart = (() => {
    if (!address) return ''
    const tokens = address.trim().split(/\s+/)
    return tokens.slice(0, 2)
      .map((t) => t.replace(/(특별시|광역시|특별자치시|특별자치도)$/, ''))
      .join(' ')
  })()
  if (placeName) return [regionPart, placeName].filter(Boolean).join(' ')
  return regionPart
}

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

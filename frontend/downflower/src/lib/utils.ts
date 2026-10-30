export { cn } from "cn"

export const RIBBON_PHRASES = [
  '祝結婚', '축결혼', '祝華婚', '축화혼', '謹弔', '근조',
  '祝發展', '축발전', '祝開業', '축개업', '祝榮轉', '축영전',
  '祝昇進', '축승진', '祝生日', '축생일', '祝古稀', '축고희',
  '祝開院', '축개원', '祝就任', '축취임', '祝移轉', '축이전',
  '結婚을 祝賀드립니다', '삼가 故人의 冥福을 빕니다', '대박나세요',
  '無窮한 發展을 祈願합니다', '삼가 고인의 명복을 빕니다', '생신을 축하드립니다',
] as const

export function splitOrderNumber(orderNumber: string): [string, string] {
  const idx = orderNumber.lastIndexOf('-')
  return idx < 0 ? [orderNumber, ''] : [orderNumber.slice(0, idx), orderNumber.slice(idx + 1)]
}

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

export function getDeliveryDateTextStyle(deliveryAt: string): { color?: string; fontWeight?: number } {
  const datePart = deliveryAt.split(' ')[0]
  if (!datePart) return {}
  const todaySeoul = new Date().toLocaleDateString('en-CA', { timeZone: 'Asia/Seoul' })
  const diff = Math.round(
    (new Date(datePart).getTime() - new Date(todaySeoul).getTime()) / 86400000,
  )
  if (diff < 0) return { color: '#D32F2F', fontWeight: 600 }
  if (diff === 0) return { color: '#EF6C00', fontWeight: 600 }
  if (diff === 1) return { color: '#1565C0', fontWeight: 600 }
  return {}
}

export type RegionGuideCategory =
  | 'BAN_ALL'
  | 'BAN_WREATH'
  | 'WREATH_RESTRICTED'
  | 'ALL_ALLOWED'
  | 'RICE_WREATH'
  | 'RICE_RIBBON_ONLY'
  | 'OBJET'
  | 'THREE_TIER'
  | 'MEMORIAL_BASKET'
  | 'BASKET_ALT'
  | 'EXTRA_FEE'
  | 'PLACEMENT_TIME'
  | 'NOTICE'

export type RegionGuideTone = 'danger' | 'warn' | 'info' | 'ok'

export const REGION_GUIDE_CATEGORIES: { value: RegionGuideCategory; label: string; tone: RegionGuideTone }[] = [
  { value: 'BAN_ALL', label: '상품 일체 반입 금지', tone: 'danger' },
  { value: 'BAN_WREATH', label: '화환 반입 불가', tone: 'danger' },
  { value: 'WREATH_RESTRICTED', label: '특정 상품만 가능', tone: 'warn' },
  { value: 'RICE_RIBBON_ONLY', label: '쌀화환 리본 컷팅', tone: 'warn' },
  { value: 'RICE_WREATH', label: '쌀화환', tone: 'warn' },
  { value: 'THREE_TIER', label: '3단 화환', tone: 'warn' },
  { value: 'OBJET', label: '오브제', tone: 'warn' },
  { value: 'MEMORIAL_BASKET', label: '근조바구니', tone: 'warn' },
  { value: 'BASKET_ALT', label: '바구니 대체', tone: 'warn' },
  { value: 'EXTRA_FEE', label: '추가 배송비', tone: 'info' },
  { value: 'PLACEMENT_TIME', label: '배치시간', tone: 'info' },
  { value: 'ALL_ALLOWED', label: '반입 가능', tone: 'ok' },
  { value: 'NOTICE', label: '공통 유의사항', tone: 'info' },
]

export function regionGuideCategoryMeta(value: string) {
  return REGION_GUIDE_CATEGORIES.find((c) => c.value === value) ?? { value, label: value, tone: 'info' as RegionGuideTone }
}

export function regionGuideCategoryOrder(value: string): number {
  const idx = REGION_GUIDE_CATEGORIES.findIndex((c) => c.value === value)
  return idx === -1 ? REGION_GUIDE_CATEGORIES.length : idx
}

export const REGION_GUIDE_TONE_CLASS: Record<RegionGuideTone, { badge: string; box: string }> = {
  danger: { badge: 'bg-red-100 text-red-700', box: 'border-red-200 bg-red-50' },
  warn: { badge: 'bg-amber-100 text-amber-800', box: 'border-amber-200 bg-amber-50' },
  info: { badge: 'bg-blue-100 text-blue-700', box: 'border-blue-200 bg-blue-50' },
  ok: { badge: 'bg-green-100 text-green-700', box: 'border-green-200 bg-green-50' },
}

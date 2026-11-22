export const PRODUCT_CATEGORIES = [
  { value: 'ANNIVERSARY', label: '축하' },
  { value: 'MEMORIAL', label: '근조' },
  { value: 'ORCHID_EAST', label: '동양란' },
  { value: 'ORCHID_WEST', label: '서양란' },
  { value: 'FLOWER', label: '꽃' },
  { value: 'INDOOR', label: '관엽' },
  { value: 'RICE_WREATH', label: '쌀화환' },
] as const

export type ProductCategory = (typeof PRODUCT_CATEGORIES)[number]['value']

export function categoryLabel(value: string | null | undefined): string {
  if (!value) return '-'
  return PRODUCT_CATEGORIES.find((c) => c.value === value)?.label ?? value
}

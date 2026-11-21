import { SIDO_LIST } from '@/data/regions'

export type RegionLike = { sido: string; sigungu: string }

const shortSido = (sido: string) =>
  sido.replace(/(특별자치시|특별자치도|광역시|특별시|자치시|자치도|시|도)$/, '')

export function formatRegions(regions: RegionLike[]): string {
  const grouped = new Map<string, string[]>()
  for (const r of regions) {
    const list = grouped.get(r.sido) ?? []
    list.push(r.sigungu)
    grouped.set(r.sido, list)
  }

  const nationwide = SIDO_LIST.length > 0 && SIDO_LIST.every((s) => grouped.get(s.name)?.includes('전체'))
  if (nationwide) return '전국 전체'

  return [...grouped.entries()]
    .map(([sido, list]) => `${shortSido(sido)} ${list.includes('전체') ? '전체' : list.join(', ')}`)
    .join(' · ')
}

'use client'

import { useMemo } from 'react'
import { X } from 'lucide-react'
import type { RegionGuide } from '@/lib/api/regionGuideApi'
import {
  REGION_GUIDE_TONE_CLASS,
  regionGuideCategoryMeta,
  regionGuideCategoryOrder,
} from '@/lib/constants/regionGuideCategory'

type Props = {
  sido: string
  guides: RegionGuide[]
  onClose: () => void
}

export default function RegionGuidePopup({ sido, guides, onClose }: Props) {
  const groups = useMemo(() => {
    const map = new Map<string, RegionGuide[]>()
    for (const g of guides) {
      const list = map.get(g.category) ?? []
      list.push(g)
      map.set(g.category, list)
    }
    return [...map.entries()].sort(([a], [b]) => regionGuideCategoryOrder(a) - regionGuideCategoryOrder(b))
  }, [guides])

  return (
    <div
      className="fixed inset-0 z-[110] flex items-center justify-center bg-black/60 p-0 sm:p-4"
      onClick={(e) => { e.stopPropagation(); onClose() }}
    >
      <div
        className="flex h-full w-full max-w-2xl flex-col bg-white shadow-xl sm:h-auto sm:max-h-[85vh] sm:rounded-lg"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between border-b px-4 py-3">
          <h2 className="text-base font-semibold">[{sido}] 화환 반입안내</h2>
          <button onClick={onClose} className="rounded p-1 hover:bg-gray-100" aria-label="닫기">
            <X className="h-4 w-4" />
          </button>
        </div>

        <div className="min-h-0 flex-1 space-y-4 overflow-y-auto p-4">
          {groups.map(([category, items]) => {
            const meta = regionGuideCategoryMeta(category)
            const tone = REGION_GUIDE_TONE_CLASS[meta.tone]
            return (
              <section key={category} className={`rounded-lg border p-3 ${tone.box}`}>
                <span className={`inline-block rounded px-2 py-0.5 text-xs font-semibold ${tone.badge}`}>{meta.label}</span>
                <ul className="mt-2 space-y-2">
                  {items.map((g) => (
                    <li key={g.id} className="text-sm text-gray-800">
                      {(g.placeName || g.sigungu) && (
                        <p className="font-medium">
                          {g.sigungu && <span className="mr-1.5 text-xs text-gray-500">{g.sigungu}</span>}
                          {g.placeName}
                        </p>
                      )}
                      <p className="whitespace-pre-line text-[13px] leading-relaxed text-gray-700">{g.content}</p>
                      {g.aliases && <p className="mt-0.5 text-xs text-gray-400">같은 장소: {g.aliases}</p>}
                    </li>
                  ))}
                </ul>
              </section>
            )
          })}
        </div>

        <div className="space-y-2 border-t px-4 py-3">
          <p className="text-xs text-gray-500">실제 반입 가능 여부와 배송비는 발주 전 장소 및 수주사에 재확인해 주세요.</p>
          <button onClick={onClose} className="w-full rounded bg-gray-800 py-2 text-sm text-white hover:bg-gray-900">
            닫기
          </button>
        </div>
      </div>
    </div>
  )
}

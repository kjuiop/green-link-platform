'use client'

import { useState, useEffect } from 'react'
import { ImageIcon, Download } from 'lucide-react'
import { Dialog, DialogContent, DialogTitle } from '@/components/ui/dialog'
import { cn } from '@/lib/utils'
import { fetchReferencePhotos, ReferencePhotoResult } from '@/lib/api/orderApi'

/** 목록에서 상품명 옆 아이콘 — 클릭 시 팝업 */
export function ReferencePhotoIcon({ orderId, count }: { orderId: string; count: number }) {
  const [open, setOpen] = useState(false)

  if (count === 0) return null

  return (
    <>
      <button
        type="button"
        onClick={(e) => { e.stopPropagation(); setOpen(true) }}
        className="ml-1 inline-flex shrink-0 items-center gap-0.5 rounded bg-blue-50 px-1 py-0.5 text-xs text-blue-600 hover:bg-blue-100"
        title="참고 이미지 보기"
      >
        <ImageIcon className="h-3 w-3" />
        <span>{count}</span>
      </button>
      <ReferencePhotoModal open={open} onClose={() => setOpen(false)} orderId={orderId} />
    </>
  )
}

/** 상세 페이지용 인라인 갤러리 섹션 */
export function ReferencePhotoSection({ orderId }: { orderId: string }) {
  const [photos, setPhotos] = useState<ReferencePhotoResult[]>([])
  const [loading, setLoading] = useState(true)
  const [open, setOpen] = useState(false)
  const [activeIdx, setActiveIdx] = useState(0)

  useEffect(() => {
    fetchReferencePhotos(orderId)
      .then(setPhotos)
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [orderId])

  if (loading) return <p className="text-xs text-gray-400">불러오는 중...</p>
  if (photos.length === 0) return <p className="text-xs text-gray-400">참고 이미지 없음</p>

  return (
    <>
      <div className="flex flex-wrap gap-2">
        {photos.map((p, idx) => (
          <button
            key={p.id}
            type="button"
            onClick={() => { setActiveIdx(idx); setOpen(true) }}
            className="h-20 w-20 overflow-hidden rounded border border-gray-200 hover:opacity-80"
          >
            <img src={p.photoUrl} alt={`참고 이미지 ${idx + 1}`} className="h-full w-full object-cover" />
          </button>
        ))}
      </div>
      <ReferencePhotoModal open={open} onClose={() => setOpen(false)} orderId={orderId} initialIdx={activeIdx} photos={photos} />
    </>
  )
}

/** 팝업 모달 — 사진보기와 동일한 구성 */
function ReferencePhotoModal({
  open,
  onClose,
  orderId,
  initialIdx = 0,
  photos: initialPhotos,
}: {
  open: boolean
  onClose: () => void
  orderId: string
  initialIdx?: number
  photos?: ReferencePhotoResult[]
}) {
  const [photos, setPhotos] = useState<ReferencePhotoResult[]>(initialPhotos ?? [])
  const [loading, setLoading] = useState(!initialPhotos)
  const [activeIdx, setActiveIdx] = useState(initialIdx)

  useEffect(() => {
    if (!open) return
    setActiveIdx(initialIdx)
    if (initialPhotos) { setPhotos(initialPhotos); return }
    setLoading(true)
    fetchReferencePhotos(orderId).then(setPhotos).catch(() => {}).finally(() => setLoading(false))
  }, [open, orderId])

  function handleDownloadCurrent() {
    const photo = photos[activeIdx]
    if (!photo) return
    const a = document.createElement('a')
    a.href = photo.photoUrl
    a.download = `참고이미지_${activeIdx + 1}.jpg`
    a.click()
  }

  async function handleDownloadAll() {
    for (let i = 0; i < photos.length; i++) {
      const a = document.createElement('a')
      a.href = photos[i].photoUrl
      a.download = `참고이미지_${i + 1}.jpg`
      a.click()
      await new Promise((r) => setTimeout(r, 200))
    }
  }

  return (
    <Dialog open={open} onOpenChange={(v) => { if (!v) onClose() }}>
      <DialogContent className="sm:max-w-5xl" showCloseButton>
        <div className="flex items-center justify-between pr-8">
          <DialogTitle>
            참고 이미지 ({photos.length}장)
          </DialogTitle>
          {photos.length > 0 && (
            <div className="flex gap-2">
              <button
                type="button"
                onClick={handleDownloadCurrent}
                className="inline-flex items-center gap-1.5 rounded-md border px-2.5 py-1.5 text-xs font-medium text-gray-600 hover:bg-gray-50"
              >
                <Download className="h-3.5 w-3.5" />
                현재 사진
              </button>
              <button
                type="button"
                onClick={handleDownloadAll}
                className="inline-flex items-center gap-1.5 rounded-md border border-green-600 px-2.5 py-1.5 text-xs font-medium text-green-700 hover:bg-green-50"
              >
                <Download className="h-3.5 w-3.5" />
                전체 다운로드
              </button>
            </div>
          )}
        </div>

        <div className="flex gap-3" style={{ height: 'calc(80vh - 80px)' }}>
          {loading ? (
            <p className="m-auto text-xs text-gray-400">불러오는 중...</p>
          ) : photos.length === 0 ? (
            <p className="m-auto text-xs text-gray-400">참고 이미지 없음</p>
          ) : (
            <>
              <div className="flex flex-col gap-2 overflow-y-auto">
                {photos.map((p, i) => (
                  <button
                    key={p.id}
                    type="button"
                    onMouseEnter={() => setActiveIdx(i)}
                    onClick={() => setActiveIdx(i)}
                    className={cn(
                      'h-20 w-20 shrink-0 overflow-hidden rounded-lg border-2 transition-all',
                      activeIdx === i ? 'border-green-600 ring-2 ring-green-200' : 'border-transparent hover:border-gray-300',
                    )}
                  >
                    <img src={p.photoUrl} alt={`참고 이미지 ${i + 1}`} className="h-full w-full object-cover" />
                  </button>
                ))}
              </div>

              <div className="flex flex-1 items-center justify-center overflow-hidden rounded-lg border bg-gray-50">
                {photos[activeIdx] && (
                  <img
                    src={photos[activeIdx].photoUrl}
                    alt="참고 이미지"
                    className="h-full w-full object-contain"
                  />
                )}
              </div>
            </>
          )}
        </div>
      </DialogContent>
    </Dialog>
  )
}

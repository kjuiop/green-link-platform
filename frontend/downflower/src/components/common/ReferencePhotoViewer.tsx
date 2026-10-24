'use client'

import { useState, useEffect, useRef } from 'react'
import { ImageIcon, X, ChevronLeft, ChevronRight } from 'lucide-react'
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
      {open && (
        <ReferencePhotoModal orderId={orderId} onClose={() => setOpen(false)} />
      )}
    </>
  )
}

/** 상세 페이지용 인라인 갤러리 섹션 */
export function ReferencePhotoSection({ orderId }: { orderId: string }) {
  const [photos, setPhotos] = useState<ReferencePhotoResult[]>([])
  const [loading, setLoading] = useState(true)
  const [lightboxIdx, setLightboxIdx] = useState<number | null>(null)

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
            onClick={() => setLightboxIdx(idx)}
            className="h-20 w-20 overflow-hidden rounded border border-gray-200 hover:opacity-80"
          >
            <img src={p.photoUrl} alt={`참고 이미지 ${idx + 1}`} className="h-full w-full object-cover" />
          </button>
        ))}
      </div>
      {lightboxIdx !== null && (
        <Lightbox
          photos={photos}
          initialIdx={lightboxIdx}
          onClose={() => setLightboxIdx(null)}
        />
      )}
    </>
  )
}

/** 목록 팝업 모달 */
function ReferencePhotoModal({ orderId, onClose }: { orderId: string; onClose: () => void }) {
  const [photos, setPhotos] = useState<ReferencePhotoResult[]>([])
  const [loading, setLoading] = useState(true)
  const [lightboxIdx, setLightboxIdx] = useState<number | null>(null)

  useEffect(() => {
    fetchReferencePhotos(orderId).then(setPhotos).catch(() => {}).finally(() => setLoading(false))
  }, [orderId])

  return (
    <>
      <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50" onClick={onClose}>
        <div className="relative w-full max-w-md rounded-lg bg-white p-5 shadow-xl" onClick={(e) => e.stopPropagation()}>
          <div className="mb-3 flex items-center justify-between">
            <h3 className="text-sm font-semibold">참고 이미지</h3>
            <button onClick={onClose} className="text-gray-400 hover:text-gray-600"><X className="h-4 w-4" /></button>
          </div>
          {loading ? (
            <p className="py-6 text-center text-xs text-gray-400">불러오는 중...</p>
          ) : photos.length === 0 ? (
            <p className="py-6 text-center text-xs text-gray-400">참고 이미지 없음</p>
          ) : (
            <div className="flex flex-wrap gap-2">
              {photos.map((p, idx) => (
                <button
                  key={p.id}
                  type="button"
                  onClick={() => setLightboxIdx(idx)}
                  className="h-20 w-20 overflow-hidden rounded border border-gray-200 hover:opacity-80"
                >
                  <img src={p.photoUrl} alt={`참고 이미지 ${idx + 1}`} className="h-full w-full object-cover" />
                </button>
              ))}
            </div>
          )}
        </div>
      </div>
      {lightboxIdx !== null && (
        <Lightbox
          photos={photos}
          initialIdx={lightboxIdx}
          onClose={() => setLightboxIdx(null)}
        />
      )}
    </>
  )
}

function Lightbox({ photos, initialIdx, onClose }: { photos: ReferencePhotoResult[]; initialIdx: number; onClose: () => void }) {
  const [idx, setIdx] = useState(initialIdx)

  const prev = () => setIdx((i) => (i - 1 + photos.length) % photos.length)
  const next = () => setIdx((i) => (i + 1) % photos.length)

  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose()
      if (e.key === 'ArrowLeft') prev()
      if (e.key === 'ArrowRight') next()
    }
    window.addEventListener('keydown', handler)
    return () => window.removeEventListener('keydown', handler)
  }, [photos.length])

  return (
    <div className="fixed inset-0 z-[60] flex items-center justify-center bg-black/80" onClick={onClose}>
      <button onClick={(e) => { e.stopPropagation(); prev() }} className="absolute left-4 text-white hover:text-gray-300">
        <ChevronLeft className="h-8 w-8" />
      </button>
      <div onClick={(e) => e.stopPropagation()} className="relative max-h-[80vh] max-w-[80vw]">
        <img src={photos[idx].photoUrl} alt={`참고 이미지 ${idx + 1}`} className="max-h-[80vh] max-w-[80vw] rounded object-contain" />
        <span className="absolute bottom-2 right-2 rounded bg-black/50 px-2 py-0.5 text-xs text-white">{idx + 1} / {photos.length}</span>
      </div>
      <button onClick={(e) => { e.stopPropagation(); next() }} className="absolute right-4 text-white hover:text-gray-300">
        <ChevronRight className="h-8 w-8" />
      </button>
      <button onClick={onClose} className="absolute right-4 top-4 text-white hover:text-gray-300">
        <X className="h-6 w-6" />
      </button>
    </div>
  )
}

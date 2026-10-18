'use client'

import { useState, useRef, useEffect } from 'react'
import { Dialog, DialogContent, DialogTitle } from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { ClipboardCheck, Upload, X, Images, Download } from 'lucide-react'
import { cn } from '@/lib/utils'

interface SavedData {
  receiverName: string
  photos: { id: string; url: string }[]
}

interface Props {
  orderNumber: string
  senderName?: string
  recipientName?: string
  initialSaved?: SavedData
}


export function ReceiptConfirmButton({ orderNumber, senderName, recipientName, initialSaved }: Props) {
  const [formOpen, setFormOpen] = useState(false)
  const [viewerOpen, setViewerOpen] = useState(false)
  const [saved, setSaved] = useState<SavedData | null>(initialSaved ?? null)

  function handleSave(data: SavedData) {
    setSaved(data)
    setFormOpen(false)
  }

  if (saved) {
    return (
      <div className="space-y-1">
        <div className="text-sm font-medium">{saved.receiverName || '-'}</div>
        {saved.photos.length > 0 && (
          <>
            <button
              type="button"
              onClick={(e) => { e.preventDefault(); e.stopPropagation(); setViewerOpen(true) }}
              className="inline-flex items-center gap-1 rounded-md border border-blue-300 bg-blue-50 px-2 py-1 text-xs font-medium text-blue-700 hover:bg-blue-100"
            >
              <Images className="h-3 w-3" />
              사진보기
            </button>

            <PhotoViewerDialog
              open={viewerOpen}
              onClose={() => setViewerOpen(false)}
              photos={saved.photos}
              orderNumber={orderNumber}
            />
          </>
        )}
      </div>
    )
  }

  return (
    <>
      <button
        type="button"
        onClick={(e) => { e.preventDefault(); e.stopPropagation(); setFormOpen(true) }}
        className="inline-flex items-center gap-1 rounded-md border border-gray-300 bg-white px-2 py-1 text-xs font-medium text-gray-600 hover:border-gray-400 hover:bg-gray-50"
      >
        <ClipboardCheck className="h-3 w-3" />
        인수확인
      </button>

      <ReceiptFormDialog
        open={formOpen}
        onClose={() => setFormOpen(false)}
        onSave={handleSave}
        orderNumber={orderNumber}
        senderName={senderName}
        recipientName={recipientName}
      />
    </>
  )
}

// ---- 인수확인 입력 폼 ----

interface FormDialogProps {
  open: boolean
  onClose: () => void
  onSave: (data: SavedData) => void
  orderNumber: string
  senderName?: string
  recipientName?: string
}

function ReceiptFormDialog({ open, onClose, onSave, orderNumber, senderName, recipientName }: FormDialogProps) {
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [photos, setPhotos] = useState<{ id: string; url: string }[]>([])
  const [receiverName, setReceiverName] = useState('')
  const [receiverRelation, setReceiverRelation] = useState('')
  const [delivered, setDelivered] = useState(false)
  const [memo, setMemo] = useState('')
  const [confirmTime, setConfirmTime] = useState('')
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (open) {
      photos.forEach((p) => URL.revokeObjectURL(p.url))
      setPhotos([])
      setReceiverName('')
      setReceiverRelation('')
      setDelivered(false)
      setMemo('')
      const now = new Date()
      setConfirmTime(
        `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ` +
        `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
      )
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open])

  function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    const files = Array.from(e.target.files ?? [])
    const next = files.map((f, i) => ({
      id: `rc-${Date.now()}-${i}`,
      url: URL.createObjectURL(f),
    }))
    setPhotos((prev) => [...prev, ...next])
    e.target.value = ''
  }

  function handleSave() {
    setSaving(true)
    // TODO: 인수확인 저장 API 호출
    setTimeout(() => {
      setSaving(false)
      onSave({ receiverName, photos })
    }, 800)
  }

  return (
    <Dialog open={open} onOpenChange={(v) => { if (!v) onClose() }}>
      <DialogContent className="sm:max-w-2xl" showCloseButton>
        <DialogTitle>
          인수확인
          <span className="ml-2 text-xs font-normal text-gray-400">{orderNumber}</span>
        </DialogTitle>

        {/* 보내는분 / 받는분 */}
        <div className="flex gap-6 rounded-lg border bg-gray-50 px-4 py-2.5 text-sm">
          <span>
            <span className="text-gray-500">보내는분</span>
            <span className="ml-2 font-medium">{senderName ?? '-'}</span>
          </span>
          <span>
            <span className="text-gray-500">받는분</span>
            <span className="ml-2 font-medium">{recipientName ?? '-'}</span>
          </span>
        </div>

        {/* 폼 테이블 */}
        <table className="w-full divide-y rounded-lg border text-sm [border-spacing:0] overflow-hidden">
          <colgroup>
            <col className="w-20" />
            <col />
            <col className="w-20" />
            <col />
          </colgroup>
          <tbody className="divide-y">
            {/* 배송사진 */}
            <tr>
              <td className="px-3 py-2.5 align-top text-gray-500">배송사진</td>
              <td colSpan={3} className="px-3 py-2.5">
                <div className="flex flex-wrap gap-2">
                  {photos.map((p) => (
                    <div key={p.id} className="relative h-16 w-16">
                      <img src={p.url} alt="배송사진" className="h-full w-full rounded border object-cover" />
                      <button
                        type="button"
                        onClick={() => { URL.revokeObjectURL(p.url); setPhotos((prev) => prev.filter((x) => x.id !== p.id)) }}
                        className="absolute -right-1 -top-1 flex h-4 w-4 items-center justify-center rounded-full bg-red-500 text-white hover:bg-red-600"
                      >
                        <X className="h-2.5 w-2.5" />
                      </button>
                    </div>
                  ))}
                  <button
                    type="button"
                    onClick={() => fileInputRef.current?.click()}
                    className="flex h-16 w-16 flex-col items-center justify-center gap-0.5 rounded border-2 border-dashed border-gray-300 text-gray-400 hover:border-gray-400"
                  >
                    <Upload className="h-4 w-4" />
                    <span className="text-[10px]">업로드</span>
                  </button>
                  <input ref={fileInputRef} type="file" accept="image/*" multiple className="hidden" onChange={handleFileChange} />
                </div>
              </td>
            </tr>

            {/* 인수자 / 관계 */}
            <tr>
              <td className="px-3 py-2.5 text-gray-500">인수자</td>
              <td className="px-3 py-2.5">
                <input
                  type="text"
                  placeholder="이름"
                  value={receiverName}
                  onChange={(e) => setReceiverName(e.target.value)}
                  className="w-full rounded-md border px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
                />
              </td>
              <td className="px-3 py-2.5 text-gray-500">관계</td>
              <td className="px-3 py-2.5">
                <input
                  type="text"
                  placeholder=""
                  value={receiverRelation}
                  onChange={(e) => setReceiverRelation(e.target.value)}
                  className="w-full rounded-md border px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
                />
              </td>
            </tr>

            {/* 배달여부 / 인수시간 */}
            <tr>
              <td className="px-3 py-2.5 text-gray-500">배달여부</td>
              <td className="px-3 py-2.5">
                <label className="flex cursor-pointer items-center gap-1.5">
                  <input
                    type="checkbox"
                    checked={delivered}
                    onChange={(e) => setDelivered(e.target.checked)}
                    className="accent-green-700"
                  />
                  <span className={cn('font-medium', delivered ? 'text-green-700' : 'text-gray-400')}>완료</span>
                </label>
              </td>
              <td className="px-3 py-2.5 text-gray-500">인수시간</td>
              <td className="px-3 py-2.5 text-sm">{confirmTime}</td>
            </tr>

            {/* 비고 */}
            <tr>
              <td className="px-3 py-2.5 align-top text-gray-500">비고</td>
              <td colSpan={3} className="px-3 py-2.5">
                <textarea
                  placeholder="특이사항을 입력하세요"
                  rows={2}
                  value={memo}
                  onChange={(e) => setMemo(e.target.value)}
                  className="w-full resize-none rounded-md border px-2 py-1.5 text-sm placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-green-700"
                />
              </td>
            </tr>
          </tbody>
        </table>

        <Button className="w-full bg-green-700 hover:bg-green-800" disabled={saving} onClick={handleSave}>
          {saving ? '저장 중…' : '저장하기'}
        </Button>
      </DialogContent>
    </Dialog>
  )
}

// ---- 사진보기 뷰어 ----

interface ViewerProps {
  open: boolean
  onClose: () => void
  photos: { id: string; url: string }[]
  orderNumber: string
}

function PhotoViewerDialog({ open, onClose, photos, orderNumber }: ViewerProps) {
  const [activeIdx, setActiveIdx] = useState(0)

  useEffect(() => {
    if (open) setActiveIdx(0)
  }, [open])

  function handleDownloadCurrent() {
    const photo = photos[activeIdx]
    if (!photo) return
    const a = document.createElement('a')
    a.href = photo.url
    a.download = `${orderNumber}_사진${activeIdx + 1}.jpg`
    a.click()
  }

  async function handleDownloadAll() {
    for (let i = 0; i < photos.length; i++) {
      const a = document.createElement('a')
      a.href = photos[i].url
      a.download = `${orderNumber}_사진${i + 1}.jpg`
      a.click()
      await new Promise((r) => setTimeout(r, 200))
    }
  }

  return (
    <Dialog open={open} onOpenChange={(v) => { if (!v) onClose() }}>
      <DialogContent className="sm:max-w-5xl" showCloseButton>
        <div className="flex items-center justify-between pr-8">
          <DialogTitle>
            배송사진 ({photos.length}장)
            <span className="ml-2 text-xs font-normal text-gray-400">{orderNumber}</span>
          </DialogTitle>
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
        </div>

        <div className="flex gap-3" style={{ height: 'calc(80vh - 80px)' }}>
          {/* 왼쪽 썸네일 목록 */}
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
                <img src={p.url} alt={`사진 ${i + 1}`} className="h-full w-full object-cover" />
              </button>
            ))}
          </div>

          {/* 오른쪽 크게 보기 */}
          <div className="flex flex-1 items-center justify-center overflow-hidden rounded-lg border bg-gray-50">
            {photos[activeIdx] && (
              <img
                src={photos[activeIdx].url}
                alt="배송사진"
                className="h-full w-full object-contain"
              />
            )}
          </div>
        </div>
      </DialogContent>
    </Dialog>
  )
}

'use client'

import { useState, useEffect, useRef } from 'react'
import { Dialog, DialogContent, DialogTitle } from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { ClipboardCheck, Upload, X, Images, Download } from 'lucide-react'
import { cn } from '@/lib/utils'
import { fetchReceiptConfirmation, saveReceiptConfirmation, updateReceiptInfo, updateOrderStatus, ReceiptConfirmationResult } from '@/lib/api/orderApi'

interface Props {
  orderId: string
  orderNumber: string
  senderName?: string
  recipientName?: string
  // 목록 페이지에서 사전 로딩된 값. undefined이면 마운트 시 자동 fetch
  initialHasReceipt?: boolean
  onDeliveryComplete?: () => void
}

export function ReceiptConfirmButton({ orderId, orderNumber, senderName, recipientName, initialHasReceipt, onDeliveryComplete }: Props) {
  const [formOpen, setFormOpen] = useState(false)
  const [editOpen, setEditOpen] = useState(false)
  const [viewerOpen, setViewerOpen] = useState(false)
  // undefined = 아직 fetch 안함, null = fetch 완료 but 데이터 없음, object = 데이터 있음
  const [saved, setSaved] = useState<ReceiptConfirmationResult | null | undefined>(
    initialHasReceipt !== undefined ? null : undefined
  )
  const [fetching, setFetching] = useState(initialHasReceipt === undefined)

  useEffect(() => {
    if (initialHasReceipt !== undefined) return
    fetchReceiptConfirmation(orderId)
      .then(data => setSaved(data))
      .catch(() => setSaved(null))
      .finally(() => setFetching(false))
  }, [orderId, initialHasReceipt])

  async function handleConfirmedClick(e: React.MouseEvent) {
    e.preventDefault()
    e.stopPropagation()
    if (saved) { setEditOpen(true); return }
    // initialHasReceipt=true이지만 아직 full data를 fetch하지 않은 경우 lazy-load 후 edit dialog 오픈
    setFetching(true)
    try {
      const data = await fetchReceiptConfirmation(orderId)
      setSaved(data)
      if (data) setEditOpen(true)
      else setFormOpen(true)
    } catch {
      setFormOpen(true)
    } finally {
      setFetching(false)
    }
  }

  function handleOpen(e: React.MouseEvent) {
    e.preventDefault()
    e.stopPropagation()
    if (fetching) return
    setFormOpen(true)
  }

  function handleSave(result: ReceiptConfirmationResult) {
    setSaved(result)
    setFormOpen(false)
  }

  function handleUpdate(result: ReceiptConfirmationResult) {
    setSaved(result)
    setEditOpen(false)
  }

  // saved가 있거나 사전 로딩 힌트가 true면 confirmed 상태로 렌더링
  if (saved || initialHasReceipt === true) {
    return (
      <div className="flex flex-col items-start gap-1">
        <button
          type="button"
          onClick={handleConfirmedClick}
          disabled={fetching}
          className="inline-flex items-center gap-1 rounded-md border border-gray-300 bg-white px-2 py-1 text-xs font-medium text-gray-600 hover:border-gray-400 hover:bg-gray-50 disabled:cursor-wait"
        >
          <ClipboardCheck className="h-3 w-3" />
          {fetching ? '확인 중…' : '인수확인'}
        </button>
        {saved && (saved.sitePhotoUrls.length > 0 || saved.storePhotoUrls.length > 0) && (
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
              photos={[...saved.sitePhotoUrls, ...saved.storePhotoUrls].map((url, i) => ({ id: String(i), url }))}
              orderNumber={orderNumber}
            />
          </>
        )}

        {saved && (
          <ReceiptEditDialog
            open={editOpen}
            onClose={() => setEditOpen(false)}
            onSave={handleUpdate}
            orderId={orderId}
            orderNumber={orderNumber}
            senderName={senderName}
            recipientName={recipientName}
            current={saved}
          />
        )}

        <ReceiptFormDialog
          open={formOpen}
          onClose={() => setFormOpen(false)}
          onSave={handleSave}
          onDeliveryComplete={onDeliveryComplete}
          orderId={orderId}
          orderNumber={orderNumber}
          senderName={senderName}
          recipientName={recipientName}
        />
      </div>
    )
  }

  return (
    <>
      <button
        type="button"
        onClick={handleOpen}
        disabled={fetching}
        className="inline-flex items-center gap-1 rounded-md border border-gray-300 bg-white px-2 py-1 text-xs font-medium text-gray-600 hover:border-gray-400 hover:bg-gray-50 disabled:cursor-wait"
      >
        <ClipboardCheck className="h-3 w-3" />
        {fetching ? '확인 중…' : '인수확인'}
      </button>

      <ReceiptFormDialog
        open={formOpen}
        onClose={() => setFormOpen(false)}
        onSave={handleSave}
        onDeliveryComplete={onDeliveryComplete}
        orderId={orderId}
        orderNumber={orderNumber}
        senderName={senderName}
        recipientName={recipientName}
      />
    </>
  )
}

// ---- 인수확인 신규 입력 폼 ----

interface FormDialogProps {
  open: boolean
  onClose: () => void
  onSave: (result: ReceiptConfirmationResult) => void
  onDeliveryComplete?: () => void
  orderId: string
  orderNumber: string
  senderName?: string
  recipientName?: string
}

function ReceiptFormDialog({ open, onClose, onSave, onDeliveryComplete, orderId, orderNumber, senderName, recipientName }: FormDialogProps) {
  const [sitePhotoFiles, setSitePhotoFiles] = useState<{ id: string; file: File; url: string }[]>([])
  const [storePhotoFiles, setStorePhotoFiles] = useState<{ id: string; file: File; url: string }[]>([])
  const sitePhotoInputRef = useRef<HTMLInputElement>(null)
  const storePhotoInputRef = useRef<HTMLInputElement>(null)
  const [receiverName, setReceiverName] = useState('')
  const [receiverRelation, setReceiverRelation] = useState('')
  const [delivered, setDelivered] = useState(false)
  const [memo, setMemo] = useState('')
  const [confirmTime, setConfirmTime] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (open) {
      sitePhotoFiles.forEach((p) => URL.revokeObjectURL(p.url))
      storePhotoFiles.forEach((p) => URL.revokeObjectURL(p.url))
      setSitePhotoFiles([])
      setStorePhotoFiles([])
      setReceiverName('')
      setReceiverRelation('')
      setDelivered(false)
      setMemo('')
      setError(null)
      const now = new Date()
      setConfirmTime(
        `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ` +
        `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
      )
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open])

  function makeFileChangeHandler(setter: React.Dispatch<React.SetStateAction<{ id: string; file: File; url: string }[]>>) {
    return (e: React.ChangeEvent<HTMLInputElement>) => {
      const files = Array.from(e.target.files ?? [])
      const next = files.map((f, i) => ({
        id: `rc-${Date.now()}-${i}`,
        file: f,
        url: URL.createObjectURL(f),
      }))
      setter((prev) => [...prev, ...next])
      e.target.value = ''
    }
  }

  function removeFile(
    setter: React.Dispatch<React.SetStateAction<{ id: string; file: File; url: string }[]>>,
    id: string,
    url: string
  ) {
    URL.revokeObjectURL(url)
    setter((prev) => prev.filter((x) => x.id !== id))
  }

  async function handleSave() {
    if (!receiverName.trim()) {
      setError('인수자 이름을 입력해주세요.')
      return
    }
    if (!receiverRelation.trim()) {
      setError('관계를 입력해주세요.')
      return
    }
    if (sitePhotoFiles.length === 0) {
      setError('현장사진을 1장 이상 업로드해주세요.')
      return
    }
    setSaving(true)
    setError(null)
    try {
      const result = await saveReceiptConfirmation(
        orderId,
        { receiverName, receiverRelation, delivered, memo, confirmedAt: confirmTime },
        sitePhotoFiles.map((p) => p.file),
        storePhotoFiles.map((p) => p.file)
      )
      sitePhotoFiles.forEach((p) => URL.revokeObjectURL(p.url))
      storePhotoFiles.forEach((p) => URL.revokeObjectURL(p.url))
      try {
        await updateOrderStatus(orderId, 'complete', result.sitePhotoUrls)
      } catch {
        // 이미 배송완료 상태인 경우 무시
      }
      onSave(result)
      onDeliveryComplete?.()
    } catch (e) {
      setError(e instanceof Error ? e.message : '저장에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <Dialog open={open} onOpenChange={(v) => { if (!v) onClose() }}>
      <DialogContent className="sm:max-w-2xl" showCloseButton>
        <DialogTitle>
          인수확인
          <span className="ml-2 text-xs font-normal text-gray-400">{orderNumber}</span>
        </DialogTitle>

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

        <table className="w-full divide-y rounded-lg border text-sm [border-spacing:0] overflow-hidden">
          <colgroup>
            <col className="w-20" />
            <col />
            <col className="w-20" />
            <col />
          </colgroup>
          <tbody className="divide-y">
            <tr>
              <td className="px-3 py-2.5 align-top text-gray-500">
                현장사진
                <span className="ml-0.5 text-red-500">*</span>
              </td>
              <td colSpan={3} className="px-3 py-2.5">
                <div className="flex flex-wrap gap-2">
                  {sitePhotoFiles.map((p) => (
                    <div key={p.id} className="relative h-16 w-16">
                      <img src={p.url} alt="현장사진" className="h-full w-full rounded border object-cover" />
                      <button
                        type="button"
                        onClick={() => removeFile(setSitePhotoFiles, p.id, p.url)}
                        className="absolute -right-1 -top-1 flex h-4 w-4 items-center justify-center rounded-full bg-red-500 text-white hover:bg-red-600"
                      >
                        <X className="h-2.5 w-2.5" />
                      </button>
                    </div>
                  ))}
                  <button
                    type="button"
                    onClick={() => sitePhotoInputRef.current?.click()}
                    className="flex h-16 w-16 cursor-pointer flex-col items-center justify-center gap-0.5 rounded border-2 border-dashed border-gray-300 text-gray-400 hover:border-gray-400"
                  >
                    <Upload className="h-4 w-4" />
                    <span className="text-[10px]">업로드</span>
                  </button>
                  <input ref={sitePhotoInputRef} type="file" accept="image/*" multiple className="hidden" onChange={makeFileChangeHandler(setSitePhotoFiles)} />
                </div>
              </td>
            </tr>

            <tr>
              <td className="px-3 py-2.5 align-top text-gray-500">매장사진</td>
              <td colSpan={3} className="px-3 py-2.5">
                <div className="flex flex-wrap gap-2">
                  {storePhotoFiles.map((p) => (
                    <div key={p.id} className="relative h-16 w-16">
                      <img src={p.url} alt="매장사진" className="h-full w-full rounded border object-cover" />
                      <button
                        type="button"
                        onClick={() => removeFile(setStorePhotoFiles, p.id, p.url)}
                        className="absolute -right-1 -top-1 flex h-4 w-4 items-center justify-center rounded-full bg-red-500 text-white hover:bg-red-600"
                      >
                        <X className="h-2.5 w-2.5" />
                      </button>
                    </div>
                  ))}
                  <button
                    type="button"
                    onClick={() => storePhotoInputRef.current?.click()}
                    className="flex h-16 w-16 cursor-pointer flex-col items-center justify-center gap-0.5 rounded border-2 border-dashed border-gray-300 text-gray-400 hover:border-gray-400"
                  >
                    <Upload className="h-4 w-4" />
                    <span className="text-[10px]">업로드</span>
                  </button>
                  <input ref={storePhotoInputRef} type="file" accept="image/*" multiple className="hidden" onChange={makeFileChangeHandler(setStorePhotoFiles)} />
                </div>
              </td>
            </tr>

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

        {error && (
          <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-600">{error}</div>
        )}

        <Button className="w-full bg-green-700 hover:bg-green-800" disabled={saving} onClick={handleSave}>
          {saving ? '처리 중…' : '배송완료'}
        </Button>
      </DialogContent>
    </Dialog>
  )
}

// ---- 인수자 정보 수정 폼 ----

interface EditDialogProps {
  open: boolean
  onClose: () => void
  onSave: (result: ReceiptConfirmationResult) => void
  orderId: string
  orderNumber: string
  senderName?: string
  recipientName?: string
  current: ReceiptConfirmationResult
}

function ReceiptEditDialog({ open, onClose, onSave, orderId, orderNumber, senderName, recipientName, current }: EditDialogProps) {
  const [receiverName, setReceiverName] = useState('')
  const [receiverRelation, setReceiverRelation] = useState('')
  const [delivered, setDelivered] = useState(false)
  const [memo, setMemo] = useState('')
  const [confirmTime, setConfirmTime] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (open) {
      setReceiverName(current.receiverName ?? '')
      setReceiverRelation(current.receiverRelation ?? '')
      setDelivered(current.delivered)
      setMemo(current.memo ?? '')
      setConfirmTime(current.confirmedAt
        ? current.confirmedAt.replace('T', ' ').slice(0, 16)
        : '')
      setError(null)
    }
  }, [open, current])

  async function handleSave() {
    setSaving(true)
    setError(null)
    try {
      const result = await updateReceiptInfo(orderId, {
        receiverName,
        receiverRelation,
        delivered,
        memo,
        confirmedAt: confirmTime || undefined,
      })
      onSave(result)
    } catch (e) {
      setError(e instanceof Error ? e.message : '수정에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <Dialog open={open} onOpenChange={(v) => { if (!v) onClose() }}>
      <DialogContent className="sm:max-w-2xl" showCloseButton>
        <DialogTitle>
          인수확인 수정
          <span className="ml-2 text-xs font-normal text-gray-400">{orderNumber}</span>
        </DialogTitle>

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

        <table className="w-full divide-y rounded-lg border text-sm [border-spacing:0] overflow-hidden">
          <colgroup>
            <col className="w-20" />
            <col />
            <col className="w-20" />
            <col />
          </colgroup>
          <tbody className="divide-y">
            {current.sitePhotoUrls.length > 0 && (
              <tr>
                <td className="px-3 py-2.5 align-top text-gray-500">현장사진</td>
                <td colSpan={3} className="px-3 py-2.5">
                  <div className="flex flex-wrap gap-2">
                    {current.sitePhotoUrls.map((url, idx) => (
                      <div key={idx} className="h-16 w-16 overflow-hidden rounded border">
                        <img src={url} alt="현장사진" className="h-full w-full object-cover" />
                      </div>
                    ))}
                  </div>
                </td>
              </tr>
            )}
            {current.storePhotoUrls.length > 0 && (
              <tr>
                <td className="px-3 py-2.5 align-top text-gray-500">매장사진</td>
                <td colSpan={3} className="px-3 py-2.5">
                  <div className="flex flex-wrap gap-2">
                    {current.storePhotoUrls.map((url, idx) => (
                      <div key={idx} className="h-16 w-16 overflow-hidden rounded border">
                        <img src={url} alt="매장사진" className="h-full w-full object-cover" />
                      </div>
                    ))}
                  </div>
                </td>
              </tr>
            )}
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
              <td className="px-3 py-2.5">
                <input
                  type="text"
                  placeholder="YYYY-MM-DD HH:mm"
                  value={confirmTime}
                  onChange={(e) => setConfirmTime(e.target.value)}
                  className="w-full rounded-md border px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
                />
              </td>
            </tr>

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

        {error && (
          <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-600">{error}</div>
        )}

        <Button className="w-full bg-green-700 hover:bg-green-800" disabled={saving} onClick={handleSave}>
          {saving ? '수정 중…' : '수정하기'}
        </Button>
      </DialogContent>
    </Dialog>
  )
}

// ---- 인수정보 표시 셀 (이름·관계만 표시, 버튼 없음) ----

interface ReceiptInfoCellProps {
  hasReceipt?: boolean
  receiverName?: string | null
  receiverRelation?: string | null
}

export function ReceiptInfoCell({ hasReceipt, receiverName, receiverRelation }: ReceiptInfoCellProps) {
  if (!hasReceipt) return <span className="text-xs text-gray-400">-</span>

  return (
    <div className="text-xs">
      <span className="font-medium text-gray-800">{receiverName || '-'}</span>
      {receiverRelation && (
        <span className="ml-1 text-gray-400">({receiverRelation})</span>
      )}
    </div>
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

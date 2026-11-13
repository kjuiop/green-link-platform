'use client'

import { useEffect, useState } from 'react'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { fetchVendor, fetchVendorRecentPhotos, VendorRecentPhoto } from '@/lib/api/vendorApi'
import { Vendor } from '@/types/vendor'

type Props = {
  vendorId: string | null
  onClose: () => void
}

function InfoRow({ label, value, className }: { label: string; value?: string | null; className?: string }) {
  if (!value) return null
  return (
    <div className={className}>
      <span className="text-gray-500">{label}: </span>
      <span className="text-gray-800">{value}</span>
    </div>
  )
}

export default function VendorInfoPopup({ vendorId, onClose }: Props) {
  const [vendor, setVendor] = useState<Vendor | null>(null)
  const [photos, setPhotos] = useState<VendorRecentPhoto[]>([])
  const [expandedPhoto, setExpandedPhoto] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (!vendorId) {
      setVendor(null)
      setPhotos([])
      return
    }
    setLoading(true)
    Promise.all([
      fetchVendor(vendorId),
      fetchVendorRecentPhotos(vendorId, 5),
    ])
      .then(([v, p]) => {
        setVendor(v)
        setPhotos(p)
      })
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [vendorId])

  return (
    <>
      <Dialog open={!!vendorId} onOpenChange={(open) => { if (!open) onClose() }}>
        <DialogContent className="max-w-lg max-h-[80vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>{vendor?.name ?? '업체 정보'}</DialogTitle>
          </DialogHeader>

          {loading ? (
            <p className="py-8 text-center text-sm text-gray-400">불러오는 중...</p>
          ) : vendor ? (
            <div className="space-y-5 text-sm">
              {/* 기본정보 */}
              <div className="grid grid-cols-2 gap-x-6 gap-y-1.5">
                <InfoRow label="대표자명" value={vendor.name} />
                <InfoRow label="업체 연락처" value={vendor.companyPhone} />
                <InfoRow label="담당자명" value={vendor.managerName} />
                <InfoRow label="담당자 연락처" value={vendor.phone} />
                <InfoRow label="주소" value={[vendor.address, vendor.detailAddress].filter(Boolean).join(' ')} className="col-span-2" />
                <InfoRow
                  label="서비스 지역"
                  value={vendor.regions?.map((r) => r.sigunguName ?? r.sidoName).join(', ')}
                  className="col-span-2"
                />
                <InfoRow
                  label="가능 상품"
                  value={vendor.capabilities?.join(', ')}
                  className="col-span-2"
                />
              </div>

              {/* 최근 배송완료 사진 */}
              <div>
                <p className="mb-2 text-xs font-medium text-gray-500">최근 배송완료 사진</p>
                {photos.length === 0 ? (
                  <p className="text-sm text-gray-400">등록된 배송완료 사진이 없습니다.</p>
                ) : (
                  <div className="flex flex-wrap gap-2">
                    {photos.map((p) => (
                      <img
                        key={p.id}
                        src={p.photoUrl}
                        alt="배송완료 사진"
                        className="h-20 w-20 cursor-pointer rounded object-cover hover:opacity-80 transition-opacity"
                        onClick={() => setExpandedPhoto(p.photoUrl)}
                      />
                    ))}
                  </div>
                )}
              </div>
            </div>
          ) : null}
        </DialogContent>
      </Dialog>

      {/* 사진 확대보기 */}
      {expandedPhoto && (
        <div
          className="fixed inset-0 z-[100] flex items-center justify-center bg-black/75"
          onClick={() => setExpandedPhoto(null)}
        >
          <img
            src={expandedPhoto}
            alt="배송완료 사진 확대"
            className="max-h-[90vh] max-w-[90vw] rounded shadow-xl"
          />
        </div>
      )}
    </>
  )
}

'use client'

import { useState, useEffect } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { OrderStatusBadge } from '@/components/common/StatusBadge'
import { fetchOrders, fetchPhotos, PhotoResult } from '@/lib/api/orderApi'
import { Order } from '@/types/order'
import Image from 'next/image'
import Link from 'next/link'

type OrderWithPhotos = { order: Order; photos: PhotoResult[] }

export default function PhotosPage() {
  const [items, setItems] = useState<OrderWithPhotos[]>([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)

  useEffect(() => {
    fetchOrders()
      .then(async (orders) => {
        const deliveredOrders = orders.filter(
          (o) => o.status === '수주완료' || o.status === '배송완료'
        )
        const results = await Promise.all(
          deliveredOrders.map(async (order) => {
            const photos = await fetchPhotos(order.id).catch(() => [] as PhotoResult[])
            return { order, photos }
          })
        )
        setItems(results.filter((r) => r.photos.length > 0))
      })
      .catch(() => setLoadError('사진 목록을 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) {
    return (
      <div className="space-y-4">
        <h1 className="text-xl font-bold">사진관리</h1>
        <p className="py-10 text-center text-sm text-gray-400">불러오는 중...</p>
      </div>
    )
  }

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">사진관리</h1>

      {loadError && (
        <div className="rounded-md border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-600">
          {loadError}
        </div>
      )}

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {items.map(({ order, photos }) => (
          <Card key={order.id}>
            <CardHeader className="pb-2">
              <div className="flex items-center justify-between">
                <CardTitle className="text-sm">
                  <Link href={`/orders/${order.id}`} className="text-blue-600 hover:underline">
                    {order.orderNumber}
                  </Link>
                </CardTitle>
                <OrderStatusBadge status={order.status} />
              </div>
              <p className="text-xs text-gray-500">
                {order.vendorName} · {order.deliveryAt}
              </p>
            </CardHeader>
            <CardContent>
              <div className="flex flex-wrap gap-2">
                {photos.map((photo) => (
                  <div key={photo.id} className="relative">
                    <Image
                      src={photo.photoUrl}
                      alt="배송 사진"
                      width={80}
                      height={80}
                      className="rounded border object-cover"
                    />
                    {photo.isSendToCustomer && (
                      <span className="absolute bottom-0.5 left-0.5 rounded bg-green-700 px-1 py-0.5 text-[9px] text-white">
                        발송
                      </span>
                    )}
                  </div>
                ))}
              </div>
              <p className="mt-2 text-xs text-gray-500">총 {photos.length}장</p>
            </CardContent>
          </Card>
        ))}

        {items.length === 0 && !loadError && (
          <p className="col-span-full py-10 text-center text-gray-400">등록된 사진이 없습니다.</p>
        )}
      </div>
    </div>
  )
}

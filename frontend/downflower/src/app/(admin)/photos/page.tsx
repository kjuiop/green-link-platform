import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { mockOrders } from '@/lib/mock/orders'
import { mockPhotos } from '@/lib/mock/photos'
import { OrderStatusBadge } from '@/components/common/StatusBadge'
import Image from 'next/image'
import Link from 'next/link'

export default function PhotosPage() {
  const ordersWithPhotos = mockOrders.filter((o) => {
    return mockPhotos.some((p) => p.orderId === o.id)
  })

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">사진관리</h1>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {ordersWithPhotos.map((order) => {
          const photos = mockPhotos.filter((p) => p.orderId === order.id)
          return (
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
                        src={photo.url}
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
          )
        })}

        {ordersWithPhotos.length === 0 && (
          <p className="col-span-full py-10 text-center text-gray-400">등록된 사진이 없습니다.</p>
        )}
      </div>
    </div>
  )
}

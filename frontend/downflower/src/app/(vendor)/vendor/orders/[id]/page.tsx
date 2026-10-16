import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { OrderStatusBadge } from '@/components/common/StatusBadge'
import { mockOrders } from '@/lib/mock/orders'
import { mockPhotos } from '@/lib/mock/photos'
import { notFound } from 'next/navigation'
import Image from 'next/image'

interface Props {
  params: Promise<{ id: string }>
}

export default async function VendorOrderDetailPage({ params }: Props) {
  const { id } = await params
  const order = mockOrders.find((o) => o.id === id)
  if (!order) notFound()

  const photos = mockPhotos.filter((p) => p.orderId === order.id)

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <h1 className="text-xl font-bold">{order.orderNumber}</h1>
          <OrderStatusBadge status={order.status} />
        </div>
        {order.status === '발주대기' && (
          <div className="flex gap-2">
            <Button variant="outline" className="border-red-300 text-red-600 hover:bg-red-50">
              수주거절
            </Button>
            <Button className="bg-green-700 hover:bg-green-800">수주확인</Button>
          </div>
        )}
        {order.status === '수주완료' && (
          <Button className="bg-blue-700 hover:bg-blue-800">배송완료 처리</Button>
        )}
      </div>

      <div className="grid gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-sm">배송정보</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2 text-sm">
            <Row label="상품명" value={order.productName} />
            <Row label="배송예정" value={order.deliveryAt} />
            <Row label="주소" value={`${order.address} ${order.addressDetail}`} />
            <Row label="장소명" value={order.placeName} />
            <Row label="빈소·호실" value={order.roomName} />
            {order.eventInfo && <Row label="행사정보" value={order.eventInfo} />}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-sm">리본 문구</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2 text-sm">
            <Row label="리본 왼쪽" value={order.ribbonLeft} />
            <Row label="리본 오른쪽" value={order.ribbonRight} />
          </CardContent>
        </Card>

        {order.vendorMemo && (
          <Card>
            <CardHeader>
              <CardTitle className="text-sm">전달사항</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-sm">{order.vendorMemo}</p>
            </CardContent>
          </Card>
        )}

        {order.status === '수주완료' && (
          <Card className="lg:col-span-2">
            <CardHeader>
              <CardTitle className="text-sm">사진 업로드</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex flex-wrap gap-2">
                {photos.map((photo) => (
                  <div key={photo.id} className="relative">
                    <Image
                      src={photo.url}
                      alt="배송 사진"
                      width={100}
                      height={100}
                      className="rounded border object-cover"
                    />
                    {photo.isSendToCustomer && (
                      <span className="absolute bottom-0.5 left-0.5 rounded bg-green-700 px-1 py-0.5 text-[9px] text-white">
                        발송
                      </span>
                    )}
                  </div>
                ))}
                <Button
                  variant="outline"
                  className="h-24 w-24 rounded border-2 border-dashed border-gray-300 text-gray-400 hover:border-gray-400 hover:text-gray-600"
                >
                  + 추가
                </Button>
              </div>
              <p className="mt-2 text-xs text-gray-400">
                사진 업로드 후 &ldquo;배송완료 처리&rdquo; 버튼을 눌러야 배송완료 처리 및 문자가 발송됩니다.
              </p>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  )
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex gap-2">
      <span className="w-24 flex-shrink-0 text-gray-500">{label}</span>
      <span>{value}</span>
    </div>
  )
}

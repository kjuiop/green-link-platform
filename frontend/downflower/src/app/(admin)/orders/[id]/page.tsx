import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { OrderStatusBadge, MessageStatusBadge } from '@/components/common/StatusBadge'
import { mockOrders } from '@/lib/mock/orders'
import { mockPhotos } from '@/lib/mock/photos'
import { notFound } from 'next/navigation'
import Image from 'next/image'

interface Props {
  params: Promise<{ id: string }>
}

export default async function OrderDetailPage({ params }: Props) {
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
          <MessageStatusBadge status={order.messageStatus} />
        </div>
        <div className="flex gap-2">
          {order.status === '발주대기' && (
            <>
              <Button variant="outline" className="border-red-300 text-red-600 hover:bg-red-50">
                수주거절
              </Button>
              <Button className="bg-green-700 hover:bg-green-800">수주확인</Button>
            </>
          )}
          {order.status === '수주완료' && (
            <Button className="bg-blue-700 hover:bg-blue-800">배송완료 처리</Button>
          )}
        </div>
      </div>

      <div className="grid gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-sm">기본정보</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2 text-sm">
            <Row label="상품명" value={order.productName} />
            <Row label="수량" value={`${order.quantity}개`} />
            <Row label="발주금액" value={`${order.amount.toLocaleString()}원`} />
            <Row label="수주업체" value={order.vendorName} />
            <Row label="발주일시" value={order.orderedAt} />
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-sm">배송정보</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2 text-sm">
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

        <Card>
          <CardHeader>
            <CardTitle className="text-sm">주문자 / 받는 분</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2 text-sm">
            <Row label="주문자" value={`${order.ordererName} ${order.ordererPhone}`} />
            {order.recipientName && (
              <Row label="받는 분" value={`${order.recipientName} ${order.recipientPhone}`} />
            )}
            <Row
              label="사진 발송"
              value={[
                order.sendPhotoToOrderer ? '주문자' : '',
                order.sendPhotoToRecipient ? '받는 분' : '',
              ]
                .filter(Boolean)
                .join(', ') || '미발송'}
            />
          </CardContent>
        </Card>

        {photos.length > 0 && (
          <Card className="lg:col-span-2">
            <CardHeader>
              <CardTitle className="text-sm">배송 사진 ({photos.length}장)</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex gap-3">
                {photos.map((photo) => (
                  <div key={photo.id} className="relative">
                    <Image
                      src={photo.url}
                      alt="배송 사진"
                      width={120}
                      height={120}
                      className="rounded-lg border object-cover"
                    />
                    {photo.isSendToCustomer && (
                      <span className="absolute bottom-1 left-1 rounded bg-green-700 px-1 py-0.5 text-[10px] text-white">
                        발송
                      </span>
                    )}
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        )}

        {(order.adminMemo || order.vendorMemo) && (
          <Card className="lg:col-span-2">
            <CardHeader>
              <CardTitle className="text-sm">메모</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2 text-sm">
              {order.adminMemo && <Row label="관리자 메모" value={order.adminMemo} />}
              {order.vendorMemo && <Row label="업체 전달사항" value={order.vendorMemo} />}
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

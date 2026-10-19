'use client'

import { useState, useEffect } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { fetchOrders } from '@/lib/api/orderApi'
import { Order } from '@/types/order'

export default function VendorDashboardPage() {
  const [orders, setOrders] = useState<Order[]>([])

  useEffect(() => {
    const vendorId = typeof window !== 'undefined' ? localStorage.getItem('vendorId') ?? undefined : undefined
    fetchOrders({ vendorId }).then(setOrders).catch(() => {})
  }, [])

  const pending = orders.filter((o) => o.status === '발주대기').length
  const active = orders.filter((o) => o.status === '수주완료').length
  const completed = orders.filter((o) => o.status === '배송완료').length
  const failed = orders.filter((o) => o.messageStatus === '발송실패').length

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-bold">대시보드</h1>

      <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-500">전체 수주</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">{orders.length}</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-yellow-600">미승인</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-yellow-600">{pending}</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-blue-600">수주완료</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-blue-600">{active}</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-green-600">배송완료</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-green-600">{completed}</p>
          </CardContent>
        </Card>
      </div>

      {failed > 0 && (
        <Card className="border-red-200 bg-red-50">
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-red-600">문자 발송 실패 {failed}건</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-sm text-red-500">발송 실패한 주문을 확인해주세요.</p>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader>
          <CardTitle className="text-sm">최근 수주</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-gray-400">수주 목록은 수주 리스트에서 확인하세요.</p>
        </CardContent>
      </Card>
    </div>
  )
}

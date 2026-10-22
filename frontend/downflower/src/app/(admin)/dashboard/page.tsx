'use client'

import { useState, useEffect } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { fetchOrders } from '@/lib/api/orderApi'
import { Order } from '@/types/order'

export default function DashboardPage() {
  const [orders, setOrders] = useState<Order[]>([])
  const [loading, setLoading] = useState(true)
  const [role, setRole] = useState<string>('SUPER_ADMIN')

  useEffect(() => {
    setRole(localStorage.getItem('role') ?? 'SUPER_ADMIN')
    fetchOrders()
      .then(setOrders)
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  const isVendor = role === 'VENDOR'

  if (isVendor) {
    const pending = orders.filter((o) => o.status === '발주대기').length
    const accepted = orders.filter((o) => o.status === '수주완료').length
    const completed = orders.filter((o) => o.status === '배송완료').length

    return (
      <div className="space-y-6">
        <h1 className="text-xl font-bold">대시보드</h1>
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium text-yellow-600">수주대기</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-2xl font-bold text-yellow-600">{loading ? '-' : pending}</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium text-blue-600">수주완료</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-2xl font-bold text-blue-600">{loading ? '-' : accepted}</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium text-green-600">배송완료</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-2xl font-bold text-green-600">{loading ? '-' : completed}</p>
            </CardContent>
          </Card>
        </div>
        <Card>
          <CardHeader>
            <CardTitle className="text-sm">수주 현황</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-sm text-gray-400">수주 목록은 수주 리스트에서 확인하세요.</p>
          </CardContent>
        </Card>
      </div>
    )
  }

  const pending = orders.filter((o) => o.status === '발주대기').length
  const completed = orders.filter((o) => o.status === '배송완료').length
  const failed = orders.filter((o) => o.messageStatus === '발송실패').length

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-bold">대시보드</h1>

      <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-500">전체 주문</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">{loading ? '-' : orders.length}</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-yellow-600">미승인 주문</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-yellow-600">{loading ? '-' : pending}</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-green-600">배송완료</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-green-600">{loading ? '-' : completed}</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-red-600">문자 발송 실패</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-red-600">{loading ? '-' : failed}</p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-sm">최근 주문</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-gray-400">주문 목록은 발주 리스트에서 확인하세요.</p>
        </CardContent>
      </Card>
    </div>
  )
}

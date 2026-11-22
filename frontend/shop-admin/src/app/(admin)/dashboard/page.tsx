'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { ClipboardList, PackagePlus, Store } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { fetchDashboardSummary } from '@/lib/api/dashboardApi'
import { useSession } from '@/lib/useSession'
import { cn } from '@/lib/utils'
import type { DashboardSummary, RecentOrderStatus } from '@/types/dashboard'

const STATUS_STYLE: Record<RecentOrderStatus, string> = {
  신규: 'bg-blue-100 text-blue-700',
  발주대기: 'bg-yellow-100 text-yellow-700',
  배송중: 'bg-purple-100 text-purple-700',
  완료: 'bg-green-100 text-green-700',
  취소: 'bg-gray-100 text-gray-600',
}

const QUICK_LINKS = [
  { href: '/orders', label: '주문 관리', desc: '들어온 주문을 확인합니다', icon: ClipboardList },
  { href: '/products', label: '상품 등록', desc: '판매할 상품을 추가합니다', icon: PackagePlus },
  { href: '/shop-settings', label: '쇼핑몰 설정', desc: '이름·공개 여부를 설정합니다', icon: Store },
]

export default function DashboardPage() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null)
  const [error, setError] = useState(false)

  const { session } = useSession()

  useEffect(() => {
    if (!session) return
    let cancelled = false
    fetchDashboardSummary(session)
      .then((result) => { if (!cancelled) setSummary(result) })
      .catch(() => { if (!cancelled) setError(true) })
    return () => { cancelled = true }
  }, [session])

  if (error) {
    return (
      <Card>
        <CardContent className="py-8 text-center text-sm text-red-500">
          대시보드 정보를 불러오지 못했습니다.
        </CardContent>
      </Card>
    )
  }

  const loading = summary === null

  return (
    <div className="space-y-6">
      <div className="flex items-baseline gap-2">
        <h1 className="text-xl font-bold">대시보드</h1>
        {summary && <span className="text-sm text-gray-500">{summary.scopeLabel}</span>}
      </div>

      <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-blue-600">신규 주문</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-blue-600">{loading ? '-' : summary.newOrders}</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-yellow-600">처리 대기</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-yellow-600">{loading ? '-' : summary.pendingOrders}</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">등록 상품</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">{loading ? '-' : summary.productCount}</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">쇼핑몰 상태</CardTitle>
          </CardHeader>
          <CardContent>
            {loading ? (
              <Skeleton className="h-7 w-16" />
            ) : (
              <Badge
                className={cn(
                  summary.shopOpen ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-600',
                )}
              >
                {summary.shopOpen ? '공개' : '비공개'}
              </Badge>
            )}
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-sm">최근 주문</CardTitle>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="space-y-2">
              <Skeleton className="h-8 w-full" />
              <Skeleton className="h-8 w-full" />
              <Skeleton className="h-8 w-full" />
            </div>
          ) : summary.recentOrders.length === 0 ? (
            <p className="py-6 text-center text-sm text-gray-400">주문이 없습니다.</p>
          ) : (
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>주문번호</TableHead>
                    <TableHead>고객</TableHead>
                    <TableHead>상품</TableHead>
                    <TableHead className="text-right">금액</TableHead>
                    <TableHead>상태</TableHead>
                    <TableHead>주문일시</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {summary.recentOrders.map((order) => (
                    <TableRow key={order.id}>
                      <TableCell className="whitespace-nowrap font-mono text-xs">{order.orderNo}</TableCell>
                      <TableCell className="whitespace-nowrap">{order.customerName}</TableCell>
                      <TableCell className="whitespace-nowrap">{order.productName}</TableCell>
                      <TableCell className="whitespace-nowrap text-right">
                        {order.amount.toLocaleString('ko-KR')}원
                      </TableCell>
                      <TableCell>
                        <Badge className={cn('whitespace-nowrap', STATUS_STYLE[order.status])}>
                          {order.status}
                        </Badge>
                      </TableCell>
                      <TableCell className="whitespace-nowrap text-xs text-gray-500">{order.orderedAt}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}
        </CardContent>
      </Card>

      <div className="grid gap-4 sm:grid-cols-3">
        {QUICK_LINKS.map(({ href, label, desc, icon: Icon }) => (
          <Link key={href} href={href}>
            <Card className="h-full transition-colors hover:bg-gray-50">
              <CardContent className="flex items-center gap-3 py-4">
                <Icon className="h-5 w-5 shrink-0 text-green-700" />
                <div>
                  <p className="text-sm font-medium">{label}</p>
                  <p className="text-xs text-gray-500">{desc}</p>
                </div>
              </CardContent>
            </Card>
          </Link>
        ))}
      </div>
    </div>
  )
}

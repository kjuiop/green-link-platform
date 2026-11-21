'use client'

import { useState, useEffect, useCallback } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Pagination } from '@/components/ui/Pagination'
import { PointTransactionTable, formatPoint } from '@/components/point/PointTransactionTable'
import {
  fetchMyPoint,
  fetchMyPointTransactions,
  CompanyPoint,
  PointTransaction,
} from '@/lib/api/pointApi'
import { usePointScope } from '@/components/point/usePointScope'
import { PointCompanySelect } from '@/components/point/PointCompanySelect'

const PAGE_SIZE = 20

export default function MyPointPage() {
  const scope = usePointScope()
  const companyId = scope.selectedId

  const [point, setPoint] = useState<CompanyPoint | null>(null)
  const [transactions, setTransactions] = useState<PointTransaction[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const load = useCallback(() => {
    if (scope.isSuperAdmin && !companyId) return
    setLoading(true)
    setError('')
    Promise.all([fetchMyPoint(companyId), fetchMyPointTransactions(page, PAGE_SIZE, companyId)])
      .then(([p, tx]) => {
        setPoint(p)
        setTransactions(tx.content)
        setTotalPages(tx.totalPages)
        setTotalElements(tx.totalElements)
      })
      .catch((e) => setError(e instanceof Error ? e.message : '포인트 정보를 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }, [scope.isSuperAdmin, companyId, page])

  useEffect(() => {
    if (!scope.ready) return
    setPoint(null)
    setTransactions([])
    setPage(0)
    load()
  }, [scope.ready, scope.isSuperAdmin, companyId])

  useEffect(() => {
    if (!scope.ready) return
    load()
  }, [page])

  if (!scope.ready) return null

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold">포인트관리</h1>
        <PointCompanySelect scope={scope} />
      </div>

      {scope.isSuperAdmin && !companyId && (
        <p className="rounded-md bg-yellow-50 px-3 py-2 text-sm text-yellow-700">회사를 선택해주세요.</p>
      )}

      <div className="grid max-w-xl gap-3 sm:grid-cols-2">
        <Card>
          <CardHeader className="pb-1 pt-3">
            <CardTitle className="text-xs font-medium text-gray-500">사용 가능 포인트</CardTitle>
          </CardHeader>
          <CardContent className="pb-3">
            <p className="text-2xl font-bold text-green-700">{point ? formatPoint(point.balance) : '-'}</p>
            <p className="mt-1 text-xs text-gray-400">충전은 무통장 입금 후 관리자에게 문의해주세요.</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-1 pt-3">
            <CardTitle className="text-xs font-medium text-gray-500">예치금 (진행 중 발주)</CardTitle>
          </CardHeader>
          <CardContent className="pb-3">
            <p className="text-2xl font-bold text-orange-600">{point ? formatPoint(point.depositBalance) : '-'}</p>
            <p className="mt-1 text-xs text-gray-400">배송완료 시 사용 확정, 취소·거절 시 환불됩니다.</p>
          </CardContent>
        </Card>
      </div>

      {error && <p className="text-sm text-red-500">{error}</p>}

      <PointTransactionTable transactions={transactions} loading={loading} />

      <Pagination
        page={page}
        totalPages={totalPages}
        totalElements={totalElements}
        size={PAGE_SIZE}
        onPageChange={setPage}
      />
    </div>
  )
}

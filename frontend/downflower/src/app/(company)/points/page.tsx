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

const PAGE_SIZE = 20

export default function MyPointPage() {
  const [point, setPoint] = useState<CompanyPoint | null>(null)
  const [transactions, setTransactions] = useState<PointTransaction[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const load = useCallback(() => {
    setLoading(true)
    setError('')
    Promise.all([fetchMyPoint(), fetchMyPointTransactions(page, PAGE_SIZE)])
      .then(([p, tx]) => {
        setPoint(p)
        setTransactions(tx.content)
        setTotalPages(tx.totalPages)
        setTotalElements(tx.totalElements)
      })
      .catch((e) => setError(e instanceof Error ? e.message : '포인트 정보를 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }, [page])

  useEffect(() => { load() }, [load])

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">포인트관리</h1>

      <Card className="max-w-sm">
        <CardHeader className="pb-1 pt-3">
          <CardTitle className="text-xs font-medium text-gray-500">사용 가능 포인트</CardTitle>
        </CardHeader>
        <CardContent className="pb-3">
          <p className="text-2xl font-bold text-green-700">{point ? formatPoint(point.balance) : '-'}</p>
          <p className="mt-1 text-xs text-gray-400">충전은 무통장 입금 후 관리자에게 문의해주세요.</p>
        </CardContent>
      </Card>

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

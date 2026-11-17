'use client'

import { useState, useEffect, useCallback } from 'react'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog'
import { Pagination } from '@/components/ui/Pagination'
import { PointTransactionTable, formatPoint } from '@/components/point/PointTransactionTable'
import { Search } from 'lucide-react'
import {
  fetchAdminPointList,
  fetchAdminPointTransactions,
  chargePoint,
  CompanyPoint,
  PointTransaction,
} from '@/lib/api/pointApi'

const HISTORY_PAGE_SIZE = 10

function companyTypeLabel(t: string) {
  if (t === 'VENDOR') return '수주사'
  if (t === 'ORDERING') return '발주사'
  if (t === 'BOTH') return '수발주사'
  return t
}

export default function AdminPointPage() {
  const [companies, setCompanies] = useState<CompanyPoint[]>([])
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const [chargeTarget, setChargeTarget] = useState<CompanyPoint | null>(null)
  const [amount, setAmount] = useState('')
  const [memo, setMemo] = useState('')
  const [charging, setCharging] = useState(false)
  const [chargeError, setChargeError] = useState('')

  const [historyTarget, setHistoryTarget] = useState<CompanyPoint | null>(null)
  const [transactions, setTransactions] = useState<PointTransaction[]>([])
  const [historyPage, setHistoryPage] = useState(0)
  const [historyTotalPages, setHistoryTotalPages] = useState(0)
  const [historyTotalElements, setHistoryTotalElements] = useState(0)
  const [historyLoading, setHistoryLoading] = useState(false)

  const load = useCallback(() => {
    setLoading(true)
    setError('')
    fetchAdminPointList()
      .then(setCompanies)
      .catch((e) => setError(e instanceof Error ? e.message : '목록을 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => { load() }, [load])

  useEffect(() => {
    if (!historyTarget) return
    setHistoryLoading(true)
    fetchAdminPointTransactions(historyTarget.companyId, historyPage, HISTORY_PAGE_SIZE)
      .then((r) => {
        setTransactions(r.content)
        setHistoryTotalPages(r.totalPages)
        setHistoryTotalElements(r.totalElements)
      })
      .catch(() => setTransactions([]))
      .finally(() => setHistoryLoading(false))
  }, [historyTarget, historyPage])

  const displayed = search.trim()
    ? companies.filter((c) => c.companyName.includes(search.trim()))
    : companies

  function openCharge(c: CompanyPoint) {
    setChargeTarget(c)
    setAmount('')
    setMemo('')
    setChargeError('')
  }

  function openHistory(c: CompanyPoint) {
    setTransactions([])
    setHistoryPage(0)
    setHistoryTarget(c)
  }

  const parsedAmount = Number(amount.replace(/,/g, ''))
  const amountValid = Number.isInteger(parsedAmount) && parsedAmount > 0

  async function submitCharge() {
    if (!chargeTarget || !amountValid) return
    setCharging(true)
    setChargeError('')
    try {
      await chargePoint(chargeTarget.companyId, parsedAmount, memo)
      setChargeTarget(null)
      load()
    } catch (e) {
      setChargeError(e instanceof Error ? e.message : '충전에 실패했습니다.')
    } finally {
      setCharging(false)
    }
  }

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">포인트관리</h1>

      <div className="flex flex-wrap items-end gap-2 rounded-lg border bg-white p-4">
        <div className="relative">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-gray-400" />
          <Input
            placeholder="업체명 검색"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-48 pl-8"
          />
        </div>
      </div>

      {error && <p className="text-sm text-red-500">{error}</p>}

      <div className="rounded-lg border bg-white">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>업체명</TableHead>
              <TableHead>유형</TableHead>
              <TableHead className="text-right">보유 포인트</TableHead>
              <TableHead className="text-center">처리</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={4} className="py-10 text-center text-sm text-gray-400">불러오는 중...</TableCell>
              </TableRow>
            ) : displayed.length === 0 ? (
              <TableRow>
                <TableCell colSpan={4} className="py-10 text-center text-sm text-gray-400">업체가 없습니다.</TableCell>
              </TableRow>
            ) : (
              displayed.map((c) => (
                <TableRow key={c.companyId} className="hover:bg-gray-50">
                  <TableCell className="font-medium">{c.companyName}</TableCell>
                  <TableCell className="text-sm text-gray-500">{companyTypeLabel(c.companyType)}</TableCell>
                  <TableCell className="text-right text-sm font-medium text-green-700">{formatPoint(c.balance)}</TableCell>
                  <TableCell className="text-center">
                    <div className="flex justify-center gap-1">
                      <Button size="sm" className="h-7 bg-green-600 text-xs hover:bg-green-700" onClick={() => openCharge(c)}>
                        충전
                      </Button>
                      <Button size="sm" variant="outline" className="h-7 text-xs" onClick={() => openHistory(c)}>
                        이력
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {/* 충전 모달 */}
      <Dialog open={chargeTarget !== null} onOpenChange={(open) => { if (!open) setChargeTarget(null) }}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>포인트 충전 - {chargeTarget?.companyName}</DialogTitle>
          </DialogHeader>
          <div className="space-y-3">
            <p className="text-sm text-gray-500">
              현재 보유 포인트: <strong>{chargeTarget ? formatPoint(chargeTarget.balance) : ''}</strong>
            </p>
            <div className="space-y-1">
              <Label htmlFor="charge-amount">충전 금액</Label>
              <Input
                id="charge-amount"
                inputMode="numeric"
                placeholder="예: 100000"
                value={amount}
                onChange={(e) => setAmount(e.target.value.replace(/[^\d]/g, ''))}
              />
              {amountValid && (
                <p className="text-xs text-gray-500">충전 후 잔액: {formatPoint((chargeTarget?.balance ?? 0) + parsedAmount)}</p>
              )}
            </div>
            <div className="space-y-1">
              <Label htmlFor="charge-memo">메모 (선택)</Label>
              <Input
                id="charge-memo"
                placeholder="예: 11/18 무통장 입금 확인"
                maxLength={255}
                value={memo}
                onChange={(e) => setMemo(e.target.value)}
              />
            </div>
            {chargeError && <p className="text-sm text-red-500">{chargeError}</p>}
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setChargeTarget(null)} disabled={charging}>취소</Button>
            <Button className="bg-green-600 hover:bg-green-700" onClick={submitCharge} disabled={!amountValid || charging}>
              {charging ? '충전 중...' : '충전'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* 이력 모달 */}
      <Dialog open={historyTarget !== null} onOpenChange={(open) => { if (!open) setHistoryTarget(null) }}>
        <DialogContent className="max-w-3xl">
          <DialogHeader>
            <DialogTitle>포인트 이력 - {historyTarget?.companyName}</DialogTitle>
          </DialogHeader>
          <PointTransactionTable transactions={transactions} loading={historyLoading} />
          <Pagination
            page={historyPage}
            totalPages={historyTotalPages}
            totalElements={historyTotalElements}
            size={HISTORY_PAGE_SIZE}
            onPageChange={setHistoryPage}
          />
        </DialogContent>
      </Dialog>
    </div>
  )
}

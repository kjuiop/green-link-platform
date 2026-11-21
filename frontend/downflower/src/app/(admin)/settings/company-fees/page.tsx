'use client'

import { useState, useEffect } from 'react'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog'
import { Pagination } from '@/components/ui/Pagination'
import { Search } from 'lucide-react'
import { fetchVendorFees, updateVendorFee, type VendorFee } from '@/lib/api/vendorFeeApi'
import { fetchOrderingFees, updateOrderingFee, type FeeDiscountType, type OrderingFee } from '@/lib/api/companyFeeApi'
import { PageResult } from '@/types/page'

type Tab = 'vendor' | 'ordering'

const PAGE_SIZE = 20
const MAX_FEE = 10_000_000

function won(n: number) {
  return `${n.toLocaleString('ko-KR')}원`
}

function companyTypeLabel(t: string) {
  if (t === 'VENDOR') return '수주사'
  if (t === 'ORDERING') return '발주사'
  if (t === 'BOTH') return '수발주사'
  return t
}

function discountLabel(type: FeeDiscountType, value: number) {
  if (type === 'NONE') return '-'
  if (type === 'PERCENT') return `${value}%`
  return won(value)
}

// ─── 수주사 수수료 탭 ───────────────────────────────────────────────────

interface VendorFeeLoaded {
  key: string
  data: PageResult<VendorFee> | null
  error: string
}

function VendorFeeTab() {
  const [input, setInput] = useState('')
  const [keyword, setKeyword] = useState('')
  const [page, setPage] = useState(0)
  const [reloadToken, setReloadToken] = useState(0)
  const [loaded, setLoaded] = useState<VendorFeeLoaded | null>(null)

  const [editTarget, setEditTarget] = useState<VendorFee | null>(null)
  const [feeInput, setFeeInput] = useState('')
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState('')

  const requestKey = `${keyword}|${page}|${reloadToken}`

  useEffect(() => {
    let cancelled = false
    fetchVendorFees(keyword, page, PAGE_SIZE)
      .then((data) => { if (!cancelled) setLoaded({ key: requestKey, data, error: '' }) })
      .catch((e) => { if (!cancelled) setLoaded({ key: requestKey, data: null, error: e instanceof Error ? e.message : '목록을 불러오지 못했습니다.' }) })
    return () => { cancelled = true }
  }, [keyword, page, requestKey])

  const loading = loaded?.key !== requestKey
  const rows = loaded?.data?.content ?? []
  const error = loading ? '' : loaded?.error ?? ''

  function submitSearch() { setPage(0); setKeyword(input) }
  function openEdit(v: VendorFee) { setEditTarget(v); setFeeInput(String(v.vendorFee)); setSaveError('') }

  const parsedFee = Number(feeInput)
  const feeValid = feeInput !== '' && Number.isInteger(parsedFee) && parsedFee >= 0 && parsedFee <= MAX_FEE

  async function submitFee() {
    if (!editTarget || !feeValid) return
    setSaving(true); setSaveError('')
    try {
      await updateVendorFee(editTarget.companyId, parsedFee)
      setEditTarget(null); setReloadToken((t) => t + 1)
    } catch (e) {
      setSaveError(e instanceof Error ? e.message : '수정에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="space-y-4">
      <p className="text-sm text-gray-500">수주사별 상품 1개당 수수료입니다. 발주 화면 상품 단가에 더해져 표시되며, 수주사 화면과 정산에서는 차감됩니다.</p>

      <div className="flex flex-wrap items-end gap-2 rounded-lg border bg-white p-4">
        <div className="relative">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-gray-400" />
          <Input placeholder="업체명 검색" value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => { if (e.key === 'Enter') submitSearch() }}
            className="w-48 pl-8" />
        </div>
        <Button variant="outline" onClick={submitSearch}>검색</Button>
      </div>

      {error && <p className="text-sm text-red-500">{error}</p>}

      <div className="rounded-lg border bg-white">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>업체명</TableHead>
              <TableHead>유형</TableHead>
              <TableHead>사업자번호</TableHead>
              <TableHead className="text-right">수수료 (상품 1개당)</TableHead>
              <TableHead className="text-center">처리</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow><TableCell colSpan={5} className="py-10 text-center text-sm text-gray-400">불러오는 중...</TableCell></TableRow>
            ) : rows.length === 0 ? (
              <TableRow><TableCell colSpan={5} className="py-10 text-center text-sm text-gray-400">수주사가 없습니다.</TableCell></TableRow>
            ) : rows.map((v) => (
              <TableRow key={v.companyId} className="hover:bg-gray-50">
                <TableCell className="font-medium">{v.companyName}</TableCell>
                <TableCell className="text-sm text-gray-500">{companyTypeLabel(v.type)}</TableCell>
                <TableCell className="text-sm text-gray-500">{v.businessNo ?? '-'}</TableCell>
                <TableCell className="text-right text-sm font-medium text-green-700">{won(v.vendorFee)}</TableCell>
                <TableCell className="text-center">
                  <Button size="sm" variant="outline" className="h-7 text-xs" onClick={() => openEdit(v)}>수정</Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      {loaded?.data && (
        <Pagination page={page} totalPages={loaded.data.totalPages} totalElements={loaded.data.totalElements} size={PAGE_SIZE} onPageChange={setPage} />
      )}

      <Dialog open={editTarget !== null} onOpenChange={(open) => { if (!open) setEditTarget(null) }}>
        <DialogContent>
          <DialogHeader><DialogTitle>수수료 수정 - {editTarget?.companyName}</DialogTitle></DialogHeader>
          <div className="space-y-3">
            <div className="space-y-1">
              <Label htmlFor="vendor-fee">수수료 (상품 1개당, 원)</Label>
              <Input id="vendor-fee" inputMode="numeric" value={feeInput}
                onChange={(e) => setFeeInput(e.target.value.replace(/[^\d]/g, ''))} />
              {!feeValid && feeInput !== '' && (
                <p className="text-xs text-red-500">0원 이상 {MAX_FEE.toLocaleString('ko-KR')}원 이하로 입력해 주세요.</p>
              )}
              <p className="text-xs text-gray-500">변경된 수수료는 이후 등록·수정되는 주문부터 적용됩니다.</p>
            </div>
            {saveError && <p className="text-sm text-red-500">{saveError}</p>}
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setEditTarget(null)} disabled={saving}>취소</Button>
            <Button className="bg-green-600 hover:bg-green-700" onClick={submitFee} disabled={!feeValid || saving}>
              {saving ? '저장 중...' : '저장'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}

// ─── 발주사 수수료 감면 탭 ───────────────────────────────────────────────

interface OrderingFeeLoaded {
  key: string
  data: PageResult<OrderingFee> | null
  error: string
}

function OrderingFeeTab() {
  const [input, setInput] = useState('')
  const [keyword, setKeyword] = useState('')
  const [page, setPage] = useState(0)
  const [reloadToken, setReloadToken] = useState(0)
  const [loaded, setLoaded] = useState<OrderingFeeLoaded | null>(null)

  const [editTarget, setEditTarget] = useState<OrderingFee | null>(null)
  const [discountType, setDiscountType] = useState<FeeDiscountType>('NONE')
  const [valueInput, setValueInput] = useState('')
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState('')

  const requestKey = `${keyword}|${page}|${reloadToken}`

  useEffect(() => {
    let cancelled = false
    fetchOrderingFees(keyword, page, PAGE_SIZE)
      .then((data) => { if (!cancelled) setLoaded({ key: requestKey, data, error: '' }) })
      .catch((e) => { if (!cancelled) setLoaded({ key: requestKey, data: null, error: e instanceof Error ? e.message : '목록을 불러오지 못했습니다.' }) })
    return () => { cancelled = true }
  }, [keyword, page, requestKey])

  const loading = loaded?.key !== requestKey
  const rows = loaded?.data?.content ?? []
  const error = loading ? '' : loaded?.error ?? ''

  function submitSearch() { setPage(0); setKeyword(input) }

  function openEdit(v: OrderingFee) {
    setEditTarget(v)
    setDiscountType(v.orderingFeeDiscountType)
    setValueInput(v.orderingFeeDiscountType === 'NONE' ? '' : String(v.orderingFeeDiscountValue))
    setSaveError('')
  }

  const parsedValue = Number(valueInput)
  const valueValid = discountType === 'NONE'
    || (valueInput !== '' && Number.isInteger(parsedValue) && parsedValue >= 0
        && (discountType === 'PERCENT' ? parsedValue <= 100 : parsedValue <= MAX_FEE))

  async function submitFee() {
    if (!editTarget || !valueValid) return
    setSaving(true); setSaveError('')
    try {
      await updateOrderingFee(editTarget.companyId, discountType, discountType === 'NONE' ? 0 : parsedValue)
      setEditTarget(null); setReloadToken((t) => t + 1)
    } catch (e) {
      setSaveError(e instanceof Error ? e.message : '수정에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="space-y-4">
      <p className="text-sm text-gray-500">발주사별 수수료 감면 설정입니다. 감면이 적용된 발주사의 정산 화면에서 수수료 감면 / 정산금액이 표시됩니다.</p>

      <div className="flex flex-wrap items-end gap-2 rounded-lg border bg-white p-4">
        <div className="relative">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-gray-400" />
          <Input placeholder="업체명 검색" value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => { if (e.key === 'Enter') submitSearch() }}
            className="w-48 pl-8" />
        </div>
        <Button variant="outline" onClick={submitSearch}>검색</Button>
      </div>

      {error && <p className="text-sm text-red-500">{error}</p>}

      <div className="rounded-lg border bg-white">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>업체명</TableHead>
              <TableHead>유형</TableHead>
              <TableHead>사업자번호</TableHead>
              <TableHead className="text-right">감면 방식</TableHead>
              <TableHead className="text-right">감면 값</TableHead>
              <TableHead className="text-center">처리</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow><TableCell colSpan={6} className="py-10 text-center text-sm text-gray-400">불러오는 중...</TableCell></TableRow>
            ) : rows.length === 0 ? (
              <TableRow><TableCell colSpan={6} className="py-10 text-center text-sm text-gray-400">발주사가 없습니다.</TableCell></TableRow>
            ) : rows.map((v) => (
              <TableRow key={v.companyId} className="hover:bg-gray-50">
                <TableCell className="font-medium">{v.companyName}</TableCell>
                <TableCell className="text-sm text-gray-500">{companyTypeLabel(v.type)}</TableCell>
                <TableCell className="text-sm text-gray-500">{v.businessNo ?? '-'}</TableCell>
                <TableCell className="text-right text-sm text-gray-600">
                  {v.orderingFeeDiscountType === 'NONE' ? '-' : v.orderingFeeDiscountType === 'PERCENT' ? '퍼센트' : '정액'}
                </TableCell>
                <TableCell className="text-right text-sm font-medium text-blue-700">
                  {discountLabel(v.orderingFeeDiscountType, v.orderingFeeDiscountValue)}
                </TableCell>
                <TableCell className="text-center">
                  <Button size="sm" variant="outline" className="h-7 text-xs" onClick={() => openEdit(v)}>수정</Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      {loaded?.data && (
        <Pagination page={page} totalPages={loaded.data.totalPages} totalElements={loaded.data.totalElements} size={PAGE_SIZE} onPageChange={setPage} />
      )}

      <Dialog open={editTarget !== null} onOpenChange={(open) => { if (!open) setEditTarget(null) }}>
        <DialogContent>
          <DialogHeader><DialogTitle>수수료 감면 수정 - {editTarget?.companyName}</DialogTitle></DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label>감면 방식</Label>
              <div className="flex gap-4 text-sm">
                {(['NONE', 'FIXED', 'PERCENT'] as FeeDiscountType[]).map((t) => (
                  <label key={t} className="flex cursor-pointer items-center gap-1.5">
                    <input type="radio" name="discountType" value={t}
                      checked={discountType === t}
                      onChange={() => { setDiscountType(t); setValueInput('') }}
                      className="accent-green-700" />
                    {t === 'NONE' ? '없음' : t === 'FIXED' ? '정액' : '퍼센트'}
                  </label>
                ))}
              </div>
            </div>

            {discountType !== 'NONE' && (
              <div className="space-y-1">
                <Label htmlFor="discount-value">
                  감면 값 ({discountType === 'PERCENT' ? '%' : '원'})
                </Label>
                <Input id="discount-value" inputMode="numeric" value={valueInput}
                  onChange={(e) => setValueInput(e.target.value.replace(/[^\d]/g, ''))}
                  placeholder={discountType === 'PERCENT' ? '0~100' : '금액 입력'} />
                {!valueValid && valueInput !== '' && (
                  <p className="text-xs text-red-500">
                    {discountType === 'PERCENT' ? '0~100 사이 값을 입력해 주세요.' : `0원 이상 ${MAX_FEE.toLocaleString('ko-KR')}원 이하로 입력해 주세요.`}
                  </p>
                )}
              </div>
            )}

            {saveError && <p className="text-sm text-red-500">{saveError}</p>}
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setEditTarget(null)} disabled={saving}>취소</Button>
            <Button className="bg-green-600 hover:bg-green-700" onClick={submitFee} disabled={!valueValid || saving}>
              {saving ? '저장 중...' : '저장'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}

// ─── 페이지 ─────────────────────────────────────────────────────────────

export default function CompanyFeesPage() {
  const [activeTab, setActiveTab] = useState<Tab>('vendor')

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-xl font-bold">수수료관리</h1>
      </div>

      <div className="flex gap-1 border-b">
        {([['vendor', '수주사 수수료'], ['ordering', '발주사 수수료 감면']] as [Tab, string][]).map(([key, label]) => (
          <button
            key={key}
            onClick={() => setActiveTab(key)}
            className={activeTab === key
              ? 'border-b-2 border-green-700 px-4 py-2 text-sm font-medium text-green-700'
              : 'px-4 py-2 text-sm text-gray-500 hover:text-gray-900'}
          >
            {label}
          </button>
        ))}
      </div>

      {activeTab === 'vendor' ? <VendorFeeTab /> : <OrderingFeeTab />}
    </div>
  )
}

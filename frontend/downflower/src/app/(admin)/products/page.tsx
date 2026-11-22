'use client'

import { useState, useEffect, useCallback } from 'react'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Badge } from '@/components/ui/badge'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog'
import { Textarea } from '@/components/ui/textarea'
import { Pagination } from '@/components/ui/Pagination'
import { VendorProduct } from '@/types/product'
import { PageResult } from '@/types/page'
import { PRODUCT_CATEGORIES, categoryLabel, type ProductCategory } from '@/lib/constants/productCategory'
import {
  fetchVendorProducts,
  createVendorProduct,
  updateVendorProduct,
  changeVendorProductSetting,
  deleteVendorProduct,
} from '@/lib/api/productApi'
import { useVendorProductScope } from '@/components/product/useVendorProductScope'
import { ProductCompanySelect } from '@/components/product/ProductCompanySelect'

const PAGE_SIZE = 20

const emptyForm = { name: '', price: '', memo: '', sortOrder: '', category: '' }

function formatPrice(value: string) {
  const digits = value.replace(/\D/g, '')
  return digits ? Number(digits).toLocaleString() : ''
}

function isBase(product: VendorProduct) {
  return product.baseProductId !== null
}

export default function VendorProductsPage() {
  const scope = useVendorProductScope()
  const companyId = scope.selectedId

  const [result, setResult] = useState<PageResult<VendorProduct> | null>(null)
  const [page, setPage] = useState(0)
  const [keywordInput, setKeywordInput] = useState('')
  const [keyword, setKeyword] = useState('')
  const [loading, setLoading] = useState(true)
  const [pageError, setPageError] = useState('')

  const [ownModalOpen, setOwnModalOpen] = useState(false)
  const [editTarget, setEditTarget] = useState<VendorProduct | null>(null)
  const [form, setForm] = useState(emptyForm)

  const [priceTarget, setPriceTarget] = useState<VendorProduct | null>(null)
  const [priceInput, setPriceInput] = useState('')

  const [modalError, setModalError] = useState('')
  const [modalSaving, setModalSaving] = useState(false)

  const [deleteTarget, setDeleteTarget] = useState<VendorProduct | null>(null)
  const [deleting, setDeleting] = useState(false)

  const fetchPage = useCallback((p: number, k: string) => {
    if (scope.isSuperAdmin && !companyId) {
      setLoading(false)
      return
    }
    setLoading(true)
    setPageError('')
    fetchVendorProducts(k, p, PAGE_SIZE, companyId)
      .then(setResult)
      .catch((e: Error) => setPageError(e.message))
      .finally(() => setLoading(false))
  }, [scope.isSuperAdmin, companyId])

  useEffect(() => {
    if (!scope.ready) return
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setPage(0)
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setKeyword('')
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setKeywordInput('')
    fetchPage(0, '')
  }, [scope.ready, fetchPage])

  const refresh = () => fetchPage(page, keyword)

  const changePage = (p: number) => {
    setPage(p)
    fetchPage(p, keyword)
  }

  const handleSearch = () => {
    setPage(0)
    setKeyword(keywordInput)
    fetchPage(0, keywordInput)
  }

  const openRegister = () => {
    setEditTarget(null)
    setForm(emptyForm)
    setModalError('')
    setOwnModalOpen(true)
  }

  const openEditOwn = (product: VendorProduct) => {
    setEditTarget(product)
    setForm({
      name: product.name,
      price: product.price.toLocaleString(),
      memo: product.memo ?? '',
      sortOrder: String(product.sortOrder),
      category: product.category ?? '',
    })
    setModalError('')
    setOwnModalOpen(true)
  }

  const openPrice = (product: VendorProduct) => {
    setPriceTarget(product)
    setPriceInput(product.price.toLocaleString())
    setModalError('')
  }

  const handleSaveOwn = async () => {
    const rawPrice = Number(form.price.replace(/,/g, ''))
    if (!form.category) { setModalError('상품 종류를 선택해주세요.'); return }
    if (!form.name.trim()) { setModalError('상품명을 입력해주세요.'); return }
    if (!rawPrice || rawPrice <= 0) { setModalError('가격을 입력해주세요.'); return }

    setModalSaving(true)
    setModalError('')
    const req = {
      name: form.name.trim(),
      price: rawPrice,
      memo: form.memo.trim() || undefined,
      sortOrder: Number(form.sortOrder) || 0,
      category: form.category as ProductCategory,
    }
    try {
      if (editTarget) {
        await updateVendorProduct(editTarget.id, req, companyId)
        setOwnModalOpen(false)
        refresh()
      } else {
        await createVendorProduct(req, companyId)
        setOwnModalOpen(false)
        if (page !== 0) changePage(0)
        else refresh()
      }
    } catch (e: unknown) {
      setModalError(e instanceof Error ? e.message : '저장에 실패했습니다.')
    } finally {
      setModalSaving(false)
    }
  }

  const handleSavePrice = async () => {
    if (!priceTarget) return
    const rawPrice = Number(priceInput.replace(/,/g, ''))
    if (!rawPrice || rawPrice <= 0) { setModalError('가격을 입력해주세요.'); return }

    setModalSaving(true)
    setModalError('')
    try {
      await changeVendorProductSetting(priceTarget.id, rawPrice, priceTarget.enabled, companyId)
      setPriceTarget(null)
      refresh()
    } catch (e: unknown) {
      setModalError(e instanceof Error ? e.message : '저장에 실패했습니다.')
    } finally {
      setModalSaving(false)
    }
  }

  const handleToggle = async (product: VendorProduct) => {
    setPageError('')
    try {
      await changeVendorProductSetting(product.id, product.price, !product.enabled, companyId)
      refresh()
    } catch (e: unknown) {
      setPageError(e instanceof Error ? e.message : '사용 여부 변경에 실패했습니다.')
    }
  }

  const handleDelete = async () => {
    if (!deleteTarget) return
    setDeleting(true)
    try {
      await deleteVendorProduct(deleteTarget.id, companyId)
      setDeleteTarget(null)
      refresh()
    } catch (e: unknown) {
      setPageError(e instanceof Error ? e.message : '삭제에 실패했습니다.')
    } finally {
      setDeleting(false)
    }
  }

  const products = result?.content ?? []

  const toggleButton = (product: VendorProduct) => (
    <button
      onClick={() => handleToggle(product)}
      className={`rounded-full px-3 py-1 text-xs font-medium ${
        product.enabled ? 'bg-green-100 text-green-700 hover:bg-green-200' : 'bg-gray-100 text-gray-500 hover:bg-gray-200'
      }`}
    >
      {product.enabled ? 'ON' : 'OFF'}
    </button>
  )

  const actionButtons = (product: VendorProduct) =>
    isBase(product) ? (
      <button onClick={() => openPrice(product)} className="rounded border px-2.5 py-1 text-xs text-gray-600 hover:bg-gray-50">가격 수정</button>
    ) : (
      <>
        <button onClick={() => openEditOwn(product)} className="rounded border px-2.5 py-1 text-xs text-gray-600 hover:bg-gray-50">수정</button>
        <button onClick={() => setDeleteTarget(product)} className="rounded border border-red-200 px-2.5 py-1 text-xs text-red-500 hover:bg-red-50">삭제</button>
      </>
    )

  if (!scope.ready) return null

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold">상품 관리</h1>
          <p className="mt-1 text-xs text-gray-500">기본상품은 가격과 사용 여부만 변경할 수 있고, 직접 등록한 상품은 자유롭게 관리할 수 있습니다.</p>
        </div>
        <div className="flex items-center gap-3">
          <ProductCompanySelect scope={scope} />
          <Button className="bg-green-700 hover:bg-green-800" onClick={openRegister} disabled={scope.isSuperAdmin && !companyId}>
            상품 등록
          </Button>
        </div>
      </div>

      {scope.isSuperAdmin && !companyId && (
        <p className="rounded-md bg-yellow-50 px-3 py-2 text-sm text-yellow-700">수주사를 선택해주세요.</p>
      )}

      <div className="flex gap-2">
        <Input
          placeholder="상품명 검색"
          value={keywordInput}
          onChange={(e) => setKeywordInput(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
          className="max-w-xs"
        />
        <Button variant="outline" onClick={handleSearch}>검색</Button>
      </div>

      {pageError && (
        <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-600">{pageError}</p>
      )}

      {/* 데스크톱 테이블 */}
      <div className="hidden rounded-lg border bg-white md:block">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-20">구분</TableHead>
              <TableHead className="w-24">종류</TableHead>
              <TableHead>상품명</TableHead>
              <TableHead className="w-44 text-right">가격</TableHead>
              <TableHead>메모</TableHead>
              <TableHead className="w-24 text-center">사용</TableHead>
              <TableHead className="w-36 text-center">관리</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={7} className="py-10 text-center text-sm text-gray-400">불러오는 중...</TableCell>
              </TableRow>
            ) : products.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} className="py-10 text-center text-sm text-gray-400">등록된 상품이 없습니다.</TableCell>
              </TableRow>
            ) : (
              products.map((product) => (
                <TableRow key={product.id} className={product.enabled ? '' : 'opacity-60'}>
                  <TableCell>
                    <Badge variant={isBase(product) ? 'secondary' : 'outline'}>{isBase(product) ? '기본' : '자체'}</Badge>
                  </TableCell>
                  <TableCell className="text-sm">{categoryLabel(product.category)}</TableCell>
                  <TableCell className="font-medium">{product.name}</TableCell>
                  <TableCell className="text-right text-sm">
                    {product.price.toLocaleString()}원
                    {product.basePrice !== null && product.basePrice !== product.price && (
                      <div className="text-xs text-gray-400">기본 {product.basePrice.toLocaleString()}원</div>
                    )}
                  </TableCell>
                  <TableCell className="text-sm text-gray-500">{product.memo ?? '—'}</TableCell>
                  <TableCell className="text-center">{toggleButton(product)}</TableCell>
                  <TableCell className="text-center">
                    <div className="flex justify-center gap-1.5">{actionButtons(product)}</div>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
        {result && result.totalPages > 1 && (
          <div className="border-t px-4 py-3">
            <Pagination
              page={result.page}
              totalPages={result.totalPages}
              totalElements={result.totalElements}
              size={result.size}
              onPageChange={changePage}
            />
          </div>
        )}
      </div>

      {/* 모바일 카드 */}
      <div className="space-y-3 md:hidden">
        {loading ? (
          <div className="rounded-lg border bg-white py-10 text-center text-sm text-gray-400">불러오는 중...</div>
        ) : products.length === 0 ? (
          <div className="rounded-lg border bg-white py-10 text-center text-sm text-gray-400">등록된 상품이 없습니다.</div>
        ) : (
          products.map((product) => (
            <div key={product.id} className={`rounded-lg border bg-white p-4 ${product.enabled ? '' : 'opacity-60'}`}>
              <div className="flex items-start justify-between gap-2">
                <div>
                  <div className="flex items-center gap-2">
                    <Badge variant={isBase(product) ? 'secondary' : 'outline'}>{isBase(product) ? '기본' : '자체'}</Badge>
                    <span className="font-medium">{product.name}</span>
                  </div>
                  <div className="mt-0.5 text-xs text-gray-500">{categoryLabel(product.category)}</div>
                  <div className="mt-1 text-sm font-semibold text-green-700">
                    {product.price.toLocaleString()}원
                    {product.basePrice !== null && product.basePrice !== product.price && (
                      <span className="ml-2 text-xs font-normal text-gray-400">기본 {product.basePrice.toLocaleString()}원</span>
                    )}
                  </div>
                  {product.memo && <div className="mt-0.5 text-xs text-gray-500">{product.memo}</div>}
                </div>
                {toggleButton(product)}
              </div>
              <div className="mt-3 flex justify-end gap-1.5">{actionButtons(product)}</div>
            </div>
          ))
        )}
        {result && result.totalPages > 1 && (
          <Pagination
            page={result.page}
            totalPages={result.totalPages}
            totalElements={result.totalElements}
            size={result.size}
            onPageChange={changePage}
          />
        )}
      </div>

      {/* 자체 상품 등록/수정 모달 */}
      <Dialog open={ownModalOpen} onOpenChange={setOwnModalOpen}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>{editTarget ? `${editTarget.name} 수정` : '상품 등록'}</DialogTitle>
          </DialogHeader>
          <div className="space-y-3">
            <div className="space-y-1.5">
              <Label>상품 종류 *</Label>
              <select
                value={form.category}
                onChange={(e) => setForm((f) => ({ ...f, category: e.target.value }))}
                className="h-9 w-full rounded-md border border-input bg-white px-3 text-sm focus:outline-none focus:ring-1 focus:ring-green-700"
              >
                <option value="">선택</option>
                {PRODUCT_CATEGORIES.map((c) => (
                  <option key={c.value} value={c.value}>{c.label}</option>
                ))}
              </select>
            </div>
            <div className="grid grid-cols-3 gap-3">
              <div className="col-span-2 space-y-1.5">
                <Label>상품명 *</Label>
                <Input placeholder="예: 근조화환 3단" value={form.name} onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))} />
              </div>
              <div className="space-y-1.5">
                <Label>순서</Label>
                <Input
                  inputMode="numeric"
                  placeholder="0"
                  value={form.sortOrder}
                  onChange={(e) => setForm((f) => ({ ...f, sortOrder: e.target.value.replace(/\D/g, '') }))}
                />
              </div>
            </div>
            <div className="space-y-1.5">
              <Label>가격 *</Label>
              <div className="relative">
                <Input
                  inputMode="numeric"
                  placeholder="0"
                  value={form.price}
                  onChange={(e) => setForm((f) => ({ ...f, price: formatPrice(e.target.value) }))}
                  className="pr-6"
                />
                <span className="absolute right-3 top-1/2 -translate-y-1/2 text-sm text-gray-400">원</span>
              </div>
            </div>
            <div className="space-y-1.5">
              <Label>메모</Label>
              <Textarea
                placeholder="용도, 특이사항 등 (선택)"
                className="min-h-[70px] resize-none"
                value={form.memo}
                onChange={(e) => setForm((f) => ({ ...f, memo: e.target.value }))}
              />
            </div>
            {modalError && <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-600">{modalError}</p>}
          </div>
          <DialogFooter className="gap-2">
            <Button variant="outline" onClick={() => setOwnModalOpen(false)}>취소</Button>
            <Button className="bg-green-700 hover:bg-green-800" disabled={modalSaving} onClick={handleSaveOwn}>
              {modalSaving ? '저장 중...' : editTarget ? '저장' : '등록'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* 기본상품 가격 수정 모달 */}
      <Dialog open={!!priceTarget} onOpenChange={(open) => !open && setPriceTarget(null)}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>{priceTarget?.name} 가격 수정</DialogTitle>
          </DialogHeader>
          <div className="space-y-3">
            <div className="space-y-1.5">
              <Label>가격 *</Label>
              <div className="relative">
                <Input
                  inputMode="numeric"
                  placeholder="0"
                  value={priceInput}
                  onChange={(e) => setPriceInput(formatPrice(e.target.value))}
                  className="pr-6"
                />
                <span className="absolute right-3 top-1/2 -translate-y-1/2 text-sm text-gray-400">원</span>
              </div>
            </div>
            {priceTarget?.basePrice != null && (
              <div className="flex items-center justify-between text-xs text-gray-500">
                <span>기본 가격 {priceTarget.basePrice.toLocaleString()}원</span>
                <button
                  type="button"
                  className="text-blue-700 hover:underline"
                  onClick={() => setPriceInput(priceTarget.basePrice!.toLocaleString())}
                >
                  기본 가격으로 되돌리기
                </button>
              </div>
            )}
            {modalError && <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-600">{modalError}</p>}
          </div>
          <DialogFooter className="gap-2">
            <Button variant="outline" onClick={() => setPriceTarget(null)}>취소</Button>
            <Button className="bg-green-700 hover:bg-green-800" disabled={modalSaving} onClick={handleSavePrice}>
              {modalSaving ? '저장 중...' : '저장'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* 삭제 확인 모달 */}
      <Dialog open={!!deleteTarget} onOpenChange={(open) => !open && setDeleteTarget(null)}>
        <DialogContent className="max-w-xs">
          <DialogHeader>
            <DialogTitle>상품 삭제</DialogTitle>
          </DialogHeader>
          <p className="text-sm text-gray-600">
            <span className="font-semibold">{deleteTarget?.name}</span>을(를) 삭제하시겠습니까?
            <br />
            <span className="text-xs text-gray-400">삭제 후 복구할 수 없습니다.</span>
          </p>
          <DialogFooter className="gap-2">
            <Button variant="outline" onClick={() => setDeleteTarget(null)}>취소</Button>
            <Button variant="destructive" disabled={deleting} onClick={handleDelete}>
              {deleting ? '삭제 중...' : '삭제'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}

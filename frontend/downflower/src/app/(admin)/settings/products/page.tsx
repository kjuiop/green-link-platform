'use client'

import { useState, useEffect, useCallback } from 'react'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog'
import { Textarea } from '@/components/ui/textarea'
import { Pagination } from '@/components/ui/Pagination'
import { Product } from '@/types/product'
import { PageResult } from '@/types/page'
import { fetchProducts, createProduct, updateProduct, deleteProduct } from '@/lib/api/productApi'

const PAGE_SIZE = 20

const emptyForm = { name: '', price: '', memo: '' }

function formatPrice(value: string) {
  const digits = value.replace(/\D/g, '')
  return digits ? Number(digits).toLocaleString() : ''
}

export default function ProductsPage() {
  const [result, setResult] = useState<PageResult<Product> | null>(null)
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [pageError, setPageError] = useState('')

  const [modalOpen, setModalOpen] = useState(false)
  const [editTarget, setEditTarget] = useState<Product | null>(null)
  const [form, setForm] = useState(emptyForm)
  const [modalError, setModalError] = useState('')
  const [modalSaving, setModalSaving] = useState(false)

  const [deleteTarget, setDeleteTarget] = useState<Product | null>(null)
  const [deleting, setDeleting] = useState(false)

  const load = useCallback((p: number) => {
    setLoading(true)
    setPageError('')
    fetchProducts(p, PAGE_SIZE)
      .then(setResult)
      .catch((e) => setPageError(e.message))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => { load(page) }, [page, load])

  const openRegister = () => {
    setEditTarget(null)
    setForm(emptyForm)
    setModalError('')
    setModalOpen(true)
  }

  const openEdit = (product: Product) => {
    setEditTarget(product)
    setForm({
      name: product.name,
      price: product.price.toLocaleString(),
      memo: product.memo ?? '',
    })
    setModalError('')
    setModalOpen(true)
  }

  const handleSave = async () => {
    const rawPrice = Number(form.price.replace(/,/g, ''))
    if (!form.name.trim()) { setModalError('상품명을 입력해주세요.'); return }
    if (!rawPrice || rawPrice <= 0) { setModalError('가격을 입력해주세요.'); return }

    setModalSaving(true)
    setModalError('')
    try {
      if (editTarget) {
        await updateProduct(editTarget.id, {
          name: form.name.trim(),
          price: rawPrice,
          memo: form.memo.trim() || undefined,
        })
      } else {
        await createProduct({
          name: form.name.trim(),
          price: rawPrice,
          memo: form.memo.trim() || undefined,
        })
        setPage(0)
      }
      load(editTarget ? page : 0)
      setModalOpen(false)
    } catch (e: unknown) {
      setModalError(e instanceof Error ? e.message : '저장에 실패했습니다.')
    } finally {
      setModalSaving(false)
    }
  }

  const handleDelete = async () => {
    if (!deleteTarget) return
    setDeleting(true)
    try {
      await deleteProduct(deleteTarget.id)
      setDeleteTarget(null)
      load(page)
    } catch (e: unknown) {
      setPageError(e instanceof Error ? e.message : '삭제에 실패했습니다.')
    } finally {
      setDeleting(false)
    }
  }

  const products = result?.content ?? []

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold">상품관리</h1>
        <Button className="bg-green-700 hover:bg-green-800" onClick={openRegister}>상품 등록</Button>
      </div>

      {pageError && (
        <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-600">{pageError}</p>
      )}

      {/* 데스크톱 테이블 */}
      <div className="hidden rounded-lg border bg-white md:block">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>상품명</TableHead>
              <TableHead className="w-36 text-right">기본가격</TableHead>
              <TableHead>메모</TableHead>
              <TableHead className="w-32">등록일</TableHead>
              <TableHead className="w-24 text-center">관리</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={5} className="py-10 text-center text-sm text-gray-400">
                  불러오는 중...
                </TableCell>
              </TableRow>
            ) : products.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} className="py-10 text-center text-sm text-gray-400">
                  등록된 상품이 없습니다.
                </TableCell>
              </TableRow>
            ) : (
              products.map((product) => (
                <TableRow key={product.id}>
                  <TableCell>
                    <button
                      onClick={() => openEdit(product)}
                      className="font-medium text-blue-700 hover:underline"
                    >
                      {product.name}
                    </button>
                  </TableCell>
                  <TableCell className="text-right text-sm">
                    {product.price.toLocaleString()}원
                  </TableCell>
                  <TableCell className="text-sm text-gray-500">{product.memo ?? '—'}</TableCell>
                  <TableCell className="text-sm text-gray-500">
                    {product.createdAt.slice(0, 10)}
                  </TableCell>
                  <TableCell className="text-center">
                    <div className="flex justify-center gap-1.5">
                      <button
                        onClick={() => openEdit(product)}
                        className="rounded border px-2.5 py-1 text-xs text-gray-600 hover:bg-gray-50"
                      >
                        수정
                      </button>
                      <button
                        onClick={() => setDeleteTarget(product)}
                        className="rounded border border-red-200 px-2.5 py-1 text-xs text-red-500 hover:bg-red-50"
                      >
                        삭제
                      </button>
                    </div>
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
              onPageChange={setPage}
            />
          </div>
        )}
      </div>

      {/* 모바일 카드 */}
      <div className="space-y-3 md:hidden">
        {loading ? (
          <div className="rounded-lg border bg-white py-10 text-center text-sm text-gray-400">
            불러오는 중...
          </div>
        ) : products.length === 0 ? (
          <div className="rounded-lg border bg-white py-10 text-center text-sm text-gray-400">
            등록된 상품이 없습니다.
          </div>
        ) : (
          products.map((product) => (
            <div key={product.id} className="rounded-lg border bg-white p-4">
              <div className="flex items-start justify-between gap-2">
                <div>
                  <button
                    onClick={() => openEdit(product)}
                    className="font-medium text-blue-700 hover:underline"
                  >
                    {product.name}
                  </button>
                  <div className="mt-1 text-sm font-semibold text-green-700">
                    {product.price.toLocaleString()}원
                  </div>
                  {product.memo && (
                    <div className="mt-0.5 text-xs text-gray-500">{product.memo}</div>
                  )}
                  <div className="mt-1 text-xs text-gray-400">{product.createdAt.slice(0, 10)}</div>
                </div>
                <div className="flex gap-1.5">
                  <button
                    onClick={() => openEdit(product)}
                    className="rounded border px-2.5 py-1 text-xs text-gray-600 hover:bg-gray-50"
                  >
                    수정
                  </button>
                  <button
                    onClick={() => setDeleteTarget(product)}
                    className="rounded border border-red-200 px-2.5 py-1 text-xs text-red-500 hover:bg-red-50"
                  >
                    삭제
                  </button>
                </div>
              </div>
            </div>
          ))
        )}
        {result && result.totalPages > 1 && (
          <Pagination
            page={result.page}
            totalPages={result.totalPages}
            totalElements={result.totalElements}
            size={result.size}
            onPageChange={setPage}
          />
        )}
      </div>

      {/* 등록/수정 모달 */}
      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>{editTarget ? `${editTarget.name} 수정` : '상품 등록'}</DialogTitle>
          </DialogHeader>
          <div className="space-y-3">
            <div className="space-y-1.5">
              <Label>상품명 *</Label>
              <Input
                placeholder="예: 근조화환 3단"
                value={form.name}
                onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
              />
            </div>
            <div className="space-y-1.5">
              <Label>기본가격 *</Label>
              <div className="relative">
                <Input
                  inputMode="numeric"
                  placeholder="0"
                  value={form.price}
                  onChange={(e) =>
                    setForm((f) => ({ ...f, price: formatPrice(e.target.value) }))
                  }
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
            {modalError && (
              <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-600">{modalError}</p>
            )}
          </div>
          <DialogFooter className="gap-2">
            <Button variant="outline" onClick={() => setModalOpen(false)}>취소</Button>
            <Button
              className="bg-green-700 hover:bg-green-800"
              disabled={modalSaving}
              onClick={handleSave}
            >
              {modalSaving ? '저장 중...' : editTarget ? '저장' : '등록'}
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
            <Button
              variant="destructive"
              disabled={deleting}
              onClick={handleDelete}
            >
              {deleting ? '삭제 중...' : '삭제'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}

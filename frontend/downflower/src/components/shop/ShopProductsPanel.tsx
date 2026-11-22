'use client'

import Link from 'next/link'
import { useCallback, useEffect, useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Button } from '@/components/ui/button'
import { ShopCompanySelect } from '@/components/shop/ShopCompanySelect'
import { useShopScope } from '@/components/shop/useShopScope'
import {
  SHOP_CATEGORIES,
  createShopProduct,
  deleteShopProduct,
  fetchShopProducts,
  reorderShopProducts,
  updateShopProduct,
  uploadShopImage,
  type SaveShopProductData,
  type ShopProduct,
} from '@/lib/api/shopApi'

const EMPTY: SaveShopProductData = {
  category: 'FUNERAL',
  name: '',
  description: '',
  price: 0,
  imageUrl: '',
  visible: true,
}

function won(n: number) {
  return `${n.toLocaleString('ko-KR')}원`
}

interface ShopProductsPanelProps {
  scope: ReturnType<typeof useShopScope>
  showHeader?: boolean
}

export function ShopProductsPanel({ scope, showHeader = true }: ShopProductsPanelProps) {
  const companyId = scope.selectedId

  const [products, setProducts] = useState<ShopProduct[]>([])
  const [loading, setLoading] = useState(true)
  const [editingId, setEditingId] = useState<number | 'new' | null>(null)
  const [form, setForm] = useState<SaveShopProductData>(EMPTY)
  const [priceText, setPriceText] = useState('')
  const [saving, setSaving] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [busyId, setBusyId] = useState<number | null>(null)
  const [message, setMessage] = useState<{ kind: 'ok' | 'error'; text: string } | null>(null)

  const load = useCallback(async () => {
    if (scope.isSuperAdmin && !companyId) {
      setLoading(false)
      return
    }
    setLoading(true)
    try {
      setProducts(await fetchShopProducts(companyId))
    } catch (e) {
      setMessage({ kind: 'error', text: e instanceof Error ? e.message : '불러오지 못했습니다.' })
    } finally {
      setLoading(false)
    }
  }, [scope.isSuperAdmin, companyId])

  useEffect(() => {
    if (!scope.ready) return
    setEditingId(null)
    load()
  }, [scope.ready, load])

  function startNew() {
    setForm(EMPTY)
    setPriceText('')
    setEditingId('new')
    setMessage(null)
  }

  function startEdit(p: ShopProduct) {
    setForm({
      category: p.category,
      name: p.name,
      description: p.description ?? '',
      price: p.price,
      imageUrl: p.imageUrl ?? '',
      visible: p.visible,
    })
    setPriceText(String(p.price))
    setEditingId(p.id)
    setMessage(null)
  }

  async function handleUpload(file: File | undefined) {
    if (!file) return
    setUploading(true)
    setMessage(null)
    try {
      const url = await uploadShopImage(companyId, file)
      setForm((prev) => ({ ...prev, imageUrl: url }))
    } catch (e) {
      setMessage({ kind: 'error', text: e instanceof Error ? e.message : '업로드에 실패했습니다.' })
    } finally {
      setUploading(false)
    }
  }

  async function handleSave() {
    if (editingId === null) return
    const price = Number(priceText)
    if (!form.name.trim()) {
      setMessage({ kind: 'error', text: '상품명을 입력해 주세요.' })
      return
    }
    if (!Number.isInteger(price) || price < 0) {
      setMessage({ kind: 'error', text: '판매가를 숫자로 입력해 주세요.' })
      return
    }
    setSaving(true)
    setMessage(null)
    try {
      const data = { ...form, price }
      if (editingId === 'new') await createShopProduct(companyId, data)
      else await updateShopProduct(companyId, editingId, data)
      setEditingId(null)
      await load()
    } catch (e) {
      setMessage({ kind: 'error', text: e instanceof Error ? e.message : '저장에 실패했습니다.' })
    } finally {
      setSaving(false)
    }
  }

  async function handleToggleVisible(p: ShopProduct) {
    setBusyId(p.id)
    setMessage(null)
    try {
      await updateShopProduct(companyId, p.id, {
        category: p.category,
        name: p.name,
        description: p.description ?? '',
        price: p.price,
        imageUrl: p.imageUrl ?? '',
        visible: !p.visible,
      })
      await load()
    } catch (e) {
      setMessage({ kind: 'error', text: e instanceof Error ? e.message : '변경에 실패했습니다.' })
    } finally {
      setBusyId(null)
    }
  }

  async function handleDelete(p: ShopProduct) {
    if (!window.confirm(`'${p.name}' 상품을 삭제할까요?`)) return
    setBusyId(p.id)
    setMessage(null)
    try {
      await deleteShopProduct(companyId, p.id)
      await load()
    } catch (e) {
      setMessage({ kind: 'error', text: e instanceof Error ? e.message : '삭제에 실패했습니다.' })
    } finally {
      setBusyId(null)
    }
  }

  async function handleMove(index: number, delta: -1 | 1) {
    const target = index + delta
    if (target < 0 || target >= products.length) return
    const next = [...products]
    ;[next[index], next[target]] = [next[target], next[index]]
    setProducts(next)
    try {
      await reorderShopProducts(companyId, next.map((p) => p.id))
    } catch (e) {
      setMessage({ kind: 'error', text: e instanceof Error ? e.message : '순서 변경에 실패했습니다.' })
      await load()
    }
  }

  return (
    <div className="mx-auto max-w-3xl space-y-4">
      {showHeader && (
        <div className="flex flex-wrap items-center justify-between gap-2">
          <h2 className="text-base font-semibold">쇼핑몰 상품</h2>
          <div className="flex items-center gap-2">
            <ShopCompanySelect scope={scope} />
            <Button onClick={startNew} className="bg-green-700 hover:bg-green-800" disabled={editingId !== null}>
              상품 추가
            </Button>
          </div>
        </div>
      )}
      {!showHeader && (
        <div className="flex justify-end">
          <Button onClick={startNew} className="bg-green-700 hover:bg-green-800" disabled={editingId !== null}>
            상품 추가
          </Button>
        </div>
      )}
      <p className="text-xs text-gray-400">
        고객에게 보이는 쇼핑몰 전용 상품입니다. 수주사 상품과는 별개이며, 판매가는 정산·포인트에 반영되지 않습니다.{' '}
        <Link href="/integration-settings?market=shop" className="underline">쇼핑몰 설정</Link>
      </p>

      {message && (
        <p className={message.kind === 'ok' ? 'text-sm text-green-700' : 'text-sm text-red-500'}>{message.text}</p>
      )}

      {editingId !== null && (
        <Card>
          <CardHeader>
            <CardTitle className="text-sm">{editingId === 'new' ? '상품 추가' : '상품 수정'}</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="grid gap-3 sm:grid-cols-2">
              <div className="space-y-1.5">
                <Label>분류 *</Label>
                <select
                  className="w-full rounded-md border bg-white px-2 py-2 text-sm"
                  value={form.category}
                  onChange={(e) => setForm((prev) => ({ ...prev, category: e.target.value }))}
                >
                  {SHOP_CATEGORIES.map((c) => (
                    <option key={c.value} value={c.value}>{c.label}</option>
                  ))}
                </select>
              </div>
              <div className="space-y-1.5">
                <Label>판매가(원) *</Label>
                <Input
                  inputMode="numeric"
                  value={priceText}
                  onChange={(e) => setPriceText(e.target.value.replace(/[^\d]/g, ''))}
                />
              </div>
            </div>
            <div className="space-y-1.5">
              <Label>상품명 *</Label>
              <Input value={form.name} maxLength={100} onChange={(e) => setForm((prev) => ({ ...prev, name: e.target.value }))} />
            </div>
            <div className="space-y-1.5">
              <Label>설명</Label>
              <Textarea
                rows={3}
                maxLength={2000}
                value={form.description}
                onChange={(e) => setForm((prev) => ({ ...prev, description: e.target.value }))}
              />
            </div>
            <div className="space-y-1.5">
              <Label>상품 이미지</Label>
              {form.imageUrl && (
                // eslint-disable-next-line @next/next/no-img-element
                <img src={form.imageUrl} alt="" className="h-32 w-32 rounded border bg-gray-50 object-cover" />
              )}
              <div className="flex items-center gap-2">
                <input
                  type="file"
                  accept="image/jpeg,image/png,image/webp"
                  disabled={uploading}
                  onChange={(e) => { handleUpload(e.target.files?.[0]); e.target.value = '' }}
                  className="text-xs"
                />
                {form.imageUrl && (
                  <button
                    type="button"
                    className="text-xs text-gray-500 underline"
                    onClick={() => setForm((prev) => ({ ...prev, imageUrl: '' }))}
                  >
                    삭제
                  </button>
                )}
                {uploading && <span className="text-xs text-gray-400">업로드 중…</span>}
              </div>
            </div>
            <label className="flex items-center gap-2 text-sm">
              <input
                type="checkbox"
                className="accent-green-700"
                checked={form.visible}
                onChange={(e) => setForm((prev) => ({ ...prev, visible: e.target.checked }))}
              />
              쇼핑몰에 노출
            </label>
            <div className="flex justify-end gap-2">
              <Button variant="outline" onClick={() => setEditingId(null)} disabled={saving}>취소</Button>
              <Button onClick={handleSave} disabled={saving || uploading} className="bg-green-700 hover:bg-green-800">
                {saving ? '저장 중…' : '저장'}
              </Button>
            </div>
          </CardContent>
        </Card>
      )}

      {loading ? (
        <p className="py-8 text-center text-sm text-gray-400">불러오는 중…</p>
      ) : products.length === 0 ? (
        <p className="py-8 text-center text-sm text-gray-400">등록된 상품이 없습니다.</p>
      ) : (
        <div className="space-y-2">
          {products.map((p, i) => (
            <Card key={p.id}>
              <CardContent className="flex items-center gap-3 py-3 text-sm">
                {p.imageUrl ? (
                  // eslint-disable-next-line @next/next/no-img-element
                  <img src={p.imageUrl} alt="" className="h-16 w-16 shrink-0 rounded border object-cover" />
                ) : (
                  <div className="flex h-16 w-16 shrink-0 items-center justify-center rounded border bg-gray-50 text-xs text-gray-400">
                    없음
                  </div>
                )}
                <div className="min-w-0 flex-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="rounded bg-gray-100 px-1.5 py-0.5 text-xs text-gray-600">{p.categoryLabel}</span>
                    <span className="truncate font-medium">{p.name}</span>
                    {!p.visible && <span className="text-xs text-gray-400">(숨김)</span>}
                  </div>
                  <div className="text-gray-600">{won(p.price)}</div>
                </div>
                <div className="flex shrink-0 flex-wrap justify-end gap-1">
                  <Button variant="outline" size="sm" disabled={i === 0} onClick={() => handleMove(i, -1)}>▲</Button>
                  <Button variant="outline" size="sm" disabled={i === products.length - 1} onClick={() => handleMove(i, 1)}>▼</Button>
                  <Button variant="outline" size="sm" disabled={busyId === p.id} onClick={() => handleToggleVisible(p)}>
                    {p.visible ? '숨기기' : '노출'}
                  </Button>
                  <Button variant="outline" size="sm" disabled={editingId !== null} onClick={() => startEdit(p)}>수정</Button>
                  <Button variant="outline" size="sm" disabled={busyId === p.id} onClick={() => handleDelete(p)}>삭제</Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}

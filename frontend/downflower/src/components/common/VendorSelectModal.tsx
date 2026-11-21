'use client'

import { useEffect, useMemo, useState } from 'react'
import { ArrowLeft, X } from 'lucide-react'
import { SIDO_LIST } from '@/data/regions'
import { PRODUCT_CATEGORIES, categoryLabel, type ProductCategory } from '@/lib/constants/productCategory'
import { fetchAllProducts, fetchSellableVendorProducts, searchVendorsByProduct } from '@/lib/api/productApi'
import { fetchVendorRecentPhotos, type VendorRecentPhoto } from '@/lib/api/vendorApi'
import { formatRegions } from '@/lib/utils/formatRegions'
import type { Product, VendorProduct, VendorSearchParams, VendorSearchResult } from '@/types/product'

type Props = {
  open: boolean
  onSelect: (vendor: VendorSearchResult, product: VendorProduct) => void
  onClose: () => void
}

type SearchState = { key: string; vendors: VendorSearchResult[]; error: string }
type DetailState = { vendorId: string; products: VendorProduct[]; photos: VendorRecentPhoto[] }

const selectClass =
  'h-8 w-full rounded border border-gray-200 bg-white px-2 text-xs focus:outline-none focus:ring-1 focus:ring-green-700 disabled:bg-gray-50 disabled:text-gray-400'

export default function VendorSelectModal({ open, onSelect, onClose }: Props) {
  if (!open) return null
  return <VendorSelectModalBody onSelect={onSelect} onClose={onClose} />
}

function VendorSelectModalBody({ onSelect, onClose }: Omit<Props, 'open'>) {
  const [category, setCategory] = useState<ProductCategory | ''>('')
  const [baseProductId, setBaseProductId] = useState('')
  const [keywordInput, setKeywordInput] = useState('')
  const [keyword, setKeyword] = useState('')
  const [sido, setSido] = useState('')
  const [sigungu, setSigungu] = useState('')

  const [baseProducts, setBaseProducts] = useState<Product[]>([])
  const [search, setSearch] = useState<SearchState | null>(null)
  const [selectedVendor, setSelectedVendor] = useState<VendorSearchResult | null>(null)
  const [detail, setDetail] = useState<DetailState | null>(null)
  const [showAllProducts, setShowAllProducts] = useState(false)
  const [expandedPhoto, setExpandedPhoto] = useState<string | null>(null)

  const searchKey = JSON.stringify([category, baseProductId, keyword, sido, sigungu])
  const searchLoading = search?.key !== searchKey
  const detailLoading = selectedVendor !== null && detail?.vendorId !== selectedVendor.vendorId

  useEffect(() => {
    fetchAllProducts().then(setBaseProducts).catch(() => {})
  }, [])

  useEffect(() => {
    let cancelled = false
    const params: VendorSearchParams = {
      category: category || undefined,
      baseProductId: baseProductId || undefined,
      keyword: keyword || undefined,
      sido: sido || undefined,
      sigungu: sigungu || undefined,
    }
    searchVendorsByProduct(params)
      .then((vendors) => { if (!cancelled) setSearch({ key: searchKey, vendors, error: '' }) })
      .catch((e: Error) => { if (!cancelled) setSearch({ key: searchKey, vendors: [], error: e.message }) })
    return () => { cancelled = true }
  }, [searchKey, category, baseProductId, keyword, sido, sigungu])

  const selectedVendorId = selectedVendor?.vendorId
  useEffect(() => {
    if (!selectedVendorId) return
    let cancelled = false
    Promise.all([
      fetchSellableVendorProducts(selectedVendorId).catch(() => [] as VendorProduct[]),
      fetchVendorRecentPhotos(selectedVendorId, 5),
    ]).then(([products, photos]) => {
      if (!cancelled) setDetail({ vendorId: selectedVendorId, products, photos })
    })
    return () => { cancelled = true }
  }, [selectedVendorId])

  const baseProductOptions = useMemo(
    () => (category ? baseProducts.filter((p) => p.category === category) : baseProducts),
    [baseProducts, category],
  )
  const sigunguList = useMemo(() => SIDO_LIST.find((s) => s.name === sido)?.sigunguList ?? [], [sido])

  const handleCategoryChange = (value: string) => {
    setCategory(value as ProductCategory | '')
    setBaseProductId('')
  }

  const handleSidoChange = (value: string) => {
    setSido(value)
    setSigungu('')
  }

  const applyKeyword = () => setKeyword(keywordInput.trim())

  const handleVendorClick = (vendor: VendorSearchResult) => {
    setSelectedVendor(vendor)
    setShowAllProducts(false)
  }

  const detailProducts = useMemo(() => {
    if (!detail) return []
    if (showAllProducts) return detail.products
    return detail.products.filter((p) => {
      if (category && p.category !== category) return false
      if (baseProductId && p.baseProductId !== baseProductId) return false
      if (keyword && !p.name.toLowerCase().includes(keyword.toLowerCase())) return false
      return true
    })
  }, [detail, showAllProducts, category, baseProductId, keyword])

  const vendors = search?.vendors ?? []
  const isClosed = selectedVendor?.businessStatus === 'CLOSED'

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-0 md:p-4" onClick={onClose}>
      <div
        className="flex h-full w-full max-w-5xl flex-col bg-white shadow-xl md:h-[85vh] md:rounded-lg"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between border-b px-4 py-3">
          <h2 className="text-base font-semibold">수주업체·상품 선택</h2>
          <button onClick={onClose} className="rounded p-1 hover:bg-gray-100">
            <X className="h-4 w-4" />
          </button>
        </div>

        <div className="space-y-2 border-b bg-gray-50 px-4 py-3">
          <div className="grid grid-cols-1 gap-2 sm:grid-cols-3">
            <select value={category} onChange={(e) => handleCategoryChange(e.target.value)} className={selectClass}>
              <option value="">상품 종류 전체</option>
              {PRODUCT_CATEGORIES.map((c) => (
                <option key={c.value} value={c.value}>{c.label}</option>
              ))}
            </select>
            <select value={baseProductId} onChange={(e) => setBaseProductId(e.target.value)} className={selectClass}>
              <option value="">기본상품 전체</option>
              {baseProductOptions.map((p) => (
                <option key={p.id} value={p.id}>{p.name}</option>
              ))}
            </select>
            <div className="flex gap-1.5">
              <input
                value={keywordInput}
                onChange={(e) => setKeywordInput(e.target.value)}
                onKeyDown={(e) => { if (e.key === 'Enter') applyKeyword() }}
                placeholder="상품명 검색"
                className="h-8 min-w-0 flex-1 rounded border border-gray-200 bg-white px-2 text-xs focus:outline-none focus:ring-1 focus:ring-green-700"
              />
              <button onClick={applyKeyword} className="shrink-0 rounded bg-green-700 px-3 text-xs text-white hover:bg-green-800">
                검색
              </button>
            </div>
          </div>
          <div className="grid grid-cols-2 gap-2 sm:grid-cols-3">
            <select value={sido} onChange={(e) => handleSidoChange(e.target.value)} className={selectClass}>
              <option value="">시·도 전체</option>
              {SIDO_LIST.map((s) => (
                <option key={s.name} value={s.name}>{s.name}</option>
              ))}
            </select>
            <select value={sigungu} onChange={(e) => setSigungu(e.target.value)} disabled={!sido} className={selectClass}>
              <option value="">시·군·구 전체</option>
              {sigunguList.map((sg) => (
                <option key={sg} value={sg}>{sg}</option>
              ))}
            </select>
          </div>
        </div>

        <div className="flex min-h-0 flex-1">
          <div className={`min-h-0 w-full flex-col border-r md:flex md:w-5/12 ${selectedVendor ? 'hidden' : 'flex'}`}>
            <p className="border-b px-4 py-2 text-xs text-gray-500">
              {searchLoading ? '검색 중...' : `${vendors.length}개 업체`}
            </p>
            <div className="flex-1 space-y-2 overflow-y-auto p-3">
              {search?.error && <p className="rounded bg-red-50 px-3 py-2 text-xs text-red-600">{search.error}</p>}
              {!searchLoading && vendors.length === 0 && !search?.error && (
                <p className="py-10 text-center text-sm text-gray-400">조건에 맞는 수주업체가 없습니다.</p>
              )}
              {vendors.map((v) => {
                const active = selectedVendor?.vendorId === v.vendorId
                return (
                  <button
                    key={v.vendorId}
                    onClick={() => handleVendorClick(v)}
                    className={`w-full rounded-lg border p-3 text-left transition-colors ${
                      active ? 'border-green-600 bg-green-50' : 'bg-white hover:border-green-500 hover:bg-green-50'
                    }`}
                  >
                    <div className="flex items-center gap-2">
                      <p className="text-sm font-medium">{v.companyName}</p>
                      {v.businessStatus === 'CLOSED' && (
                        <span className="rounded bg-gray-200 px-1.5 py-0.5 text-xs text-gray-500">휴무중</span>
                      )}
                    </div>
                    <p className="mt-1 text-xs text-gray-600">
                      최저 <span className="font-semibold text-green-700">{v.minPrice.toLocaleString()}원</span>
                      <span className="ml-2 text-gray-400">상품 {v.matchedProductCount}개</span>
                    </p>
                    {v.regions.length > 0 && (
                      <p className="mt-1 truncate text-xs text-gray-400">{formatRegions(v.regions)}</p>
                    )}
                  </button>
                )
              })}
            </div>
          </div>

          <div className={`min-h-0 w-full flex-col md:flex md:w-7/12 ${selectedVendor ? 'flex' : 'hidden'}`}>
            {!selectedVendor ? (
              <p className="m-auto text-sm text-gray-400">왼쪽에서 수주업체를 선택하세요.</p>
            ) : (
              <div className="flex-1 space-y-5 overflow-y-auto p-4">
                <button
                  onClick={() => setSelectedVendor(null)}
                  className="flex items-center gap-1 text-xs text-gray-500 hover:text-gray-800 md:hidden"
                >
                  <ArrowLeft className="h-3.5 w-3.5" /> 업체 목록
                </button>

                <div>
                  <div className="flex items-center gap-2">
                    <h3 className="text-base font-semibold">{selectedVendor.companyName}</h3>
                    {isClosed && <span className="rounded bg-gray-200 px-1.5 py-0.5 text-xs text-gray-500">휴무중</span>}
                  </div>
                  <dl className="mt-2 space-y-1 text-xs">
                    <div className="flex gap-2">
                      <dt className="w-16 shrink-0 text-gray-400">배송 가능</dt>
                      <dd className="text-gray-700">
                        {selectedVendor.regions.length > 0 ? formatRegions(selectedVendor.regions) : '-'}
                      </dd>
                    </div>
                    <div className="flex gap-2">
                      <dt className="w-16 shrink-0 text-gray-400">연락처</dt>
                      <dd className="text-gray-700">{selectedVendor.phone ?? '-'}</dd>
                    </div>
                  </dl>
                </div>

                <div>
                  <p className="mb-2 text-xs font-medium text-gray-500">최근 배송완료 사진</p>
                  {detailLoading ? (
                    <p className="text-xs text-gray-400">불러오는 중...</p>
                  ) : !detail || detail.photos.length === 0 ? (
                    <p className="text-xs text-gray-400">등록된 배송완료 사진이 없습니다.</p>
                  ) : (
                    <div className="flex flex-wrap gap-2">
                      {detail.photos.map((p) => (
                        <img
                          key={p.id}
                          src={p.photoUrl}
                          alt="배송완료 사진"
                          className="h-20 w-20 cursor-pointer rounded object-cover transition-opacity hover:opacity-80"
                          onClick={() => setExpandedPhoto(p.photoUrl)}
                        />
                      ))}
                    </div>
                  )}
                </div>

                <div>
                  <div className="mb-2 flex items-center justify-between">
                    <p className="text-xs font-medium text-gray-500">상품 목록</p>
                    <button
                      onClick={() => setShowAllProducts((v) => !v)}
                      className="text-xs text-blue-600 hover:underline"
                    >
                      {showAllProducts ? '조건에 맞는 상품만' : '전체 상품 보기'}
                    </button>
                  </div>
                  {detailLoading ? (
                    <p className="py-6 text-center text-xs text-gray-400">불러오는 중...</p>
                  ) : detailProducts.length === 0 ? (
                    <p className="py-6 text-center text-xs text-gray-400">표시할 상품이 없습니다.</p>
                  ) : (
                    <ul className="space-y-2">
                      {detailProducts.map((p) => (
                        <li key={p.id} className="flex items-center justify-between gap-3 rounded-lg border p-3">
                          <div className="min-w-0">
                            <p className="truncate text-sm font-medium">{p.name}</p>
                            <p className="mt-0.5 text-xs text-gray-500">
                              {categoryLabel(p.category)}
                              {p.memo && <span className="ml-2 text-gray-400">{p.memo}</span>}
                            </p>
                            <p className="mt-0.5 text-sm font-semibold text-green-700">{p.price.toLocaleString()}원</p>
                          </div>
                          <button
                            disabled={isClosed}
                            onClick={() => onSelect(selectedVendor, p)}
                            className="shrink-0 rounded bg-green-700 px-3 py-1.5 text-xs text-white hover:bg-green-800 disabled:cursor-not-allowed disabled:bg-gray-300"
                          >
                            선택
                          </button>
                        </li>
                      ))}
                    </ul>
                  )}
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {expandedPhoto && (
        <div
          className="fixed inset-0 z-[100] flex items-center justify-center bg-black/75"
          onClick={(e) => { e.stopPropagation(); setExpandedPhoto(null) }}
        >
          <img src={expandedPhoto} alt="배송완료 사진 확대" className="max-h-[90vh] max-w-[90vw] rounded shadow-xl" />
        </div>
      )}
    </div>
  )
}

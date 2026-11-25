'use client'

import { useEffect, useState } from 'react'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { createBrand, deleteBrand, fetchBrands, renameBrand } from '@/lib/api/brandApi'
import { fetchCompanies } from '@/lib/api/shopAdminApi'
import { useSession } from '@/lib/useSession'
import type { Brand, CompanyOption } from '@/types/brand'

const SELECT_CLASS = 'h-8 rounded-md border border-gray-200 bg-white px-2 text-sm'

export default function BrandsPage() {
  const { session } = useSession()
  const isSuper = session?.role === 'SUPER_ADMIN'

  const [companies, setCompanies] = useState<CompanyOption[]>([])
  const [companyFilter, setCompanyFilter] = useState('')
  const [brands, setBrands] = useState<Brand[] | null>(null)
  const [loadError, setLoadError] = useState(false)
  const [reloadKey, setReloadKey] = useState(0)
  const [busy, setBusy] = useState(false)
  const [actionError, setActionError] = useState('')

  const [newName, setNewName] = useState('')
  const [newCompany, setNewCompany] = useState('')
  const [editingId, setEditingId] = useState<number | null>(null)
  const [editName, setEditName] = useState('')

  useEffect(() => {
    if (!isSuper) return
    let cancelled = false
    fetchCompanies()
      .then((result) => { if (!cancelled) setCompanies(result) })
      .catch(() => { if (!cancelled) setCompanies([]) })
    return () => { cancelled = true }
  }, [isSuper])

  useEffect(() => {
    if (!session) return
    let cancelled = false
    const companyId = isSuper && companyFilter ? Number(companyFilter) : undefined
    fetchBrands(companyId)
      .then((result) => {
        if (cancelled) return
        setBrands(result)
        setLoadError(false)
      })
      .catch(() => { if (!cancelled) setLoadError(true) })
    return () => { cancelled = true }
  }, [session, isSuper, companyFilter, reloadKey])

  const companyName = (id: number) => companies.find((c) => c.id === id)?.name ?? `#${id}`

  function changeCompanyFilter(next: string) {
    setCompanyFilter(next)
    setBrands(null)
    setLoadError(false)
    setEditingId(null)
    setActionError('')
  }

  async function run(action: () => Promise<void>, fallback: string): Promise<boolean> {
    if (busy) return false
    setActionError('')
    setBusy(true)
    try {
      await action()
      setReloadKey((key) => key + 1)
      return true
    } catch (e) {
      setActionError(e instanceof Error ? e.message : fallback)
      return false
    } finally {
      setBusy(false)
    }
  }

  async function handleCreate() {
    const name = newName.trim()
    if (!name) {
      setActionError('브랜드 이름을 입력하세요.')
      return
    }
    if (isSuper && !newCompany) {
      setActionError('회사를 선택하세요.')
      return
    }
    const ok = await run(
      async () => { await createBrand(name, isSuper ? Number(newCompany) : undefined) },
      '브랜드를 만들지 못했습니다.',
    )
    if (ok) setNewName('')
  }

  async function handleRename(id: number) {
    const name = editName.trim()
    if (!name) {
      setActionError('브랜드 이름을 입력하세요.')
      return
    }
    const ok = await run(async () => { await renameBrand(id, name) }, '이름을 변경하지 못했습니다.')
    if (ok) setEditingId(null)
  }

  async function handleDelete(brand: Brand) {
    if (!window.confirm(`'${brand.name}' 브랜드를 삭제할까요? 배정된 관리자의 브랜드 배정도 함께 해제됩니다.`)) return
    await run(() => deleteBrand(brand.id), '브랜드를 삭제하지 못했습니다.')
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h1 className="text-xl font-bold">브랜드 관리</h1>
        {isSuper && (
          <select
            aria-label="회사 필터"
            className={SELECT_CLASS}
            value={companyFilter}
            onChange={(e) => changeCompanyFilter(e.target.value)}
          >
            <option value="">전체 회사</option>
            {companies.map((c) => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </select>
        )}
      </div>

      <Card>
        <CardContent>
          <div className="flex flex-wrap items-center gap-2">
            {isSuper && (
              <select
                aria-label="브랜드를 만들 회사"
                className={SELECT_CLASS}
                value={newCompany}
                disabled={busy}
                onChange={(e) => setNewCompany(e.target.value)}
              >
                <option value="">회사 선택</option>
                {companies.map((c) => (
                  <option key={c.id} value={c.id}>{c.name}</option>
                ))}
              </select>
            )}
            <Input
              className="min-w-48 flex-1"
              placeholder="새 브랜드 이름 (최대 100자)"
              aria-label="새 브랜드 이름"
              maxLength={100}
              value={newName}
              disabled={busy}
              onChange={(e) => { setNewName(e.target.value); setActionError('') }}
              onKeyDown={(e) => e.key === 'Enter' && handleCreate()}
            />
            <Button size="sm" className="bg-green-700 hover:bg-green-800" disabled={busy} onClick={handleCreate}>
              브랜드 추가
            </Button>
          </div>
          {actionError && <p className="mt-2 text-xs text-red-500">{actionError}</p>}
        </CardContent>
      </Card>

      <Card>
        <CardContent>
          {loadError ? (
            <p className="py-8 text-center text-sm text-red-500">브랜드 목록을 불러오지 못했습니다.</p>
          ) : brands === null ? (
            <div className="space-y-2">
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-full" />
            </div>
          ) : brands.length === 0 ? (
            <p className="py-8 text-center text-sm text-gray-400">등록된 브랜드가 없습니다.</p>
          ) : (
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>브랜드</TableHead>
                    {isSuper && <TableHead>회사</TableHead>}
                    <TableHead className="text-right">동작</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {brands.map((brand) => (
                    <TableRow key={brand.id}>
                      <TableCell>
                        {editingId === brand.id ? (
                          <Input
                            className="max-w-xs"
                            aria-label="브랜드 이름 수정"
                            maxLength={100}
                            value={editName}
                            disabled={busy}
                            autoFocus
                            onChange={(e) => { setEditName(e.target.value); setActionError('') }}
                            onKeyDown={(e) => e.key === 'Enter' && handleRename(brand.id)}
                          />
                        ) : (
                          <span className="font-medium">{brand.name}</span>
                        )}
                      </TableCell>
                      {isSuper && <TableCell className="whitespace-nowrap">{companyName(brand.companyId)}</TableCell>}
                      <TableCell className="whitespace-nowrap text-right">
                        <div className="flex items-center justify-end gap-1">
                          {editingId === brand.id ? (
                            <>
                              <Button size="sm" disabled={busy} onClick={() => handleRename(brand.id)}>저장</Button>
                              <Button
                                size="sm"
                                variant="ghost"
                                disabled={busy}
                                onClick={() => { setEditingId(null); setActionError('') }}
                              >
                                취소
                              </Button>
                            </>
                          ) : (
                            <>
                              <Button
                                size="sm"
                                variant="outline"
                                disabled={busy}
                                onClick={() => { setEditingId(brand.id); setEditName(brand.name); setActionError('') }}
                              >
                                이름 수정
                              </Button>
                              <Button size="sm" variant="outline" disabled={busy} onClick={() => handleDelete(brand)}>
                                삭제
                              </Button>
                            </>
                          )}
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}

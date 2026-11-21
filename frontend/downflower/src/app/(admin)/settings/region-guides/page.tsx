'use client'

import { useEffect, useMemo, useState } from 'react'
import { Plus, Search } from 'lucide-react'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog'
import { SIDO_LIST } from '@/data/regions'
import {
  changeRegionGuideActive,
  createRegionGuide,
  deleteRegionGuide,
  fetchRegionGuidesBySido,
  updateRegionGuide,
  type RegionGuide,
  type SaveRegionGuideInput,
} from '@/lib/api/regionGuideApi'
import {
  REGION_GUIDE_CATEGORIES,
  REGION_GUIDE_TONE_CLASS,
  regionGuideCategoryMeta,
  type RegionGuideCategory,
} from '@/lib/constants/regionGuideCategory'

const CONTENT_MAX = 1000

interface Loaded {
  key: string
  data: RegionGuide[]
  error: string
}

function emptyForm(sido: string): SaveRegionGuideInput {
  return { sido, sigungu: '', placeName: '', category: 'BAN_ALL', content: '', aliases: '', sortOrder: 0 }
}

function toForm(g: RegionGuide): SaveRegionGuideInput {
  return {
    sido: g.sido,
    sigungu: g.sigungu ?? '',
    placeName: g.placeName ?? '',
    category: g.category,
    content: g.content,
    aliases: g.aliases ?? '',
    sortOrder: g.sortOrder,
  }
}

export default function RegionGuidePage() {
  const [sido, setSido] = useState(SIDO_LIST[0]?.name ?? '서울특별시')
  const [search, setSearch] = useState('')
  const [reloadToken, setReloadToken] = useState(0)
  const [loaded, setLoaded] = useState<Loaded | null>(null)

  const [formOpen, setFormOpen] = useState(false)
  const [editId, setEditId] = useState<number | null>(null)
  const [form, setForm] = useState<SaveRegionGuideInput>(emptyForm(sido))
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState('')

  const [deleteTarget, setDeleteTarget] = useState<RegionGuide | null>(null)
  const [actionError, setActionError] = useState('')

  const requestKey = `${sido}|${reloadToken}`

  useEffect(() => {
    let cancelled = false
    fetchRegionGuidesBySido(sido)
      .then((data) => { if (!cancelled) setLoaded({ key: requestKey, data, error: '' }) })
      .catch((e) => {
        if (!cancelled) setLoaded({ key: requestKey, data: [], error: e instanceof Error ? e.message : '목록을 불러오지 못했습니다.' })
      })
    return () => { cancelled = true }
  }, [sido, requestKey])

  const loading = loaded?.key !== requestKey
  const error = loading ? '' : loaded?.error ?? ''

  const rows = useMemo(() => {
    const all = loaded?.data ?? []
    const q = search.trim().toLowerCase()
    if (!q) return all
    return all.filter((g) =>
      [g.placeName, g.sigungu, g.content, g.aliases].some((v) => v?.toLowerCase().includes(q)),
    )
  }, [loaded, search])

  function openCreate() {
    setEditId(null)
    setForm(emptyForm(sido))
    setSaveError('')
    setFormOpen(true)
  }

  function openEdit(g: RegionGuide) {
    setEditId(g.id)
    setForm(toForm(g))
    setSaveError('')
    setFormOpen(true)
  }

  const isNotice = form.category === 'NOTICE'
  const formValid = form.content.trim() !== '' && (isNotice || form.placeName.trim() !== '')

  async function submitForm() {
    if (!formValid) return
    setSaving(true)
    setSaveError('')
    try {
      if (editId === null) await createRegionGuide(form)
      else await updateRegionGuide(editId, form)
      setFormOpen(false)
      if (form.sido !== sido) setSido(form.sido)
      setReloadToken((t) => t + 1)
    } catch (e) {
      setSaveError(e instanceof Error ? e.message : '저장에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  async function toggleActive(g: RegionGuide) {
    setActionError('')
    try {
      await changeRegionGuideActive(g.id, !g.active)
      setReloadToken((t) => t + 1)
    } catch (e) {
      setActionError(e instanceof Error ? e.message : '노출 상태 변경에 실패했습니다.')
    }
  }

  async function confirmDelete() {
    if (!deleteTarget) return
    setActionError('')
    try {
      await deleteRegionGuide(deleteTarget.id)
      setDeleteTarget(null)
      setReloadToken((t) => t + 1)
    } catch (e) {
      setActionError(e instanceof Error ? e.message : '삭제에 실패했습니다.')
      setDeleteTarget(null)
    }
  }

  const selectClass =
    'h-9 rounded-md border border-gray-200 bg-white px-2 text-sm focus:outline-none focus:ring-1 focus:ring-green-700'

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-xl font-bold">지역별 안내문구 관리</h1>
        <p className="mt-1 text-sm text-gray-500">
          신규발주의 수주업체 선택 화면에서 시·도를 선택하면 노출되는 화환 반입안내입니다. 참고용 안내이며 배송비·발주가 자동으로 제한되지 않습니다.
          노출을 끄면 팝업에서 숨겨지고, 시·도에 노출 중인 안내가 없으면 버튼이 표시되지 않습니다.
        </p>
      </div>

      <div className="flex flex-wrap items-end justify-between gap-2 rounded-lg border bg-white p-4">
        <div className="flex flex-wrap items-center gap-2">
          <select value={sido} onChange={(e) => setSido(e.target.value)} className={selectClass}>
            {SIDO_LIST.map((s) => (
              <option key={s.name} value={s.name}>{s.name}</option>
            ))}
          </select>
          <div className="relative">
            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-gray-400" />
            <Input
              placeholder="장소·내용 검색"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-56 pl-8"
            />
          </div>
        </div>
        <Button className="bg-green-600 hover:bg-green-700" onClick={openCreate}>
          <Plus className="mr-1 h-4 w-4" /> 안내문구 추가
        </Button>
      </div>

      {(error || actionError) && <p className="text-sm text-red-500">{error || actionError}</p>}

      <div className="overflow-x-auto rounded-lg border bg-white">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>구분</TableHead>
              <TableHead>시·군·구</TableHead>
              <TableHead>장소</TableHead>
              <TableHead>내용</TableHead>
              <TableHead className="text-center">정렬</TableHead>
              <TableHead className="text-center">노출</TableHead>
              <TableHead>수정일</TableHead>
              <TableHead className="text-center">처리</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={8} className="py-10 text-center text-sm text-gray-400">불러오는 중...</TableCell>
              </TableRow>
            ) : rows.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} className="py-10 text-center text-sm text-gray-400">등록된 안내문구가 없습니다.</TableCell>
              </TableRow>
            ) : (
              rows.map((g) => {
                const meta = regionGuideCategoryMeta(g.category)
                return (
                  <TableRow key={g.id} className={g.active ? 'hover:bg-gray-50' : 'bg-gray-50 text-gray-400'}>
                    <TableCell>
                      <span className={`whitespace-nowrap rounded px-2 py-0.5 text-xs font-medium ${REGION_GUIDE_TONE_CLASS[meta.tone].badge}`}>
                        {meta.label}
                      </span>
                    </TableCell>
                    <TableCell className="text-sm">{g.sigungu ?? '-'}</TableCell>
                    <TableCell className="text-sm font-medium">
                      {g.placeName ?? '-'}
                      {g.aliases && <span className="block text-xs font-normal text-gray-400">= {g.aliases}</span>}
                    </TableCell>
                    <TableCell className="max-w-md whitespace-pre-line text-sm">{g.content}</TableCell>
                    <TableCell className="text-center text-sm">{g.sortOrder}</TableCell>
                    <TableCell className="text-center">
                      <Button size="sm" variant="outline" className="h-7 text-xs" onClick={() => toggleActive(g)}>
                        {g.active ? '노출중' : '숨김'}
                      </Button>
                    </TableCell>
                    <TableCell className="whitespace-nowrap text-xs text-gray-500">{g.updatedAt ?? '-'}</TableCell>
                    <TableCell className="whitespace-nowrap text-center">
                      <Button size="sm" variant="outline" className="mr-1 h-7 text-xs" onClick={() => openEdit(g)}>
                        수정
                      </Button>
                      <Button
                        size="sm"
                        variant="outline"
                        className="h-7 text-xs text-red-600"
                        onClick={() => setDeleteTarget(g)}
                      >
                        삭제
                      </Button>
                    </TableCell>
                  </TableRow>
                )
              })
            )}
          </TableBody>
        </Table>
      </div>

      <Dialog open={formOpen} onOpenChange={(open) => { if (!open) setFormOpen(false) }}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editId === null ? '안내문구 추가' : '안내문구 수정'}</DialogTitle>
          </DialogHeader>
          <div className="space-y-3">
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1">
                <Label htmlFor="rg-sido">시·도</Label>
                <select
                  id="rg-sido"
                  value={form.sido}
                  onChange={(e) => setForm({ ...form, sido: e.target.value })}
                  className={`${selectClass} w-full`}
                >
                  {SIDO_LIST.map((s) => (
                    <option key={s.name} value={s.name}>{s.name}</option>
                  ))}
                </select>
              </div>
              <div className="space-y-1">
                <Label htmlFor="rg-category">구분</Label>
                <select
                  id="rg-category"
                  value={form.category}
                  onChange={(e) => setForm({ ...form, category: e.target.value as RegionGuideCategory })}
                  className={`${selectClass} w-full`}
                >
                  {REGION_GUIDE_CATEGORIES.map((c) => (
                    <option key={c.value} value={c.value}>{c.label}</option>
                  ))}
                </select>
              </div>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1">
                <Label htmlFor="rg-sigungu">시·군·구 (선택)</Label>
                <Input id="rg-sigungu" value={form.sigungu} onChange={(e) => setForm({ ...form, sigungu: e.target.value })} />
              </div>
              <div className="space-y-1">
                <Label htmlFor="rg-place">장소명{isNotice ? ' (선택)' : ''}</Label>
                <Input id="rg-place" value={form.placeName} onChange={(e) => setForm({ ...form, placeName: e.target.value })} />
              </div>
            </div>
            <div className="space-y-1">
              <Label htmlFor="rg-content">안내 내용</Label>
              <Textarea
                id="rg-content"
                rows={4}
                maxLength={CONTENT_MAX}
                value={form.content}
                onChange={(e) => setForm({ ...form, content: e.target.value })}
              />
              <p className="text-right text-xs text-gray-400">{form.content.length}/{CONTENT_MAX}</p>
            </div>
            <div className="grid grid-cols-3 gap-3">
              <div className="col-span-2 space-y-1">
                <Label htmlFor="rg-aliases">같은 장소 (선택)</Label>
                <Input
                  id="rg-aliases"
                  placeholder="예: 창녕서울병원 장례식장"
                  value={form.aliases}
                  onChange={(e) => setForm({ ...form, aliases: e.target.value })}
                />
              </div>
              <div className="space-y-1">
                <Label htmlFor="rg-sort">정렬순서</Label>
                <Input
                  id="rg-sort"
                  inputMode="numeric"
                  value={String(form.sortOrder)}
                  onChange={(e) => setForm({ ...form, sortOrder: Number(e.target.value.replace(/[^\d]/g, '')) || 0 })}
                />
              </div>
            </div>
            {saveError && <p className="text-sm text-red-500">{saveError}</p>}
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setFormOpen(false)} disabled={saving}>취소</Button>
            <Button className="bg-green-600 hover:bg-green-700" onClick={submitForm} disabled={!formValid || saving}>
              {saving ? '저장 중...' : '저장'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <AlertDialog open={deleteTarget !== null} onOpenChange={(open) => { if (!open) setDeleteTarget(null) }}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>안내문구를 삭제할까요?</AlertDialogTitle>
            <AlertDialogDescription>
              {deleteTarget?.placeName ?? regionGuideCategoryMeta(deleteTarget?.category ?? '').label} 안내가 삭제되며 되돌릴 수 없습니다.
              일시적으로 숨기려면 노출을 끄세요.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>취소</AlertDialogCancel>
            <AlertDialogAction onClick={confirmDelete}>삭제</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}

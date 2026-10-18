'use client'

import { useState, useRef, useEffect } from 'react'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog'
import { Textarea } from '@/components/ui/textarea'
import { Vendor, VendorStatus } from '@/types/vendor'
import { fetchVendors, createVendor, updateVendor, updateVendorStatus } from '@/lib/api/vendorApi'
import { Search } from 'lucide-react'

const STATUS_FILTERS = [
  { label: '전체', value: 'ALL' },
  { label: '활성', value: 'ACTIVE' },
  { label: '대기', value: 'PENDING' },
  { label: '정지', value: 'INACTIVE' },
] as const

type FilterStatus = 'ALL' | VendorStatus

const STATUS_LABELS: Record<VendorStatus, string> = {
  ACTIVE: '활성',
  INACTIVE: '정지',
  PENDING: '대기',
}

const emptyForm = {
  name: '', businessNumber: '', managerName: '', phone: '',
  address: '', loginId: '', password: '',
}

function formatPhone(value: string) {
  const digits = value.replace(/\D/g, '').slice(0, 11)
  if (digits.length <= 3) return digits
  if (digits.length <= 7) return `${digits.slice(0, 3)}-${digits.slice(3)}`
  return `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7)}`
}

export default function VendorsPage() {
  const [vendors, setVendors] = useState<Vendor[]>([])
  const [loading, setLoading] = useState(true)
  const [query, setQuery] = useState('')
  const [statusFilter, setStatusFilter] = useState<FilterStatus>('ALL')
  const [appliedQuery, setAppliedQuery] = useState('')
  const [appliedStatus, setAppliedStatus] = useState<FilterStatus>('ALL')
  const [selected, setSelected] = useState<Set<string>>(new Set())
  const [staged, setStaged] = useState<Record<string, VendorStatus>>({})
  const [saving, setSaving] = useState(false)
  const [pageError, setPageError] = useState('')

  const [modalOpen, setModalOpen] = useState(false)
  const [editTarget, setEditTarget] = useState<Vendor | null>(null)
  const [form, setForm] = useState(emptyForm)
  const [modalError, setModalError] = useState('')
  const [modalSaving, setModalSaving] = useState(false)

  useEffect(() => {
    fetchVendors()
      .then(setVendors)
      .catch((e) => setPageError(e.message))
      .finally(() => setLoading(false))
  }, [])

  const handleSearch = () => {
    setAppliedQuery(query)
    setAppliedStatus(statusFilter)
    setSelected(new Set())
    setStaged({})
  }

  const handleReset = () => {
    setQuery(''); setStatusFilter('ALL')
    setAppliedQuery(''); setAppliedStatus('ALL')
    setSelected(new Set())
    setStaged({})
  }

  const filtered = vendors.filter((v) => {
    const matchStatus = appliedStatus === 'ALL' || v.status === appliedStatus
    const matchQuery =
      !appliedQuery ||
      v.name.includes(appliedQuery) ||
      v.managerName.includes(appliedQuery) ||
      v.phone.includes(appliedQuery)
    return matchStatus && matchQuery
  })

  const allChecked = filtered.length > 0 && filtered.every((v) => selected.has(v.id))
  const someChecked = filtered.some((v) => selected.has(v.id))

  const toggleAll = () => {
    if (allChecked) {
      setSelected((prev) => { const next = new Set(prev); filtered.forEach((v) => next.delete(v.id)); return next })
    } else {
      setSelected((prev) => { const next = new Set(prev); filtered.forEach((v) => next.add(v.id)); return next })
    }
  }

  const toggleOne = (id: string) => {
    setSelected((prev) => {
      const next = new Set(prev)
      next.has(id) ? next.delete(id) : next.add(id)
      return next
    })
  }

  const handleStatusChange = (vendor: Vendor, status: VendorStatus) => {
    setStaged((prev) => ({ ...prev, [vendor.id]: status }))
  }

  const handleStagedSave = async () => {
    if (Object.keys(staged).length === 0) return
    setSaving(true)
    setPageError('')
    try {
      await Promise.all(
        Object.entries(staged).map(([id, status]) =>
          updateVendorStatus(id, status === 'ACTIVE' ? 'activate' : 'suspend')
        )
      )
      const updated = await fetchVendors()
      setVendors(updated)
      setStaged({})
      setSelected(new Set())
    } catch (e: unknown) {
      setPageError(e instanceof Error ? e.message : '상태 변경에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  const openRegister = () => {
    setEditTarget(null)
    setForm(emptyForm)
    setModalError('')
    setModalOpen(true)
  }

  const openEdit = (vendor: Vendor) => {
    setEditTarget(vendor)
    setForm({
      name: vendor.name,
      businessNumber: vendor.businessNumber ?? '',
      managerName: vendor.managerName,
      phone: vendor.phone,
      address: vendor.address,
      loginId: vendor.loginId,
      password: '',
    })
    setModalError('')
    setModalOpen(true)
  }

  const handleSave = async () => {
    if (!form.name || !form.managerName || !form.phone || !form.loginId) return
    if (!editTarget && !form.password) return

    setModalSaving(true)
    setModalError('')
    try {
      if (editTarget) {
        await updateVendor(editTarget.id, {
          companyName: form.name,
          businessNo: form.businessNumber || undefined,
          address: form.address,
          managerName: form.managerName,
          phone: form.phone,
        })
      } else {
        await createVendor({
          companyName: form.name,
          businessNo: form.businessNumber || undefined,
          address: form.address,
          managerName: form.managerName,
          phone: form.phone,
          loginId: form.loginId,
          password: form.password,
        })
      }
      const updated = await fetchVendors()
      setVendors(updated)
      setModalOpen(false)
    } catch (e: unknown) {
      setModalError(e instanceof Error ? e.message : '저장에 실패했습니다.')
    } finally {
      setModalSaving(false)
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold">업체 계정관리</h1>
        <Button className="bg-green-700 hover:bg-green-800" onClick={openRegister}>업체 등록</Button>
      </div>

      {pageError && (
        <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-600">{pageError}</p>
      )}

      <div className="rounded-lg border bg-white p-4">
        <div className="flex flex-wrap gap-3">
          {STATUS_FILTERS.map(({ label, value }) => (
            <label key={value} className="flex cursor-pointer items-center gap-1.5">
              <input
                type="radio"
                name="statusFilter"
                checked={statusFilter === value}
                onChange={() => setStatusFilter(value)}
                className="accent-green-700"
              />
              <span className="text-sm text-gray-700">{label}</span>
            </label>
          ))}
        </div>
        <div className="mt-3 flex gap-2">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
            <input
              type="text"
              placeholder="업체명, 담당자명, 연락처로 검색"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
              className="w-full rounded-md border px-3 py-2 pl-9 text-sm placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-green-700"
            />
          </div>
          <button onClick={handleSearch} className="rounded-md bg-green-700 px-4 py-2 text-sm font-medium text-white hover:bg-green-800">검색</button>
          <button onClick={handleReset} className="rounded-md border px-4 py-2 text-sm font-medium text-gray-500 hover:bg-gray-50">초기화</button>
        </div>
      </div>

      {/* 데스크톱 테이블 */}
      <div className="hidden rounded-lg border bg-white md:block">
        <div className="flex items-center justify-end border-b px-4 py-2">
          <button
            onClick={handleStagedSave}
            disabled={saving || Object.keys(staged).length === 0}
            className="rounded-md bg-green-700 px-4 py-2 text-sm font-medium text-white hover:bg-green-800 disabled:opacity-50"
          >
            {saving ? '저장 중...' : '저장'}
          </button>
        </div>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-10">
                <input
                  type="checkbox"
                  checked={allChecked}
                  ref={(el) => { if (el) el.indeterminate = someChecked && !allChecked }}
                  onChange={toggleAll}
                  className="accent-green-700"
                />
              </TableHead>
              <TableHead>업체명</TableHead>
              <TableHead>담당자명</TableHead>
              <TableHead>연락처</TableHead>
              <TableHead>주소</TableHead>
              <TableHead>아이디</TableHead>
              <TableHead>상태</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={7} className="py-10 text-center text-sm text-gray-400">
                  불러오는 중...
                </TableCell>
              </TableRow>
            ) : filtered.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} className="py-10 text-center text-sm text-gray-400">
                  검색 결과가 없습니다.
                </TableCell>
              </TableRow>
            ) : (
              filtered.map((vendor) => (
                <TableRow key={vendor.id} className={selected.has(vendor.id) ? 'bg-blue-50' : ''}>
                  <TableCell>
                    <input
                      type="checkbox"
                      checked={selected.has(vendor.id)}
                      onChange={() => toggleOne(vendor.id)}
                      className="accent-green-700"
                    />
                  </TableCell>
                  <TableCell>
                    <button onClick={() => openEdit(vendor)} className="font-medium text-blue-700 hover:underline">
                      {vendor.name}
                    </button>
                  </TableCell>
                  <TableCell className="text-sm">{vendor.managerName}</TableCell>
                  <TableCell className="text-sm">{vendor.phone}</TableCell>
                  <TableCell className="text-sm">{vendor.address}</TableCell>
                  <TableCell className="font-mono text-xs text-gray-500">{vendor.loginId}</TableCell>
                  <TableCell>
                    <select
                      value={staged[vendor.id] ?? vendor.status}
                      onChange={(e) => handleStatusChange(vendor, e.target.value as VendorStatus)}
                      className="rounded border border-gray-300 bg-white px-2 py-1 text-xs focus:outline-none focus:ring-1 focus:ring-green-700"
                    >
                      <option value="ACTIVE">활성</option>
                      <option value="INACTIVE">정지</option>
                      <option value="PENDING" disabled>대기</option>
                    </select>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {/* 모바일 카드 */}
      <div className="space-y-3 md:hidden">
        {Object.keys(staged).length > 0 && (
          <div className="flex justify-end">
            <button
              onClick={handleStagedSave}
              disabled={saving}
              className="rounded-md bg-green-700 px-4 py-2 text-sm font-medium text-white hover:bg-green-800 disabled:opacity-50"
            >
              {saving ? '저장 중...' : '변경 저장'}
            </button>
          </div>
        )}
        {loading ? (
          <div className="rounded-lg border bg-white py-10 text-center text-sm text-gray-400">
            불러오는 중...
          </div>
        ) : filtered.length === 0 ? (
          <div className="rounded-lg border bg-white py-10 text-center text-sm text-gray-400">
            검색 결과가 없습니다.
          </div>
        ) : (
          filtered.map((vendor) => (
            <div key={vendor.id} className="rounded-lg border bg-white p-4">
              <div className="flex items-center justify-between gap-2">
                <button onClick={() => openEdit(vendor)} className="truncate font-medium text-blue-700">
                  {vendor.name}
                </button>
                <select
                  value={staged[vendor.id] ?? vendor.status}
                  onChange={(e) => handleStatusChange(vendor, e.target.value as VendorStatus)}
                  className="shrink-0 rounded border border-gray-300 bg-white px-2 py-1 text-xs focus:outline-none focus:ring-1 focus:ring-green-700"
                >
                  <option value="ACTIVE">활성</option>
                  <option value="INACTIVE">정지</option>
                  <option value="PENDING" disabled>대기</option>
                </select>
              </div>
              <div className="mt-1.5 space-y-0.5 text-xs text-gray-500">
                <div>{vendor.managerName} · {vendor.phone}</div>
                {vendor.address && <div className="truncate">{vendor.address}</div>}
                <div className="font-mono text-gray-400">{vendor.loginId}</div>
                <div className={`font-medium ${vendor.status === 'ACTIVE' ? 'text-green-600' : vendor.status === 'PENDING' ? 'text-yellow-600' : 'text-red-500'}`}>
                  {STATUS_LABELS[vendor.status]}
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      {/* 등록/수정 모달 */}
      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent className="max-h-[90vh] max-w-md overflow-y-auto">
          <DialogHeader>
            <DialogTitle>{editTarget ? `${editTarget.name} 수정` : '업체 등록'}</DialogTitle>
          </DialogHeader>
          <div className="space-y-3">
            <div className="space-y-1.5">
              <Label>업체명 *</Label>
              <Input placeholder="업체명" value={form.name} onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))} />
            </div>
            <div className="space-y-1.5">
              <Label>사업자번호</Label>
              <Input placeholder="000-00-00000" value={form.businessNumber} onChange={(e) => setForm((f) => ({ ...f, businessNumber: e.target.value }))} />
            </div>
            <div className="space-y-1.5">
              <Label>담당자명 *</Label>
              <Input placeholder="담당자 성함" value={form.managerName} onChange={(e) => setForm((f) => ({ ...f, managerName: e.target.value }))} />
            </div>
            <div className="space-y-1.5">
              <Label>연락처 *</Label>
              <Input placeholder="010-0000-0000" inputMode="numeric" value={form.phone} onChange={(e) => setForm((f) => ({ ...f, phone: formatPhone(e.target.value) }))} />
            </div>
            <div className="space-y-1.5">
              <Label>주소</Label>
              <Input placeholder="업체 주소" value={form.address} onChange={(e) => setForm((f) => ({ ...f, address: e.target.value }))} />
            </div>
            <div className="space-y-1.5">
              <Label>아이디 *</Label>
              <Input placeholder="영문·숫자 조합 6자 이상" value={form.loginId} onChange={(e) => setForm((f) => ({ ...f, loginId: e.target.value }))} disabled={!!editTarget} />
            </div>
            {!editTarget && (
              <div className="space-y-1.5">
                <Label>초기 비밀번호 *</Label>
                <Input type="password" placeholder="8자 이상" value={form.password} onChange={(e) => setForm((f) => ({ ...f, password: e.target.value }))} />
              </div>
            )}
            {modalError && (
              <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-600">{modalError}</p>
            )}
          </div>
          <DialogFooter className="gap-2">
            <Button variant="outline" onClick={() => setModalOpen(false)}>취소</Button>
            <Button
              className="bg-green-700 hover:bg-green-800"
              disabled={modalSaving || !form.name || !form.managerName || !form.phone || !form.loginId || (!editTarget && !form.password)}
              onClick={handleSave}
            >
              {modalSaving ? '저장 중...' : editTarget ? '저장' : '등록'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}

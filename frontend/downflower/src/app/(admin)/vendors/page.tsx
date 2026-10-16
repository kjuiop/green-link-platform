'use client'

import { useState } from 'react'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog'
import { Textarea } from '@/components/ui/textarea'
import { mockVendors } from '@/lib/mock/vendors'
import { Vendor, VendorStatus } from '@/types/vendor'
import { Search } from 'lucide-react'

const STATUS_FILTERS = [
  { label: '전체', value: 'ALL' },
  { label: '활성', value: 'active' },
  { label: '정지', value: 'suspended' },
] as const

type FilterStatus = 'ALL' | VendorStatus

const emptyForm = { name: '', managerName: '', phone: '', address: '', loginId: '', password: '', memo: '' }

function formatPhone(value: string) {
  const digits = value.replace(/\D/g, '').slice(0, 11)
  if (digits.length <= 3) return digits
  if (digits.length <= 7) return `${digits.slice(0, 3)}-${digits.slice(3)}`
  return `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7)}`
}

export default function VendorsPage() {
  const [vendors, setVendors] = useState<Vendor[]>(mockVendors)
  const [query, setQuery] = useState('')
  const [statusFilter, setStatusFilter] = useState<FilterStatus>('ALL')
  const [appliedQuery, setAppliedQuery] = useState('')
  const [appliedStatus, setAppliedStatus] = useState<FilterStatus>('ALL')

  const [modalOpen, setModalOpen] = useState(false)
  const [editTarget, setEditTarget] = useState<Vendor | null>(null)
  const [form, setForm] = useState(emptyForm)

  const handleSearch = () => {
    setAppliedQuery(query)
    setAppliedStatus(statusFilter)
  }

  const handleReset = () => {
    setQuery(''); setStatusFilter('ALL')
    setAppliedQuery(''); setAppliedStatus('ALL')
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

  const openRegister = () => {
    setEditTarget(null)
    setForm(emptyForm)
    setModalOpen(true)
  }

  const openEdit = (vendor: Vendor) => {
    setEditTarget(vendor)
    setForm({ name: vendor.name, managerName: vendor.managerName, phone: vendor.phone, address: vendor.address, loginId: vendor.loginId, password: '', memo: vendor.memo ?? '' })
    setModalOpen(true)
  }

  const handleToggleStatus = (vendor: Vendor) => {
    setVendors((prev) =>
      prev.map((v) =>
        v.id === vendor.id ? { ...v, status: v.status === 'active' ? 'suspended' : 'active' } : v
      )
    )
  }

  const handleSave = () => {
    if (!form.name || !form.managerName || !form.phone || !form.loginId) return
    if (editTarget) {
      setVendors((prev) =>
        prev.map((v) =>
          v.id === editTarget.id
            ? { ...v, name: form.name, managerName: form.managerName, phone: form.phone, address: form.address, loginId: form.loginId, memo: form.memo || undefined }
            : v
        )
      )
    } else {
      const newVendor: Vendor = {
        id: `v${Date.now()}`,
        name: form.name,
        managerName: form.managerName,
        phone: form.phone,
        address: form.address,
        loginId: form.loginId,
        status: 'active',
        memo: form.memo || undefined,
        createdAt: new Date().toISOString().slice(0, 10),
      }
      setVendors((prev) => [...prev, newVendor])
    }
    setModalOpen(false)
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold">업체 계정관리</h1>
        <Button className="bg-green-700 hover:bg-green-800" onClick={openRegister}>업체 등록</Button>
      </div>

      <div className="rounded-lg border bg-white p-4">
        <div className="flex flex-wrap gap-4">
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
          <button
            onClick={handleSearch}
            className="rounded-md bg-green-700 px-4 py-2 text-sm font-medium text-white hover:bg-green-800"
          >
            검색
          </button>
          <button
            onClick={handleReset}
            className="rounded-md border px-4 py-2 text-sm font-medium text-gray-500 hover:bg-gray-50"
          >
            초기화
          </button>
        </div>
      </div>

      <div className="rounded-lg border bg-white">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>업체명</TableHead>
              <TableHead>담당자명</TableHead>
              <TableHead>연락처</TableHead>
              <TableHead>주소</TableHead>
              <TableHead>아이디</TableHead>
              <TableHead>상태</TableHead>
              <TableHead>관리</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filtered.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} className="py-10 text-center text-sm text-gray-400">
                  검색 결과가 없습니다.
                </TableCell>
              </TableRow>
            ) : (
              filtered.map((vendor) => (
                <TableRow key={vendor.id}>
                  <TableCell className="font-medium">{vendor.name}</TableCell>
                  <TableCell className="text-sm">{vendor.managerName}</TableCell>
                  <TableCell className="text-sm">{vendor.phone}</TableCell>
                  <TableCell className="text-sm">{vendor.address}</TableCell>
                  <TableCell className="font-mono text-xs text-gray-500">{vendor.loginId}</TableCell>
                  <TableCell>
                    <Badge
                      variant={vendor.status === 'active' ? 'default' : 'destructive'}
                      className={vendor.status === 'active' ? 'bg-green-100 text-green-800 hover:bg-green-100' : ''}
                    >
                      {vendor.status === 'active' ? '활성' : '정지'}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <div className="flex gap-1">
                      <button onClick={() => openEdit(vendor)} className="text-xs text-blue-600 hover:underline">수정</button>
                      <span className="text-gray-300">|</span>
                      <button
                        onClick={() => handleToggleStatus(vendor)}
                        className={`text-xs ${vendor.status === 'active' ? 'text-red-600' : 'text-green-600'} hover:underline`}
                      >
                        {vendor.status === 'active' ? '정지' : '활성화'}
                      </button>
                    </div>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>{editTarget ? '업체 정보 수정' : '업체 등록'}</DialogTitle>
          </DialogHeader>
          <div className="space-y-3">
            <div className="space-y-1.5">
              <Label>업체명 *</Label>
              <Input placeholder="업체명" value={form.name} onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))} />
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
            <div className="space-y-1.5">
              <Label>메모</Label>
              <Textarea placeholder="관리자 메모 (선택)" value={form.memo} onChange={(e) => setForm((f) => ({ ...f, memo: e.target.value }))} rows={2} />
            </div>
          </div>
          <DialogFooter className="gap-2">
            <Button variant="outline" onClick={() => setModalOpen(false)}>취소</Button>
            <Button
              className="bg-green-700 hover:bg-green-800"
              disabled={!form.name || !form.managerName || !form.phone || !form.loginId}
              onClick={handleSave}
            >
              {editTarget ? '저장' : '등록'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}

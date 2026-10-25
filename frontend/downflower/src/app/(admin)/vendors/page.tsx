'use client'

import { useState, useEffect, useCallback } from 'react'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog'
import { Pagination } from '@/components/ui/Pagination'
import { PageResult } from '@/types/page'
import { CompanyInfo, CompanyType, fetchCompaniesPaged, updateCompanyType } from '@/lib/api/companyApi'
import { createVendor } from '@/lib/api/vendorApi'
import { Search } from 'lucide-react'

const PAGE_SIZE = 20

const COMPANY_TYPE_OPTIONS: { value: CompanyType; label: string }[] = [
  { value: 'VENDOR', label: '수주사' },
  { value: 'ORDERING', label: '발주사' },
  { value: 'BOTH', label: '둘 다' },
]

const COMPANY_TYPE_LABELS: Record<CompanyType, string> = {
  VENDOR: '수주사',
  ORDERING: '발주사',
  BOTH: '발주+수주',
}

const STATUS_LABELS: Record<string, string> = {
  ACTIVE: '활성',
  INACTIVE: '정지',
}

type RegisterForm = {
  name: string
  businessNumber: string
  managerName: string
  phone: string
  address: string
  loginId: string
  password: string
  companyType: CompanyType
}

const emptyRegisterForm: RegisterForm = {
  name: '', businessNumber: '', managerName: '', phone: '',
  address: '', loginId: '', password: '', companyType: 'VENDOR',
}

function formatPhone(value: string) {
  const digits = value.replace(/\D/g, '').slice(0, 11)
  if (digits.length <= 3) return digits
  if (digits.length <= 7) return `${digits.slice(0, 3)}-${digits.slice(3)}`
  return `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7)}`
}

export default function VendorsPage() {
  const [result, setResult] = useState<PageResult<CompanyInfo> | null>(null)
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [query, setQuery] = useState('')
  const [appliedQuery, setAppliedQuery] = useState('')
  const [pageError, setPageError] = useState('')

  const [typeModalOpen, setTypeModalOpen] = useState(false)
  const [typeTarget, setTypeTarget] = useState<CompanyInfo | null>(null)
  const [selectedType, setSelectedType] = useState<CompanyType>('VENDOR')
  const [typeSaving, setTypeSaving] = useState(false)
  const [typeError, setTypeError] = useState('')

  const [registerOpen, setRegisterOpen] = useState(false)
  const [registerForm, setRegisterForm] = useState<RegisterForm>(emptyRegisterForm)
  const [registerError, setRegisterError] = useState('')
  const [registerSaving, setRegisterSaving] = useState(false)

  const load = useCallback((p: number) => {
    setLoading(true)
    setPageError('')
    fetchCompaniesPaged(p, PAGE_SIZE)
      .then(setResult)
      .catch((e) => setPageError(e.message))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => { load(page) }, [page, load])

  const handleSearch = () => { setAppliedQuery(query); setPage(0) }
  const handleReset = () => { setQuery(''); setAppliedQuery(''); setPage(0) }

  const companies = result?.content ?? []
  const filtered = companies.filter((c) => {
    if (!appliedQuery) return true
    return c.companyName.includes(appliedQuery) || (c.businessNo ?? '').includes(appliedQuery)
  })

  const openTypeEdit = (company: CompanyInfo) => {
    setTypeTarget(company)
    setSelectedType(company.type)
    setTypeError('')
    setTypeModalOpen(true)
  }

  const handleTypeSave = async () => {
    if (!typeTarget) return
    setTypeSaving(true)
    setTypeError('')
    try {
      await updateCompanyType(typeTarget.id, selectedType)
      load(page)
      setTypeModalOpen(false)
    } catch (e: unknown) {
      setTypeError(e instanceof Error ? e.message : '업체유형 변경에 실패했습니다.')
    } finally {
      setTypeSaving(false)
    }
  }

  const openRegister = () => {
    setRegisterForm(emptyRegisterForm)
    setRegisterError('')
    setRegisterOpen(true)
  }

  const handleRegister = async () => {
    if (!registerForm.name || !registerForm.managerName || !registerForm.phone || !registerForm.loginId || !registerForm.password) return
    setRegisterSaving(true)
    setRegisterError('')
    try {
      await createVendor({
        companyName: registerForm.name,
        businessNo: registerForm.businessNumber || undefined,
        address: registerForm.address,
        managerName: registerForm.managerName,
        phone: registerForm.phone,
        loginId: registerForm.loginId,
        password: registerForm.password,
        companyType: registerForm.companyType,
      })
      load(0)
      setPage(0)
      setRegisterOpen(false)
    } catch (e: unknown) {
      setRegisterError(e instanceof Error ? e.message : '업체 등록에 실패했습니다.')
    } finally {
      setRegisterSaving(false)
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
        <div className="flex gap-2">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
            <input
              type="text"
              placeholder="업체명, 사업자번호로 검색"
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
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>업체명</TableHead>
              <TableHead>업체유형</TableHead>
              <TableHead>상태</TableHead>
              <TableHead>사업자번호</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={4} className="py-10 text-center text-sm text-gray-400">
                  불러오는 중...
                </TableCell>
              </TableRow>
            ) : filtered.length === 0 ? (
              <TableRow>
                <TableCell colSpan={4} className="py-10 text-center text-sm text-gray-400">
                  검색 결과가 없습니다.
                </TableCell>
              </TableRow>
            ) : (
              filtered.map((company) => (
                <TableRow key={company.id}>
                  <TableCell>
                    <button onClick={() => openTypeEdit(company)} className="font-medium text-blue-700 hover:underline">
                      {company.companyName}
                    </button>
                  </TableCell>
                  <TableCell>
                    <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${
                      company.type === 'ORDERING' ? 'bg-blue-100 text-blue-700' :
                      company.type === 'BOTH' ? 'bg-purple-100 text-purple-700' :
                      'bg-green-100 text-green-700'
                    }`}>
                      {COMPANY_TYPE_LABELS[company.type]}
                    </span>
                  </TableCell>
                  <TableCell className="text-sm text-gray-600">
                    {STATUS_LABELS[company.status] ?? company.status}
                  </TableCell>
                  <TableCell className="text-sm text-gray-500">{company.businessNo ?? '-'}</TableCell>
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
              onPageChange={(p) => setPage(p)}
            />
          </div>
        )}
      </div>

      {/* 모바일 카드 */}
      <div className="space-y-3 md:hidden">
        {loading ? (
          <div className="rounded-lg border bg-white py-10 text-center text-sm text-gray-400">불러오는 중...</div>
        ) : filtered.length === 0 ? (
          <div className="rounded-lg border bg-white py-10 text-center text-sm text-gray-400">검색 결과가 없습니다.</div>
        ) : (
          filtered.map((company) => (
            <div key={company.id} className="rounded-lg border bg-white p-4">
              <div className="flex items-center justify-between gap-2">
                <button onClick={() => openTypeEdit(company)} className="truncate font-medium text-blue-700">
                  {company.companyName}
                </button>
                <span className={`shrink-0 rounded-full px-2 py-0.5 text-xs font-medium ${
                  company.type === 'ORDERING' ? 'bg-blue-100 text-blue-700' :
                  company.type === 'BOTH' ? 'bg-purple-100 text-purple-700' :
                  'bg-green-100 text-green-700'
                }`}>
                  {COMPANY_TYPE_LABELS[company.type]}
                </span>
              </div>
              <div className="mt-1.5 text-xs text-gray-500">
                {company.businessNo && <div>{company.businessNo}</div>}
                <div>{STATUS_LABELS[company.status] ?? company.status}</div>
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
            onPageChange={(p) => setPage(p)}
          />
        )}
      </div>

      {/* 업체유형 수정 모달 */}
      <Dialog open={typeModalOpen} onOpenChange={setTypeModalOpen}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>{typeTarget?.companyName} — 업체유형 변경</DialogTitle>
          </DialogHeader>
          <div className="space-y-3">
            <div className="flex gap-2">
              {COMPANY_TYPE_OPTIONS.map((opt) => (
                <label key={opt.value} className={`flex flex-1 cursor-pointer items-center justify-center gap-1.5 rounded-md border-2 px-3 py-2 text-sm font-medium transition-colors ${
                  selectedType === opt.value
                    ? 'border-green-600 bg-green-50 text-green-800'
                    : 'border-gray-200 text-gray-600 hover:border-green-300'
                }`}>
                  <input
                    type="radio"
                    name="companyType"
                    value={opt.value}
                    checked={selectedType === opt.value}
                    onChange={() => setSelectedType(opt.value)}
                    className="sr-only"
                  />
                  {opt.label}
                </label>
              ))}
            </div>
            {typeError && (
              <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-600">{typeError}</p>
            )}
          </div>
          <DialogFooter className="gap-2">
            <Button variant="outline" onClick={() => setTypeModalOpen(false)}>취소</Button>
            <Button className="bg-green-700 hover:bg-green-800" disabled={typeSaving} onClick={handleTypeSave}>
              {typeSaving ? '저장 중...' : '저장'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* 업체 등록 모달 */}
      <Dialog open={registerOpen} onOpenChange={setRegisterOpen}>
        <DialogContent className="max-h-[90vh] max-w-md overflow-y-auto">
          <DialogHeader>
            <DialogTitle>업체 등록</DialogTitle>
          </DialogHeader>
          <div className="space-y-3">
            <div className="space-y-1.5">
              <Label>업체유형 *</Label>
              <div className="flex gap-2">
                {COMPANY_TYPE_OPTIONS.map((opt) => (
                  <label key={opt.value} className={`flex flex-1 cursor-pointer items-center justify-center gap-1.5 rounded-md border-2 px-3 py-2 text-sm font-medium transition-colors ${
                    registerForm.companyType === opt.value
                      ? 'border-green-600 bg-green-50 text-green-800'
                      : 'border-gray-200 text-gray-600 hover:border-green-300'
                  }`}>
                    <input
                      type="radio"
                      name="registerCompanyType"
                      value={opt.value}
                      checked={registerForm.companyType === opt.value}
                      onChange={() => setRegisterForm((f) => ({ ...f, companyType: opt.value }))}
                      className="sr-only"
                    />
                    {opt.label}
                  </label>
                ))}
              </div>
            </div>
            <div className="space-y-1.5">
              <Label>업체명 *</Label>
              <Input placeholder="업체명" value={registerForm.name} onChange={(e) => setRegisterForm((f) => ({ ...f, name: e.target.value }))} />
            </div>
            <div className="space-y-1.5">
              <Label>사업자번호</Label>
              <Input placeholder="000-00-00000" value={registerForm.businessNumber} onChange={(e) => setRegisterForm((f) => ({ ...f, businessNumber: e.target.value }))} />
            </div>
            <div className="space-y-1.5">
              <Label>담당자명 *</Label>
              <Input placeholder="담당자 성함" value={registerForm.managerName} onChange={(e) => setRegisterForm((f) => ({ ...f, managerName: e.target.value }))} />
            </div>
            <div className="space-y-1.5">
              <Label>연락처 *</Label>
              <Input placeholder="010-0000-0000" inputMode="numeric" value={registerForm.phone} onChange={(e) => setRegisterForm((f) => ({ ...f, phone: formatPhone(e.target.value) }))} />
            </div>
            <div className="space-y-1.5">
              <Label>주소</Label>
              <Input placeholder="업체 주소" value={registerForm.address} onChange={(e) => setRegisterForm((f) => ({ ...f, address: e.target.value }))} />
            </div>
            <div className="space-y-1.5">
              <Label>아이디 *</Label>
              <Input placeholder="영문·숫자 조합 6자 이상" value={registerForm.loginId} onChange={(e) => setRegisterForm((f) => ({ ...f, loginId: e.target.value }))} />
            </div>
            <div className="space-y-1.5">
              <Label>초기 비밀번호 *</Label>
              <Input type="password" placeholder="8자 이상" value={registerForm.password} onChange={(e) => setRegisterForm((f) => ({ ...f, password: e.target.value }))} />
            </div>
            {registerError && (
              <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-600">{registerError}</p>
            )}
          </div>
          <DialogFooter className="gap-2">
            <Button variant="outline" onClick={() => setRegisterOpen(false)}>취소</Button>
            <Button
              className="bg-green-700 hover:bg-green-800"
              disabled={registerSaving || !registerForm.name || !registerForm.managerName || !registerForm.phone || !registerForm.loginId || !registerForm.password}
              onClick={handleRegister}
            >
              {registerSaving ? '저장 중...' : '등록'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}

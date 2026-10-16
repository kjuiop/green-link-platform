'use client'

import { useState } from 'react'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { mockVendors } from '@/lib/mock/vendors'
import { Search } from 'lucide-react'

const STATUS_FILTERS = [
  { label: '전체', value: 'ALL' },
  { label: '활성', value: 'active' },
  { label: '정지', value: 'inactive' },
] as const

export default function VendorsPage() {
  const [query, setQuery] = useState('')
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'active' | 'inactive'>('ALL')
  const [appliedQuery, setAppliedQuery] = useState('')
  const [appliedStatus, setAppliedStatus] = useState<'ALL' | 'active' | 'inactive'>('ALL')

  const handleSearch = () => {
    setAppliedQuery(query)
    setAppliedStatus(statusFilter)
  }

  const handleReset = () => {
    setQuery(''); setStatusFilter('ALL')
    setAppliedQuery(''); setAppliedStatus('ALL')
  }

  const filtered = mockVendors.filter((v) => {
    const matchStatus = appliedStatus === 'ALL' || v.status === appliedStatus
    const matchQuery =
      !appliedQuery ||
      v.name.includes(appliedQuery) ||
      v.managerName.includes(appliedQuery) ||
      v.phone.includes(appliedQuery)
    return matchStatus && matchQuery
  })

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold">업체 계정관리</h1>
        <Button className="bg-green-700 hover:bg-green-800">업체 등록</Button>
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
              <TableHead>상태</TableHead>
              <TableHead>관리</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filtered.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="py-10 text-center text-sm text-gray-400">
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
                      <button className="text-xs text-blue-600 hover:underline">수정</button>
                      <span className="text-gray-300">|</span>
                      <button
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
    </div>
  )
}

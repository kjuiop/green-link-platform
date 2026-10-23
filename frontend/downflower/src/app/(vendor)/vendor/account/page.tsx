'use client'

import { useState, useEffect } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import { fetchVendor } from '@/lib/api/vendorApi'
import { Vendor } from '@/types/vendor'

export default function VendorAccountPage() {
  const [vendor, setVendor] = useState<Vendor | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const vendorId = typeof window !== 'undefined' ? localStorage.getItem('vendorId') : null
    if (!vendorId) { setLoading(false); return }
    fetchVendor(vendorId).then(setVendor).catch(() => {}).finally(() => setLoading(false))
  }, [])

  if (loading) {
    return (
      <div className="space-y-4">
        <h1 className="text-xl font-bold">내 계정정보</h1>
        <p className="py-10 text-center text-sm text-gray-400">불러오는 중...</p>
      </div>
    )
  }

  if (!vendor) {
    return (
      <div className="space-y-4">
        <h1 className="text-xl font-bold">내 계정정보</h1>
        <p className="py-10 text-center text-sm text-gray-400">계정 정보를 불러오지 못했습니다.</p>
      </div>
    )
  }

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">내 계정정보</h1>

      <div className="grid gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-sm">업체정보</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="space-y-1.5">
              <Label>아이디</Label>
              <Input defaultValue={vendor.loginId} readOnly className="bg-gray-50 text-gray-500" />
            </div>
            <div className="space-y-1.5">
              <Label>업체명</Label>
              <Input defaultValue={vendor.name} readOnly />
            </div>
            <div className="space-y-1.5">
              <Label>담당자명</Label>
              <Input defaultValue={vendor.managerName} readOnly />
            </div>
            <div className="space-y-1.5">
              <Label>연락처</Label>
              <Input defaultValue={vendor.phone} readOnly />
            </div>
            <div className="space-y-1.5">
              <Label>주소</Label>
              <Input defaultValue={vendor.address} readOnly />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-sm">비밀번호 변경</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="space-y-1.5">
              <Label>현재 비밀번호</Label>
              <Input type="password" placeholder="현재 비밀번호" />
            </div>
            <div className="space-y-1.5">
              <Label>새 비밀번호</Label>
              <Input type="password" placeholder="새 비밀번호" />
            </div>
            <div className="space-y-1.5">
              <Label>새 비밀번호 확인</Label>
              <Input type="password" placeholder="새 비밀번호 확인" />
            </div>
            <Button className="w-full bg-green-700 hover:bg-green-800">비밀번호 변경</Button>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

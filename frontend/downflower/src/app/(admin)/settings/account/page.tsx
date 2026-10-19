'use client'

import { useState, useEffect } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import { fetchAdminProfile, updateAdminProfile, type AdminProfile } from '@/lib/api/adminApi'

export default function SettingsAccountPage() {
  const [profile, setProfile] = useState<AdminProfile | null>(null)
  const [name, setName] = useState('')
  const [phone, setPhone] = useState('')
  const [saving, setSaving] = useState(false)
  const [saveMsg, setSaveMsg] = useState('')

  useEffect(() => {
    fetchAdminProfile()
      .then((data) => {
        setProfile(data)
        setName(data.name ?? '')
        setPhone(data.phone ?? '')
      })
      .catch(() => {})
  }, [])

  async function handleSave() {
    setSaving(true)
    setSaveMsg('')
    try {
      await updateAdminProfile(name, phone)
      setSaveMsg('저장되었습니다.')
    } catch (e) {
      setSaveMsg(e instanceof Error ? e.message : '저장에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">내 계정</h1>

      <Card className="max-w-lg">
        <CardHeader>
          <CardTitle className="text-sm">기본 정보</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="space-y-1.5">
            <Label>아이디</Label>
            <Input value={profile?.loginId ?? ''} disabled className="bg-gray-50" />
          </div>
          <div className="space-y-1.5">
            <Label>이름</Label>
            <Input value={name} onChange={(e) => setName(e.target.value)} />
          </div>
          <div className="space-y-1.5">
            <Label>연락처</Label>
            <Input value={phone} onChange={(e) => setPhone(e.target.value)} />
          </div>
          {saveMsg && (
            <p className={`text-xs ${saveMsg.includes('실패') ? 'text-red-500' : 'text-green-600'}`}>
              {saveMsg}
            </p>
          )}
          <Button className="bg-green-700 hover:bg-green-800" onClick={handleSave} disabled={saving}>
            {saving ? '저장 중...' : '저장'}
          </Button>
        </CardContent>
      </Card>

      <Card className="max-w-lg">
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
            <Input type="password" placeholder="8자 이상" />
          </div>
          <div className="space-y-1.5">
            <Label>새 비밀번호 확인</Label>
            <Input type="password" placeholder="새 비밀번호 재입력" />
          </div>
          <Button className="bg-green-700 hover:bg-green-800">변경</Button>
        </CardContent>
      </Card>
    </div>
  )
}

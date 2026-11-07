'use client'

import { useState, useEffect } from 'react'
import { Card, CardContent } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import {
  CompanySmsIntegration,
  SmsProvider,
  fetchCompanies,
  fetchCompanySmsIntegration,
  saveCompanySmsIntegration,
  CompanyInfo,
} from '@/lib/api/companyApi'

const PROVIDERS: { value: SmsProvider; label: string }[] = [
  { value: 'MESSAGEME', label: '메세지미 (MessageMe)' },
  { value: 'ALIGO', label: '알리고 (Aligo)' },
]

type FormProps = {
  companyId: number
  initialIntegration: CompanySmsIntegration | null
}

function SmsIntegrationForm({ companyId, initialIntegration }: FormProps) {
  const [provider, setProvider] = useState<SmsProvider>(initialIntegration?.provider ?? 'MESSAGEME')
  const [apiKey, setApiKey] = useState(initialIntegration?.apiKey ?? '')
  const [senderNumber, setSenderNumber] = useState(initialIntegration?.senderNumber ?? '')
  const [saving, setSaving] = useState(false)
  const [saveResult, setSaveResult] = useState<'idle' | 'success' | 'error'>('idle')

  async function handleSave() {
    setSaving(true)
    setSaveResult('idle')
    try {
      await saveCompanySmsIntegration(companyId, { provider, apiKey, senderNumber })
      setSaveResult('success')
      setTimeout(() => setSaveResult('idle'), 2000)
    } catch {
      setSaveResult('error')
    } finally {
      setSaving(false)
    }
  }

  return (
    <Card>
      <CardContent className="space-y-4 pt-6">
        <div className="space-y-1.5">
          <Label>SMS 제공사</Label>
          <div className="flex gap-3">
            {PROVIDERS.map(({ value, label }) => (
              <label key={value} className="flex cursor-pointer items-center gap-2">
                <input
                  type="radio"
                  name={`provider-${companyId}`}
                  value={value}
                  checked={provider === value}
                  onChange={() => setProvider(value)}
                  className="accent-green-700"
                />
                <span className="text-sm">{label}</span>
              </label>
            ))}
          </div>
        </div>

        <div className="grid gap-3 sm:grid-cols-2">
          <div className="space-y-1.5">
            <Label>API Key</Label>
            <Input
              placeholder="API Key 입력"
              value={apiKey}
              onChange={(e) => setApiKey(e.target.value)}
            />
          </div>
          <div className="space-y-1.5">
            <Label>발신번호</Label>
            <Input
              placeholder="010-0000-0000"
              value={senderNumber}
              onChange={(e) => setSenderNumber(e.target.value)}
            />
          </div>
        </div>

        {saveResult === 'error' && (
          <p className="text-sm text-red-500">저장에 실패했습니다.</p>
        )}

        <Button
          onClick={handleSave}
          disabled={saving}
          className={
            saveResult === 'success'
              ? 'bg-green-600 hover:bg-green-700'
              : 'bg-green-700 hover:bg-green-800'
          }
        >
          {saving ? '저장 중…' : saveResult === 'success' ? '저장됨 ✓' : '저장'}
        </Button>
      </CardContent>
    </Card>
  )
}

export default function SmsSettingsPage() {
  const [role, setRole] = useState<string>('')
  const [myCompanyId, setMyCompanyId] = useState<number | null>(null)

  // ORDERING state
  const [myIntegration, setMyIntegration] = useState<CompanySmsIntegration | null>(null)
  const [orderingLoading, setOrderingLoading] = useState(false)

  // SUPER_ADMIN state
  const [companies, setCompanies] = useState<CompanyInfo[]>([])
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [selectedIntegration, setSelectedIntegration] = useState<CompanySmsIntegration | null>(null)
  const [integrationLoading, setIntegrationLoading] = useState(false)

  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)

  useEffect(() => {
    const r = localStorage.getItem('role') ?? ''
    const cid = localStorage.getItem('vendorId')
    setRole(r)
    setMyCompanyId(cid ? Number(cid) : null)
  }, [])

  useEffect(() => {
    if (!role) return

    if (role === 'ORDERING') {
      if (!myCompanyId) {
        setLoading(false)
        return
      }
      setOrderingLoading(true)
      fetchCompanySmsIntegration(myCompanyId)
        .then(setMyIntegration)
        .catch(() => setError(true))
        .finally(() => {
          setOrderingLoading(false)
          setLoading(false)
        })
      return
    }

    if (role === 'SUPER_ADMIN') {
      fetchCompanies()
        .then((list) => {
          const ordering = list.filter((c) => c.type === 'ORDERING' || c.type === 'BOTH')
          setCompanies(ordering)
          if (ordering.length > 0) setSelectedId(ordering[0].id)
        })
        .catch(() => setError(true))
        .finally(() => setLoading(false))
    }
  }, [role, myCompanyId])

  useEffect(() => {
    if (!selectedId) return
    setIntegrationLoading(true)
    fetchCompanySmsIntegration(selectedId)
      .then(setSelectedIntegration)
      .catch(() => setSelectedIntegration(null))
      .finally(() => setIntegrationLoading(false))
  }, [selectedId])

  if (!role || loading) {
    return <div className="py-10 text-center text-sm text-gray-400">불러오는 중...</div>
  }

  if (error) {
    return <div className="py-10 text-center text-sm text-red-500">데이터를 불러오지 못했습니다.</div>
  }

  if (role === 'ORDERING') {
    return (
      <div className="space-y-4">
        <h1 className="text-xl font-bold">문자 연동 설정</h1>
        {orderingLoading ? (
          <div className="py-10 text-center text-sm text-gray-400">불러오는 중...</div>
        ) : myCompanyId ? (
          <SmsIntegrationForm companyId={myCompanyId} initialIntegration={myIntegration} />
        ) : (
          <div className="py-10 text-center text-sm text-red-500">회사 정보를 불러오지 못했습니다.</div>
        )}
      </div>
    )
  }

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">문자 연동 설정</h1>

      <div className="space-y-1.5">
        <Label>발주사 선택</Label>
        <select
          value={selectedId ?? ''}
          onChange={(e) => setSelectedId(Number(e.target.value))}
          className="w-full rounded-md border px-3 py-2 text-sm"
        >
          {companies.map((c) => (
            <option key={c.id} value={c.id}>
              {c.companyName}
            </option>
          ))}
        </select>
      </div>

      {integrationLoading ? (
        <div className="py-10 text-center text-sm text-gray-400">불러오는 중...</div>
      ) : selectedId ? (
        <SmsIntegrationForm
          key={selectedId}
          companyId={selectedId}
          initialIntegration={selectedIntegration}
        />
      ) : (
        <Card>
          <CardContent className="py-10 text-center text-sm text-gray-400">
            등록된 발주사가 없습니다.
          </CardContent>
        </Card>
      )}
    </div>
  )
}

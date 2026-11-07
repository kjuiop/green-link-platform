'use client'

import { useState, useEffect } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import {
  CompanyInfo,
  CompanySmsIntegration,
  SmsProvider,
  fetchCompanies,
  fetchCompanySmsIntegration,
  saveCompanySmsIntegration,
} from '@/lib/api/companyApi'
import { ChevronDown, ChevronUp } from 'lucide-react'

const PROVIDERS: { value: SmsProvider; label: string }[] = [
  { value: 'MESSAGEME', label: '메세지미 (MessageMe)' },
  { value: 'ALIGO', label: '알리고 (Aligo)' },
]

type IntegrationState = {
  provider: SmsProvider
  apiKey: string
  senderNumber: string
  saving: boolean
  saveResult: 'idle' | 'success' | 'error'
}

type RowState = {
  integration: CompanySmsIntegration | null
  expanded: boolean
  form: IntegrationState
}

function defaultForm(integration: CompanySmsIntegration | null): IntegrationState {
  return {
    provider: integration?.provider ?? 'MESSAGEME',
    apiKey: integration?.apiKey ?? '',
    senderNumber: integration?.senderNumber ?? '',
    saving: false,
    saveResult: 'idle',
  }
}

export default function SmsSettingsPage() {
  const [companies, setCompanies] = useState<CompanyInfo[]>([])
  const [rows, setRows] = useState<Record<number, RowState>>({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)

  useEffect(() => {
    fetchCompanies()
      .then(async (list) => {
        const ordering = list.filter((c) => c.type === 'ORDERING' || c.type === 'BOTH')
        setCompanies(ordering)
        const entries = await Promise.all(
          ordering.map(async (c) => {
            const integration = await fetchCompanySmsIntegration(c.id).catch(() => null)
            return [c.id, { integration, expanded: false, form: defaultForm(integration) }] as const
          }),
        )
        setRows(Object.fromEntries(entries))
      })
      .catch(() => setError(true))
      .finally(() => setLoading(false))
  }, [])

  function toggleExpand(id: number) {
    setRows((prev) => ({
      ...prev,
      [id]: { ...prev[id], expanded: !prev[id].expanded },
    }))
  }

  function updateForm(id: number, patch: Partial<IntegrationState>) {
    setRows((prev) => ({
      ...prev,
      [id]: { ...prev[id], form: { ...prev[id].form, ...patch } },
    }))
  }

  async function handleSave(company: CompanyInfo) {
    const row = rows[company.id]
    if (!row) return
    updateForm(company.id, { saving: true, saveResult: 'idle' })
    try {
      const saved = await saveCompanySmsIntegration(company.id, {
        provider: row.form.provider,
        apiKey: row.form.apiKey,
        senderNumber: row.form.senderNumber,
      })
      setRows((prev) => ({
        ...prev,
        [company.id]: {
          ...prev[company.id],
          integration: saved,
          form: { ...prev[company.id].form, saving: false, saveResult: 'success' },
        },
      }))
      setTimeout(() => updateForm(company.id, { saveResult: 'idle' }), 2000)
    } catch {
      updateForm(company.id, { saving: false, saveResult: 'error' })
    }
  }

  if (loading) {
    return <div className="py-10 text-center text-sm text-gray-400">불러오는 중...</div>
  }

  if (error) {
    return <div className="py-10 text-center text-sm text-red-500">데이터를 불러오지 못했습니다.</div>
  }

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">문자 연동 설정</h1>

      {companies.length === 0 ? (
        <Card>
          <CardContent className="py-10 text-center text-sm text-gray-400">
            등록된 발주사가 없습니다.
          </CardContent>
        </Card>
      ) : (
        companies.map((company) => {
          const row = rows[company.id]
          if (!row) return null
          const { expanded, form, integration } = row

          return (
            <Card key={company.id}>
              <CardHeader className="cursor-pointer select-none" onClick={() => toggleExpand(company.id)}>
                <div className="flex items-center justify-between">
                  <div>
                    <CardTitle className="text-sm font-semibold">{company.companyName}</CardTitle>
                    <p className="mt-0.5 text-xs text-gray-500">
                      {integration
                        ? `${PROVIDERS.find((p) => p.value === integration.provider)?.label} 연동 중`
                        : '연동 미설정'}
                    </p>
                  </div>
                  {expanded ? (
                    <ChevronUp className="h-4 w-4 text-gray-400" />
                  ) : (
                    <ChevronDown className="h-4 w-4 text-gray-400" />
                  )}
                </div>
              </CardHeader>

              {expanded && (
                <CardContent className="space-y-4 pt-0">
                  <div className="space-y-1.5">
                    <Label>SMS 제공사</Label>
                    <div className="flex gap-3">
                      {PROVIDERS.map(({ value, label }) => (
                        <label key={value} className="flex cursor-pointer items-center gap-2">
                          <input
                            type="radio"
                            name={`provider-${company.id}`}
                            value={value}
                            checked={form.provider === value}
                            onChange={() => updateForm(company.id, { provider: value })}
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
                        value={form.apiKey}
                        onChange={(e) => updateForm(company.id, { apiKey: e.target.value })}
                      />
                    </div>
                    <div className="space-y-1.5">
                      <Label>발신번호</Label>
                      <Input
                        placeholder="010-0000-0000"
                        value={form.senderNumber}
                        onChange={(e) => updateForm(company.id, { senderNumber: e.target.value })}
                      />
                    </div>
                  </div>

                  {form.saveResult === 'error' && (
                    <p className="text-sm text-red-500">저장에 실패했습니다.</p>
                  )}

                  <Button
                    onClick={() => handleSave(company)}
                    disabled={form.saving}
                    className={
                      form.saveResult === 'success'
                        ? 'bg-green-600 hover:bg-green-700'
                        : 'bg-green-700 hover:bg-green-800'
                    }
                  >
                    {form.saving ? '저장 중…' : form.saveResult === 'success' ? '저장됨 ✓' : '저장'}
                  </Button>
                </CardContent>
              )}
            </Card>
          )
        })
      )}
    </div>
  )
}

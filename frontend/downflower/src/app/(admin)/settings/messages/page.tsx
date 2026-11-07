'use client'

import { useState, useEffect } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { cn } from '@/lib/utils'
import { fetchSmsSettings, saveSmsSettings } from '@/lib/api/settingsApi'
import type { SmsProvider } from '@/lib/api/companyApi'

type EventType = '장례식' | '결혼식' | '기타'

const EVENT_TABS: EventType[] = ['장례식', '결혼식', '기타']

const VARIABLES = [
  { key: '{주문자명}', desc: '주문자 이름' },
  { key: '{장소}', desc: '배송 장소명' },
  { key: '{주문번호}', desc: '주문번호' },
]

const PROVIDERS: { value: SmsProvider; label: string }[] = [
  { value: 'ALIGO', label: '알리고 (Aligo)' },
  { value: 'MESSAGEME', label: '메세지미 (MessageMe)' },
]

export default function SettingsMessagesPage() {
  const [activeTab, setActiveTab] = useState<EventType>('장례식')
  const [senderNumber, setSenderNumber] = useState('')
  const [senderName, setSenderName] = useState('')
  const [provider, setProvider] = useState<SmsProvider>('ALIGO')
  const [apiKey, setApiKey] = useState('')
  const [userId, setUserId] = useState('')
  const [templates, setTemplates] = useState<Record<EventType, string>>({
    장례식: '',
    결혼식: '',
    기타: '',
  })
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(false)
  const [saving, setSaving] = useState(false)
  const [saveResult, setSaveResult] = useState<'idle' | 'success' | 'error'>('idle')

  useEffect(() => {
    fetchSmsSettings()
      .then((data) => {
        setSenderNumber(data.senderNumber ?? '')
        setSenderName(data.senderName ?? '')
        setProvider(data.provider ?? 'ALIGO')
        setApiKey(data.apiKey ?? '')
        setUserId(data.userId ?? '')
        setTemplates({
          장례식: data.templateFuneral ?? '',
          결혼식: data.templateWedding ?? '',
          기타: data.templateOthers ?? '',
        })
      })
      .catch(() => setLoadError(true))
      .finally(() => setLoading(false))
  }, [])

  async function handleSave() {
    setSaving(true)
    setSaveResult('idle')
    try {
      await saveSmsSettings({
        senderNumber,
        senderName,
        templateFuneral: templates['장례식'],
        templateWedding: templates['결혼식'],
        templateOthers: templates['기타'],
        provider,
        apiKey,
        userId,
      })
      setSaveResult('success')
      setTimeout(() => setSaveResult('idle'), 2000)
    } catch {
      setSaveResult('error')
    } finally {
      setSaving(false)
    }
  }

  function insertVariable(key: string) {
    setTemplates((prev) => ({
      ...prev,
      [activeTab]: prev[activeTab] + key,
    }))
  }

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">문자 설정</h1>

      {/* 인증문자 발송 제공사 */}
      <Card>
        <CardHeader>
          <CardTitle className="text-sm">인증문자 발송 설정</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-1.5">
            <Label>SMS 제공사</Label>
            <div className="flex gap-3">
              {PROVIDERS.map(({ value, label }) => (
                <label key={value} className="flex cursor-pointer items-center gap-2">
                  <input
                    type="radio"
                    name="provider"
                    value={value}
                    checked={provider === value}
                    onChange={() => setProvider(value)}
                    className="accent-green-700"
                    disabled={loading}
                  />
                  <span className="text-sm">{label}</span>
                </label>
              ))}
            </div>
          </div>

          <div className="grid gap-3 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label>발신번호</Label>
              <Input
                placeholder="010-0000-0000"
                value={senderNumber}
                onChange={(e) => setSenderNumber(e.target.value)}
                disabled={loading}
              />
            </div>
            <div className="space-y-1.5">
              <Label>발신자명</Label>
              <Input
                placeholder="온우림"
                value={senderName}
                onChange={(e) => setSenderName(e.target.value)}
                disabled={loading}
              />
            </div>
            <div className="space-y-1.5">
              <Label>API Key</Label>
              <Input
                placeholder={provider === 'ALIGO' ? '알리고 API Key' : '메세지미 API Key'}
                value={apiKey}
                onChange={(e) => setApiKey(e.target.value)}
                disabled={loading}
              />
            </div>
            {provider === 'ALIGO' && (
              <div className="space-y-1.5">
                <Label>User ID</Label>
                <Input
                  placeholder="알리고 User ID"
                  value={userId}
                  onChange={(e) => setUserId(e.target.value)}
                  disabled={loading}
                />
              </div>
            )}
          </div>
        </CardContent>
      </Card>

      {/* 이벤트별 문자 템플릿 */}
      <Card>
        <CardHeader>
          <CardTitle className="text-sm">배송완료 문자 템플릿</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex gap-1 rounded-lg border bg-gray-50 p-1">
            {EVENT_TABS.map((tab) => (
              <button
                key={tab}
                type="button"
                onClick={() => setActiveTab(tab)}
                className={cn(
                  'flex-1 rounded-md py-1.5 text-sm font-medium transition-colors',
                  activeTab === tab
                    ? 'bg-white text-green-700 shadow-sm'
                    : 'text-gray-500 hover:text-gray-700',
                )}
              >
                {tab}
              </button>
            ))}
          </div>

          <div className="flex flex-wrap gap-2">
            {VARIABLES.map(({ key, desc }) => (
              <button
                key={key}
                type="button"
                onClick={() => insertVariable(key)}
                title={desc}
                className="rounded border border-dashed border-gray-300 bg-gray-50 px-2 py-0.5 font-mono text-xs text-gray-600 hover:border-green-500 hover:text-green-700"
              >
                {key}
              </button>
            ))}
            <span className="self-center text-xs text-gray-400">클릭하면 커서 위치에 삽입</span>
          </div>

          <div className="space-y-1.5">
            <Label>{activeTab} 문자 내용</Label>
            <Textarea
              key={activeTab}
              rows={8}
              value={templates[activeTab]}
              onChange={(e) =>
                setTemplates((prev) => ({ ...prev, [activeTab]: e.target.value }))
              }
              className="font-mono text-sm"
              disabled={loading}
            />
          </div>

          {loadError && (
            <p className="text-sm text-red-500">설정을 불러오지 못했습니다. 새로고침 해주세요.</p>
          )}

          {saveResult === 'error' && (
            <p className="text-sm text-red-500">저장에 실패했습니다. 다시 시도해주세요.</p>
          )}

          <Button
            onClick={handleSave}
            disabled={saving || loading}
            className={cn(
              'min-w-20',
              saveResult === 'success' ? 'bg-green-600 hover:bg-green-700' : 'bg-green-700 hover:bg-green-800',
            )}
          >
            {saving ? '저장 중…' : saveResult === 'success' ? '저장됨 ✓' : '저장'}
          </Button>
        </CardContent>
      </Card>
    </div>
  )
}

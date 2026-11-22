'use client'

import { useState, useEffect } from 'react'
import { Card, CardContent } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import {
  CompanySmsIntegration,
  SmsProvider,
  SaveCompanySmsIntegrationData,
  fetchCompanies,
  fetchCompanySmsIntegration,
  saveCompanySmsIntegration,
  CompanyInfo,
} from '@/lib/api/companyApi'
import { BarobillSenderCheck } from '@/components/sms/BarobillSenderCheck'
import { BarobillKakaoTools } from '@/components/sms/BarobillKakaoTools'

const PROVIDERS: { value: SmsProvider; label: string }[] = [
  { value: 'MESSAGEME', label: '메세지미 (MessageMe)' },
  { value: 'ALIGO', label: '알리고 (Aligo)' },
  { value: 'BAROBILL', label: '바로빌 (Barobill)' },
]

type FormProps = {
  companyId: number
  initialIntegration: CompanySmsIntegration | null
}

function SmsIntegrationForm({ companyId, initialIntegration }: FormProps) {
  const [provider, setProvider] = useState<SmsProvider>(initialIntegration?.provider ?? 'MESSAGEME')
  const [apiKey, setApiKey] = useState(initialIntegration?.apiKey ?? '')
  const [senderNumber, setSenderNumber] = useState(initialIntegration?.senderNumber ?? '')
  const [userId, setUserId] = useState(initialIntegration?.userId ?? '')
  const [alimtalkEnabled, setAlimtalkEnabled] = useState(initialIntegration?.alimtalkEnabled ?? false)
  const [alimtalkTemplateFuneral, setAlimtalkTemplateFuneral] = useState(initialIntegration?.alimtalkTemplateFuneral ?? '')
  const [alimtalkTemplateWedding, setAlimtalkTemplateWedding] = useState(initialIntegration?.alimtalkTemplateWedding ?? '')
  const [alimtalkTemplateEtc, setAlimtalkTemplateEtc] = useState(initialIntegration?.alimtalkTemplateEtc ?? '')
  const [alimtalkSenderKey, setAlimtalkSenderKey] = useState(initialIntegration?.alimtalkSenderKey ?? '')
  const [saving, setSaving] = useState(false)
  const [saveResult, setSaveResult] = useState<'idle' | 'success' | 'error'>('idle')

  async function handleSave() {
    setSaving(true)
    setSaveResult('idle')
    try {
      const data: SaveCompanySmsIntegrationData = {
        provider, apiKey, senderNumber, userId,
        alimtalkEnabled,
        alimtalkTemplateFuneral,
        alimtalkTemplateWedding,
        alimtalkTemplateEtc,
        alimtalkSenderKey,
      }
      await saveCompanySmsIntegration(companyId, data)
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
      <CardContent className="space-y-6 pt-6">
        {/* SMS 설정 */}
        <div className="space-y-4">
          <p className="text-xs font-semibold uppercase tracking-wide text-gray-400">문자(SMS) 설정</p>
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
              <Label>{provider === 'BAROBILL' ? '연동인증키 (CERTKEY)' : 'API Key'}</Label>
              <Input
                placeholder={provider === 'BAROBILL' ? '바로빌 연동인증키 입력' : 'API Key 입력'}
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
            {provider === 'BAROBILL' && (
              <div className="space-y-1.5">
                <Label>바로빌 아이디</Label>
                <Input
                  placeholder="바로빌 회원 아이디"
                  value={userId}
                  onChange={(e) => setUserId(e.target.value)}
                />
              </div>
            )}
          </div>
          {provider === 'BAROBILL' && <BarobillSenderCheck companyId={companyId} />}
        </div>

        {/* 알림톡 설정 */}
        <div className="space-y-4 border-t pt-4">
          <p className="text-xs font-semibold uppercase tracking-wide text-gray-400">카카오 알림톡 설정</p>
          <label className="flex cursor-pointer items-center gap-2">
            <input
              type="checkbox"
              checked={alimtalkEnabled}
              onChange={(e) => setAlimtalkEnabled(e.target.checked)}
              className="accent-green-700"
            />
            <span className="text-sm">알림톡 발송 활성화</span>
          </label>
          {alimtalkEnabled && (
            provider === 'BAROBILL' ? (
              <BarobillKakaoTools
                companyId={companyId}
                senderKey={alimtalkSenderKey}
                onSenderKeyChange={setAlimtalkSenderKey}
                templateFuneral={alimtalkTemplateFuneral}
                templateWedding={alimtalkTemplateWedding}
                templateEtc={alimtalkTemplateEtc}
                onTemplateFuneralChange={setAlimtalkTemplateFuneral}
                onTemplateWeddingChange={setAlimtalkTemplateWedding}
                onTemplateEtcChange={setAlimtalkTemplateEtc}
              />
            ) : (
            <div className="space-y-3">
              <p className="text-xs text-gray-400">
                메시지미에 등록된 알림톡 전송키와 템플릿 코드를 입력하세요. 발송 실패 시 SMS로 자동 전환됩니다.
              </p>

              <div className="space-y-1.5">
                <Label>전송키 (Sender Key)</Label>
                <Input
                  placeholder="카카오채널 전송키 입력"
                  value={alimtalkSenderKey}
                  onChange={(e) => setAlimtalkSenderKey(e.target.value)}
                />
              </div>

              {/* 템플릿 문구 예시 */}
              <div className="rounded-md border border-blue-100 bg-blue-50 p-3 text-xs text-blue-800 space-y-3">
                <p className="font-semibold">📌 발송 문구 예시</p>
                <div className="space-y-1">
                  <p className="text-xs font-medium text-blue-600">장례식</p>
                  <div className="rounded bg-blue-100 p-2 leading-relaxed text-blue-700 whitespace-pre-line">{`안녕하세요, 온우림입니다.
홍길동님께 근조 화환 배송이 완료되었습니다.

■ 주문번호: 20241115-001
■ 받는 분: 홍길동
■ 장례식장: 서울추모공원

배송 사진을 아래 버튼에서 확인하실 수 있습니다.`}</div>
                </div>
                <div className="space-y-1">
                  <p className="text-xs font-medium text-blue-600">결혼식</p>
                  <div className="rounded bg-blue-100 p-2 leading-relaxed text-blue-700 whitespace-pre-line">{`안녕하세요, 온우림입니다.
홍길동님께 축하 화환 배송이 완료되었습니다.

■ 주문번호: 20241115-001
■ 받는 분: 홍길동
■ 예식장: 강남웨딩홀

배송 사진을 아래 버튼에서 확인하실 수 있습니다.`}</div>
                </div>
              </div>

              <div className="space-y-1.5">
                <Label>장례식 템플릿 코드</Label>
                <Input
                  placeholder="order_delivered_funeral"
                  value={alimtalkTemplateFuneral}
                  onChange={(e) => setAlimtalkTemplateFuneral(e.target.value)}
                />
              </div>
              <div className="space-y-1.5">
                <Label>결혼식 템플릿 코드</Label>
                <Input
                  placeholder="order_delivered_wedding"
                  value={alimtalkTemplateWedding}
                  onChange={(e) => setAlimtalkTemplateWedding(e.target.value)}
                />
              </div>
              <div className="space-y-1.5">
                <Label>기타 템플릿 코드</Label>
                <Input
                  placeholder="order_delivered_etc"
                  value={alimtalkTemplateEtc}
                  onChange={(e) => setAlimtalkTemplateEtc(e.target.value)}
                />
              </div>
            </div>
            )
          )}
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
      return
    }

    setLoading(false)
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

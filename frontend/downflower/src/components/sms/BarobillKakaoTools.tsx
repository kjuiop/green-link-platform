'use client'

import { useState } from 'react'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import {
  fetchBarobillKakaoChannels,
  fetchBarobillKakaoManagementUrl,
  fetchBarobillKakaoTemplates,
  type KakaoChannel,
  type KakaoTemplate,
  type KakaoUrlType,
} from '@/lib/api/settingsApi'

type Props = {
  companyId: number
  senderKey: string
  onSenderKeyChange: (v: string) => void
  templateFuneral: string
  templateWedding: string
  templateEtc: string
  onTemplateFuneralChange: (v: string) => void
  onTemplateWeddingChange: (v: string) => void
  onTemplateEtcChange: (v: string) => void
}

const MANAGEMENT_LINKS: { type: KakaoUrlType; label: string }[] = [
  { type: 'CHANNEL', label: '채널 관리' },
  { type: 'TEMPLATE', label: '템플릿 관리' },
  { type: 'HISTORY', label: '발송내역' },
]

export function BarobillKakaoTools(props: Props) {
  const { companyId, senderKey } = props
  const [channels, setChannels] = useState<KakaoChannel[] | null>(null)
  const [templates, setTemplates] = useState<KakaoTemplate[] | null>(null)
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState<string | null>(null)

  async function loadTemplates(channelId: string) {
    setTemplates(null)
    if (!channelId) return
    const r = await fetchBarobillKakaoTemplates(companyId, channelId)
    if (r.code !== 0) throw new Error(r.message ?? '템플릿 목록을 불러오지 못했습니다.')
    setTemplates(r.templates)
  }

  async function run(action: () => Promise<void>) {
    setLoading(true)
    setMessage(null)
    try {
      await action()
    } catch (e) {
      setMessage(e instanceof Error ? e.message : '요청에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  function handleLoadChannels() {
    return run(async () => {
      const r = await fetchBarobillKakaoChannels(companyId)
      if (r.code !== 0) throw new Error(r.message ?? '채널 목록을 불러오지 못했습니다.')
      setChannels(r.channels)
      if (r.channels.length === 0) {
        setMessage('등록된 카카오 채널이 없습니다. 채널 관리에서 먼저 등록하세요.')
      } else if (senderKey) {
        await loadTemplates(senderKey)
      }
    })
  }

  function handleSelectChannel(channelId: string) {
    props.onSenderKeyChange(channelId)
    return run(() => loadTemplates(channelId))
  }

  function handleOpen(type: KakaoUrlType) {
    const popup = window.open('', '_blank')
    if (popup) popup.opener = null
    return run(async () => {
      try {
        const r = await fetchBarobillKakaoManagementUrl(companyId, type)
        if (r.code !== 0 || !r.url) throw new Error(r.message ?? '관리 페이지 주소를 불러오지 못했습니다.')
        if (!r.url.startsWith('https://')) throw new Error('올바르지 않은 관리 페이지 주소입니다.')
        if (!popup) throw new Error('팝업이 차단되었습니다. 팝업 허용 후 다시 시도하세요.')
        popup.location.href = r.url
      } catch (e) {
        popup?.close()
        throw e
      }
    })
  }

  const templateFields: { label: string; value: string; onChange: (v: string) => void }[] = [
    { label: '장례식 템플릿', value: props.templateFuneral, onChange: props.onTemplateFuneralChange },
    { label: '결혼식 템플릿', value: props.templateWedding, onChange: props.onTemplateWeddingChange },
    { label: '기타 템플릿', value: props.templateEtc, onChange: props.onTemplateEtcChange },
  ]

  return (
    <div className="space-y-3">
      <p className="text-xs text-gray-400">
        바로빌에서 승인된 채널·템플릿을 조회해 선택하세요. 대체문자는 발송하지 않습니다.
        템플릿 등록·수정은 바로빌 관리 페이지에서 하며, 카카오 재심사 후 다시 조회하면 반영됩니다.
      </p>

      <div className="flex flex-wrap gap-2">
        {MANAGEMENT_LINKS.map(({ type, label }) => (
          <Button key={type} type="button" variant="outline" size="sm" disabled={loading} onClick={() => handleOpen(type)}>
            {label} ↗
          </Button>
        ))}
        <Button type="button" variant="outline" size="sm" disabled={loading} onClick={handleLoadChannels}>
          {loading ? '조회 중…' : '채널·템플릿 조회'}
        </Button>
      </div>
      {message && <p className="text-sm text-red-500">{message}</p>}
      <p className="text-xs text-gray-400">저장된 바로빌 설정 기준으로 조회합니다. 먼저 저장한 뒤 사용하세요.</p>

      <div className="space-y-1.5">
        <Label>카카오 채널 ID</Label>
        {channels ? (
          <select
            value={senderKey}
            onChange={(e) => handleSelectChannel(e.target.value)}
            className="w-full rounded-md border px-3 py-2 text-sm"
          >
            <option value="">채널 선택</option>
            {senderKey && !channels.some((c) => c.channelId === senderKey) && (
              <option value={senderKey}>{senderKey} (조회되지 않음)</option>
            )}
            {channels.map((c) => (
              <option key={c.channelId} value={c.channelId}>
                {c.channelName} ({c.channelId})
              </option>
            ))}
          </select>
        ) : (
          <p className="rounded-md border bg-gray-50 px-3 py-2 text-sm text-gray-600">
            {senderKey || '선택된 채널 없음'}
          </p>
        )}
      </div>

      {templateFields.map(({ label, value, onChange }) => (
        <div key={label} className="space-y-1.5">
          <Label>{label}</Label>
          {templates ? (
            <select
              value={value}
              onChange={(e) => onChange(e.target.value)}
              className="w-full rounded-md border px-3 py-2 text-sm"
            >
              <option value="">선택 안 함</option>
              {value && !templates.some((t) => t.templateName === value) && (
                <option value={value}>{value} (조회되지 않음)</option>
              )}
              {templates.map((t) => (
                <option key={t.templateName} value={t.templateName}>{t.templateName}</option>
              ))}
            </select>
          ) : (
            <p className="rounded-md border bg-gray-50 px-3 py-2 text-sm text-gray-600">{value || '선택된 템플릿 없음'}</p>
          )}
        </div>
      ))}
    </div>
  )
}

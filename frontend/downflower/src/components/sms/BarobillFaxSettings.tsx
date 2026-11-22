'use client'

import { useEffect, useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import { fetchFaxSettings, saveFaxSettings } from '@/lib/api/faxApi'

export function BarobillFaxSettings() {
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState('')
  const [saving, setSaving] = useState(false)
  const [message, setMessage] = useState<{ ok: boolean; text: string } | null>(null)

  const [faxSenderNumber, setFaxSenderNumber] = useState('')
  const [faxUrl, setFaxUrl] = useState('')
  const [ftpHost, setFtpHost] = useState('')
  const [ftpPort, setFtpPort] = useState('')
  const [ftpUsername, setFtpUsername] = useState('')
  const [ftpPassword, setFtpPassword] = useState('')
  const [ftpDirectory, setFtpDirectory] = useState('')

  useEffect(() => {
    fetchFaxSettings()
      .then((d) => {
        setFaxSenderNumber(d.faxSenderNumber ?? '')
        setFaxUrl(d.faxUrl ?? '')
        setFtpHost(d.ftpHost ?? '')
        setFtpPort(d.ftpPort != null ? String(d.ftpPort) : '')
        setFtpUsername(d.ftpUsername ?? '')
        setFtpPassword(d.ftpPassword ?? '')
        setFtpDirectory(d.ftpDirectory ?? '')
      })
      .catch((e) => setLoadError(e instanceof Error ? e.message : '팩스 설정을 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }, [])

  const portValid = ftpPort === '' || (Number.isInteger(Number(ftpPort)) && Number(ftpPort) > 0 && Number(ftpPort) <= 65535)

  async function handleSave() {
    if (!portValid) return
    setSaving(true)
    setMessage(null)
    try {
      const saved = await saveFaxSettings({
        faxSenderNumber,
        faxUrl,
        ftpHost,
        ftpPort: ftpPort === '' ? null : Number(ftpPort),
        ftpUsername,
        ftpPassword,
        ftpDirectory,
      })
      setFtpPassword(saved.ftpPassword ?? '')
      setMessage({ ok: true, text: '팩스 설정이 저장되었습니다.' })
    } catch (e) {
      setMessage({ ok: false, text: e instanceof Error ? e.message : '팩스 설정 저장에 실패했습니다.' })
    } finally {
      setSaving(false)
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-sm">팩스 발송 설정 (바로빌)</CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <p className="text-xs text-gray-500">
          수주 승인 시 수주사로 주문서를 팩스 발송할 때 사용하는 시스템 계정 설정입니다. 비용은 위 바로빌 계정에서 차감됩니다.
          팩스 파일은 바로빌 FTP로 업로드된 뒤 발송되므로 FTP 접속 정보가 필요합니다.
        </p>
        <div className="grid gap-3 sm:grid-cols-2">
          <div className="space-y-1.5">
            <Label>팩스 발신번호</Label>
            <Input
              placeholder="02-0000-0000"
              value={faxSenderNumber}
              onChange={(e) => setFaxSenderNumber(e.target.value)}
              disabled={loading}
            />
          </div>
          <div className="space-y-1.5">
            <Label>팩스 API URL (선택)</Label>
            <Input
              placeholder="비워두면 기본 주소 사용"
              value={faxUrl}
              onChange={(e) => setFaxUrl(e.target.value)}
              disabled={loading}
            />
          </div>
          <div className="space-y-1.5">
            <Label>FTP 호스트</Label>
            <Input value={ftpHost} onChange={(e) => setFtpHost(e.target.value)} disabled={loading} />
          </div>
          <div className="space-y-1.5">
            <Label>FTP 포트</Label>
            <Input
              inputMode="numeric"
              placeholder="21"
              value={ftpPort}
              onChange={(e) => setFtpPort(e.target.value.replace(/[^\d]/g, ''))}
              disabled={loading}
            />
            {!portValid && <p className="text-xs text-red-500">1~65535 사이로 입력해 주세요.</p>}
          </div>
          <div className="space-y-1.5">
            <Label>FTP 아이디</Label>
            <Input value={ftpUsername} onChange={(e) => setFtpUsername(e.target.value)} disabled={loading} />
          </div>
          <div className="space-y-1.5">
            <Label>FTP 비밀번호</Label>
            <Input
              type="password"
              autoComplete="new-password"
              value={ftpPassword}
              onChange={(e) => setFtpPassword(e.target.value)}
              disabled={loading}
            />
          </div>
          <div className="space-y-1.5 sm:col-span-2">
            <Label>FTP 업로드 디렉터리</Label>
            <Input placeholder="/" value={ftpDirectory} onChange={(e) => setFtpDirectory(e.target.value)} disabled={loading} />
          </div>
        </div>

        {loadError && <p className="text-sm text-red-500">{loadError}</p>}
        {message && <p className={message.ok ? 'text-sm text-green-700' : 'text-sm text-red-500'}>{message.text}</p>}

        <Button
          onClick={handleSave}
          disabled={saving || loading || !portValid}
          className="min-w-20 bg-green-700 hover:bg-green-800"
        >
          {saving ? '저장 중…' : '팩스 설정 저장'}
        </Button>
      </CardContent>
    </Card>
  )
}

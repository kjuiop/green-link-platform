'use client'

import { useCallback, useEffect, useState } from 'react'
import { Card, CardContent } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import { CompanySelect } from '@/components/coupang/CompanySelect'
import { useCoupangScope } from '@/components/coupang/useCoupangScope'
import {
  fetchCoupangCredential,
  fetchCoupangSyncLogs,
  saveCoupangCredential,
  testCoupangCredential,
  type CoupangCredential,
  type CoupangSyncLog,
} from '@/lib/api/coupangApi'

function formatDateTime(v: string | null) {
  return v ? v.replace('T', ' ').slice(0, 16) : '-'
}

interface CoupangSettingsPanelProps {
  scope: ReturnType<typeof useCoupangScope>
}

export function CoupangSettingsPanel({ scope }: CoupangSettingsPanelProps) {
  const companyId = scope.selectedId

  const [credential, setCredential] = useState<CoupangCredential | null>(null)
  const [logs, setLogs] = useState<CoupangSyncLog[]>([])
  const [loading, setLoading] = useState(true)
  const [vendorCode, setVendorCode] = useState('')
  const [accessKey, setAccessKey] = useState('')
  const [secretKey, setSecretKey] = useState('')
  const [keyIssuedAt, setKeyIssuedAt] = useState('')
  const [enabled, setEnabled] = useState(true)
  const [saving, setSaving] = useState(false)
  const [testing, setTesting] = useState(false)
  const [message, setMessage] = useState<{ kind: 'ok' | 'error'; text: string } | null>(null)

  const load = useCallback(async () => {
    if (scope.isSuperAdmin && !companyId) {
      setLoading(false)
      return
    }
    setLoading(true)
    try {
      const [cred, history] = await Promise.all([
        fetchCoupangCredential(companyId),
        fetchCoupangSyncLogs(companyId, 10).catch(() => []),
      ])
      setCredential(cred)
      setLogs(history)
      setVendorCode(cred?.vendorCode ?? '')
      setKeyIssuedAt(cred?.keyIssuedAt ?? '')
      setEnabled(cred?.enabled ?? true)
      setAccessKey('')
      setSecretKey('')
    } catch (e) {
      setMessage({ kind: 'error', text: e instanceof Error ? e.message : '불러오지 못했습니다.' })
    } finally {
      setLoading(false)
    }
  }, [scope.isSuperAdmin, companyId])

  useEffect(() => {
    if (!scope.ready) return
    load()
  }, [scope.ready, load])

  async function handleSave() {
    setSaving(true)
    setMessage(null)
    try {
      const saved = await saveCoupangCredential({
        companyId, vendorCode, accessKey, secretKey, keyIssuedAt, enabled,
      })
      setCredential(saved)
      setAccessKey('')
      setSecretKey('')
      setMessage({ kind: 'ok', text: '저장되었습니다.' })
    } catch (e) {
      setMessage({ kind: 'error', text: e instanceof Error ? e.message : '저장에 실패했습니다.' })
    } finally {
      setSaving(false)
    }
  }

  async function handleTest() {
    setTesting(true)
    setMessage(null)
    try {
      const result = await testCoupangCredential(companyId)
      setMessage({ kind: result.success ? 'ok' : 'error', text: result.message })
    } catch (e) {
      setMessage({ kind: 'error', text: e instanceof Error ? e.message : '연결 테스트에 실패했습니다.' })
    } finally {
      setTesting(false)
    }
  }

  const expiring = credential && !credential.expired && credential.daysUntilExpire !== null && credential.daysUntilExpire <= 14

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <CompanySelect scope={scope} />
      </div>

      {credential?.expired && (
        <div className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700">
          Open API 키가 만료되어 주문 수집이 중단되었습니다. 쿠팡 WING에서 키를 재발급한 뒤 아래에 다시 등록해 주세요.
        </div>
      )}
      {expiring && credential && (
        <div className="rounded-md border border-amber-200 bg-amber-50 p-3 text-sm text-amber-800">
          Open API 키가 {credential.daysUntilExpire}일 후({credential.keyExpiresAt}) 만료됩니다. 만료 전에 WING에서 재발급해 주세요.
        </div>
      )}
      {credential?.lastError && (
        <div className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700">
          최근 수집 오류 ({formatDateTime(credential.lastErrorAt)}): {credential.lastError}
        </div>
      )}

      <Card className="max-w-2xl">
        <CardContent className="space-y-4 pt-6">
          {loading ? (
            <p className="text-sm text-gray-400">불러오는 중…</p>
          ) : scope.isSuperAdmin && !companyId ? (
            <p className="text-sm text-gray-400">발주사를 선택해 주세요.</p>
          ) : (
            <>
              <div className="space-y-1.5">
                <Label>업체코드 (vendorId)</Label>
                <Input placeholder="A00000000" value={vendorCode} onChange={(e) => setVendorCode(e.target.value)} />
              </div>
              <div className="space-y-1.5">
                <Label>Access Key</Label>
                <Input
                  placeholder={credential ? `등록됨 (${credential.accessKeyMasked}) — 변경 시에만 입력` : ''}
                  value={accessKey}
                  onChange={(e) => setAccessKey(e.target.value)}
                  autoComplete="off"
                />
              </div>
              <div className="space-y-1.5">
                <Label>Secret Key</Label>
                <Input
                  type="password"
                  placeholder={credential?.secretKeyRegistered ? '등록됨 — 변경 시에만 입력' : ''}
                  value={secretKey}
                  onChange={(e) => setSecretKey(e.target.value)}
                  autoComplete="new-password"
                />
              </div>
              <div className="space-y-1.5">
                <Label>키 발급일</Label>
                <Input type="date" value={keyIssuedAt} onChange={(e) => setKeyIssuedAt(e.target.value)} />
                <p className="text-xs text-gray-400">
                  Open API 키는 발급일로부터 180일간 유효합니다.
                  {credential && ` 만료일: ${credential.keyExpiresAt}`}
                </p>
              </div>
              <label className="flex cursor-pointer items-center gap-2 text-sm">
                <input
                  type="checkbox"
                  checked={enabled}
                  onChange={(e) => setEnabled(e.target.checked)}
                  className="accent-green-700"
                />
                주문 자동 수집 사용
              </label>

              {message && (
                <p className={message.kind === 'ok' ? 'text-sm text-green-700' : 'text-sm text-red-500'}>{message.text}</p>
              )}

              <div className="flex gap-2">
                <Button onClick={handleSave} disabled={saving} className="bg-green-700 hover:bg-green-800">
                  {saving ? '저장 중…' : '저장'}
                </Button>
                <Button variant="outline" onClick={handleTest} disabled={testing || !credential}>
                  {testing ? '확인 중…' : '연결 테스트'}
                </Button>
              </div>
              {credential && (
                <p className="text-xs text-gray-400">마지막 수집: {formatDateTime(credential.lastCollectedAt)}</p>
              )}
            </>
          )}
        </CardContent>
      </Card>

      {logs.length > 0 && (
        <Card>
          <CardContent className="pt-6">
            <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-gray-400">최근 수집 이력</p>
            <ul className="divide-y text-sm">
              {logs.map((l) => (
                <li key={l.id} className="flex flex-wrap items-center justify-between gap-2 py-2">
                  <span className="text-gray-500">
                    {formatDateTime(l.createdAt)} · {l.triggerType === 'MANUAL' ? '수동' : '자동'}
                  </span>
                  {l.success ? (
                    <span className="text-gray-700">조회 {l.fetchedCount}건 / 신규 {l.insertedCount}건</span>
                  ) : (
                    <span className="text-red-500">{l.errorMessage ?? '실패'}</span>
                  )}
                </li>
              ))}
            </ul>
          </CardContent>
        </Card>
      )}
    </div>
  )
}

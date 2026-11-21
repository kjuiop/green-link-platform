'use client'

import Link from 'next/link'
import { useCallback, useEffect, useState } from 'react'
import QRCode from 'qrcode'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Button } from '@/components/ui/button'
import { ShopCompanySelect } from '@/components/shop/ShopCompanySelect'
import { useShopScope } from '@/components/shop/useShopScope'
import {
  checkShopSlug,
  fetchShopSettings,
  saveShopSettings,
  setShopPublished,
  uploadShopImage,
  type SaveShopSettingsData,
  type ShopSettings,
} from '@/lib/api/shopApi'
import { shopOrigin, shopUrl } from '@/lib/shop/shopUrl'

const EMPTY: SaveShopSettingsData = {
  slug: '',
  displayName: '',
  logoUrl: '',
  phone: '',
  themeColor: '#15803d',
  bannerUrl: '',
  bannerTitle: '',
  bannerSubtitle: '',
  notice: '',
  deliveryGuide: '',
  businessInfo: '',
}

function toForm(s: ShopSettings): SaveShopSettingsData {
  return {
    slug: s.slug ?? '',
    displayName: s.displayName ?? '',
    logoUrl: s.logoUrl ?? '',
    phone: s.phone ?? '',
    themeColor: s.themeColor ?? '#15803d',
    bannerUrl: s.bannerUrl ?? '',
    bannerTitle: s.bannerTitle ?? '',
    bannerSubtitle: s.bannerSubtitle ?? '',
    notice: s.notice ?? '',
    deliveryGuide: s.deliveryGuide ?? '',
    businessInfo: s.businessInfo ?? '',
  }
}

interface ShopSettingsPanelProps {
  scope: ReturnType<typeof useShopScope>
}

export function ShopSettingsPanel({ scope }: ShopSettingsPanelProps) {
  const companyId = scope.selectedId

  const [settings, setSettings] = useState<ShopSettings | null>(null)
  const [form, setForm] = useState<SaveShopSettingsData>(EMPTY)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [uploading, setUploading] = useState<'logo' | 'banner' | null>(null)
  const [slugState, setSlugState] = useState<'idle' | 'ok' | 'taken'>('idle')
  const [qr, setQr] = useState('')
  const [copied, setCopied] = useState(false)
  const [message, setMessage] = useState<{ kind: 'ok' | 'error'; text: string } | null>(null)

  const load = useCallback(async () => {
    if (scope.isSuperAdmin && !companyId) {
      setLoading(false)
      return
    }
    setLoading(true)
    try {
      const s = await fetchShopSettings(companyId)
      setSettings(s)
      setForm(toForm(s))
      setSlugState('idle')
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

  const publicUrl = settings?.exists && settings.slug ? shopUrl(settings.slug, shopOrigin()) : ''

  useEffect(() => {
    if (!publicUrl) {
      setQr('')
      return
    }
    QRCode.toDataURL(publicUrl, { width: 240, margin: 1 }).then(setQr).catch(() => setQr(''))
  }, [publicUrl])

  function set<K extends keyof SaveShopSettingsData>(key: K, value: SaveShopSettingsData[K]) {
    setForm((prev) => ({ ...prev, [key]: value }))
  }

  async function handleSlugCheck() {
    setMessage(null)
    try {
      setSlugState((await checkShopSlug(companyId, form.slug)) ? 'ok' : 'taken')
    } catch (e) {
      setSlugState('idle')
      setMessage({ kind: 'error', text: e instanceof Error ? e.message : '확인에 실패했습니다.' })
    }
  }

  async function handleUpload(kind: 'logo' | 'banner', file: File | undefined) {
    if (!file) return
    setUploading(kind)
    setMessage(null)
    try {
      const url = await uploadShopImage(companyId, file)
      set(kind === 'logo' ? 'logoUrl' : 'bannerUrl', url)
    } catch (e) {
      setMessage({ kind: 'error', text: e instanceof Error ? e.message : '업로드에 실패했습니다.' })
    } finally {
      setUploading(null)
    }
  }

  async function handleSave() {
    setSaving(true)
    setMessage(null)
    try {
      const saved = await saveShopSettings(companyId, !!settings?.exists, form)
      setSettings(saved)
      setForm(toForm(saved))
      setMessage({ kind: 'ok', text: '쇼핑몰 설정이 저장되었습니다.' })
    } catch (e) {
      setMessage({ kind: 'error', text: e instanceof Error ? e.message : '저장에 실패했습니다.' })
    } finally {
      setSaving(false)
    }
  }

  async function handleTogglePublished() {
    if (!settings) return
    setMessage(null)
    try {
      const next = await setShopPublished(companyId, !settings.published)
      setSettings(next)
      setMessage({ kind: 'ok', text: next.published ? '쇼핑몰을 공개했습니다.' : '쇼핑몰을 비공개로 전환했습니다.' })
    } catch (e) {
      setMessage({ kind: 'error', text: e instanceof Error ? e.message : '공개 상태 변경에 실패했습니다.' })
    }
  }

  async function handleCopy() {
    try {
      await navigator.clipboard.writeText(publicUrl)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    } catch {
      setMessage({ kind: 'error', text: '복사에 실패했습니다. 주소를 직접 선택해 복사해 주세요.' })
    }
  }

  const exists = !!settings?.exists

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <ShopCompanySelect scope={scope} />
      </div>

      {message && (
        <p className={message.kind === 'ok' ? 'text-sm text-green-700' : 'text-sm text-red-500'}>{message.text}</p>
      )}

      {exists && (
        <Card>
          <CardHeader>
            <CardTitle className="text-sm">공개 주소</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="flex flex-wrap items-center gap-2">
              <Input readOnly value={publicUrl} className="flex-1 bg-gray-50" onFocus={(e) => e.currentTarget.select()} />
              <Button variant="outline" onClick={handleCopy}>{copied ? '복사됨' : '주소 복사'}</Button>
              <a href={publicUrl} target="_blank" rel="noreferrer">
                <Button variant="outline">열어보기</Button>
              </a>
            </div>
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div className="flex items-center gap-2 text-sm">
                <span className={settings?.published ? 'font-medium text-green-700' : 'font-medium text-gray-500'}>
                  {settings?.published ? '공개 중' : '비공개'}
                </span>
                <Button size="sm" variant="outline" onClick={handleTogglePublished}>
                  {settings?.published ? '비공개로 전환' : '공개하기'}
                </Button>
              </div>
              {qr && (
                <div className="flex flex-col items-center gap-1">
                  {/* eslint-disable-next-line @next/next/no-img-element */}
                  <img src={qr} alt="쇼핑몰 QR 코드" className="h-28 w-28" />
                  <a href={qr} download={`shop-${settings?.slug}.png`} className="text-xs text-green-700 underline">
                    QR 이미지 저장
                  </a>
                </div>
              )}
            </div>
            <p className="text-xs text-gray-400">
              공개하려면 노출 중인 상품이 1개 이상 있어야 합니다.{' '}
              <Link href="/customer-orders/listing?channel=SHOP&sub=products" className="underline">상품 관리</Link>
            </p>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader>
          <CardTitle className="text-sm">{exists ? '기본 정보' : '쇼핑몰 만들기'}</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-1.5">
            <Label>쇼핑몰 주소 *</Label>
            <div className="flex flex-wrap items-center gap-2">
              <span className="text-sm text-gray-500">/s/</span>
              <Input
                className="w-48"
                placeholder="my-flower"
                value={form.slug}
                maxLength={30}
                disabled={loading}
                onChange={(e) => { set('slug', e.target.value.toLowerCase()); setSlugState('idle') }}
              />
              <Button variant="outline" size="sm" onClick={handleSlugCheck} disabled={!form.slug || loading}>
                중복 확인
              </Button>
              {slugState === 'ok' && <span className="text-xs text-green-700">사용할 수 있습니다.</span>}
              {slugState === 'taken' && <span className="text-xs text-red-500">사용할 수 없는 주소입니다.</span>}
            </div>
            <p className="text-xs text-gray-400">
              영문 소문자·숫자·하이픈, 3~30자. 주소를 바꾸면 기존에 배포한 링크와 QR은 더 이상 열리지 않습니다.
            </p>
          </div>

          <div className="grid gap-3 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label>쇼핑몰 이름 *</Label>
              <Input value={form.displayName} maxLength={100} disabled={loading} onChange={(e) => set('displayName', e.target.value)} />
            </div>
            <div className="space-y-1.5">
              <Label>대표 전화번호</Label>
              <Input value={form.phone} maxLength={30} disabled={loading} onChange={(e) => set('phone', e.target.value)} />
            </div>
            <div className="space-y-1.5">
              <Label>대표 색상</Label>
              <div className="flex items-center gap-2">
                <input
                  type="color"
                  value={form.themeColor}
                  onChange={(e) => set('themeColor', e.target.value)}
                  className="h-9 w-12 cursor-pointer rounded border"
                />
                <span className="font-mono text-xs text-gray-500">{form.themeColor}</span>
              </div>
            </div>
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            {(['logo', 'banner'] as const).map((kind) => {
              const url = kind === 'logo' ? form.logoUrl : form.bannerUrl
              return (
                <div key={kind} className="space-y-1.5">
                  <Label>{kind === 'logo' ? '로고 이미지' : '배너 이미지'}</Label>
                  {url && (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img src={url} alt="" className="h-24 w-full rounded border bg-gray-50 object-contain" />
                  )}
                  <div className="flex items-center gap-2">
                    <input
                      type="file"
                      accept="image/jpeg,image/png,image/webp"
                      disabled={loading || uploading !== null}
                      onChange={(e) => { handleUpload(kind, e.target.files?.[0]); e.target.value = '' }}
                      className="text-xs"
                    />
                    {url && (
                      <button
                        type="button"
                        className="text-xs text-gray-500 underline"
                        onClick={() => set(kind === 'logo' ? 'logoUrl' : 'bannerUrl', '')}
                      >
                        삭제
                      </button>
                    )}
                  </div>
                  {uploading === kind && <p className="text-xs text-gray-400">업로드 중…</p>}
                </div>
              )
            })}
          </div>

          <div className="grid gap-3 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label>배너 제목</Label>
              <Input value={form.bannerTitle} maxLength={100} disabled={loading} onChange={(e) => set('bannerTitle', e.target.value)} />
            </div>
            <div className="space-y-1.5">
              <Label>배너 설명</Label>
              <Input value={form.bannerSubtitle} maxLength={200} disabled={loading} onChange={(e) => set('bannerSubtitle', e.target.value)} />
            </div>
          </div>

          <div className="space-y-1.5">
            <Label>공지사항</Label>
            <Textarea rows={3} value={form.notice} maxLength={2000} disabled={loading} onChange={(e) => set('notice', e.target.value)} />
          </div>
          <div className="space-y-1.5">
            <Label>배송 안내</Label>
            <Textarea rows={4} value={form.deliveryGuide} maxLength={4000} disabled={loading} onChange={(e) => set('deliveryGuide', e.target.value)} />
          </div>
          <div className="space-y-1.5">
            <Label>사업자 정보</Label>
            <Textarea rows={3} value={form.businessInfo} maxLength={2000} disabled={loading} onChange={(e) => set('businessInfo', e.target.value)} />
          </div>

          <Button
            onClick={handleSave}
            disabled={saving || loading || !form.slug || !form.displayName}
            className="min-w-24 bg-green-700 hover:bg-green-800"
          >
            {saving ? '저장 중…' : exists ? '저장' : '쇼핑몰 만들기'}
          </Button>
        </CardContent>
      </Card>
    </div>
  )
}

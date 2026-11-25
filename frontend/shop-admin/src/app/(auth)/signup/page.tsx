'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import { ApiError } from '@/lib/api/client'
import { businessNoExists, login, signup } from '@/lib/api/authApi'
import { getSession, setSession } from '@/lib/session'
import { markTokenFresh } from '@/lib/useTokenRefresh'
import type { Session } from '@/types/auth'

type Lookup = 'idle' | 'checking' | 'new' | 'existing'
type JoinRole = 'PARTNER_ADMIN' | 'BRAND_ADMIN'

const ROLE_OPTIONS: { value: JoinRole; label: string; desc: string }[] = [
  { value: 'PARTNER_ADMIN', label: '파트너 관리자', desc: '회사의 쇼핑몰 전체를 관리합니다' },
  { value: 'BRAND_ADMIN', label: '브랜드 관리자', desc: '배정받은 브랜드만 관리합니다' },
]

function homeOf(session: Session): string {
  return session.accessStatus === 'APPROVED' ? '/dashboard' : '/access'
}

function formatBusinessNo(raw: string): string {
  const d = raw.replace(/\D/g, '').slice(0, 10)
  if (d.length <= 3) return d
  if (d.length <= 5) return `${d.slice(0, 3)}-${d.slice(3)}`
  return `${d.slice(0, 3)}-${d.slice(3, 5)}-${d.slice(5)}`
}

function validateCommon(f: {
  loginId: string; password: string; passwordConfirm: string; name: string; phone: string; email: string
}): string {
  if (!/^[A-Za-z0-9_.-]{4,30}$/.test(f.loginId)) return '아이디는 영문·숫자·._- 4~30자로 입력하세요'
  if (f.password.length < 8 || f.password.length > 64 || !/[A-Za-z]/.test(f.password) || !/\d/.test(f.password)) {
    return '비밀번호는 영문과 숫자를 포함한 8~64자로 입력하세요'
  }
  if (f.password !== f.passwordConfirm) return '비밀번호가 일치하지 않습니다'
  if (!f.name.trim() || f.name.length > 50) return '이름을 입력하세요 (50자 이내)'
  if (!/^[0-9-]{9,20}$/.test(f.phone)) return '연락처를 숫자와 하이픈으로 입력하세요'
  if (f.email.length > 100) return '이메일은 100자 이내로 입력하세요'
  return ''
}

export default function SignupPage() {
  const router = useRouter()
  const [loginId, setLoginId] = useState('')
  const [password, setPassword] = useState('')
  const [passwordConfirm, setPasswordConfirm] = useState('')
  const [name, setName] = useState('')
  const [phone, setPhone] = useState('')
  const [email, setEmail] = useState('')
  const [businessNo, setBusinessNo] = useState('')
  const [lookup, setLookup] = useState<Lookup>('idle')
  const [companyName, setCompanyName] = useState('')
  const [ceoName, setCeoName] = useState('')
  const [requestedRole, setRequestedRole] = useState<JoinRole>('PARTNER_ADMIN')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    const session = getSession()
    if (session) router.replace(homeOf(session))
  }, [router])

  function clearError() { setError('') }

  function handleBusinessNoChange(value: string) {
    setBusinessNo(formatBusinessNo(value))
    setLookup('idle')
    clearError()
  }

  async function handleLookup() {
    if (lookup === 'checking') return
    if (businessNo.replace(/\D/g, '').length !== 10) { setError('사업자번호 10자리를 입력하세요'); return }
    setError('')
    setLookup('checking')
    try {
      setLookup((await businessNoExists(businessNo)) ? 'existing' : 'new')
    } catch (e) {
      setLookup('idle')
      setError(e instanceof Error ? e.message : '서버 연결에 실패했습니다.')
    }
  }

  async function handleSubmit() {
    if (submitting) return
    const invalid = validateCommon({ loginId, password, passwordConfirm, name, phone, email })
    if (invalid) { setError(invalid); return }
    if (lookup !== 'new' && lookup !== 'existing') { setError('사업자번호를 먼저 확인하세요'); return }
    if (lookup === 'new' && (!companyName.trim() || companyName.length > 100)) {
      setError('회사명을 입력하세요 (100자 이내)')
      return
    }
    if (lookup === 'new' && ceoName.length > 50) { setError('대표자명은 50자 이내로 입력하세요'); return }

    setError('')
    setSubmitting(true)
    let signedUp = false
    try {
      await signup({
        loginId,
        password,
        name: name.trim(),
        phone,
        email: email.trim() || undefined,
        businessNo,
        ...(lookup === 'new'
          ? { companyName: companyName.trim(), ceoName: ceoName.trim() || undefined }
          : { requestedRole }),
      })
      signedUp = true
      const session = await login(loginId, password)
      setSession(session)
      markTokenFresh()
      router.push(homeOf(session))
    } catch (e) {
      if (signedUp) {
        router.replace('/login')
        return
      }
      if (e instanceof ApiError && e.code === 'SHOP_SIGNUP_003') setLookup('idle')
      setError(e instanceof Error ? e.message : '서버 연결에 실패했습니다.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 p-4">
      <div className="w-full max-w-sm space-y-4">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-green-700">🛍️ 쇼핑몰 관리</h1>
          <p className="mt-1 text-sm text-gray-500">회원가입</p>
        </div>

        <Card>
          <CardHeader>
            <CardTitle className="text-base">계정 정보</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="loginId">아이디</Label>
              <Input id="loginId" placeholder="영문·숫자 4~30자" autoComplete="username" value={loginId}
                onChange={(e) => { setLoginId(e.target.value); clearError() }} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="password">비밀번호</Label>
              <Input id="password" type="password" placeholder="영문+숫자 8자 이상" autoComplete="new-password"
                value={password} onChange={(e) => { setPassword(e.target.value); clearError() }} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="passwordConfirm">비밀번호 확인</Label>
              <Input id="passwordConfirm" type="password" autoComplete="new-password" value={passwordConfirm}
                onChange={(e) => { setPasswordConfirm(e.target.value); clearError() }} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="name">이름</Label>
              <Input id="name" autoComplete="name" value={name}
                onChange={(e) => { setName(e.target.value); clearError() }} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="phone">연락처</Label>
              <Input id="phone" type="tel" placeholder="010-0000-0000" autoComplete="tel" value={phone}
                onChange={(e) => { setPhone(e.target.value); clearError() }} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="email">이메일 (선택)</Label>
              <Input id="email" type="email" autoComplete="email" value={email}
                onChange={(e) => { setEmail(e.target.value); clearError() }} />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-base">회사 정보</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="businessNo">사업자번호</Label>
              <div className="flex gap-2">
                <Input id="businessNo" inputMode="numeric" placeholder="000-00-00000" value={businessNo}
                  onChange={(e) => handleBusinessNoChange(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleLookup()} />
                <Button type="button" variant="outline" onClick={handleLookup} disabled={lookup === 'checking'}>
                  {lookup === 'checking' ? '확인 중' : '확인'}
                </Button>
              </div>
            </div>

            {lookup === 'new' && (
              <>
                <p className="rounded-md bg-green-50 px-3 py-2 text-xs text-green-700">
                  등록되지 않은 사업자번호입니다. 새 회사로 가입하며 파트너 관리자로 바로 이용할 수 있습니다.
                </p>
                <div className="space-y-1.5">
                  <Label htmlFor="companyName">회사명</Label>
                  <Input id="companyName" value={companyName}
                    onChange={(e) => { setCompanyName(e.target.value); clearError() }} />
                </div>
                <div className="space-y-1.5">
                  <Label htmlFor="ceoName">대표자명 (선택)</Label>
                  <Input id="ceoName" value={ceoName}
                    onChange={(e) => { setCeoName(e.target.value); clearError() }} />
                </div>
              </>
            )}

            {lookup === 'existing' && (
              <>
                <p className="rounded-md bg-amber-50 px-3 py-2 text-xs text-amber-700">
                  이미 등록된 회사입니다. 합류를 신청하면 승인 후 이용할 수 있습니다.
                </p>
                <div className="space-y-2">
                  <Label>희망 역할</Label>
                  {ROLE_OPTIONS.map((opt) => (
                    <label key={opt.value}
                      className="flex cursor-pointer items-start gap-2 rounded-md border p-2.5 text-sm has-[:checked]:border-green-600 has-[:checked]:bg-green-50">
                      <input type="radio" name="requestedRole" className="mt-0.5" value={opt.value}
                        checked={requestedRole === opt.value} onChange={() => setRequestedRole(opt.value)} />
                      <span>
                        <span className="font-medium">{opt.label}</span>
                        <span className="block text-xs text-gray-500">{opt.desc}</span>
                      </span>
                    </label>
                  ))}
                </div>
              </>
            )}

            {error && <p className="text-xs text-red-500">{error}</p>}
            <Button className="w-full bg-green-700 hover:bg-green-800" onClick={handleSubmit}
              disabled={submitting || (lookup !== 'new' && lookup !== 'existing')}>
              {submitting ? '가입 중...' : lookup === 'existing' ? '합류 신청' : '가입하기'}
            </Button>
          </CardContent>
        </Card>

        <p className="text-center text-sm text-gray-500">
          이미 계정이 있으신가요?{' '}
          <Link href="/login" className="font-medium text-green-700 hover:underline">로그인</Link>
        </p>
      </div>
    </div>
  )
}

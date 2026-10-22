'use client'

import { useState, useEffect, useRef } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import { Upload, X, Search } from 'lucide-react'
import Link from 'next/link'

const TERMS_VENDOR = `제1조 (목적)
본 약관은 온우림(이하 "플랫폼")가 제공하는 수발주 관리 서비스를 이용하는 수주업체(이하 "회원")의 권리·의무 및 책임 사항을 규정함을 목적으로 합니다.

제2조 (회원의 정의)
플랫폼에 가입한 수주업체로서 배정된 발주 건을 수주·배송하고, 배송완료 사진을 등록할 수 있는 자를 말합니다.

제3조 (가입 자격)
사업자 등록을 완료한 화훼 업체의 대표자 또는 담당자로서, 플랫폼의 모든 규정을 준수하고 신뢰 있는 업무 수행이 가능한 자로 한다. 가입 신청 후 관리자의 승인이 완료된 경우에만 서비스를 이용할 수 있습니다.

제4조 (회원의 의무)
① 배정된 주문은 지정된 시간과 장소에 정확히 배송하여야 합니다.
② 배송 완료 즉시 사진을 촬영하여 플랫폼에 등록하여야 합니다.
③ 부득이한 사정으로 배송이 지연될 경우 사전에 관리자에게 통보하여야 합니다.
④ 아이디 및 비밀번호를 제3자에게 양도하거나 도용하게 해서는 안 됩니다.
⑤ 플랫폼의 공지사항 및 업무 지침을 숙지하고 준수하여야 합니다.

제5조 (주문 수주 및 배송)
① 주문 배정 즉시 상품명, 수량, 배송일시, 장소, 리본 문구 등을 확인합니다.
② 배송 완료 후 사진을 플랫폼에 업로드하고, 배송완료 처리를 진행합니다.
③ 배송사진은 실제 배송된 상품과 일치하여야 하며, 허위 등록 시 제명 처리됩니다.

제6조 (클레임 처리)
① 클레임 발생 시 즉시 관리자에게 통보하고 적극적으로 해결에 협조합니다.
② 배송 1시간 이상 지연(천재지변 제외) 시 그 책임은 수주업체에 있습니다.
③ 배송 누락 시 발주가의 300% 보상 처리됩니다.
④ 배송사진과 실제 상품이 다를 경우 최대 500% 보상 처리 및 제명 처리됩니다.

제7조 (정산)
① 정산은 매월 1회 진행됩니다.
② 수주금액의 10%(부가세 별도)가 플랫폼 수수료로 공제됩니다.
③ 월 정산 대금은 매월 10일까지 납부, 본부 송금은 매월 25일에 처리됩니다.
④ 1개월 이상 미정산 시 서비스 이용이 정지될 수 있습니다.

제8조 (자격 상실 및 제명)
다음 각 호에 해당하는 경우 자격이 상실될 수 있습니다.
① 정산 대금 1개월 이상 연체
② 연간 3회 이상 클레임 발생
③ 배송사진 허위 등록
④ 아이디·비밀번호 제3자 도용
⑤ 사업장 폐업 또는 무단 양도
⑥ 3개월 이상 무발주 상태 지속
⑦ 플랫폼 및 회원사 명예 훼손

제9조 (면책 및 재판관할)
플랫폼 관련 분쟁의 소송은 관할 법원에서 처리하며, 자격 상실 시 납입된 비용은 반환되지 않습니다.`

const TERMS_ORDERING = `제1조 (목적)
본 약관은 온우림(이하 "플랫폼")이 제공하는 수발주 관리 서비스를 이용하는 발주업체(이하 "회원")의 권리·의무 및 책임 사항을 규정함을 목적으로 합니다.

제2조 (회원의 정의)
플랫폼에 가입한 발주업체로서 수주업체에 주문을 발주하고 배송 현황을 관리하는 자를 말합니다.

제3조 (가입 자격)
사업자 등록을 완료한 업체의 대표자 또는 담당자로서, 플랫폼의 모든 규정을 준수하고 신뢰 있는 업무 수행이 가능한 자로 한다.

제4조 (회원의 의무)
1) 발주 정보를 정확하게 입력하여야 합니다.
2) 주문 취소 시 수주업체에 즉시 통보하여야 합니다.
3) 아이디 및 비밀번호를 제3자에게 양도하거나 도용하게 해서는 안 됩니다.`

const PRIVACY_CONTENT = `수집 항목: 업체명, 사업자번호, 대표자명, 담당자명, 아이디, 비밀번호, 연락처, 이메일, 주소

수집 목적: 회원 식별 및 인증, 수발주 업무 처리, 정산 관리, 서비스 이용 관련 공지

보유 기간: 회원 탈퇴 또는 서비스 계약 종료 후 5년 (관련 법령에 따름)

개인정보는 수집 목적 이외의 용도로 사용되지 않으며, 법령에 따른 경우를 제외하고 제3자에게 제공하지 않습니다.`

type RoleOption = 'VENDOR' | 'ORDERING' | 'BOTH'

const ROLE_OPTIONS: { value: RoleOption; label: string; desc: string }[] = [
  { value: 'VENDOR', label: '수주업체', desc: '발주된 주문을 수주·배송합니다' },
  { value: 'ORDERING', label: '발주업체', desc: '수주업체에 발주를 등록합니다' },
  { value: 'BOTH', label: '둘 다', desc: '발주와 수주를 모두 진행합니다' },
]

interface CompanyInfo {
  businessNo: string
  companyName: string
  ceoName: string
  phone: string
  address: string
  detailAddress: string
}

interface FileFieldProps {
  label: string
  required?: boolean
  file: File | null
  onChange: (f: File | null) => void
}

function FileUploadField({ label, required, file, onChange }: FileFieldProps) {
  const ref = useRef<HTMLInputElement>(null)
  const previewUrl = file && file.type.startsWith('image/') ? URL.createObjectURL(file) : null

  return (
    <div className="space-y-1.5">
      <Label>
        {label}{required && <span className="ml-0.5 text-red-500"> *</span>}
      </Label>
      <div
        onClick={() => ref.current?.click()}
        className="flex cursor-pointer items-center gap-3 rounded-md border-2 border-dashed border-gray-200 px-3 py-2.5 hover:border-green-400 hover:bg-green-50"
      >
        {file ? (
          <>
            {previewUrl ? (
              <img src={previewUrl} alt="미리보기" className="h-10 w-10 shrink-0 rounded border object-cover" />
            ) : (
              <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded border bg-gray-100 text-xs font-medium text-gray-500">PDF</div>
            )}
            <div className="min-w-0 flex-1">
              <div className="truncate text-sm font-medium text-gray-700">{file.name}</div>
              <div className="text-xs text-gray-400">{(file.size / 1024).toFixed(0)} KB</div>
            </div>
            <button
              type="button"
              onClick={(e) => { e.stopPropagation(); onChange(null) }}
              className="shrink-0 text-gray-400 hover:text-red-500"
            >
              <X className="h-4 w-4" />
            </button>
          </>
        ) : (
          <>
            <Upload className="h-5 w-5 shrink-0 text-gray-400" />
            <span className="text-sm text-gray-400">클릭하여 파일 선택 (이미지, PDF)</span>
          </>
        )}
      </div>
      <input
        ref={ref}
        type="file"
        accept="image/*,.pdf"
        className="hidden"
        onChange={(e) => { onChange(e.target.files?.[0] ?? null); e.target.value = '' }}
      />
    </div>
  )
}

export default function SignupPage() {
  const [step, setStep] = useState<'role' | 'terms' | 'form'>('role')
  const [selectedRole, setSelectedRole] = useState<RoleOption | null>(null)
  const [agreeTerms, setAgreeTerms] = useState(false)
  const [agreePrivacy, setAgreePrivacy] = useState(false)
  const agreeAll = agreeTerms && agreePrivacy

  // 업체 정보
  const [businessNo, setBusinessNo] = useState('')
  const [companySearched, setCompanySearched] = useState(false)
  const [existingCompany, setExistingCompany] = useState<CompanyInfo | null>(null)
  const [companyName, setCompanyName] = useState('')
  const [ceoName, setCeoName] = useState('')
  const [companyPhone, setCompanyPhone] = useState('')
  const [address, setAddress] = useState('')
  const [detailAddress, setDetailAddress] = useState('')

  // 계정 정보
  const [loginId, setLoginId] = useState('')
  const [password, setPassword] = useState('')
  const [passwordConfirm, setPasswordConfirm] = useState('')
  const [name, setName] = useState('')
  const [phone, setPhone] = useState('')
  const [email, setEmail] = useState('')
  const [codeSent, setCodeSent] = useState(false)
  const [codeValue, setCodeValue] = useState('')
  const [verified, setVerified] = useState(false)
  const [codeError, setCodeError] = useState('')
  const [countdown, setCountdown] = useState(0)

  // 첨부서류
  const [businessDoc, setBusinessDoc] = useState<File | null>(null)
  const [bankDoc, setBankDoc] = useState<File | null>(null)

  useEffect(() => {
    if (countdown <= 0) return
    const timer = setInterval(() => setCountdown((c) => c - 1), 1000)
    return () => clearInterval(timer)
  }, [countdown])

  const formatPhone = (value: string) => {
    const raw = value.replace(/\D/g, '')
    const isSeoul = raw.startsWith('02')
    const digits = raw.slice(0, isSeoul ? 10 : 11)
    if (isSeoul) {
      if (digits.length <= 2) return digits
      if (digits.length <= 5) return `${digits.slice(0, 2)}-${digits.slice(2)}`
      if (digits.length <= 9) return `${digits.slice(0, 2)}-${digits.slice(2, 5)}-${digits.slice(5)}`
      return `${digits.slice(0, 2)}-${digits.slice(2, 6)}-${digits.slice(6)}`
    }
    if (digits.length <= 3) return digits
    if (digits.length <= 6) return `${digits.slice(0, 3)}-${digits.slice(3)}`
    if (digits.length <= 10) return `${digits.slice(0, 3)}-${digits.slice(3, 6)}-${digits.slice(6)}`
    return `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7)}`
  }

  const formatBusinessNo = (value: string) => {
    const digits = value.replace(/\D/g, '').slice(0, 10)
    if (digits.length <= 3) return digits
    if (digits.length <= 5) return `${digits.slice(0, 3)}-${digits.slice(3)}`
    return `${digits.slice(0, 3)}-${digits.slice(3, 5)}-${digits.slice(5)}`
  }

  const handleSearchCompany = async () => {
    if (!businessNo || businessNo.length < 12) return
    try {
      const res = await fetch(`/api/companies/search?businessNo=${encodeURIComponent(businessNo)}`)
      if (res.ok) {
        const data: CompanyInfo = await res.json()
        setExistingCompany(data)
        setCompanyName(data.companyName)
        setCeoName(data.ceoName ?? '')
        setCompanyPhone(data.phone ?? '')
        setAddress(data.address ?? '')
        setDetailAddress(data.detailAddress ?? '')
      } else {
        setExistingCompany(null)
      }
    } catch {
      setExistingCompany(null)
    }
    setCompanySearched(true)
  }

  const handleSendCode = () => {
    setCodeSent(true)
    setCodeValue('')
    setCodeError('')
    setVerified(false)
    setCountdown(60)
  }

  const handleVerify = () => {
    if (codeValue === '123456') {
      setVerified(true)
      setCodeError('')
    } else {
      setCodeError('인증번호가 올바르지 않습니다.')
    }
  }

  const formatCountdown = (s: number) =>
    `${String(Math.floor(s / 60)).padStart(2, '0')}:${String(s % 60).padStart(2, '0')}`

  const handleToggleAll = () => {
    const next = !agreeAll
    setAgreeTerms(next)
    setAgreePrivacy(next)
  }

  const resolveRoles = (): string[] => {
    if (selectedRole === 'BOTH') return ['ORDERING', 'VENDOR']
    if (selectedRole === 'ORDERING') return ['ORDERING']
    return ['VENDOR']
  }

  const handleSubmit = async () => {
    if (password !== passwordConfirm) {
      alert('비밀번호가 일치하지 않습니다.')
      return
    }
    const res = await fetch('/api/auth/signup', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        loginId,
        password,
        name,
        phone,
        businessNo,
        companyName,
        ceoName,
        companyPhone,
        address,
        detailAddress,
        roles: resolveRoles(),
      }),
    })
    if (res.ok) {
      alert('가입 신청이 완료되었습니다. 관리자 승인 후 로그인하실 수 있습니다.')
      window.location.href = '/login'
    } else {
      const body = await res.json().catch(() => null)
      alert(body?.message ?? '가입 신청 중 오류가 발생했습니다.')
    }
  }

  const termsContent = selectedRole === 'ORDERING'
    ? TERMS_ORDERING
    : TERMS_VENDOR

  // Step: 역할 선택
  if (step === 'role') {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-50 py-8">
        <div className="w-full max-w-sm space-y-4">
          <div className="text-center">
            <h1 className="text-2xl font-bold text-green-700">🌿 다운플라워</h1>
            <p className="mt-1 text-sm text-gray-500">회원가입</p>
          </div>
          <Card>
            <CardHeader>
              <CardTitle className="text-base">업체 유형 선택</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              {ROLE_OPTIONS.map((opt) => (
                <button
                  key={opt.value}
                  type="button"
                  onClick={() => setSelectedRole(opt.value)}
                  className={`w-full rounded-lg border-2 px-4 py-3 text-left transition-colors ${
                    selectedRole === opt.value
                      ? 'border-green-600 bg-green-50'
                      : 'border-gray-200 hover:border-green-300'
                  }`}
                >
                  <div className="font-medium text-gray-800">{opt.label}</div>
                  <div className="mt-0.5 text-xs text-gray-500">{opt.desc}</div>
                </button>
              ))}
              <Button
                className="mt-2 w-full bg-green-700 hover:bg-green-800 disabled:opacity-40"
                disabled={!selectedRole}
                onClick={() => setStep('terms')}
              >
                다음
              </Button>
              <div className="text-center text-xs text-gray-400">
                이미 계정이 있으신가요?{' '}
                <Link href="/login" className="text-green-700 hover:underline">로그인</Link>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    )
  }

  // Step: 약관 동의
  if (step === 'terms') {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-50 py-8">
        <div className="w-full max-w-sm space-y-4">
          <div className="text-center">
            <h1 className="text-2xl font-bold text-green-700">🌿 다운플라워</h1>
            <p className="mt-1 text-sm text-gray-500">회원가입</p>
          </div>
          <Card>
            <CardHeader>
              <CardTitle className="text-base">약관 동의</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <label className="flex cursor-pointer items-center gap-2 rounded-lg border border-green-200 bg-green-50 px-2 py-3">
                <input
                  type="checkbox"
                  checked={agreeAll}
                  onChange={handleToggleAll}
                  className="h-4 w-4 accent-green-700"
                />
                <span className="text-sm font-semibold text-green-800">전체 동의</span>
              </label>

              <div className="space-y-2">
                <label className="flex cursor-pointer items-center gap-2 px-2">
                  <input
                    type="checkbox"
                    checked={agreeTerms}
                    onChange={(e) => setAgreeTerms(e.target.checked)}
                    className="h-4 w-4 accent-green-700"
                  />
                  <span className="text-sm font-medium">
                    <span className="text-red-500">[필수]</span> 서비스 이용약관 동의
                  </span>
                </label>
                <div className="h-40 overflow-y-auto rounded-md border bg-gray-50 p-3 text-xs leading-relaxed text-gray-600 whitespace-pre-line">
                  {termsContent}
                </div>
              </div>

              <div className="space-y-2">
                <label className="flex cursor-pointer items-center gap-2 px-2">
                  <input
                    type="checkbox"
                    checked={agreePrivacy}
                    onChange={(e) => setAgreePrivacy(e.target.checked)}
                    className="h-4 w-4 accent-green-700"
                  />
                  <span className="text-sm font-medium">
                    <span className="text-red-500">[필수]</span> 개인정보 수집·이용 동의
                  </span>
                </label>
                <div className="h-28 overflow-y-auto rounded-md border bg-gray-50 p-3 text-xs leading-relaxed text-gray-600 whitespace-pre-line">
                  {PRIVACY_CONTENT}
                </div>
              </div>

              <div className="flex gap-2">
                <Button variant="outline" className="flex-1" onClick={() => setStep('role')}>
                  이전
                </Button>
                <Button
                  className="flex-1 bg-green-700 hover:bg-green-800 disabled:opacity-40"
                  disabled={!agreeAll}
                  onClick={() => setStep('form')}
                >
                  다음
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    )
  }

  // Step: 가입 폼
  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 py-8">
      <div className="w-full max-w-sm space-y-4">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-green-700">🌿 다운플라워</h1>
          <p className="mt-1 text-sm text-gray-500">회원가입</p>
        </div>

        <Card>
          <CardHeader>
            <CardTitle className="text-base">업체 정보</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {/* 사업자번호 검색 */}
            <div className="space-y-1.5">
              <Label>사업자번호 *</Label>
              <div className="flex gap-2">
                <Input
                  placeholder="000-00-00000"
                  value={businessNo}
                  onChange={(e) => {
                    setBusinessNo(formatBusinessNo(e.target.value))
                    setCompanySearched(false)
                    setExistingCompany(null)
                  }}
                  inputMode="numeric"
                  className="flex-1"
                />
                <button
                  type="button"
                  onClick={handleSearchCompany}
                  className="shrink-0 flex items-center gap-1 rounded-md border border-green-700 px-3 py-2 text-xs font-medium text-green-700 hover:bg-green-50"
                >
                  <Search className="h-3.5 w-3.5" />
                  검색
                </button>
              </div>
              {companySearched && existingCompany && (
                <p className="text-xs text-green-700">✓ 등록된 업체입니다. 업체 정보가 자동으로 채워졌습니다.</p>
              )}
              {companySearched && !existingCompany && (
                <p className="text-xs text-gray-400">신규 업체입니다. 아래 정보를 직접 입력해 주세요.</p>
              )}
            </div>

            <div className="space-y-1.5">
              <Label>업체명 *</Label>
              <Input
                placeholder="업체명을 입력하세요"
                value={companyName}
                onChange={(e) => setCompanyName(e.target.value)}
                readOnly={!!existingCompany}
                className={existingCompany ? 'bg-gray-50' : ''}
              />
            </div>
            <div className="space-y-1.5">
              <Label>대표자명</Label>
              <Input
                placeholder="대표자 성함"
                value={ceoName}
                onChange={(e) => setCeoName(e.target.value)}
                readOnly={!!existingCompany}
                className={existingCompany ? 'bg-gray-50' : ''}
              />
            </div>
            <div className="space-y-1.5">
              <Label>업체 연락처</Label>
              <Input
                placeholder="02-0000-0000 / 010-0000-0000"
                value={companyPhone}
                onChange={(e) => setCompanyPhone(formatPhone(e.target.value))}
                inputMode="numeric"
                readOnly={!!existingCompany}
                className={existingCompany ? 'bg-gray-50' : ''}
              />
            </div>
            <div className="space-y-1.5">
              <Label>주소</Label>
              <Input
                placeholder="도로명 주소"
                value={address}
                onChange={(e) => setAddress(e.target.value)}
                readOnly={!!existingCompany}
                className={existingCompany ? 'bg-gray-50' : ''}
              />
            </div>
            <div className="space-y-1.5">
              <Label>상세주소</Label>
              <Input
                placeholder="건물명, 동/호수 등"
                value={detailAddress}
                onChange={(e) => setDetailAddress(e.target.value)}
                readOnly={!!existingCompany}
                className={existingCompany ? 'bg-gray-50' : ''}
              />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-base">계정 정보</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="space-y-1.5">
              <Label>담당자명 *</Label>
              <Input
                placeholder="담당자 성함"
                value={name}
                onChange={(e) => setName(e.target.value)}
              />
            </div>
            <div className="space-y-1.5">
              <Label>아이디 *</Label>
              <Input
                placeholder="영문·숫자 조합 6자 이상"
                value={loginId}
                onChange={(e) => setLoginId(e.target.value)}
              />
            </div>
            <div className="space-y-1.5">
              <Label>비밀번호 *</Label>
              <Input
                type="password"
                placeholder="8자 이상"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
            <div className="space-y-1.5">
              <Label>비밀번호 확인 *</Label>
              <Input
                type="password"
                placeholder="비밀번호 재입력"
                value={passwordConfirm}
                onChange={(e) => setPasswordConfirm(e.target.value)}
              />
              {password && passwordConfirm && password !== passwordConfirm && (
                <p className="text-xs text-red-500">비밀번호가 일치하지 않습니다.</p>
              )}
            </div>

            {/* 연락처 + 인증번호 */}
            <div className="space-y-1.5">
              <Label>연락처 *</Label>
              <div className="flex gap-2">
                <Input
                  placeholder="010-0000-0000"
                  value={phone}
                  onChange={(e) => setPhone(formatPhone(e.target.value))}
                  inputMode="numeric"
                  disabled={verified}
                  className="flex-1"
                />
                <button
                  type="button"
                  onClick={handleSendCode}
                  disabled={verified}
                  className="shrink-0 rounded-md border border-green-700 px-3 py-2 text-xs font-medium text-green-700 hover:bg-green-50 disabled:opacity-40"
                >
                  {codeSent ? '재발송' : '인증번호 받기'}
                </button>
              </div>
            </div>

            {codeSent && !verified && (
              <div className="space-y-1.5">
                <div className="flex gap-2">
                  <div className="relative flex-1">
                    <Input
                      placeholder="인증번호 6자리"
                      value={codeValue}
                      onChange={(e) => setCodeValue(e.target.value)}
                      maxLength={6}
                    />
                    {countdown > 0 && (
                      <span className="absolute right-3 top-1/2 -translate-y-1/2 text-xs text-red-500">
                        {formatCountdown(countdown)}
                      </span>
                    )}
                  </div>
                  <button
                    type="button"
                    onClick={handleVerify}
                    disabled={countdown === 0}
                    className="shrink-0 rounded-md bg-green-700 px-3 py-2 text-xs font-medium text-white hover:bg-green-800 disabled:opacity-40"
                  >
                    확인
                  </button>
                </div>
                {codeError && <p className="text-xs text-red-500">{codeError}</p>}
                {countdown === 0 && (
                  <p className="text-xs text-gray-400">인증 시간이 만료되었습니다. 재발송해주세요.</p>
                )}
              </div>
            )}

            {verified && (
              <p className="flex items-center gap-1 text-xs font-medium text-green-700">
                <span>✓</span> 휴대폰 인증이 완료되었습니다.
              </p>
            )}

            <div className="space-y-1.5">
              <Label>이메일</Label>
              <Input
                type="email"
                placeholder="example@email.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>

            {/* 수주업체는 첨부서류 필수 */}
            {(selectedRole === 'VENDOR' || selectedRole === 'BOTH') && (
              <div className="space-y-3 rounded-lg border bg-gray-50 p-3">
                <p className="text-xs font-medium text-gray-500">첨부서류 (수주업체 필수)</p>
                <FileUploadField label="사업자 사본" required file={businessDoc} onChange={setBusinessDoc} />
                <FileUploadField label="통장 사본" required file={bankDoc} onChange={setBankDoc} />
              </div>
            )}
            {selectedRole === 'ORDERING' && (
              <div className="space-y-3 rounded-lg border bg-gray-50 p-3">
                <p className="text-xs font-medium text-gray-500">첨부서류</p>
                <FileUploadField label="사업자 사본" required file={businessDoc} onChange={setBusinessDoc} />
              </div>
            )}

            <div className="flex gap-2">
              <Button variant="outline" className="flex-1" onClick={() => setStep('terms')}>
                이전
              </Button>
              <Button className="flex-1 bg-green-700 hover:bg-green-800" onClick={handleSubmit}>
                가입 신청
              </Button>
            </div>

            <p className="text-center text-xs text-gray-400">
              가입 신청 후 관리자 승인이 완료되면 로그인하실 수 있습니다.
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

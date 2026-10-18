'use client'

import { useState, useEffect, useRef } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import { Upload, X } from 'lucide-react'
import Link from 'next/link'

const TERMS_CONTENT = `제1조 (목적)
본 약관은 다운플라워(이하 "플랫폼")가 제공하는 수발주 관리 서비스를 이용하는 수주업체(이하 "회원")의 권리·의무 및 책임 사항을 규정함을 목적으로 합니다.

제2조 (회원의 정의)
플랫폼에 가입한 수주업체로서 배정된 발주 건을 수주·배송하고, 배송완료 사진을 등록할 수 있는 자를 말합니다.

제3조 (가입 자격)
사업자 등록을 완료한 화훼 업체의 대표자 또는 담당자로서, 플랫폼의 모든 규정을 준수하고 신뢰 있는 업무 수행이 가능한 자로 한다. 가입 신청 후 관리자의 승인이 완료된 경우에만 서비스를 이용할 수 있습니다.

제4조 (회원의 의무)
1) 배정된 주문은 지정된 시간과 장소에 정확히 배송하여야 합니다.
2) 배송 완료 즉시 사진을 촬영하여 플랫폼에 등록하여야 합니다.
3) 부득이한 사정으로 배송이 지연될 경우 사전에 관리자에게 통보하여야 합니다.
4) 아이디 및 비밀번호를 제3자에게 양도하거나 도용하게 해서는 안 됩니다.
5) 플랫폼의 공지사항 및 업무 지침을 숙지하고 준수하여야 합니다.

제5조 (주문 수주 및 배송)
1) 주문 배정 즉시 상품명, 수량, 배송일시, 장소, 리본 문구 등을 확인합니다.
2) 배송 완료 후 사진을 플랫폼에 업로드하고, 배송완료 처리를 진행합니다.
3) 배송사진은 실제 배송된 상품과 일치하여야 하며, 허위 등록 시 제명 처리됩니다.

제6조 (클레임 처리)
1) 클레임 발생 시 즉시 관리자에게 통보하고 적극적으로 해결에 협조합니다.
2) 배송 1시간 이상 지연(천재지변 제외) 시 그 책임은 수주업체에 있습니다.
3) 배송 누락 시 발주가의 300% 보상 처리됩니다.
4) 배송사진과 실제 상품이 다를 경우 최대 500% 보상 처리 및 제명 처리됩니다.

제7조 (정산)
1) 정산은 매월 1회 진행됩니다.
2) 수주금액의 10%(부가세 별도)가 플랫폼 수수료로 공제됩니다.
3) 월 정산 대금은 매월 10일까지 납부, 본부 송금은 매월 25일에 처리됩니다.
4) 1개월 이상 미정산 시 서비스 이용이 정지될 수 있습니다.

제8조 (자격 상실 및 제명)
다음 각 호에 해당하는 경우 자격이 상실될 수 있습니다.
1) 정산 대금 1개월 이상 연체
2) 연간 3회 이상 클레임 발생
3) 배송사진 허위 등록
4) 아이디·비밀번호 제3자 도용
5) 사업장 폐업 또는 무단 양도
6) 3개월 이상 무발주 상태 지속
7) 플랫폼 및 회원사 명예 훼손

제9조 (면책 및 재판관할)
플랫폼 관련 분쟁의 소송은 관할 법원에서 처리하며, 자격 상실 시 납입된 비용은 반환되지 않습니다.`

const PRIVACY_CONTENT = `수집 항목: 업체명, 사업자번호, 대표자명, 담당자명, 아이디, 비밀번호, 연락처, 이메일, 주소

수집 목적: 회원 식별 및 인증, 수발주 업무 처리, 정산 관리, 서비스 이용 관련 공지

보유 기간: 회원 탈퇴 또는 서비스 계약 종료 후 5년 (관련 법령에 따름)

개인정보는 수집 목적 이외의 용도로 사용되지 않으며, 법령에 따른 경우를 제외하고 제3자에게 제공하지 않습니다.`

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
  const [step, setStep] = useState<'terms' | 'form'>('terms')
  const [agreeTerms, setAgreeTerms] = useState(false)
  const [agreePrivacy, setAgreePrivacy] = useState(false)
  const agreeAll = agreeTerms && agreePrivacy

  const [phone, setPhone] = useState('')
  const [codeSent, setCodeSent] = useState(false)
  const [codeValue, setCodeValue] = useState('')
  const [verified, setVerified] = useState(false)
  const [codeError, setCodeError] = useState('')
  const [countdown, setCountdown] = useState(0)

  const [email, setEmail] = useState('')
  const [businessDoc, setBusinessDoc] = useState<File | null>(null)
  const [bankDoc, setBankDoc] = useState<File | null>(null)

  useEffect(() => {
    if (countdown <= 0) return
    const timer = setInterval(() => setCountdown((c) => c - 1), 1000)
    return () => clearInterval(timer)
  }, [countdown])

  const formatPhone = (value: string) => {
    const digits = value.replace(/\D/g, '').slice(0, 11)
    if (digits.length <= 3) return digits
    if (digits.length <= 7) return `${digits.slice(0, 3)}-${digits.slice(3)}`
    return `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7)}`
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

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 py-8">
      <div className="w-full max-w-sm space-y-4">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-green-700">🌿 다운플라워</h1>
          <p className="mt-1 text-sm text-gray-500">수주업체 회원가입</p>
        </div>

        {step === 'terms' ? (
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
                  {TERMS_CONTENT}
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

              <Button
                className="w-full bg-green-700 hover:bg-green-800 disabled:opacity-40"
                disabled={!agreeAll}
                onClick={() => setStep('form')}
              >
                다음
              </Button>

              <div className="text-center text-xs text-gray-400">
                이미 계정이 있으신가요?{' '}
                <Link href="/login" className="text-green-700 hover:underline">로그인</Link>
              </div>
            </CardContent>
          </Card>
        ) : (
          <Card>
            <CardHeader>
              <CardTitle className="text-base">회원가입</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <div className="space-y-1.5">
                <Label>업체명 *</Label>
                <Input placeholder="업체명을 입력하세요" />
              </div>
              <div className="space-y-1.5">
                <Label>사업자번호 *</Label>
                <Input placeholder="000-00-00000" />
              </div>
              <div className="space-y-1.5">
                <Label>대표자명 *</Label>
                <Input placeholder="대표자 성함" />
              </div>
              <div className="space-y-1.5">
                <Label>담당자명 *</Label>
                <Input placeholder="담당자 성함" />
              </div>
              <div className="space-y-1.5">
                <Label>아이디 *</Label>
                <Input placeholder="영문·숫자 조합 6자 이상" />
              </div>
              <div className="space-y-1.5">
                <Label>비밀번호 *</Label>
                <Input type="password" placeholder="8자 이상" />
              </div>
              <div className="space-y-1.5">
                <Label>비밀번호 확인 *</Label>
                <Input type="password" placeholder="비밀번호 재입력" />
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

              <div className="space-y-1.5">
                <Label>주소</Label>
                <Input placeholder="업체 주소" />
              </div>

              {/* 첨부서류 */}
              <div className="space-y-3 rounded-lg border bg-gray-50 p-3">
                <p className="text-xs font-medium text-gray-500">첨부서류</p>
                <FileUploadField
                  label="사업자 사본"
                  required
                  file={businessDoc}
                  onChange={setBusinessDoc}
                />
                <FileUploadField
                  label="통장 사본"
                  required
                  file={bankDoc}
                  onChange={setBankDoc}
                />
              </div>

              <div className="flex gap-2">
                <Button variant="outline" className="flex-1" onClick={() => setStep('terms')}>
                  이전
                </Button>
                <Button className="flex-1 bg-green-700 hover:bg-green-800">
                  가입 신청
                </Button>
              </div>

              <p className="text-center text-xs text-gray-400">
                가입 신청 후 관리자 승인이 완료되면 로그인하실 수 있습니다.
              </p>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  )
}

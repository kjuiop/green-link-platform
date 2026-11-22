const ACCOUNTS = [
  { id: 'admin', desc: '최고관리자' },
  { id: 'shop', desc: '쇼핑몰 관리자 (승인됨)' },
  { id: 'pending', desc: '승인 대기' },
  { id: 'new', desc: '권한 없음 (신청 전)' },
]

export function MockHint() {
  return (
    <div className="rounded-lg border border-dashed bg-white p-3 text-xs text-gray-500">
      <p className="mb-1.5 font-medium text-gray-600">Mock 로그인 (비밀번호는 아무 값)</p>
      <ul className="space-y-0.5">
        {ACCOUNTS.map((a) => (
          <li key={a.id}>
            <code className="rounded bg-gray-100 px-1 py-0.5">{a.id}</code> — {a.desc}
          </li>
        ))}
      </ul>
    </div>
  )
}

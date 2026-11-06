'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { Paperclip, X } from 'lucide-react'
import { createPost } from '@/lib/api/postApi'

function isSuperAdmin(): boolean {
  return typeof window !== 'undefined' && localStorage.getItem('role') === 'SUPER_ADMIN'
}

export default function CommunityNewPage() {
  const router = useRouter()
  const isAdmin = isSuperAdmin()

  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')
  const [files, setFiles] = useState<File[]>([])
  const [isNotice, setIsNotice] = useState(false)
  const [isPinned, setIsPinned] = useState(false)
  const [noticeStartAt, setNoticeStartAt] = useState('')
  const [noticeEndAt, setNoticeEndAt] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selected = Array.from(e.target.files ?? [])
    setFiles((prev) => [...prev, ...selected])
    e.target.value = ''
  }

  const removeFile = (idx: number) => {
    setFiles((prev) => prev.filter((_, i) => i !== idx))
  }

  const handleSubmit = async () => {
    if (!title.trim()) { setError('제목을 입력하세요.'); return }
    if (!content.trim()) { setError('내용을 입력하세요.'); return }
    setSaving(true)
    setError('')
    try {
      const post = await createPost(
        {
          title: title.trim(),
          content: content.trim(),
          isNotice,
          isPinned,
          noticeStartAt: noticeStartAt ? noticeStartAt.replace('T', ' ') + ':00' : '',
          noticeEndAt: noticeEndAt ? noticeEndAt.replace('T', ' ') + ':00' : '',
        },
        files,
      )
      router.push(`/community/${post.id}`)
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : '등록에 실패했습니다.')
      setSaving(false)
    }
  }

  return (
    <div className="mx-auto max-w-3xl space-y-4">
      <h1 className="text-xl font-bold">게시글 작성</h1>

      <div className="rounded-lg border bg-white p-6 space-y-4">
        {/* 제목 */}
        <div className="space-y-1.5">
          <label className="block text-sm font-medium text-gray-700">제목 *</label>
          <input
            type="text"
            placeholder="제목을 입력하세요"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            className="w-full rounded-md border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
          />
        </div>

        {/* 내용 */}
        <div className="space-y-1.5">
          <label className="block text-sm font-medium text-gray-700">내용 *</label>
          <textarea
            placeholder="내용을 입력하세요"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            rows={12}
            className="w-full resize-y rounded-md border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
          />
        </div>

        {/* 파일 첨부 */}
        <div className="space-y-1.5">
          <label className="block text-sm font-medium text-gray-700">첨부파일</label>
          <label className="inline-flex cursor-pointer items-center gap-1.5 rounded-md border px-3 py-1.5 text-sm text-gray-600 hover:bg-gray-50">
            <Paperclip className="h-4 w-4" />
            파일 선택
            <input type="file" multiple onChange={handleFileChange} className="hidden" />
          </label>
          {files.length > 0 && (
            <ul className="mt-2 space-y-1">
              {files.map((f, i) => (
                <li key={i} className="flex items-center gap-2 text-sm text-gray-600">
                  <Paperclip className="h-3.5 w-3.5 shrink-0 text-gray-400" />
                  <span className="flex-1 truncate">{f.name}</span>
                  <button onClick={() => removeFile(i)} className="text-gray-400 hover:text-red-500">
                    <X className="h-4 w-4" />
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>

        {/* 공지 설정 (슈퍼관리자 전용) */}
        {isAdmin && (
          <div className="rounded-md border border-green-200 bg-green-50 p-4 space-y-3">
            <p className="text-xs font-semibold text-green-800">공지 설정 (관리자 전용)</p>
            <div className="flex gap-4">
              <label className="flex items-center gap-1.5 text-sm">
                <input
                  type="checkbox"
                  checked={isNotice}
                  onChange={(e) => setIsNotice(e.target.checked)}
                  className="accent-green-700"
                />
                공지글
              </label>
              <label className="flex items-center gap-1.5 text-sm">
                <input
                  type="checkbox"
                  checked={isPinned}
                  onChange={(e) => setIsPinned(e.target.checked)}
                  className="accent-green-700"
                />
                상단 고정
              </label>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="mb-1 block text-xs text-gray-600">노출 시작일</label>
                <input
                  type="datetime-local"
                  value={noticeStartAt}
                  onChange={(e) => setNoticeStartAt(e.target.value)}
                  className="w-full rounded-md border bg-white px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
                />
              </div>
              <div>
                <label className="mb-1 block text-xs text-gray-600">노출 종료일</label>
                <input
                  type="datetime-local"
                  value={noticeEndAt}
                  onChange={(e) => setNoticeEndAt(e.target.value)}
                  className="w-full rounded-md border bg-white px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
                />
              </div>
            </div>
          </div>
        )}

        {error && (
          <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-600">{error}</p>
        )}
      </div>

      <div className="flex justify-between">
        <button
          onClick={() => router.push('/community')}
          className="rounded-md border px-4 py-2 text-sm text-gray-600 hover:bg-gray-50"
        >
          취소
        </button>
        <button
          onClick={handleSubmit}
          disabled={saving}
          className="rounded-md bg-green-700 px-5 py-2 text-sm font-medium text-white hover:bg-green-800 disabled:opacity-50"
        >
          {saving ? '등록 중...' : '등록'}
        </button>
      </div>
    </div>
  )
}

'use client'

import { useState, useEffect, use } from 'react'
import { useRouter } from 'next/navigation'
import { Pin, Paperclip, ExternalLink } from 'lucide-react'
import { fetchPost, deletePost, updateNoticeSettings, type UpdateNoticePayload } from '@/lib/api/postApi'
import type { Post } from '@/types/post'

function getCurrentAdminId(): number | null {
  try {
    const token = localStorage.getItem('accessToken')
    if (!token) return null
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=')
    const payload = JSON.parse(atob(padded))
    return payload.sub ? Number(payload.sub) : null
  } catch {
    return null
  }
}

function isSuperAdmin(): boolean {
  return typeof window !== 'undefined' && localStorage.getItem('role') === 'SUPER_ADMIN'
}

function formatDateTime(dt: string | null) {
  if (!dt) return '-'
  return dt.slice(0, 16).replace('T', ' ')
}

function toDatetimeLocal(dt: string | null): string {
  if (!dt) return ''
  return dt.slice(0, 16).replace(' ', 'T')
}

function fromDatetimeLocal(val: string): string {
  if (!val) return ''
  return val.replace('T', ' ') + ':00'
}

export default function CommunityDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params)
  const router = useRouter()
  const [post, setPost] = useState<Post | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [deleting, setDeleting] = useState(false)

  const [noticeOpen, setNoticeOpen] = useState(false)
  const [noticeForm, setNoticeForm] = useState({ isNotice: false, isPinned: false, startAt: '', endAt: '' })
  const [noticeSaving, setNoticeSaving] = useState(false)
  const [noticeError, setNoticeError] = useState('')

  const adminId = typeof window !== 'undefined' ? getCurrentAdminId() : null
  const isAdmin = typeof window !== 'undefined' ? isSuperAdmin() : false

  useEffect(() => {
    setLoading(true)
    fetchPost(Number(id))
      .then((p) => {
        setPost(p)
        setNoticeForm({
          isNotice: p.isNotice,
          isPinned: p.isPinned,
          startAt: toDatetimeLocal(p.noticeStartAt),
          endAt: toDatetimeLocal(p.noticeEndAt),
        })
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [id])

  const canEdit = post && (isAdmin || post.authorId === adminId)

  const handleDelete = async () => {
    if (!confirm('정말 삭제하시겠습니까?')) return
    setDeleting(true)
    try {
      await deletePost(Number(id))
      router.push('/community')
    } catch (e: unknown) {
      alert(e instanceof Error ? e.message : '삭제에 실패했습니다.')
      setDeleting(false)
    }
  }

  const handleNoticeSave = async () => {
    setNoticeSaving(true)
    setNoticeError('')
    try {
      const payload: UpdateNoticePayload = {
        isNotice: noticeForm.isNotice,
        isPinned: noticeForm.isPinned,
        noticeStartAt: fromDatetimeLocal(noticeForm.startAt),
        noticeEndAt: fromDatetimeLocal(noticeForm.endAt),
      }
      await updateNoticeSettings(Number(id), payload)
      const updated = await fetchPost(Number(id))
      setPost(updated)
      setNoticeOpen(false)
    } catch (e: unknown) {
      setNoticeError(e instanceof Error ? e.message : '공지 설정에 실패했습니다.')
    } finally {
      setNoticeSaving(false)
    }
  }

  if (loading) {
    return <div className="py-20 text-center text-sm text-gray-400">불러오는 중...</div>
  }

  if (error || !post) {
    return (
      <div className="rounded-md bg-red-50 px-4 py-3 text-sm text-red-600">
        {error || '게시글을 찾을 수 없습니다.'}
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-3xl space-y-4">
      {/* 헤더 */}
      <div className="rounded-lg border bg-white p-6">
        <div className="flex items-start gap-2">
          {post.isNotice && (
            <span className="mt-0.5 shrink-0 rounded bg-green-700 px-2 py-0.5 text-xs font-semibold text-white">공지</span>
          )}
          {post.isPinned && (
            <Pin className="mt-1 h-4 w-4 shrink-0 text-green-600" />
          )}
          <h1 className="text-lg font-bold leading-snug">{post.title}</h1>
        </div>
        <div className="mt-3 flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-gray-500 border-b pb-3">
          <span>{post.companyName}</span>
          <span>{formatDateTime(post.createdAt)}</span>
          <span>조회 {post.viewCount}</span>
        </div>

        {/* 본문 */}
        <div className="mt-5 min-h-[120px] whitespace-pre-wrap text-sm leading-relaxed text-gray-800">
          {post.content}
        </div>

        {/* 첨부파일 */}
        {post.attachments.length > 0 && (
          <div className="mt-5 border-t pt-4">
            <p className="mb-2 text-xs font-medium text-gray-500">첨부파일</p>
            <ul className="space-y-1">
              {post.attachments.map((att) => (
                <li key={att.id}>
                  <a
                    href={att.url}
                    target="_blank"
                    rel="noreferrer"
                    className="inline-flex items-center gap-1.5 text-sm text-blue-600 hover:underline"
                  >
                    <Paperclip className="h-3.5 w-3.5" />
                    {att.fileName}
                    <ExternalLink className="h-3 w-3" />
                  </a>
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>

      {/* 공지 설정 (슈퍼관리자 전용) */}
      {isAdmin && (
        <div className="rounded-lg border bg-white p-4">
          <div className="flex items-center justify-between">
            <span className="text-sm font-medium text-gray-700">공지 설정</span>
            <button
              onClick={() => setNoticeOpen((v) => !v)}
              className="text-xs text-green-700 hover:underline"
            >
              {noticeOpen ? '닫기' : '수정'}
            </button>
          </div>
          {!noticeOpen && (
            <div className="mt-2 flex flex-wrap gap-4 text-xs text-gray-500">
              <span>공지: {post.isNotice ? 'ON' : 'OFF'}</span>
              <span>고정: {post.isPinned ? 'ON' : 'OFF'}</span>
              <span>시작: {post.noticeStartAt ? formatDateTime(post.noticeStartAt) : '-'}</span>
              <span>종료: {post.noticeEndAt ? formatDateTime(post.noticeEndAt) : '-'}</span>
            </div>
          )}
          {noticeOpen && (
            <div className="mt-3 space-y-3">
              <div className="flex gap-4">
                <label className="flex items-center gap-1.5 text-sm">
                  <input
                    type="checkbox"
                    checked={noticeForm.isNotice}
                    onChange={(e) => setNoticeForm((f) => ({ ...f, isNotice: e.target.checked }))}
                    className="accent-green-700"
                  />
                  공지글
                </label>
                <label className="flex items-center gap-1.5 text-sm">
                  <input
                    type="checkbox"
                    checked={noticeForm.isPinned}
                    onChange={(e) => setNoticeForm((f) => ({ ...f, isPinned: e.target.checked }))}
                    className="accent-green-700"
                  />
                  상단 고정
                </label>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="mb-1 block text-xs text-gray-500">노출 시작일</label>
                  <input
                    type="datetime-local"
                    value={noticeForm.startAt}
                    onChange={(e) => setNoticeForm((f) => ({ ...f, startAt: e.target.value }))}
                    className="w-full rounded-md border px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
                  />
                </div>
                <div>
                  <label className="mb-1 block text-xs text-gray-500">노출 종료일</label>
                  <input
                    type="datetime-local"
                    value={noticeForm.endAt}
                    onChange={(e) => setNoticeForm((f) => ({ ...f, endAt: e.target.value }))}
                    className="w-full rounded-md border px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
                  />
                </div>
              </div>
              {noticeError && (
                <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-600">{noticeError}</p>
              )}
              <div className="flex justify-end gap-2">
                <button
                  onClick={() => setNoticeOpen(false)}
                  className="rounded-md border px-3 py-1.5 text-sm text-gray-600 hover:bg-gray-50"
                >
                  취소
                </button>
                <button
                  onClick={handleNoticeSave}
                  disabled={noticeSaving}
                  className="rounded-md bg-green-700 px-3 py-1.5 text-sm font-medium text-white hover:bg-green-800 disabled:opacity-50"
                >
                  {noticeSaving ? '저장 중...' : '저장'}
                </button>
              </div>
            </div>
          )}
        </div>
      )}

      {/* 버튼 영역 */}
      <div className="flex justify-between">
        <button
          onClick={() => router.push('/community')}
          className="rounded-md border px-4 py-2 text-sm text-gray-600 hover:bg-gray-50"
        >
          목록
        </button>
        {canEdit && (
          <div className="flex gap-2">
            <button
              onClick={() => router.push(`/community/${post.id}/edit`)}
              className="rounded-md border px-4 py-2 text-sm text-gray-600 hover:bg-gray-50"
            >
              수정
            </button>
            <button
              onClick={handleDelete}
              disabled={deleting}
              className="rounded-md bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700 disabled:opacity-50"
            >
              {deleting ? '삭제 중...' : '삭제'}
            </button>
          </div>
        )}
      </div>
    </div>
  )
}

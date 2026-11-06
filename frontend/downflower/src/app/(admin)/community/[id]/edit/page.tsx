'use client'

import { useState, useEffect, use } from 'react'
import { useRouter } from 'next/navigation'
import { fetchPost, updatePost } from '@/lib/api/postApi'

export default function CommunityEditPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params)
  const router = useRouter()

  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    fetchPost(Number(id))
      .then((p) => {
        setTitle(p.title)
        setContent(p.content)
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [id])

  const handleSubmit = async () => {
    if (!title.trim()) { setError('제목을 입력하세요.'); return }
    if (!content.trim()) { setError('내용을 입력하세요.'); return }
    setSaving(true)
    setError('')
    try {
      await updatePost(Number(id), { title: title.trim(), content: content.trim() })
      router.push(`/community/${id}`)
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : '수정에 실패했습니다.')
      setSaving(false)
    }
  }

  if (loading) {
    return <div className="py-20 text-center text-sm text-gray-400">불러오는 중...</div>
  }

  return (
    <div className="mx-auto max-w-3xl space-y-4">
      <h1 className="text-xl font-bold">게시글 수정</h1>

      <div className="rounded-lg border bg-white p-6 space-y-4">
        <div className="space-y-1.5">
          <label className="block text-sm font-medium text-gray-700">제목 *</label>
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            className="w-full rounded-md border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
          />
        </div>

        <div className="space-y-1.5">
          <label className="block text-sm font-medium text-gray-700">내용 *</label>
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            rows={12}
            className="w-full resize-y rounded-md border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
          />
        </div>

        {error && (
          <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-600">{error}</p>
        )}
      </div>

      <div className="flex justify-between">
        <button
          onClick={() => router.push(`/community/${id}`)}
          className="rounded-md border px-4 py-2 text-sm text-gray-600 hover:bg-gray-50"
        >
          취소
        </button>
        <button
          onClick={handleSubmit}
          disabled={saving}
          className="rounded-md bg-green-700 px-5 py-2 text-sm font-medium text-white hover:bg-green-800 disabled:opacity-50"
        >
          {saving ? '저장 중...' : '저장'}
        </button>
      </div>
    </div>
  )
}

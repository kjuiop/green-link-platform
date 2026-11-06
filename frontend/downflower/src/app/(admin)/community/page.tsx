'use client'

import { useState, useEffect, useCallback } from 'react'
import { useRouter } from 'next/navigation'
import { Search, Paperclip, Pin } from 'lucide-react'
import { Pagination } from '@/components/ui/Pagination'
import { fetchPosts } from '@/lib/api/postApi'
import type { PostSummary, SpringPage } from '@/types/post'

const PAGE_SIZE = 20

function formatDate(datetime: string) {
  return datetime?.slice(0, 10) ?? '-'
}

export default function CommunityPage() {
  const router = useRouter()
  const [result, setResult] = useState<SpringPage<PostSummary> | null>(null)
  const [page, setPage] = useState(0)
  const [keyword, setKeyword] = useState('')
  const [appliedKeyword, setAppliedKeyword] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = useCallback((p: number, kw: string) => {
    setLoading(true)
    setError('')
    fetchPosts(p, PAGE_SIZE, kw || undefined)
      .then(setResult)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => { load(page, appliedKeyword) }, [page, appliedKeyword, load])

  const handleSearch = () => { setAppliedKeyword(keyword); setPage(0) }
  const handleReset = () => { setKeyword(''); setAppliedKeyword(''); setPage(0) }

  const posts = result?.content ?? []
  const totalElements = result?.totalElements ?? 0
  const offset = page * PAGE_SIZE

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold">커뮤니티방</h1>
        <button
          onClick={() => router.push('/community/new')}
          className="rounded-md bg-green-700 px-4 py-2 text-sm font-medium text-white hover:bg-green-800"
        >
          글쓰기
        </button>
      </div>

      {error && (
        <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-600">{error}</p>
      )}

      <div className="rounded-lg border bg-white p-4">
        <div className="flex gap-2">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
            <input
              type="text"
              placeholder="제목 또는 업체명으로 검색"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
              className="w-full rounded-md border px-3 py-2 pl-9 text-sm placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-green-700"
            />
          </div>
          <button onClick={handleSearch} className="rounded-md bg-green-700 px-4 py-2 text-sm font-medium text-white hover:bg-green-800">검색</button>
          <button onClick={handleReset} className="rounded-md border px-4 py-2 text-sm font-medium text-gray-500 hover:bg-gray-50">초기화</button>
        </div>
      </div>

      {/* 데스크톱 테이블 */}
      <div className="hidden rounded-lg border bg-white md:block">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b bg-gray-50 text-left text-xs text-gray-500">
              <th className="px-4 py-3 w-14 text-center">번호</th>
              <th className="px-4 py-3 w-28">업체명</th>
              <th className="px-4 py-3">제목</th>
              <th className="px-4 py-3 w-24 text-center">작성일</th>
              <th className="px-4 py-3 w-16 text-center">조회</th>
              <th className="px-4 py-3 w-10 text-center">첨부</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={6} className="py-10 text-center text-sm text-gray-400">불러오는 중...</td>
              </tr>
            ) : posts.length === 0 ? (
              <tr>
                <td colSpan={6} className="py-10 text-center text-sm text-gray-400">게시글이 없습니다.</td>
              </tr>
            ) : (
              posts.map((post, idx) => (
                <tr key={post.id} className="border-b last:border-0 hover:bg-gray-50">
                  <td className="px-4 py-3 text-center text-gray-500">
                    {post.isNotice ? '' : totalElements - offset - idx}
                  </td>
                  <td className="px-4 py-3 text-gray-600 truncate max-w-[112px]">{post.companyName}</td>
                  <td className="px-4 py-3">
                    <button
                      onClick={() => router.push(`/community/${post.id}`)}
                      className="flex items-center gap-1.5 text-left hover:text-green-700"
                    >
                      {post.isNotice && (
                        <span className="shrink-0 rounded bg-green-700 px-1.5 py-0.5 text-xs font-medium text-white">공지</span>
                      )}
                      {post.isPinned && (
                        <Pin className="shrink-0 h-3.5 w-3.5 text-green-600" />
                      )}
                      <span className="line-clamp-1">{post.title}</span>
                    </button>
                  </td>
                  <td className="px-4 py-3 text-center text-gray-500">{formatDate(post.createdAt)}</td>
                  <td className="px-4 py-3 text-center text-gray-500">{post.viewCount}</td>
                  <td className="px-4 py-3 text-center">
                    {post.hasAttachment && <Paperclip className="mx-auto h-3.5 w-3.5 text-gray-400" />}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
        {result && result.totalPages > 1 && (
          <div className="border-t px-4 py-3">
            <Pagination
              page={result.number}
              totalPages={result.totalPages}
              totalElements={result.totalElements}
              size={result.size}
              onPageChange={(p) => setPage(p)}
            />
          </div>
        )}
      </div>

      {/* 모바일 카드 */}
      <div className="space-y-2 md:hidden">
        {loading ? (
          <div className="rounded-lg border bg-white py-10 text-center text-sm text-gray-400">불러오는 중...</div>
        ) : posts.length === 0 ? (
          <div className="rounded-lg border bg-white py-10 text-center text-sm text-gray-400">게시글이 없습니다.</div>
        ) : (
          posts.map((post) => (
            <button
              key={post.id}
              onClick={() => router.push(`/community/${post.id}`)}
              className="w-full rounded-lg border bg-white p-4 text-left hover:bg-gray-50"
            >
              <div className="flex items-start gap-2">
                {post.isNotice && (
                  <span className="mt-0.5 shrink-0 rounded bg-green-700 px-1.5 py-0.5 text-xs font-medium text-white">공지</span>
                )}
                <span className="font-medium line-clamp-2">{post.title}</span>
              </div>
              <div className="mt-1.5 flex items-center gap-3 text-xs text-gray-500">
                <span>{post.companyName}</span>
                <span>{formatDate(post.createdAt)}</span>
                <span>조회 {post.viewCount}</span>
                {post.hasAttachment && <Paperclip className="h-3 w-3" />}
              </div>
            </button>
          ))
        )}
        {result && result.totalPages > 1 && (
          <Pagination
            page={result.number}
            totalPages={result.totalPages}
            totalElements={result.totalElements}
            size={result.size}
            onPageChange={(p) => setPage(p)}
          />
        )}
      </div>
    </div>
  )
}

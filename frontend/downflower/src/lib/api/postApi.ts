import { fetchWithAuth, authHeaders, authBearerHeader } from './fetchWithAuth'
import type { Post, PostSummary, SpringPage } from '@/types/post'

export async function fetchPosts(
  page: number,
  size: number,
  keyword?: string,
): Promise<SpringPage<PostSummary>> {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  if (keyword) params.set('keyword', keyword)
  const res = await fetchWithAuth(`/api/posts?${params}`, { headers: authBearerHeader() })
  if (!res.ok) throw new Error('게시글 목록을 불러오지 못했습니다.')
  return res.json()
}

export async function fetchPost(id: number): Promise<Post> {
  const res = await fetchWithAuth(`/api/posts/${id}`, { headers: authBearerHeader() })
  if (!res.ok) throw new Error('게시글을 불러오지 못했습니다.')
  return res.json()
}

export type CreatePostPayload = {
  title: string
  content: string
  isNotice: boolean
  isPinned: boolean
  noticeStartAt: string
  noticeEndAt: string
}

export async function createPost(payload: CreatePostPayload, files: File[]): Promise<Post> {
  const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null
  const formData = new FormData()
  formData.append('data', new Blob([JSON.stringify(payload)], { type: 'application/json' }))
  files.forEach((f) => formData.append('files', f))
  const res = await fetchWithAuth('/api/posts', {
    method: 'POST',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    body: formData,
  })
  if (!res.ok) {
    const data = await res.json().catch(() => ({}))
    throw new Error(data.message ?? '게시글 등록에 실패했습니다.')
  }
  return res.json()
}

export type UpdatePostPayload = { title: string; content: string }

export async function updatePost(id: number, payload: UpdatePostPayload): Promise<Post> {
  const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null
  const formData = new FormData()
  formData.append('data', new Blob([JSON.stringify(payload)], { type: 'application/json' }))
  const res = await fetchWithAuth(`/api/posts/${id}`, {
    method: 'PUT',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    body: formData,
  })
  if (!res.ok) {
    const data = await res.json().catch(() => ({}))
    throw new Error(data.message ?? '게시글 수정에 실패했습니다.')
  }
  return res.json()
}

export async function deletePost(id: number): Promise<void> {
  const res = await fetchWithAuth(`/api/posts/${id}`, {
    method: 'DELETE',
    headers: authBearerHeader(),
  })
  if (!res.ok) throw new Error('게시글 삭제에 실패했습니다.')
}

export type UpdateNoticePayload = {
  isNotice: boolean
  isPinned: boolean
  noticeStartAt: string
  noticeEndAt: string
}

export async function updateNoticeSettings(id: number, payload: UpdateNoticePayload): Promise<void> {
  const res = await fetchWithAuth(`/api/posts/${id}/notice`, {
    method: 'PATCH',
    headers: authHeaders(),
    body: JSON.stringify(payload),
  })
  if (!res.ok) throw new Error('공지 설정 변경에 실패했습니다.')
}

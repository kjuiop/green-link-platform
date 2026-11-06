export interface PostAttachment {
  id: number
  fileName: string
  url: string
}

export interface PostSummary {
  id: number
  authorId: number
  companyName: string
  title: string
  isNotice: boolean
  isPinned: boolean
  noticeEndAt: string | null
  viewCount: number
  hasAttachment: boolean
  createdAt: string
}

export interface Post {
  id: number
  authorId: number
  companyName: string
  title: string
  content: string
  isNotice: boolean
  isPinned: boolean
  noticeStartAt: string | null
  noticeEndAt: string | null
  viewCount: number
  createdAt: string
  attachments: PostAttachment[]
}

export interface SpringPage<T> {
  content: T[]
  number: number
  size: number
  totalElements: number
  totalPages: number
}

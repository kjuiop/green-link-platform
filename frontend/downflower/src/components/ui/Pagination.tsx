'use client'

import { ChevronLeft, ChevronRight } from 'lucide-react'
import { cn } from '@/lib/utils'

interface PaginationProps {
  page: number         // 0-indexed
  totalPages: number
  totalElements: number
  size: number
  onPageChange: (page: number) => void
  className?: string
}

export function Pagination({ page, totalPages, totalElements, size, onPageChange, className }: PaginationProps) {
  if (totalPages <= 1) return null

  const from = page * size + 1
  const to = Math.min((page + 1) * size, totalElements)

  const pages: (number | '...')[] = []
  if (totalPages <= 7) {
    for (let i = 0; i < totalPages; i++) pages.push(i)
  } else {
    pages.push(0)
    if (page > 2) pages.push('...')
    for (let i = Math.max(1, page - 1); i <= Math.min(totalPages - 2, page + 1); i++) pages.push(i)
    if (page < totalPages - 3) pages.push('...')
    pages.push(totalPages - 1)
  }

  return (
    <div className={cn('flex flex-col items-center gap-2 sm:flex-row sm:justify-between', className)}>
      <p className="text-xs text-gray-500">
        {totalElements.toLocaleString()}건 중 {from}–{to}
      </p>
      <div className="flex items-center gap-1">
        <button
          onClick={() => onPageChange(page - 1)}
          disabled={page === 0}
          className="rounded border p-1.5 text-gray-500 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-40"
        >
          <ChevronLeft className="h-4 w-4" />
        </button>
        {pages.map((p, i) =>
          p === '...' ? (
            <span key={`dots-${i}`} className="px-2 text-sm text-gray-400">…</span>
          ) : (
            <button
              key={p}
              onClick={() => onPageChange(p)}
              className={cn(
                'min-w-[32px] rounded border px-2.5 py-1 text-sm',
                p === page
                  ? 'border-green-700 bg-green-700 font-medium text-white'
                  : 'text-gray-600 hover:bg-gray-50',
              )}
            >
              {p + 1}
            </button>
          ),
        )}
        <button
          onClick={() => onPageChange(page + 1)}
          disabled={page >= totalPages - 1}
          className="rounded border p-1.5 text-gray-500 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-40"
        >
          <ChevronRight className="h-4 w-4" />
        </button>
      </div>
    </div>
  )
}

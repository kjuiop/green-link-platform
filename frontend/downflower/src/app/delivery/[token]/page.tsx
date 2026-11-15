'use client'

import { useEffect, useState } from 'react'
import { useParams } from 'next/navigation'
import Image from 'next/image'

type DeliveryShareData = {
  recipientName: string
  placeName: string
  completedAt: string | null
  photoUrls: string[]
}

export default function DeliverySharePage() {
  const { token } = useParams<{ token: string }>()
  const [data, setData] = useState<DeliveryShareData | null>(null)
  const [error, setError] = useState(false)

  useEffect(() => {
    fetch(`/api/public/delivery/${token}`)
      .then((r) => {
        if (!r.ok) throw new Error()
        return r.json()
      })
      .then(setData)
      .catch(() => setError(true))
  }, [token])

  if (error) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-gray-50 p-4">
        <p className="text-sm text-gray-400">유효하지 않은 링크입니다.</p>
      </main>
    )
  }

  if (!data) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-gray-50">
        <p className="text-sm text-gray-400">불러오는 중...</p>
      </main>
    )
  }

  return (
    <main className="min-h-screen bg-gray-50 p-4">
      <div className="mx-auto max-w-lg">
        <div className="mb-4">
          <h1 className="text-lg font-semibold text-gray-800">배송 완료</h1>
          <p className="mt-0.5 text-sm text-gray-500">
            {data.recipientName}님 · {data.placeName}
            {data.completedAt && (
              <span className="ml-1">· {data.completedAt.slice(0, 16).replace('T', ' ')}</span>
            )}
          </p>
        </div>

        {data.photoUrls.length === 0 ? (
          <p className="text-sm text-gray-400">등록된 사진이 없습니다.</p>
        ) : (
          <div className="grid grid-cols-2 gap-2">
            {data.photoUrls.map((url, i) => (
              <a key={i} href={url} target="_blank" rel="noreferrer" className="block overflow-hidden rounded-lg">
                <Image
                  src={url}
                  alt={`배송 사진 ${i + 1}`}
                  width={400}
                  height={400}
                  className="aspect-square w-full object-cover"
                  unoptimized
                />
              </a>
            ))}
          </div>
        )}
      </div>
    </main>
  )
}

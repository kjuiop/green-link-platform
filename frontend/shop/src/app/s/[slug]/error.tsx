'use client'

export default function ShopError({ retry }: { error: unknown; retry: () => void }) {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center gap-3 p-6 text-center">
      <h1 className="text-lg font-bold">일시적인 오류가 발생했습니다</h1>
      <p className="text-sm text-gray-500">잠시 후 다시 시도해 주세요.</p>
      <button type="button" onClick={() => retry()} className="rounded-md border bg-white px-4 py-2 text-sm">
        다시 시도
      </button>
    </main>
  )
}

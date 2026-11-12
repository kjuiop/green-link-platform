export function AccountInfoBanner() {
  return (
    <div className="flex items-center gap-4 rounded-lg border border-blue-100 bg-blue-50 px-5 py-3">
      <div className="text-xs font-semibold text-blue-700">본부계좌</div>
      <div className="h-4 w-px bg-blue-200" />
      <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-sm">
        <span className="font-medium text-gray-800">농협</span>
        <span className="font-mono font-semibold tracking-wider text-gray-900">317-0024-8488-71</span>
        <span className="text-gray-600">다운플라워(주)</span>
      </div>
    </div>
  )
}

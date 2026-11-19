'use client'

import { type CoupangScope } from './useCoupangScope'

export function CompanySelect({ scope }: { scope: CoupangScope }) {
  if (!scope.isSuperAdmin) return null
  return (
    <div className="flex items-center gap-2">
      <span className="text-sm text-gray-500">발주사</span>
      <select
        className="rounded-md border bg-white px-2 py-1.5 text-sm"
        value={scope.selectedId ?? ''}
        onChange={(e) => scope.setSelectedId(e.target.value ? Number(e.target.value) : null)}
      >
        {scope.companies.map((c) => (
          <option key={c.id} value={c.id}>{c.companyName}</option>
        ))}
      </select>
    </div>
  )
}

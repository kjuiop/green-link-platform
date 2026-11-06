'use client'

import { useState } from 'react'
import { X } from 'lucide-react'
import { SIDO_LIST } from '@/data/regions'

export interface RegionInput {
  sido: string
  sigungu: string
}

interface Props {
  selected: RegionInput[]
  onChange: (regions: RegionInput[]) => void
}

export default function RegionSelector({ selected, onChange }: Props) {
  const [sido, setSido] = useState('')
  const [sigungu, setSigungu] = useState('')

  const sigunguList = SIDO_LIST.find((s) => s.name === sido)?.sigunguList ?? []

  function add() {
    if (!sido || !sigungu) return
    const already = selected.some((r) => r.sido === sido && r.sigungu === sigungu)
    if (already) return
    onChange([...selected, { sido, sigungu }])
    setSigungu('')
  }

  function remove(index: number) {
    onChange(selected.filter((_, i) => i !== index))
  }

  return (
    <div className="space-y-2">
      <div className="flex gap-2">
        <select
          value={sido}
          onChange={(e) => { setSido(e.target.value); setSigungu('') }}
          className="flex-1 rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-600"
        >
          <option value="">시/도 선택</option>
          {SIDO_LIST.map((s) => (
            <option key={s.name} value={s.name}>{s.name}</option>
          ))}
        </select>

        <select
          value={sigungu}
          onChange={(e) => setSigungu(e.target.value)}
          disabled={!sido}
          className="flex-1 rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-600 disabled:opacity-50"
        >
          <option value="">시/군/구 선택</option>
          {sigunguList.map((sg) => (
            <option key={sg} value={sg}>{sg}</option>
          ))}
        </select>

        <button
          type="button"
          onClick={add}
          disabled={!sido || !sigungu}
          className="rounded-md bg-green-700 px-4 py-2 text-sm font-medium text-white hover:bg-green-800 disabled:opacity-40"
        >
          추가
        </button>
      </div>

      {selected.length > 0 && (
        <div className="flex flex-wrap gap-1.5">
          {selected.map((r, i) => (
            <span
              key={i}
              className="flex items-center gap-1 rounded-full bg-green-100 px-3 py-1 text-xs text-green-800"
            >
              {r.sido} {r.sigungu}
              <button type="button" onClick={() => remove(i)} className="ml-0.5 hover:text-red-500">
                <X size={12} />
              </button>
            </span>
          ))}
        </div>
      )}
    </div>
  )
}

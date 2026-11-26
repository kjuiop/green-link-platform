import type { Brand } from '@/types/brand'

const DEFAULT_SELECT_CLASS = 'h-8 rounded-md border border-gray-200 bg-white px-2 text-xs'

export function BrandPicker({ options, selected, disabled, onToggle, selectClassName = DEFAULT_SELECT_CLASS }: {
  options: Pick<Brand, 'id' | 'name'>[]
  selected: number[]
  disabled: boolean
  onToggle: (id: number) => void
  selectClassName?: string
}) {
  const remaining = options.filter((b) => !selected.includes(b.id))
  return (
    <div className="space-y-2">
      <select
        aria-label="브랜드 선택"
        className={selectClassName}
        value=""
        disabled={disabled || remaining.length === 0}
        onChange={(e) => { if (e.target.value) onToggle(Number(e.target.value)) }}
      >
        <option value="">{remaining.length === 0 ? '추가할 브랜드가 없습니다' : '브랜드 추가'}</option>
        {remaining.map((b) => (
          <option key={b.id} value={b.id}>{b.name}</option>
        ))}
      </select>
      {selected.length > 0 && (
        <div className="flex flex-wrap gap-1">
          {selected.map((id) => (
            <span key={id} className="flex items-center gap-1 rounded-full bg-green-100 px-2 py-0.5 text-xs text-green-800">
              {options.find((b) => b.id === id)?.name ?? `#${id}`}
              <button
                type="button"
                aria-label="브랜드 제거"
                className="text-green-700 hover:text-green-900 disabled:opacity-50"
                disabled={disabled}
                onClick={() => onToggle(id)}
              >
                ×
              </button>
            </span>
          ))}
        </div>
      )}
    </div>
  )
}

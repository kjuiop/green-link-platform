import Link from 'next/link'

interface Props {
  slug: string
  categories: { value: string; label: string }[]
  active: string | null
}

export function CategoryTabs({ slug, categories, active }: Props) {
  if (categories.length <= 1) return null
  const base = 'shrink-0 rounded-full border px-4 py-1.5 text-sm'
  return (
    <nav className="-mx-4 flex gap-2 overflow-x-auto px-4 pb-1" aria-label="카테고리">
      <Link
        href={`/s/${slug}`}
        className={`${base} ${active === null ? 'border-shop bg-shop text-white' : 'bg-white text-gray-600'}`}
      >
        전체
      </Link>
      {categories.map((c) => (
        <Link
          key={c.value}
          href={`/s/${slug}?category=${encodeURIComponent(c.value)}`}
          className={`${base} ${active === c.value ? 'border-shop bg-shop text-white' : 'bg-white text-gray-600'}`}
        >
          {c.label}
        </Link>
      ))}
    </nav>
  )
}

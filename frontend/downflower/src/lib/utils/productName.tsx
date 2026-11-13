import React from 'react'

export function highlightGrade(name: string): React.ReactNode {
  const parts = name.split(/(\(.+?\))/)
  if (parts.length === 1) return name
  return (
    <>
      {parts.map((part, i) =>
        /^\(.+\)$/.test(part) ? (
          <span key={i} className="font-bold text-red-500">{part}</span>
        ) : (
          part
        )
      )}
    </>
  )
}

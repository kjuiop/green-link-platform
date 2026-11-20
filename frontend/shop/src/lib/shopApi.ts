import 'server-only'
import { cache } from 'react'
import type { PublicProductView, PublicShopView } from './types'

const API = process.env.API_URL ?? 'http://localhost:8080'

async function get<T>(path: string): Promise<T | null> {
  const res = await fetch(`${API}${path}`, { next: { revalidate: 30 } })
  if (res.status === 404) return null
  if (!res.ok) throw new Error(`shop api ${res.status}`)
  return res.json()
}

export const getShop = cache((slug: string) =>
  get<PublicShopView>(`/public/shop/${encodeURIComponent(slug)}`),
)

export const getProduct = cache((slug: string, id: string) =>
  get<PublicProductView>(`/public/shop/${encodeURIComponent(slug)}/products/${encodeURIComponent(id)}`),
)

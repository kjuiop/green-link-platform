import { Product, CreateProductRequest, UpdateProductRequest } from '@/types/product'

const MOCK_PRODUCTS: Product[] = [
  { id: '1', name: '근조화환 3단', price: 150000, memo: '기본형', createdAt: '2026-09-01T00:00:00' },
  { id: '2', name: '근조화환 2단', price: 120000, createdAt: '2026-09-01T00:00:00' },
  { id: '3', name: '축하화환 대형', price: 180000, memo: '행사·개업용', createdAt: '2026-09-05T00:00:00' },
  { id: '4', name: '축하화환 중형', price: 130000, createdAt: '2026-09-05T00:00:00' },
  { id: '5', name: '졸업·입학 꽃다발', price: 50000, createdAt: '2026-09-10T00:00:00' },
]

let store = [...MOCK_PRODUCTS]
let nextId = store.length + 1

export async function fetchProducts(): Promise<Product[]> {
  return [...store]
}

export async function createProduct(req: CreateProductRequest): Promise<Product> {
  const product: Product = {
    id: String(nextId++),
    name: req.name,
    price: req.price,
    memo: req.memo,
    createdAt: new Date().toISOString(),
  }
  store = [product, ...store]
  return product
}

export async function updateProduct(id: string, req: UpdateProductRequest): Promise<Product> {
  const idx = store.findIndex((p) => p.id === id)
  if (idx === -1) throw new Error('상품을 찾을 수 없습니다.')
  store[idx] = { ...store[idx], ...req }
  return store[idx]
}

export async function deleteProduct(id: string): Promise<void> {
  store = store.filter((p) => p.id !== id)
}

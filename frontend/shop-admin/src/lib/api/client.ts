import { clearSession, getSession } from '@/lib/session'

export class ApiError extends Error {
  constructor(readonly status: number, readonly code: string, message: string) {
    super(message)
  }
}

const NETWORK_MESSAGE = '서버에 연결할 수 없습니다. 잠시 후 다시 시도하세요.'

interface Options {
  method?: 'GET' | 'POST' | 'PATCH' | 'PUT' | 'DELETE'
  body?: unknown
  auth?: boolean
}

export async function apiFetch<T>(path: string, { method = 'GET', body, auth = true }: Options = {}): Promise<T> {
  const headers: Record<string, string> = {}
  const isForm = body instanceof FormData
  if (body !== undefined && !isForm) headers['Content-Type'] = 'application/json'
  if (auth) {
    const token = getSession()?.accessToken
    if (token) headers.Authorization = `Bearer ${token}`
  }

  let res: Response
  try {
    res = await fetch(path, { method, headers, body: body === undefined ? undefined : isForm ? body : JSON.stringify(body) })
  } catch {
    throw new ApiError(503, 'NETWORK', NETWORK_MESSAGE)
  }

  if (!res.ok) {
    if (res.status === 401 && auth) {
      clearSession()
      window.location.replace('/login')
    }
    const err = (await res.json().catch(() => null)) as { code?: string; message?: string } | null
    if (res.status === 503 && !err?.code) throw new ApiError(503, 'NETWORK', NETWORK_MESSAGE)
    throw new ApiError(res.status, err?.code ?? 'UNKNOWN', err?.message ?? '요청을 처리하지 못했습니다.')
  }

  if (res.status === 204) return undefined as T
  return (await res.json()) as T
}

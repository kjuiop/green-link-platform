function getToken(): string | null {
  return typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null
}

export function authHeaders(): HeadersInit {
  const token = getToken()
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  }
}

export function authBearerHeader(): HeadersInit {
  const token = getToken()
  return token ? { Authorization: `Bearer ${token}` } : {}
}

function redirectToLogin(): void {
  if (typeof window !== 'undefined') {
    localStorage.removeItem('accessToken')
    window.location.href = '/login'
  }
}

export async function fetchWithAuth(url: string, options?: RequestInit): Promise<Response> {
  const res = await fetch(url, options)
  if (res.status === 401) {
    redirectToLogin()
    throw new Error('인증이 만료되었습니다.')
  }
  return res
}

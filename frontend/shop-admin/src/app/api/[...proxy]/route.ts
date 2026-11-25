import { NextRequest, NextResponse } from 'next/server'

const BACKEND_URL = process.env.API_URL ?? 'http://localhost:8082'

const SKIP_REQUEST_HEADERS = ['host', 'connection', 'origin', 'x-shop-client-ip', 'x-shop-proxy-secret']
// fetch가 본문을 이미 해제하므로 인코딩·길이 헤더를 그대로 넘기면 브라우저가 본문을 잘못 해석한다
const SKIP_RESPONSE_HEADERS = ['transfer-encoding', 'connection', 'content-encoding', 'content-length']

async function handler(
  request: NextRequest,
  { params }: { params: Promise<{ proxy: string[] }> },
): Promise<NextResponse> {
  const { proxy } = await params
  const url = `${BACKEND_URL}/api/${proxy.join('/')}${request.nextUrl.search}`

  const headers = new Headers()
  request.headers.forEach((value, key) => {
    if (!SKIP_REQUEST_HEADERS.includes(key.toLowerCase())) headers.set(key, value)
  })

  // 백엔드는 nginx가 덧붙인 마지막 hop(=Vercel 서버 IP)을 보게 되므로, 가입 레이트리밋용 실제 사용자 IP를 공유 시크릿과 함께 전달한다.
  const clientIp = request.headers.get('x-forwarded-for')?.split(',')[0]?.trim() || request.headers.get('x-real-ip') || ''
  if (clientIp) {
    headers.set('X-Shop-Client-Ip', clientIp)
    headers.set('X-Shop-Proxy-Secret', process.env.SHOP_PROXY_SECRET ?? '')
  }

  const body =
    request.method !== 'GET' && request.method !== 'HEAD'
      ? await request.arrayBuffer()
      : undefined

  let res: Response
  try {
    res = await fetch(url, { method: request.method, headers, body })
  } catch {
    return new NextResponse('Backend unreachable', { status: 503 })
  }

  const resHeaders = new Headers()
  res.headers.forEach((value, key) => {
    if (!SKIP_RESPONSE_HEADERS.includes(key.toLowerCase())) resHeaders.set(key, value)
  })

  return new NextResponse(res.body, { status: res.status, headers: resHeaders })
}

export const GET = handler
export const POST = handler
export const PUT = handler
export const PATCH = handler
export const DELETE = handler

import { NextRequest, NextResponse } from 'next/server'

const BACKEND_URL = process.env.API_URL ?? 'http://localhost:8082'

const SKIP_REQUEST_HEADERS = ['host', 'connection', 'origin']
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

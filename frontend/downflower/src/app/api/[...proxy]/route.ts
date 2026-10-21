import { NextRequest, NextResponse } from 'next/server'

const BACKEND_URL = process.env.API_URL ?? 'http://localhost:8080'

async function handler(
  request: NextRequest,
  { params }: { params: Promise<{ proxy: string[] }> }
): Promise<NextResponse> {
  const { proxy } = await params
  const path = proxy.join('/')
  const search = request.nextUrl.search
  const url = `${BACKEND_URL}/api/${path}${search}`

  const headers = new Headers()
  request.headers.forEach((value, key) => {
    if (!['host', 'connection'].includes(key.toLowerCase())) {
      headers.set(key, value)
    }
  })

  const body =
    request.method !== 'GET' && request.method !== 'HEAD'
      ? await request.arrayBuffer()
      : undefined

  let res: Response
  try {
    res = await fetch(url, {
      method: request.method,
      headers,
      body,
    })
  } catch {
    return new NextResponse('Backend unreachable', { status: 503 })
  }

  const resHeaders = new Headers()
  res.headers.forEach((value, key) => {
    if (!['transfer-encoding', 'connection'].includes(key.toLowerCase())) {
      resHeaders.set(key, value)
    }
  })

  return new NextResponse(res.body, {
    status: res.status,
    headers: resHeaders,
  })
}

export const GET = handler
export const POST = handler
export const PUT = handler
export const PATCH = handler
export const DELETE = handler

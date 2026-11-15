import { NextRequest, NextResponse } from 'next/server'

const BACKEND_URL = process.env.API_URL ?? 'http://localhost:8080'

async function handler(
  request: NextRequest,
  { params }: { params: Promise<{ proxy: string[] }> }
): Promise<NextResponse> {
  const { proxy } = await params
  const path = proxy.join('/')
  const search = request.nextUrl.search
  const url = `${BACKEND_URL}/public/${path}${search}`

  let res: Response
  try {
    res = await fetch(url, { method: request.method })
  } catch {
    return new NextResponse('Backend unreachable', { status: 503 })
  }

  const resHeaders = new Headers()
  res.headers.forEach((value, key) => {
    if (!['transfer-encoding', 'connection'].includes(key.toLowerCase())) {
      resHeaders.set(key, value)
    }
  })

  return new NextResponse(res.body, { status: res.status, headers: resHeaders })
}

export const GET = handler

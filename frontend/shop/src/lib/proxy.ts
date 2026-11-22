import 'server-only'

const API = process.env.API_URL ?? 'http://localhost:8080'
const MAX_BODY = 16 * 1024

// 백엔드는 nginx가 덧붙인 마지막 X-Forwarded-For hop(=Vercel 서버 IP)을 보게 되므로,
// 실제 고객 IP를 공유 시크릿과 함께 별도 헤더로 전달한다.
export async function forwardPost(req: Request, backendPath: string): Promise<Response> {
  const tooLarge = () => Response.json({ code: 'SHOP_011', message: '요청이 너무 큽니다.' }, { status: 413 })
  if (Number(req.headers.get('content-length') ?? 0) > MAX_BODY) return tooLarge()
  const body = await req.text()
  if (body.length > MAX_BODY) return tooLarge()
  const ip = req.headers.get('x-forwarded-for')?.split(',')[0]?.trim() || req.headers.get('x-real-ip') || ''
  let res: Response
  try {
    res = await fetch(`${API}${backendPath}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Shop-Client-Ip': ip,
        'X-Shop-Proxy-Secret': process.env.SHOP_PROXY_SECRET ?? '',
      },
      body,
      cache: 'no-store',
    })
  } catch {
    return Response.json({ message: '서버에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요.' }, { status: 502 })
  }
  return new Response(await res.text(), {
    status: res.status,
    headers: { 'Content-Type': 'application/json' },
  })
}

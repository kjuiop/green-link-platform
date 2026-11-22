import { forwardPost } from '@/lib/proxy'

export async function POST(req: Request, ctx: RouteContext<'/api/shop/[slug]/orders/lookup'>) {
  const { slug } = await ctx.params
  return forwardPost(req, `/public/shop/${encodeURIComponent(slug)}/orders/lookup`)
}

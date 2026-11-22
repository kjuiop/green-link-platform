// 쇼핑몰 주소 생성은 이 파일만 거친다. 서브도메인·고객 도메인 도입 시 여기만 바꾼다.
export function shopPath(slug: string, sub = ''): string {
  return `/s/${slug}${sub}`
}

export function shopUrl(slug: string, origin: string, sub = ''): string {
  return `${origin}${shopPath(slug, sub)}`
}

// 공개 쇼핑몰은 별도 앱(Vercel 프로젝트)이므로 환경변수가 우선이다. 미설정 시 현재 origin.
export function shopOrigin(): string {
  return (process.env.NEXT_PUBLIC_SHOP_ORIGIN ?? '').replace(/\/+$/, '') || window.location.origin
}

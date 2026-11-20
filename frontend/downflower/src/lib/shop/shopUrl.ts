// 쇼핑몰 주소 생성은 이 파일만 거친다. 서브도메인·고객 도메인 도입 시 여기만 바꾼다.
export function shopPath(slug: string, sub = ''): string {
  return `/s/${slug}${sub}`
}

export function shopUrl(slug: string, origin: string, sub = ''): string {
  return `${origin}${shopPath(slug, sub)}`
}

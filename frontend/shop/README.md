# shop — 고객용 공개 쇼핑몰

관리자 앱(`frontend/downflower`)과 별개의 Next 16 앱. 로그인 없이 `/s/{slug}` 에서 상품 조회 → 주문 → 주문번호 → 조회까지 진행한다.
Vercel 별도 프로젝트로 배포한다 (Root Directory = `frontend/shop`).

## 구조
- 읽기(GET): 서버 컴포넌트가 `API_URL` 로 백엔드 `/public/shop/{slug}` 를 직접 호출 (revalidate 30초). 비공개/없는 쇼핑몰은 404.
- 쓰기(주문·조회 POST): `/api/shop/[slug]/orders[/lookup]` Route Handler 가 백엔드로 중계한다.
  백엔드 레이트리밋이 실제 고객 IP 를 쓰도록 `X-Shop-Client-Ip` + `X-Shop-Proxy-Secret` 헤더를 붙인다.
- 브라우저 번들에는 백엔드 주소·시크릿이 들어가지 않는다 (모두 서버 전용 env).

## 환경변수
| 이름 | 설명 |
|---|---|
| `API_URL` | 백엔드 주소. 운영 `https://api.onurim.com`, 개발 `https://dev-api.onurim.com` |
| `SHOP_PROXY_SECRET` | 백엔드와 **동일한 값**. `openssl rand -hex 32` 로 생성 |

백엔드(dev/prod 서버 `.env`)에도 `SHOP_PROXY_SECRET` 을 같은 값으로 추가하고 재기동해야 한다.
미설정이면 동작은 하지만 IP 레이트리밋이 Vercel 서버 IP 기준이 되어 의미가 없어진다.

관리자 앱(downflower) Vercel 프로젝트에는 `NEXT_PUBLIC_SHOP_ORIGIN` 을 추가한다 (빌드 타임 인라인 → 재배포 필요).
운영 `https://shop.onurim.com`, 개발 `https://dev-shop.onurim.com`.

## Vercel 설정
1. 같은 GitHub 레포를 import → **Root Directory `frontend/shop`**, Framework Next.js
2. Production Branch `main`, `develop` 은 Preview (`vercel.json` 의 ignoreCommand 로 main/develop 만 빌드)
3. Environment Variables 에 위 값을 Production / Preview 별로 입력 (Preview 는 dev 백엔드 값)
4. Domains: `shop.onurim.com` (Production), `dev-shop.onurim.com` (Preview, Git Branch `develop`)

## Route 53
Vercel 이 Domains 화면에 안내하는 값으로 CNAME 레코드를 만든다.
- `shop` → (Vercel 안내 CNAME 값)
- `dev-shop` → (Vercel 안내 CNAME 값)

## 이후 단계 (미구현)
- 2단계: `shop.onurim.com` NS 를 Vercel 로 위임 → `*.shop.onurim.com` (proxy.ts 에서 host → `/s/{slug}` rewrite)
- 3단계: 고객 커스텀 도메인 (Vercel Domains API + `shops.custom_domain`)

# shop-admin — 쇼핑몰 백오피스

온우림 관리자 앱(`frontend/downflower`), 고객용 쇼핑몰(`frontend/shop`)과 별개의 Next 16 앱.
쇼핑몰 관리자가 상품·주문·쇼핑몰 설정을 관리하는 백오피스이며, Vercel 별도 프로젝트로 배포한다 (Root Directory = `frontend/shop-admin`).

백엔드는 `green-shop-api`(기본 `http://localhost:8082`)와 연동한다. 로그인·연동 신청·관리자 관리(신청 승인)·메뉴는 실 API이고, 대시보드 수치만 실 API가 없어 정적 더미(`src/lib/api/dashboardApi.ts`)를 사용한다.

## 개발
```bash
npm install
npm run dev      # http://localhost:3002
npm run lint
npm run build
```

## 환경변수
| 이름 | 기본값 | 설명 |
|---|---|---|
| `API_URL` | `http://localhost:8082` | shop-api 주소. `src/app/api/[...proxy]/route.ts` 프록시가 `/api/*` 요청을 이 주소로 전달한다 (서버 측에서만 사용, CORS 불필요). |

## 인증
- 로그인은 온우림 계정으로 하며 shop-api가 JWT(30분)를 발급한다. 세션은 `localStorage`(`shopAdminSession`)에 저장된다.
- 연동 승인 전(LIMITED 토큰)에는 `/api/me`, `/api/access-requests`, `/api/auth/refresh`만 호출할 수 있다.
- `/access` 화면에서 승인이 확인되면 `/api/auth/refresh`로 FULL 토큰을 다시 받는다.
- `src/lib/useTokenRefresh.ts` 가 10분마다(탭 복귀 시 포함) 토큰을 갱신한다. 401 응답 시 세션을 지우고 `/login`으로 이동한다.
- 서버 연결 불가(503)는 세션을 유지한 채 오류 메시지만 표시한다.

## 구조
- `src/lib/api/client.ts` : `apiFetch` 공통 클라이언트 (Bearer 토큰, 오류 `{code,message}` → `ApiError`, 401 처리).
- `src/lib/api/*` : 화면이 호출하는 API 계층.
- `src/lib/session.ts` : 세션 저장소.
- `src/app/(auth)` 로그인 / `src/app/(gate)` 미승인 안내 / `src/app/(admin)` 가드된 영역 (미구현 메뉴는 `[...slug]`가 "준비 중" 표시).

인증 가드는 화면 편의용이다. 실제 접근 통제는 shop-api가 한다.

## Vercel 설정
1. 같은 GitHub 레포를 import → **Root Directory `frontend/shop-admin`**, Framework Next.js
2. Production Branch `main`, `develop`은 Preview (`vercel.json`의 ignoreCommand로 main/develop만 빌드)
3. 환경변수: `API_URL` (shop-api 공개 주소)

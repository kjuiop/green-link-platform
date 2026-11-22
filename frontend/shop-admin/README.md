# shop-admin — 쇼핑몰 백오피스

온우림 관리자 앱(`frontend/downflower`), 고객용 쇼핑몰(`frontend/shop`)과 별개의 Next 16 앱.
쇼핑몰 관리자가 상품·주문·쇼핑몰 설정을 관리하는 백오피스이며, Vercel 별도 프로젝트로 배포한다 (Root Directory = `frontend/shop-admin`).

현재는 **Mock 데이터** 단계다. 로그인·대시보드만 구현되어 있고 백엔드(shop-api)와는 연결되지 않는다.

## 개발
```bash
npm install
npm run dev      # http://localhost:3002
npm run lint
npm run build
```

## Mock 로그인
비밀번호는 아무 값이나 입력하면 된다.

| 아이디 | 의미 | 결과 |
|---|---|---|
| `admin` | 최고관리자 (SUPER_ADMIN) | 대시보드 (전체 범위), `설정` 메뉴 노출 |
| `shop` | 쇼핑몰 관리자 (승인됨) | 대시보드 (회사 범위), `설정` 메뉴 없음 |
| `pending` | 연동 승인 대기 | `/access` 안내 |
| `new` | 권한 없음 (신청 전) | `/access` 안내 |

그 외 아이디는 로그인 실패. 세션은 `localStorage`(`shopAdminSession`)에만 저장된다.

## 구조
- `src/lib/api/*` : 화면이 호출하는 API 계층. 실 연동 시 **이 파일들 내부만** `fetch`로 교체한다.
- `src/lib/mock/*` : Mock 구현. 실 연동 후 삭제한다. 화면에서 직접 import 하지 않는다.
- `src/lib/session.ts` : 세션 저장소. 실 연동 시 토큰 보관 방식 변경 지점.
- `src/components/auth/MockHint.tsx` : 로그인 화면의 Mock 계정 안내. 실 연동 시 삭제.
- `src/app/(auth)` 로그인 / `src/app/(gate)` 미승인 안내 / `src/app/(admin)` 가드된 영역 (미구현 메뉴는 `[...slug]`가 "준비 중" 표시).

인증 가드는 화면 편의용이다. 실제 접근 통제는 shop-api가 한다.

## Vercel 설정
1. 같은 GitHub 레포를 import → **Root Directory `frontend/shop-admin`**, Framework Next.js
2. Production Branch `main`, `develop`은 Preview (`vercel.json`의 ignoreCommand로 main/develop만 빌드)
3. 환경변수: 현재 없음 (실 API 연동 시 추가)

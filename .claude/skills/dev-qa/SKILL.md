---
name: dev-qa
description: 개발 서버(dev.onurim.com)에서 Playwright로 PR/변경 내용을 직접 검증합니다. "/dev-qa", "dev에서 QA", "playwright로 테스트", "브라우저로 확인", "QA 해줘", "실제 동작 확인"을 요청하면 반드시 이 스킬을 사용합니다. 사용자가 명시적으로 요청할 때만 실행합니다.
---

# /dev-qa 스킬

변경 내용을 분석하고, **개발 서버(dev)** 에서 개발 테스트 계정으로 Playwright 브라우저 동작을 검증한다.
로컬 서버는 띄우지 않는다.

| 항목 | 값 |
|---|---|
| 프론트 | https://dev.onurim.com |
| API | https://dev-api.onurim.com |

---

## Step 1 — 변경 정보 수집

```bash
gh pr view --json number,title,body,files,headRefName
gh pr diff --stat
```

- PR 번호, 제목, 설명, 변경 파일 목록을 파악한다
- 열린 PR이 없으면 현재 브랜치의 커밋 기준(`git diff origin/develop...HEAD --stat`)으로 진행한다

---

## Step 2 — dev 서버 상태 및 배포 여부 확인

```bash
curl -s -o /dev/null -w "%{http_code}" https://dev.onurim.com
```

- 프론트가 200/3xx로 응답하는지만 확인한다. dev API의 `/actuator/health`는 외부에서 403으로 막혀 있어 헬스체크로 쓰지 않는다. API 장애는 로그인 시도 시 네트워크 에러로 판단한다.
- 프론트 응답이 없으면 안내 후 중단한다 (로컬 서버로 대체하지 않는다)
- **이 변경 코드가 dev에 배포되어 있는지 사용자에게 확인한다.** 미배포 상태에서 테스트하면 이전 버전을 검증하게 되므로, 배포 전이면 안내 후 중단한다.

---

## Step 3 — 변경 내용 기반 테스트 시나리오 결정

변경된 파일 경로를 분석해 테스트할 시나리오를 결정한다:

| 변경 파일 패턴 | 테스트 시나리오 | 경로 |
|---|---|---|
| `OrderController`, `orders/` | 발주 목록, 주문 상세, 신규 발주 폼 | `/orders`, `/orders/new` |
| 수주 목록 | 수주사 로그인 후 수주 목록 조회 | `/orders/received` |
| `VendorController`, `vendors/` | 업체계정관리 목록, 상세 | `/vendors` |
| `AuthController`, `login/` | 로그인 (관리자, 발주사, 수주사) | `/login` |
| `PhotoController`, `photos/` | 사진 업로드, 사진 선택 | `/photos`, `/delivery` |
| `MenuController`, `menu/` | 사이드바 메뉴 표시 | 로그인 후 사이드바 |
| 정산 관련 | 정산 내역 조회 | `/settlement` |
| `coupang/` | 쿠팡 설정·주문 조회 화면 | `/coupang/settings`, `/coupang/orders` |
| 변경 없거나 미판별 | 기본 smoke test (로그인 + 대시보드) | `/dashboard` |

PR 제목·설명도 참고해 시나리오를 추가로 판단한다.

---

## Step 4 — Playwright 테스트 실행

### 테스트 계정 (개발 서버 전용)

| 역할 | 계정 |
|---|---|
| 슈퍼관리자 | `admin` |
| 발주업체 | `testorder01` |
| 수주업체 | `testvendor01` |
| 수발주(둘 다) | `testboth01` |

- 비밀번호는 이 파일에 적지 않는다. `.env.dev`(`ADMIN_INIT_PASSWORD` 등) 또는 메모리(`reference_dev_server_account`)를 참고한다.
- 시나리오에 맞는 역할의 계정으로 로그인한다. 매번 사용자에게 묻지 않는다.
- 신규 가입 SMS 인증번호가 필요한 경우에만 사용자에게 받는다.

### 공통 준비

```
browser_navigate → https://dev.onurim.com/login
browser_snapshot  (현재 상태 확인)
계정 ID/PW 입력 → 로그인 버튼 클릭
→ 대시보드 진입 확인
browser_take_screenshot (로그인 후 상태)
```

### 시나리오별 테스트

각 시나리오마다:
1. 해당 페이지로 이동
2. `browser_snapshot`으로 DOM 상태 확인
3. 핵심 동작 수행 (클릭, 입력, 제출)
4. 결과 확인 (데이터 표시, 에러 없음, 상태 변화)
5. `browser_console_messages`, `browser_network_requests`로 콘솔 에러·4xx/5xx 응답 확인
6. `browser_take_screenshot`으로 증적 저장

#### 수주사 수주목록 시나리오
```
수주사 계정(testvendor01)으로 로그인
→ /orders/received 이동
→ 주문 목록이 표시되는지 확인 (빈 목록이면 데이터 유무 먼저 확인)
→ 본인 업체 주문만 나오는지 확인
```

#### 업체계정관리 시나리오
```
슈퍼관리자(admin)로 로그인
→ /vendors 이동
→ 목록 표시 및 정렬 확인 (browser_evaluate로 날짜 순서 검증)
```

#### 발주 폼 시나리오
```
발주사(testorder01) 또는 admin으로 로그인
→ /orders/new 이동
→ 필수값 검증 / 저장 동작 확인
```

#### Smoke Test (기본)
```
로그인 → 대시보드 진입 확인
→ 주문 목록 페이지 이동
→ 데이터 로딩 확인 (스피너 해제, 목록 표시)
→ 콘솔 에러 확인: browser_console_messages
```

---

## Step 5 — 결과 리포트 출력

각 시나리오 결과를 다음 형식으로 출력한다:

```
## QA 결과 — {PR #번호 / 브랜치}: {제목}

| 시나리오 | 결과 | 비고 |
|---|---|---|
| 수주사 수주목록 표시 | ✅ PASS | 3건 정상 조회 |
| 업체계정관리 목록 | ❌ FAIL | 정렬 미적용 |
| Smoke: 대시보드 | ✅ PASS | |

실패 항목: 1건
```

- FAIL 항목이 있으면 스크린샷과 함께 원인을 간략히 설명한다
- 콘솔 에러·실패한 네트워크 요청이 있으면 목록으로 출력한다
- 테스트 중 생성한 데이터(주문, 사진 등)가 있으면 목록으로 안내한다
- 모두 PASS면 → "✅ QA 통과" 출력

---

## 주의사항

- **운영(www.onurim.com, 운영 API)에서는 절대 실행하지 않는다.** 테스트 계정도 dev 전용이다.
- 로컬 서버(`make start`, `localhost`)는 사용하지 않는다.
- **MMS(알리고)**: 배송완료 처리 시 실제 발송이 일어나므로 발송 대상을 해제한 상태에서만 진행한다.
- **S3 사진 업로드·배송완료**: dev에서는 허용한다.
- 카카오 알림톡 등 그 외 외부 연동은 QA 대상에서 제외한다.
- 비밀번호 등 계정 비밀 정보를 코드·문서에 하드코딩하지 않는다.
- 브라우저는 테스트 완료 후 `browser_close`로 닫는다.

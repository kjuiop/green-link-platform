---
name: pr-qa
description: 현재 브랜치의 PR 변경 내용을 Playwright로 로컬 브라우저에서 직접 검증합니다. "/pr-qa", "playwright로 테스트", "브라우저로 확인", "QA 해줘", "실제 동작 확인"을 요청하면 반드시 이 스킬을 사용합니다.
---

# /pr-qa 스킬

PR의 변경 내용을 분석하고, 로컬 서버에서 Playwright로 실제 브라우저 동작을 검증한다.

---

## Step 1 — PR 정보 수집

```bash
gh pr view --json number,title,body,files,headRefName
gh pr diff --stat
```

- PR 번호, 제목, 설명, 변경 파일 목록을 파악한다
- 열린 PR이 없으면 안내 메시지 출력 후 중단

---

## Step 2 — 로컬 서버 상태 확인

다음 명령어로 서버가 실행 중인지 확인한다:

```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health 2>/dev/null
curl -s -o /dev/null -w "%{http_code}" http://localhost:3000 2>/dev/null
```

- **두 서버 모두 응답 없음** → `make start` 실행 후 최대 60초 대기
  - 백엔드: `/actuator/health` → `{"status":"UP"}` 확인
  - 프론트: HTTP 200 확인
- **백엔드만 없음** → `make back` 백그라운드 실행
- **프론트만 없음** → `make front` 백그라운드 실행
- 서버 시작 실패 시 로그 위치(`/tmp/glp/backend.log`, `/tmp/glp/frontend.log`) 안내 후 중단

---

## Step 3 — 변경 내용 기반 테스트 시나리오 결정

변경된 파일 경로를 분석해 테스트할 시나리오를 결정한다:

| 변경 파일 패턴 | 테스트 시나리오 |
|---|---|
| `OrderController`, `orders/` | 발주 목록, 주문 상세, 신규 발주 폼 |
| `VendorController`, `vendors/` | 업체계정관리 목록, 상세 |
| `AuthController`, `login/` | 로그인 (관리자, 수주사) |
| `PhotoController`, `photos/` | 사진 업로드, 사진 선택 |
| `MenuController`, `menu/` | 사이드바 메뉴 표시 |
| `OrderController.getAll` / 수주목록 | 수주사 로그인 후 수주목록 조회 |
| `CreateOrderRequest` / `orders/new` | 발주 폼 필수값 검증 |
| 변경 없거나 미판별 | 기본 smoke test (로그인 + 대시보드) |

PR 제목·설명도 참고해 시나리오를 추가로 판단한다.

---

## Step 4 — Playwright 테스트 실행

### 공통 준비

브라우저를 열고 앱에 접속한다:

```
browser_navigate → http://localhost:3000
browser_snapshot  (현재 상태 확인)
```

### 관리자 로그인

```
browser_navigate → http://localhost:3000/login
로그인 ID/PW 입력 (환경변수 ADMIN_INIT_LOGIN_ID, ADMIN_INIT_PASSWORD 또는 사용자에게 확인)
로그인 버튼 클릭
→ 대시보드 진입 확인
browser_take_screenshot (로그인 후 상태)
```

### 시나리오별 테스트

각 시나리오마다:
1. 해당 페이지로 이동
2. `browser_snapshot`으로 DOM 상태 확인
3. 핵심 동작 수행 (클릭, 입력, 제출)
4. 결과 확인 (데이터 표시, 에러 없음, 상태 변화)
5. `browser_take_screenshot` — 스크린샷을 `/tmp/glp/qa/` 에 저장

#### 수주사 수주목록 시나리오 (Bug 1)
```
수주사 계정으로 로그인
→ /vendor/orders 페이지 이동
→ 주문 목록이 표시되는지 확인 (빈 목록이면 FAIL)
→ 본인 업체 주문만 나오는지 확인
```

#### 업체계정관리 최신순 시나리오 (Bug 2)
```
관리자로 로그인
→ /admin/vendors 페이지 이동
→ 목록 첫 번째 항목의 createdAt이 최신인지 확인
→ browser_evaluate로 날짜 순서 검증
```

#### 발주 폼 주문자 필수값 시나리오 (Bug 3)
```
관리자로 로그인
→ /admin/orders/new 이동
→ 주문자 전화번호 필드 비워두고 저장 시도
→ 에러 없이 저장되면 PASS, 에러 메시지 뜨면 FAIL
```

#### Smoke Test (기본)
```
관리자 대시보드 진입 확인
→ 주문 목록 페이지 이동
→ 데이터 로딩 확인 (스피너 해제, 테이블 표시)
→ 콘솔 에러 확인: browser_console_messages
```

---

## Step 5 — 결과 리포트 출력

각 시나리오 결과를 다음 형식으로 출력한다:

```
## QA 결과 — PR #{번호}: {제목}

| 시나리오 | 결과 | 비고 |
|---|---|---|
| 수주사 수주목록 표시 | ✅ PASS | 3건 정상 조회 |
| 업체계정관리 최신순 | ❌ FAIL | 정렬 미적용, 스크린샷: /tmp/glp/qa/vendors.png |
| 발주 폼 주문자 필수 해제 | ✅ PASS | 빈 값으로 저장 성공 |
| Smoke: 대시보드 | ✅ PASS | |

실패 항목: 1건
스크린샷: /tmp/glp/qa/
```

- FAIL 항목이 있으면 스크린샷 경로와 함께 원인을 간략히 설명한다
- 콘솔 에러가 있으면 목록으로 출력한다
- 모두 PASS면 → "✅ QA 통과 — pr-iterate 진행 가능" 출력

---

## 주의사항

- 테스트 계정 정보를 코드에 하드코딩하지 않는다 — 실행 시점에 사용자에게 확인하거나 환경변수에서 읽는다
- 서버 로그에 FATAL/ERROR가 쌓이는지 확인한다 (`tail /tmp/glp/backend.log | grep -E 'ERROR|FATAL'`)
- 테스트 중 생성한 데이터(주문, 사진 등)는 테스트 완료 후 안내한다 — 운영 데이터를 오염시킬 수 있음
- 브라우저는 테스트 완료 후 `browser_close`로 닫는다

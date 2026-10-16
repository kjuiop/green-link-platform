# green-link-platform

> 발주자와 공급자를 연결하는 발주 중개 SaaS 플랫폼

---

## ⚠️ 필수 작업 규칙 (예외 없음)

### 구현 전 승인 절차
어떤 요청이든 아래 순서를 반드시 지킨다. 프로덕션 에러, 단순 수정, 긴박한 상황도 예외 없음.

1. **원인 분석 + 해결 방향 제안** → 텍스트 답변만
2. **사용자 승인 대기**
3. 승인 후에만: 이슈 생성 → `feature/{이슈번호}` 브랜치 생성 → 구현

승인 없이 코드 수정·커밋·이슈 생성·브랜치 생성 모두 금지.

### 브랜치 규칙
- 브랜치명: `feature/{이슈번호}` — 이슈를 먼저 만들고, 그 번호로 브랜치 생성
- 다른 숫자·문자열 혼용 금지

---

## 프로젝트 개요

발주자(클라이언트)와 공급자(벤더)를 연결하는 B2B 발주 중개 플랫폼.
발주 등록, 견적 제출, 계약 체결, 정산까지 원스톱으로 처리한다.

---

## 기술 스택

| 레이어 | 기술 |
|---|---|
| 백엔드 | Java 25 + Spring Boot |
| DB | MySQL |
| 빌드 | Gradle |
| 프론트 | TBD |

---

## 코드 컨벤션

- 언어: 한국어로 소통, 코드는 영어
- 브랜치 전략: `main` (배포) / `develop` (개발) / `feature/*` (기능)
- 커밋 메시지: `[#이슈번호] feat:`, `fix:`, `refactor:`, `docs:` prefix 사용

## 브랜치 보호 규칙

**`main`과 `develop`에는 직접 커밋·push 금지.**
모든 변경은 반드시 `feature/*` 브랜치에서 PR을 통해 머지한다.

- `main` → 직접 커밋/push 절대 금지
- `develop` → 직접 커밋/push 절대 금지
- 작업은 항상 `feature/{이슈번호}` 브랜치에서 시작
- 완료 후 PR 생성 → develop 머지
- 배포 시 `/deploy` 스킬로 develop → main PR 생성

---

## 디렉토리 구조 (예정)

```
green-link-platform/
├── backend/         # Java 25 + Spring Boot
│   ├── src/
│   └── build.gradle
├── frontend/        # TBD
└── docs/            # 설계 문서
    └── claude/      # Claude 생성 문서 (plan, question)
```

---

## Available Skills

스킬은 `.claude/skills/` 디렉터리에 정의되어 있으며, Claude Code 대화에서 `/스킬명`으로 호출합니다.

| 스킬 | 용도 |
|------|------|
| `plan` | 이슈 분석 → 구현 계획 문서 생성 |
| `issue-create` | 변경사항 분석 → GitHub 이슈 생성 |
| `pr-create` | 현재 브랜치 커밋 분석 → PR 생성 |
| `pr-review` | PR 코드 리뷰 → 코멘트 등록 |
| `pr-apply` | PR 리뷰 코멘트 → 코드 반영 |
| `pr-iterate` | pr-review + pr-apply를 LGTM이 나올 때까지 자동 반복 |
| `question` | 질문 답변 → docs/claude/question/ 저장 |
| `deploy` | develop → main 배포 PR 생성 (날짜 제목 + deploy 템플릿) |

## Available Agents

에이전트는 `.claude/agents/` 디렉터리에 정의되어 있으며, 작업 유형에 따라 자동으로 선택됩니다.

| 에이전트 | 용도 |
|----------|------|
| `backend-developer` | Java/Spring Boot API 구현, 도메인 모델 설계 |
| `product-manager` | 기능 요구사항 분석, GitHub 이슈 작성, 도메인 설계 문서 작성 |
| `reviewer` | PR 리뷰, 코드 품질 검토, 아키텍처 피드백 |

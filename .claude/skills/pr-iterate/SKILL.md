---
name: pr-iterate
description: PR 리뷰 → 코드 반영을 LGTM이 나올 때까지 자동으로 반복합니다. "/pr-iterate", "LGTM 나올 때까지 리뷰해줘", "리뷰 루프", "자동으로 PR 완성", "pr-review랑 pr-apply 반복"처럼 리뷰와 수정을 반복 실행하고 싶을 때 반드시 이 스킬을 사용합니다.
---

현재 브랜치의 열린 PR을 대상으로 **코드 리뷰(이슈 발견 → 코멘트 등록)** 와 **코드 수정(반영 → 커밋 → 푸시 → 답댓글)** 을 LGTM이 나올 때까지 자동으로 반복합니다.

## 전체 흐름

```
[REVIEW] → 새 이슈 있음 → [APPLY] → [REVIEW] → 새 이슈 있음 → [APPLY] → ... → LGTM ✅
```

각 이터레이션은 REVIEW → APPLY 한 쌍입니다.  
**REVIEW 단계에서 `pr-review`가 LGTM 코멘트를 PR에 등록하면 루프를 종료합니다.**  
LGTM 판단과 PR 코멘트 등록은 `pr-review` 스킬이 담당합니다. `pr-iterate`는 결과만 감지해 루프를 멈춥니다.

최대 5 이터레이션까지 시도하고, 그래도 끝나지 않으면 사용자에게 알립니다.

---

## PHASE 1 — REVIEW (이슈 발견 & 라인 코멘트 등록)

### 1-1. PR 기본 정보 수집

```bash
gh pr view --json number,title,body,url
gh repo view --json owner,name
gh pr view --json commits --jq '.commits[-1].oid'
gh pr diff
```

### 1-2. 기존 코멘트 목록 수집 (중복 방지용)

```bash
gh api repos/{owner}/{repo}/pulls/{pr_number}/comments \
  --jq '[.[] | {id: .id, in_reply_to_id: .in_reply_to_id, path: .path, line: .line, body: .body}]'
```

- `in_reply_to_id`가 **없는** 것 = 원본 리뷰 코멘트
- 원본 코멘트에 이미 답댓글이 달린 경우 = 이전 이터레이션에서 처리 완료된 것
- **기존 코멘트와 동일하거나 유사한 이슈는 이번 라운드에서 등록하지 않습니다**

### 1-3. diff 코드 리뷰

diff와 관련 파일을 읽고 아래 관점에서 새 이슈를 찾습니다:

- **도메인 설계**: 엔티티/서비스 책임 분리, 발주 상태 전이 규칙
- **에러 처리**: 예외 누락, NPE/null 위험
- **성능**: N+1 조회, API 중복 호출, blocking 호출
- **보안**: API 키 하드코딩, 인증 누락, SQL Injection
- **코드 컨벤션**: 네이밍, 패키지 구조

### 1-4. 새 이슈를 라인 코멘트로 등록

이미 등록된 코멘트와 겹치지 않는 새 이슈만 등록합니다:

```bash
gh api repos/{owner}/{repo}/pulls/{pr_number}/comments \
  --method POST \
  --field body="🤖 Claude Review

{코멘트 내용}" \
  --field commit_id="{최신 SHA}" \
  --field path="{파일 경로}" \
  --field line={라인 번호} \
  --field side="RIGHT"
```

지적할 특정 라인이 없는 종합 의견은:

```bash
gh api repos/{owner}/{repo}/issues/{pr_number}/comments \
  --method POST \
  --field body="🤖 Claude Review

{내용}"
```

### 1-5. 이터레이션 계속 여부 결정

- **새 이슈 코멘트가 0개** → PHASE 3 (종료) 로 이동  
- **새 이슈 코멘트가 1개 이상** → PHASE 2 (APPLY) 로 이동

---

## PHASE 2 — APPLY (코드 반영 → 커밋 → 푸시 → 답댓글)

### 2-1. 미처리 코멘트 수집

`in_reply_to_id`가 없는 원본 코멘트 중 아직 답댓글이 없는 것 = 미처리 코멘트

### 2-2. 타당성 검토 후 코드 수정

각 미처리 코멘트에 대해:
1. 지적이 실제 코드에서 확인되는지 검토
2. 타당하면 Edit 툴로 파일 수정
3. 타당하지 않으면 코드는 그대로 두고 답댓글에 이유를 설명

### 2-3. 커밋 & 푸시

```bash
git add {변경된 파일들}
git commit -m "[#이슈번호] fix: PR 리뷰 반영 — {변경 요약}"
git push origin {현재 브랜치}
```

### 2-4. 각 코멘트에 답댓글 등록

```bash
gh api repos/{owner}/{repo}/pulls/{pr_number}/comments \
  --method POST \
  --field body="{short_sha} 에서 반영완료" \
  --field in_reply_to={comment_id}
```

### 2-5. 다음 이터레이션

PHASE 1 (REVIEW) 로 돌아갑니다.

---

## PHASE 3 — 종료

LGTM 확인. 최종 요약을 출력합니다:
- 총 이터레이션 횟수
- 이터레이션별 발견된 이슈 수 / 반영된 커밋 SHA

---

## 주의사항

- 열린 PR이 없으면 안내 후 중단
- **무한 루프 방지**: 5 이터레이션을 초과하면 중단하고 남은 이슈 목록을 사용자에게 보고합니다
- 사용자 확인 없이 코드 수정·커밋·푸시를 자동으로 진행합니다 — 이 스킬을 호출한 것 자체가 자동 실행 승인입니다
- `gh pr comment`는 사용하지 않습니다 — 답댓글은 반드시 `in_reply_to`로 해당 스레드에 등록합니다

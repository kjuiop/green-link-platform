---
name: pr-review
description: 현재 브랜치의 열린 PR을 리뷰하고 코멘트를 등록합니다
---

현재 브랜치의 열린 PR을 찾아 코드 리뷰를 작성합니다.

다음 순서로 진행하세요:

1. 현재 브랜치의 PR 번호를 확인합니다:
   ```
   gh pr view --json number,title,body,url
   ```

2. PR의 변경 diff를 가져옵니다:
   ```
   gh pr diff
   ```

3. 변경된 파일 목록을 확인합니다:
   ```
   gh pr view --json files
   ```

4. 아래 관점에서 리뷰를 수행합니다:
   - **도메인 설계**: 엔티티·서비스 책임 분리, 발주 상태 전이 규칙 준수
   - **에러 처리**: 예외 누락, 글로벌 핸들러 미처리 케이스
   - **성능**: N+1 조회, 외부 API 중복 호출, blocking 호출
   - **보안**: API 키 하드코딩, 인증 누락, SQL Injection
   - **테스트**: 누락된 테스트 케이스, 경계값 검증
   - **코드 컨벤션**: 네이밍 규칙, 패키지 구조

5. PR에 이미 달린 **기존 코멘트**를 가져와 중복을 방지합니다:
   ```
   gh api repos/{owner}/{repo}/pulls/{pr_number}/comments --jq '[.[] | {path: .path, line: .line, body: .body}]'
   gh api repos/{owner}/{repo}/issues/{pr_number}/comments --jq '[.[] | {body: .body}]'
   ```
   - 기존 코멘트와 **동일하거나 유사한 지적**은 등록하지 않습니다

6. 리뷰 코멘트를 **라인별로** PR에 등록합니다:
   - 전체 요약 댓글 하나로 올리지 않고, 지적 사항마다 해당 파일·라인에 직접 코멘트를 답니다
   - PR의 최신 커밋 SHA를 먼저 확인합니다:
     ```
     gh pr view --json commits --jq '.commits[-1].oid'
     ```
   - 라인 코멘트는 GitHub REST API로 등록합니다:
     ```
     gh api repos/{owner}/{repo}/pulls/{pr_number}/comments \
       --method POST \
       --field body="🤖 Claude Review

코멘트 내용" \
       --field commit_id="<커밋 SHA>" \
       --field path="파일 경로" \
       --field line=<라인 번호> \
       --field side="RIGHT"
     ```
   - `owner`와 `repo`는 `gh repo view --json owner,name`으로 확인합니다
   - `line`은 diff 기준 **변경된 라인(+줄)**의 번호를 사용합니다
   - 지적할 라인이 없는 종합 의견은 PR review comment로 남깁니다:
     ```
     gh pr review --comment --body "🤖 Claude Review

..."
     ```

7. 리뷰 결과를 처리합니다:
   - **새로 등록한 코멘트가 0개**이면 → PR에 LGTM 코멘트를 등록합니다:
     ```
     gh api repos/{owner}/{repo}/issues/{pr_number}/comments \
       --method POST \
       --field body="🤖 Claude Review — LGTM ✅

새로 발견된 이슈가 없습니다. 머지 가능합니다."
     ```
   - **새로 등록한 코멘트가 1개 이상**이면 → 수정 필요 항목 목록을 출력합니다.
   - 중복으로 건너뛴 항목도 함께 요약합니다.

주의사항:
- 열린 PR이 없으면 안내 메시지 출력 후 중단
- 코드를 직접 수정하지 않음 — 리뷰 코멘트만 작성
- 댓글 하나로 묶어서 올리지 않음 — 반드시 지적 사항별로 해당 라인에 개별 코멘트 등록
- **사용자에게 확인을 요청하지 않음** — 리뷰 코멘트 등록 전 별도 승인 없이 바로 진행합니다

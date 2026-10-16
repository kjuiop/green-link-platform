---
name: pr-create
description: 현재 브랜치의 커밋을 분석해 PR을 생성합니다
---

현재 브랜치의 PR을 생성합니다.

다음 순서로 진행하세요:

1. 아래 명령어를 병렬로 실행해 현재 상태를 파악하세요:
   - `git status`
   - `git log develop..HEAD --oneline`
   - `git diff develop...HEAD --stat`

2. 커밋 내역과 변경 파일을 분석해 PR 제목과 본문을 작성하세요.
   - 제목: 커밋 메시지를 그대로 쓰지 말고, 변경 내용을 파악해 **직접 의미 있는 제목**을 작성합니다 (70자 이내)
   - 본문: 반드시 `.github/PULL_REQUEST_TEMPLATE.md` 파일을 읽어 **HTML 주석(`<!-- ... -->`)을 포함한 전체 내용을 그대로** 사용합니다
     - 템플릿의 모든 섹션을 유지합니다
     - 각 섹션을 커밋 내역과 변경 파일 분석 결과로 채웁니다
     - `- closes #` 항목: 브랜치명(`feature/123` 형식)에서 이슈 번호를 추출해 `- closes #123` 으로 채웁니다

3. 원격 브랜치 존재 여부를 확인하세요:
   - 원격 브랜치가 없으면 `git push -u origin {브랜치명}` 으로 push한 뒤 PR을 생성하세요
   - 원격 브랜치가 있으면 바로 PR을 생성하세요:
   ```
   gh pr create --base develop --title "..." --body "$(cat <<'EOF'
   <!-- 템플릿 전체 내용 (주석 포함) -->
   EOF
   )"
   ```

4. 생성된 PR URL을 출력하세요.

주의사항:
- force push 금지
- main 또는 develop 브랜치에서 직접 실행 시 경고 후 중단
- 변경사항이 없으면 PR 생성하지 않음

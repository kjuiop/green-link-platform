---
name: deploy
description: develop → main 배포 PR을 생성합니다. 오늘 날짜를 제목에 포함하고 deploy 템플릿을 사용합니다.
---

develop → main 배포 PR을 생성합니다.

다음 순서로 진행하세요:

## 1. 사전 확인

아래 명령어를 병렬로 실행합니다:
- `git branch --show-current` — develop 브랜치인지 확인
- `gh pr list --state open --base main --head develop --json number,title` — 이미 열린 deploy PR 확인
- `gh pr list --state merged --base main --head develop --limit 1 --json number,title,mergedAt --jq '.[0].mergedAt'` — 마지막 배포 시각 확인 (deploy PR만 대상)

develop 브랜치가 아니면 안내 후 중단합니다.
이미 열린 deploy PR이 있으면 안내 후 중단합니다.

## 2. 이번 배포 대상 PR 추출

마지막 배포 시각(`mergedAt`) 이후에 develop에 머지된 PR만 추출합니다:

```
gh pr list --state merged --base develop --limit 50 \
  --json number,title,mergedAt \
  --jq '[.[] | select(.mergedAt > "LAST_DEPLOY_MERGEDAT")] | sort_by(.mergedAt) | .[] | "[#\(.number)] \(.title)"'
```

- 마지막 배포가 없으면(main에 merged PR이 0건) 전체 목록을 사용합니다
- PR 목록이 0건이면 "배포할 변경사항이 없습니다"를 안내하고 중단합니다

## 3. 제목 생성

오늘 날짜를 `date +%Y-%m-%d` 로 가져와 제목을 구성합니다:
```
YYYY-MM-DD Merge develop to main
```

## 4. 본문 작성

`.github/PULL_REQUEST_TEMPLATE/deploy.md` 를 읽어 템플릿 전체를 사용합니다.
- **포함된 변경사항** 섹션: Step 2에서 추출한 PR 목록을 각 항목으로 채웁니다
  - 형식: `- [#번호] PR 제목` (PR만 나열, 개별 커밋은 포함하지 않음)

## 5. PR 생성

```
gh pr create \
  --base main \
  --head develop \
  --title "YYYY-MM-DD Merge develop to main" \
  --body "$(cat <<'EOF'
템플릿 전체 내용 (변경사항 채운 버전)
EOF
)"
```

## 6. 결과 출력

- 생성된 PR URL
- 포함된 PR 수

주의사항:
- push 금지 — 원격 브랜치 상태 그대로 사용
- develop → main 방향 고정 (다른 브랜치 조합 불가)
- `git log origin/main..origin/develop` 사용 금지 — squash merge 시 이미 반영된 커밋도 잡히는 오탐이 발생함
- 변경사항은 PR 단위로만 나열, 개별 커밋은 포함하지 않음

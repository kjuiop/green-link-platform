BACKEND_DIR  := backend
FRONTEND_DIR := frontend/downflower
BOOT_ARGS    := --spring.profiles.active=dev,local
SERVER_HOST  := 3.35.44.85
SERVER_USER  := ubuntu
SERVER_KEY   := ~/.ssh/green-link-key
SERVER_DIR   := /home/ubuntu/green-link

.PHONY: back front dev start stop db db-stop db-reset logs-back logs-front \
        deploy-prod deploy-dev deploy-nginx stop-prod stop-dev stop-nginx sync-env

# MySQL 도커 실행 + healthy 대기
db:
	@docker compose -f docker-compose.local.yml up -d
	@echo "Waiting for MySQL..."
	@until docker compose -f docker-compose.local.yml exec -T mysql mysqladmin ping -h localhost -u admin -pgreen123$$ --silent 2>/dev/null; do sleep 1; done
	@echo "MySQL ready → localhost:3307/green-dev"

# MySQL 도커 종료
db-stop:
	docker compose -f docker-compose.local.yml stop

# MySQL 도커 + 볼륨 완전 삭제
db-reset:
	docker compose -f docker-compose.local.yml down -v

# 백엔드만 실행 (포그라운드)
back: db
	cd $(BACKEND_DIR) && ./gradlew bootRun --args='$(BOOT_ARGS)'

# 프론트엔드만 실행 (포그라운드)
front:
	cd $(FRONTEND_DIR) && npm run dev

# 백엔드 + 프론트엔드 동시 실행 (Ctrl+C 로 둘 다 종료)
dev: db
	@echo "http://localhost:8080 (backend)"
	@echo "http://localhost:3000 (frontend)"
	@trap 'kill 0' SIGINT; \
		(cd $(BACKEND_DIR) && ./gradlew bootRun --args='$(BOOT_ARGS)') & \
		(cd $(FRONTEND_DIR) && npm run dev) & \
		wait

# PID 파일 기반 백그라운드 실행
start: db
	@mkdir -p /tmp/glp
	@cd $(BACKEND_DIR) && ./gradlew bootRun --args='$(BOOT_ARGS)' > /tmp/glp/backend.log 2>&1 & echo $$! > /tmp/glp/backend.pid
	@cd $(FRONTEND_DIR) && npm run dev > /tmp/glp/frontend.log 2>&1 & echo $$! > /tmp/glp/frontend.pid
	@echo "Backend  PID=$$(cat /tmp/glp/backend.pid)  → http://localhost:8080"
	@echo "Frontend PID=$$(cat /tmp/glp/frontend.pid) → http://localhost:3000"
	@echo "Logs: /tmp/glp/backend.log | /tmp/glp/frontend.log"

# 백그라운드 서버 종료 (DB는 유지)
stop:
	@[ -f /tmp/glp/backend.pid ]  && kill $$(cat /tmp/glp/backend.pid)  2>/dev/null && rm /tmp/glp/backend.pid  && echo "Backend stopped"  || echo "Backend not running"
	@[ -f /tmp/glp/frontend.pid ] && kill $$(cat /tmp/glp/frontend.pid) 2>/dev/null && rm /tmp/glp/frontend.pid && echo "Frontend stopped" || echo "Frontend not running"

# 백그라운드 로그 확인
logs-back:
	tail -f /tmp/glp/backend.log

logs-front:
	tail -f /tmp/glp/frontend.log

# ── EC2 환경변수 동기화 ──────────────────────────────────────────────────────
sync-env:
	ssh -i $(SERVER_KEY) $(SERVER_USER)@$(SERVER_HOST) "mkdir -p $(SERVER_DIR)/logs/dev $(SERVER_DIR)/logs/prod"
	scp -i $(SERVER_KEY) .env.dev  $(SERVER_USER)@$(SERVER_HOST):$(SERVER_DIR)/.env.dev
	scp -i $(SERVER_KEY) .env.prod $(SERVER_USER)@$(SERVER_HOST):$(SERVER_DIR)/.env.prod
	@echo "env files synced to $(SERVER_HOST):$(SERVER_DIR)"

# ── EC2 배포 (운영) ──────────────────────────────────────────────────────────
deploy-prod:
	docker compose -f docker-compose.prod.yml up -d --build

stop-prod:
	docker compose -f docker-compose.prod.yml down

# ── EC2 배포 (개발) ──────────────────────────────────────────────────────────
deploy-dev:
	docker compose -f docker-compose.dev.yml up -d --build

stop-dev:
	docker compose -f docker-compose.dev.yml down

# ── Nginx ────────────────────────────────────────────────────────────────────
deploy-nginx:
	docker compose -f scripts/docker-compose.nginx.yml up -d

stop-nginx:
	docker compose -f scripts/docker-compose.nginx.yml down

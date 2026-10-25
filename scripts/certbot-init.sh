#!/bin/bash
# SSL 인증서 최초 발급 스크립트
# EC2 에서 1회 실행: bash scripts/certbot-init.sh
set -e

DOMAIN_PROD="admin.onurim.com"
DOMAIN_DEV="dev-admin.onurim.com"
EMAIL="arneg0shua@gmail.com"

# certbot 설치 (없는 경우)
if ! command -v certbot &> /dev/null; then
    echo "certbot 설치 중..."
    sudo apt-get update -q
    sudo apt-get install -y certbot
fi

# Nginx 중지 (standalone 방식은 port 80 점유 필요)
echo "Nginx 중지..."
docker compose -f scripts/docker-compose.nginx.yml down 2>/dev/null || true

# 인증서 발급
echo "Let's Encrypt 인증서 발급 중..."
sudo certbot certonly \
    --standalone \
    -d "$DOMAIN_PROD" \
    -d "$DOMAIN_DEV" \
    --email "$EMAIL" \
    --agree-tos \
    --no-eff-email

echo "인증서 발급 완료: /etc/letsencrypt/live/${DOMAIN_PROD}/"

# Nginx 재시작
echo "Nginx 시작..."
docker compose -f scripts/docker-compose.nginx.yml up -d

echo "완료! https://${DOMAIN_PROD} 접속 확인 해주세요."

# 자동 갱신 cron 등록 (없는 경우)
CRON_JOB="0 3 * * * certbot renew --quiet --pre-hook 'docker compose -f /home/ubuntu/app/scripts/docker-compose.nginx.yml down' --post-hook 'docker compose -f /home/ubuntu/app/scripts/docker-compose.nginx.yml up -d'"
(crontab -l 2>/dev/null | grep -q "certbot renew") || (crontab -l 2>/dev/null; echo "$CRON_JOB") | crontab -
echo "자동 갱신 cron 등록 완료 (매일 새벽 3시)"

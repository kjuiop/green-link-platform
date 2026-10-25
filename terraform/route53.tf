data "aws_route53_zone" "main" {
  zone_id = var.hosted_zone_id
}

# 프론트엔드 (Vercel) — CNAME 값은 Vercel 대시보드에서 각 도메인 추가 후 확인
resource "aws_route53_record" "www" {
  zone_id = data.aws_route53_zone.main.zone_id
  name    = "www.${var.domain_name}"
  type    = "CNAME"
  ttl     = 300
  records = ["512b02f0b480d55f.vercel-dns-017.com."]
}

resource "aws_route53_record" "dev" {
  zone_id = data.aws_route53_zone.main.zone_id
  name    = "dev.${var.domain_name}"
  type    = "CNAME"
  ttl     = 300
  records = ["512b02f0b480d55f.vercel-dns-017.com."]
}

# 백엔드 API (EC2)
resource "aws_route53_record" "api" {
  zone_id = data.aws_route53_zone.main.zone_id
  name    = "api.${var.domain_name}"
  type    = "A"
  ttl     = 300
  records = [aws_eip.backend.public_ip]
}

resource "aws_route53_record" "dev_api" {
  zone_id = data.aws_route53_zone.main.zone_id
  name    = "dev-api.${var.domain_name}"
  type    = "A"
  ttl     = 300
  records = [aws_eip.backend.public_ip]
}

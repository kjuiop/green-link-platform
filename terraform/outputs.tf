output "ec2_public_ip" {
  description = "EC2 퍼블릭 IP (Spring Boot 접속용)"
  value       = aws_eip.backend.public_ip
}

output "rds_endpoint" {
  description = "RDS MySQL 엔드포인트 (EC2 내부에서 사용)"
  value       = aws_db_instance.mysql.endpoint
}

output "s3_bucket_name" {
  description = "S3 버킷 이름"
  value       = aws_s3_bucket.photos.bucket
}

output "ssh_command" {
  description = "EC2 SSH 접속 명령어"
  value       = "ssh -i ~/.ssh/green-link-key ubuntu@${aws_eip.backend.public_ip}"
}

output "admin_url" {
  description = "운영 관리자 URL"
  value       = "https://admin.${var.domain_name}"
}

output "dev_admin_url" {
  description = "개발 관리자 URL"
  value       = "https://dev-admin.${var.domain_name}"
}

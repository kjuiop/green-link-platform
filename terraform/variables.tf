variable "aws_region" {
  description = "AWS 리전"
  type        = string
  default     = "ap-northeast-2"
}

variable "project_name" {
  description = "프로젝트 이름 (리소스 네이밍에 사용)"
  type        = string
  default     = "green-link"
}

variable "ec2_key_name" {
  description = "EC2 SSH 접속용 Key Pair 이름"
  type        = string
}

variable "db_password" {
  description = "RDS MySQL 패스워드"
  type        = string
  sensitive   = true
}

variable "s3_bucket_name" {
  description = "S3 버킷 이름 (전세계 유일)"
  type        = string
}

variable "my_ip" {
  description = "SSH 허용할 내 IP (x.x.x.x/32)"
  type        = string
}

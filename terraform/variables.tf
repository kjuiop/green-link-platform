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

variable "aws_profile" {
  description = "AWS CLI 프로필 이름 (환경변수 AWS_PROFILE로도 설정 가능)"
  type        = string
  default     = ""
}

variable "ec2_public_key_path" {
  description = "EC2 SSH 공개키 파일 경로"
  type        = string
  default     = "~/.ssh/green-link-key.pub"
}

variable "hosted_zone_id" {
  description = "Route 53 Hosted Zone ID"
  type        = string
}

variable "domain_name" {
  description = "메인 도메인"
  type        = string
  default     = "onurim.com"
}

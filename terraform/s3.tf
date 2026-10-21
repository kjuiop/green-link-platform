resource "aws_s3_bucket" "photos" {
  bucket = var.s3_bucket_name
  tags   = { Name = "${var.project_name}-photos" }
}


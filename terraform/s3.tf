resource "aws_s3_bucket" "photos" {
  bucket = var.s3_bucket_name
  tags   = { Name = "${var.project_name}-photos" }
}

resource "aws_s3_bucket_public_access_block" "photos" {
  bucket = aws_s3_bucket.photos.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

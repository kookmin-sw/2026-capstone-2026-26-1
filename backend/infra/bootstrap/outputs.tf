# prod 구성의 backend.tf에 그대로 옮겨 적을 값들.
output "state_bucket_name" {
  description = "prod backend.tf의 bucket 값"
  value       = aws_s3_bucket.tfstate.id
}

output "region" {
  description = "prod backend.tf의 region 값"
  value       = var.region
}

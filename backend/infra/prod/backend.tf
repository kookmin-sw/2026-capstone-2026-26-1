# 원격 state 백엔드 (infra/bootstrap 에서 만든 S3 버킷 참조).
# 잠금(lock)은 S3 네이티브 잠금(use_lockfile)을 사용한다 — Terraform 1.10+에서
# 권장되는 방식으로, 별도 DynamoDB 테이블 없이 S3 조건부 쓰기로 동시 apply를 막는다.
terraform {
  backend "s3" {
    bucket       = "gilbut-tfstate-881933021252"
    key          = "prod/terraform.tfstate"
    region       = "ap-northeast-2"
    use_lockfile = true
    encrypt      = true
  }
}

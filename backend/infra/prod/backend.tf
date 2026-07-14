# 원격 state 백엔드 (infra/bootstrap 에서 만든 S3 버킷 참조).
# 잠금(lock)은 S3 네이티브 잠금(use_lockfile)을 사용한다 — Terraform 1.10+에서
# 권장되는 방식으로, 별도 DynamoDB 테이블 없이 S3 조건부 쓰기로 동시 apply를 막는다.
#
# ⚠️ bucket 값은 변수를 쓸 수 없다(backend 블록은 정적이어야 함).
#    Phase 2에서 bootstrap을 apply한 뒤 `terraform output state_bucket_name`으로
#    나온 값(gilbut-tfstate-<새 계정 ID>)으로 아래 <NEW_ACCOUNT_ID>를 교체한다.
terraform {
  backend "s3" {
    bucket       = "gilbut-tfstate-<NEW_ACCOUNT_ID>"
    key          = "prod/terraform.tfstate"
    region       = "ap-northeast-2"
    use_lockfile = true
    encrypt      = true
  }
}

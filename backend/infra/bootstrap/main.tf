# ─────────────────────────────────────────────────────────────
# Terraform State 백엔드 bootstrap
#
# 원격 state를 담을 S3 버킷과 lock용 DynamoDB 테이블을 만든다.
# "원격 state를 쓰려면 그 저장소부터 있어야 한다"는 닭-달걀 문제 때문에,
# 이 구성 하나만 예외적으로 **로컬 state**로 apply 한다 (backend 블록 없음).
# 최초 1회만 실행하며, 이후 prod 구성이 여기서 만든 버킷을 원격 백엔드로 참조한다.
# ─────────────────────────────────────────────────────────────

terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.region

  default_tags {
    tags = {
      Project   = "gilbut"
      ManagedBy = "terraform"
      Component = "tfstate-backend"
    }
  }
}

# 계정 ID를 조회해 버킷 이름을 전역 유일하게 만든다 (S3 버킷명은 전 세계 유일해야 함).
data "aws_caller_identity" "current" {}

locals {
  bucket_name = "${var.state_bucket_prefix}-${data.aws_caller_identity.current.account_id}"
}

# ── state 저장용 S3 버킷 ──────────────────────────────────────
resource "aws_s3_bucket" "tfstate" {
  bucket = local.bucket_name

  # state 파일에는 실제 인프라 구성(및 일부 민감값)이 담기므로 실수 삭제를 방지한다.
  lifecycle {
    prevent_destroy = true
  }
}

# state 이력 보존: 잘못된 apply를 이전 버전으로 되돌릴 수 있게 버저닝 활성화.
resource "aws_s3_bucket_versioning" "tfstate" {
  bucket = aws_s3_bucket.tfstate.id
  versioning_configuration {
    status = "Enabled"
  }
}

# 저장 시 서버측 암호화(AES256).
resource "aws_s3_bucket_server_side_encryption_configuration" "tfstate" {
  bucket = aws_s3_bucket.tfstate.id
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

# 퍼블릭 접근 전면 차단 (state는 절대 공개돼선 안 됨).
resource "aws_s3_bucket_public_access_block" "tfstate" {
  bucket                  = aws_s3_bucket.tfstate.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# state lock은 별도 DynamoDB 없이 S3 네이티브 잠금(use_lockfile)을 사용한다.
# prod/backend.tf 의 `use_lockfile = true` 참고 — S3 조건부 쓰기로 동시 apply를 막는다.

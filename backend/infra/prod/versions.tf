terraform {
  # 신규 생성(create) 방식으로 인프라를 구성한다.
  required_version = ">= 1.10.0" # backend.tf의 S3 네이티브 잠금(use_lockfile) 사용

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

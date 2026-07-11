terraform {
  # import 블록과 -generate-config-out을 사용하므로 1.5 이상 필요.
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.region

  # 모든 리소스에 공통 태그를 자동 부착한다.
  default_tags {
    tags = {
      Project   = "gilbut"
      ManagedBy = "terraform"
    }
  }
}

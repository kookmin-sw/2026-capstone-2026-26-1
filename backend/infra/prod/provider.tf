provider "aws" {
  region = var.region

  # 모든 리소스에 공통 태그를 자동 부착한다.
  # 단, import 대상 기존 리소스에 이미 다른 태그가 붙어 있을 수 있으므로,
  # import 직후 plan에서 태그 차이가 보이면 실물 태그에 맞추거나 여기서 조정한다.
  default_tags {
    tags = {
      Project   = "gilbut"
      ManagedBy = "terraform"
    }
  }
}

# ─────────────────────────────────────────────────────────────
# 컨테이너 레지스트리: 앱 이미지 ECR 리포지토리
# CI(deploy.yml)가 :<git-sha> / :latest 태그로 push하고, EC2가 pull한다.
# ─────────────────────────────────────────────────────────────

resource "aws_ecr_repository" "app" {
  name                 = var.ecr_repository_name
  image_tag_mutability = "MUTABLE"

  encryption_configuration {
    encryption_type = "AES256"
  }

  image_scanning_configuration {
    scan_on_push = false
  }
}

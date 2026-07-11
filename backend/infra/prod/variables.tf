# 실제 값은 gitignore된 terraform.tfvars 에 둔다 (예시는 terraform.tfvars.example).
# 리소스별 실물 ID는 Phase 0 조회(aws describe-*) 결과로 채운다.

variable "region" {
  description = "AWS 리전"
  type        = string
  default     = "ap-northeast-2"
}

# 기본 VPC/서브넷은 network.tf 의 data 소스로 참조하므로 별도 변수가 없다.

# ── 컴퓨트 ───────────────────────────────────────────────────
variable "app_instance_id" {
  description = "배스천+앱 겸용 EC2 인스턴스 ID"
  type        = string
  default     = "i-0d282f307bfe0f7d7"
}

# ── 데이터베이스 ─────────────────────────────────────────────
variable "db_identifier" {
  description = "RDS 인스턴스 식별자"
  type        = string
  default     = "capstone-db"
}

# ── 컨테이너 레지스트리 ──────────────────────────────────────
variable "ecr_repository_name" {
  description = "앱 이미지 ECR 리포지토리 이름"
  type        = string
  default     = "capstone"
}

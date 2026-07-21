# 실제 값은 gitignore된 terraform.tfvars 에 둔다 (예시는 terraform.tfvars.example).

variable "region" {
  description = "AWS 리전"
  type        = string
  default     = "ap-northeast-2"
}

# ── 컴퓨트 ───────────────────────────────────────────────────
variable "key_name" {
  description = "EC2 SSH key pair 이름 (새 계정에 미리 생성해 둔 것)"
  type        = string
  default     = "capstone-key"
}

# ── 데이터베이스 ─────────────────────────────────────────────
variable "db_identifier" {
  description = "RDS 인스턴스 식별자"
  type        = string
  default     = "capstone-db"
}

variable "db_username" {
  description = "RDS 마스터 사용자명"
  type        = string
  default     = "admin"
}

variable "db_password" {
  description = "RDS 마스터 비밀번호. terraform.tfvars(gitignore)로만 주입한다."
  type        = string
  sensitive   = true
}

# ── 컨테이너 레지스트리 ──────────────────────────────────────
variable "ecr_repository_name" {
  description = "앱 이미지 ECR 리포지토리 이름"
  type        = string
  default     = "capstone"
}

# ── DNS (Route53) ────────────────────────────────────────────
variable "domain_name" {
  description = "서비스 도메인(호스팅 영역 이름). 예: example.com"
  type        = string
}

variable "app_record_name" {
  description = "앱을 가리킬 A 레코드 FQDN. 빈 값이면 도메인 apex를 사용한다. 예: api.example.com"
  type        = string
  default     = ""
}

# ── 알림 (Slack via AWS Chatbot) ──────────────────────────────
# slack_workspace_id/slack_channel_id는 Terraform으로 만들 수 없다 — AWS Chatbot
# 콘솔에서 Slack 워크스페이스를 최초 1회 OAuth 인증해야 발급된다(수동 단계).
variable "slack_workspace_id" {
  description = "AWS Chatbot에 인증 완료된 Slack 워크스페이스(팀) ID. 콘솔 'Workspace details'에서 확인."
  type        = string
}

variable "slack_channel_id" {
  description = "알림을 받을 Slack 채널 ID (채널 링크 복사 또는 About 패널에서 확인)."
  type        = string
}

# 계정 이관 시 GitHub Secrets / 가비아 NS 교체에 필요한 값을 노출한다.

output "ecr_repository_url" {
  description = "앱 이미지 ECR 리포지토리 URL (deploy.yml의 ECR_REPO_URI)"
  value       = aws_ecr_repository.app.repository_url
}

output "app_instance_id" {
  description = "배스천+앱 EC2 인스턴스 ID (rds-access.local.md 갱신용)"
  value       = aws_instance.app.id
}

output "app_public_ip" {
  description = "EC2 고정 공인 IP(EIP). deploy.yml의 EC2_HOST / DNS A 레코드 대상."
  value       = aws_eip.app.public_ip
}

output "rds_endpoint" {
  description = "RDS 엔드포인트 (DB_URL / rds-access.local.md 갱신용)"
  value       = aws_db_instance.capstone.address
}

output "route53_name_servers" {
  description = "가비아 도메인에 설정할 네임서버 4개"
  value       = aws_route53_zone.main.name_servers
}

output "vpc_id" {
  description = "사용 중인 기본 VPC ID"
  value       = data.aws_vpc.default.id
}

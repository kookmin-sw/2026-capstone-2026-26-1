# 다른 도구/문서에서 참조하기 좋은 비민감 값만 노출한다.
# (RDS 엔드포인트·EC2 퍼블릭 IP 등은 민감할 수 있어 출력하지 않는다.)

output "ecr_repository_url" {
  description = "앱 이미지 ECR 리포지토리 URL (deploy.yml의 push 대상)"
  value       = aws_ecr_repository.app.repository_url
}

output "app_instance_id" {
  description = "배스천+앱 EC2 인스턴스 ID"
  value       = aws_instance.app.id
}

output "vpc_id" {
  description = "사용 중인 기본 VPC ID"
  value       = data.aws_vpc.default.id
}

# ─────────────────────────────────────────────────────────────
# IAM: EC2 인스턴스 역할/프로파일
#
# ec2Role는 EC2가 SSM 접속(AmazonSSMManagedInstanceCore), CloudWatch Agent
# (CloudWatchAgentServerPolicy), ECR 이미지 pull(AmazonEC2ContainerRegistryReadOnly)을
# 사용하기 위한 역할이다.
#
# ECR read 권한은 기존(구 계정)엔 없던 개선 사항이다. 붙여 두면 EC2가 인스턴스
# 프로파일만으로 docker pull 할 수 있어, deploy.yml의 정적 키 의존을 줄일 수 있다.
# ─────────────────────────────────────────────────────────────

resource "aws_iam_role" "ec2" {
  name = "ec2Role"
  path = "/"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = { Service = "ec2.amazonaws.com" }
    }]
  })

  max_session_duration = 3600
}

resource "aws_iam_instance_profile" "ec2" {
  name = "ec2Role"
  path = "/"
  role = aws_iam_role.ec2.name
}

resource "aws_iam_role_policy_attachment" "ec2_ssm" {
  role       = aws_iam_role.ec2.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy_attachment" "ec2_cwagent" {
  role       = aws_iam_role.ec2.name
  policy_arn = "arn:aws:iam::aws:policy/CloudWatchAgentServerPolicy"
}

resource "aws_iam_role_policy_attachment" "ec2_ecr" {
  role       = aws_iam_role.ec2.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

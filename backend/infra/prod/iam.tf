# ─────────────────────────────────────────────────────────────
# IAM: EC2 인스턴스 역할/프로파일
#
# ec2Role는 EC2가 SSM 접속(AmazonSSMManagedInstanceCore)과 CloudWatch Agent
# (CloudWatchAgentServerPolicy)를 사용하기 위한 역할이다.
#
# 주의: 현재 ec2Role에는 ECR pull 권한이 없다. EC2의 ECR pull이 실제로 어떻게
# 동작하는지 확인 후, 필요하면 ECR read-only 정책 연결을 여기에 추가한다(개선 과제).
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

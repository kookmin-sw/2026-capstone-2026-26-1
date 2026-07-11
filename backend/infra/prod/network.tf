# ─────────────────────────────────────────────────────────────
# 네트워킹
#
# VPC/서브넷은 AWS 기본(default) VPC를 사용한다. 우리가 만든 게 아니므로
# 생성/관리하지 않고 data 소스로 참조만 한다. 실제로 관리(import)하는 것은
# 우리가 정의한 보안그룹 3개다.
# ─────────────────────────────────────────────────────────────

data "aws_vpc" "default" {
  default = true
}

# ── EC2 퍼블릭 인바운드 (22/80/443 ← 0.0.0.0/0) ─────────────
# 주의: description 변경은 SG 재생성을 유발하므로 실물 문자열을 그대로 둔다.
resource "aws_security_group" "ec2_public" {
  description = "launch-wizard-6 created 2026-01-09T07:45:18.255Z"
  name        = "launch-wizard-6"
  vpc_id      = data.aws_vpc.default.id

  ingress = [
    {
      cidr_blocks      = ["0.0.0.0/0"]
      description      = ""
      from_port        = 22
      ipv6_cidr_blocks = []
      prefix_list_ids  = []
      protocol         = "tcp"
      security_groups  = []
      self             = false
      to_port          = 22
    },
    {
      cidr_blocks      = ["0.0.0.0/0"]
      description      = ""
      from_port        = 443
      ipv6_cidr_blocks = []
      prefix_list_ids  = []
      protocol         = "tcp"
      security_groups  = []
      self             = false
      to_port          = 443
    },
    {
      cidr_blocks      = ["0.0.0.0/0"]
      description      = ""
      from_port        = 80
      ipv6_cidr_blocks = []
      prefix_list_ids  = []
      protocol         = "tcp"
      security_groups  = []
      self             = false
      to_port          = 80
    },
  ]

  egress = [
    {
      cidr_blocks      = ["0.0.0.0/0"]
      description      = ""
      from_port        = 0
      ipv6_cidr_blocks = []
      prefix_list_ids  = []
      protocol         = "-1"
      security_groups  = []
      self             = false
      to_port          = 0
    },
  ]
}

# ── EC2 → RDS egress 전용 (3306 → rds SG) ───────────────────
resource "aws_security_group" "ec2_to_rds" {
  description = "Security group attached to instances to securely connect to capstone-db. Modification could lead to connection loss."
  name        = "ec2-rds-1"
  vpc_id      = data.aws_vpc.default.id

  ingress = []

  egress = [
    {
      cidr_blocks      = []
      description      = "Rule to allow connections to capstone-db from any instances this security group is attached to"
      from_port        = 3306
      ipv6_cidr_blocks = []
      prefix_list_ids  = []
      protocol         = "tcp"
      security_groups  = ["sg-0623dffe83b8cd0f5"] # aws_security_group.rds (순환참조 방지 위해 ID 리터럴)
      self             = false
      to_port          = 3306
    },
  ]
}

# ── RDS 인바운드 (3306 ← ec2-rds-1 SG) ──────────────────────
resource "aws_security_group" "rds" {
  description = "Security group attached to capstone-db to allow EC2 instances with specific security groups attached to connect to the database. Modification could lead to connection loss."
  name        = "rds-ec2-1"
  vpc_id      = data.aws_vpc.default.id

  egress = []

  ingress = [
    {
      cidr_blocks      = []
      description      = "Rule to allow connections from EC2 instances with sg-094b0ce6ed844dcf1 attached"
      from_port        = 3306
      ipv6_cidr_blocks = []
      prefix_list_ids  = []
      protocol         = "tcp"
      security_groups  = ["sg-094b0ce6ed844dcf1"] # aws_security_group.ec2_to_rds
      self             = false
      to_port          = 3306
    },
  ]
}

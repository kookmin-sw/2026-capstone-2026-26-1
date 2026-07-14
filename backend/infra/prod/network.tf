# ─────────────────────────────────────────────────────────────
# 네트워킹
#
# VPC/서브넷은 AWS 기본(default) VPC를 사용한다. 우리가 만든 게 아니므로
# 생성/관리하지 않고 data 소스로 참조만 한다. 우리가 생성/관리하는 것은
# 보안그룹 3개와 그 규칙들이다.
#
# 순환 참조 해소:
#   ec2_to_rds(egress → rds) 와 rds(ingress ← ec2_to_rds) 가 서로를 참조한다.
#   inline 규칙으로 두면 Terraform이 순환을 풀 수 없으므로(그래서 import 시절엔
#   sg-* ID를 리터럴로 박아뒀다), SG는 규칙 없이 먼저 만들고 규칙을 별도 리소스
#   (aws_vpc_security_group_*_rule)로 분리한다. SG가 먼저 생성된 뒤 규칙이 서로의
#   .id를 참조하므로 순환이 사라진다.
# ─────────────────────────────────────────────────────────────

data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

# ── SG 정의 (규칙은 아래 별도 리소스로) ──────────────────────
resource "aws_security_group" "ec2_public" {
  name        = "capstone-ec2-public"
  description = "EC2 public inbound (SSH/HTTP/HTTPS)"
  vpc_id      = data.aws_vpc.default.id
}

resource "aws_security_group" "ec2_to_rds" {
  name        = "capstone-ec2-to-rds"
  description = "Attached to EC2 to connect to capstone-db"
  vpc_id      = data.aws_vpc.default.id
}

resource "aws_security_group" "rds" {
  name        = "capstone-rds"
  description = "Attached to capstone-db; allow 3306 from EC2 SG only"
  vpc_id      = data.aws_vpc.default.id
}

# ── ec2_public 규칙: 인바운드 22/80/443 ← 0.0.0.0/0, 아웃바운드 all ──
resource "aws_vpc_security_group_ingress_rule" "ec2_public_ssh" {
  security_group_id = aws_security_group.ec2_public.id
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "tcp"
  from_port         = 22
  to_port           = 22
}

resource "aws_vpc_security_group_ingress_rule" "ec2_public_http" {
  security_group_id = aws_security_group.ec2_public.id
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "tcp"
  from_port         = 80
  to_port           = 80
}

resource "aws_vpc_security_group_ingress_rule" "ec2_public_https" {
  security_group_id = aws_security_group.ec2_public.id
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "tcp"
  from_port         = 443
  to_port           = 443
}

resource "aws_vpc_security_group_egress_rule" "ec2_public_all" {
  security_group_id = aws_security_group.ec2_public.id
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1"
}

# ── ec2_to_rds 규칙: 아웃바운드 3306 → rds SG (인바운드 없음) ──
resource "aws_vpc_security_group_egress_rule" "ec2_to_rds_mysql" {
  security_group_id            = aws_security_group.ec2_to_rds.id
  referenced_security_group_id = aws_security_group.rds.id
  ip_protocol                  = "tcp"
  from_port                    = 3306
  to_port                      = 3306
  description                  = "Allow MySQL egress to capstone-db"
}

# ── rds 규칙: 인바운드 3306 ← ec2_to_rds SG (아웃바운드 없음) ──
resource "aws_vpc_security_group_ingress_rule" "rds_mysql" {
  security_group_id            = aws_security_group.rds.id
  referenced_security_group_id = aws_security_group.ec2_to_rds.id
  ip_protocol                  = "tcp"
  from_port                    = 3306
  to_port                      = 3306
  description                  = "Allow MySQL from EC2 SG"
}

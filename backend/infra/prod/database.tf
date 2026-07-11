# ─────────────────────────────────────────────────────────────
# 데이터베이스: RDS MySQL (capstone-db)
#
# 퍼블릭 액세스 차단, IAM DB 인증 활성화. 앱은 .env의 고정 비밀번호로 접속하고,
# 로컬 분석 접근은 SSM 포트포워딩 + IAM 토큰을 쓴다(backend/.claude/rules/rds-access.md).
# ─────────────────────────────────────────────────────────────

resource "aws_db_subnet_group" "rds" {
  description = "Created from the RDS Management Console"
  name        = "rds-ec2-db-subnet-group-1"
  subnet_ids = [
    "subnet-036f4d166ca436cb3",
    "subnet-052e89307938bc9dc",
    "subnet-071582799133e2c9e",
    "subnet-0a248340839d2b689",
  ]
}

resource "aws_db_instance" "capstone" {
  identifier     = "capstone-db"
  engine         = "mysql"
  engine_version = "8.0.43"
  instance_class = "db.t4g.micro"

  allocated_storage = 20
  storage_type      = "gp2"
  storage_encrypted = true
  kms_key_id        = "arn:aws:kms:ap-northeast-2:881933021252:key/e7f01b5c-80b3-463c-b77e-3bdda664f7d6"

  db_name              = "capstone"
  username             = "admin"
  db_subnet_group_name = aws_db_subnet_group.rds.name

  vpc_security_group_ids = [aws_security_group.rds.id]
  parameter_group_name   = "default.mysql8.0"
  option_group_name      = "default:mysql-8-0"
  ca_cert_identifier     = "rds-ca-rsa2048-g1"

  availability_zone = "ap-northeast-2a"
  multi_az          = false
  port              = 3306
  network_type      = "IPV4"

  iam_database_authentication_enabled = true
  publicly_accessible                 = false
  auto_minor_version_upgrade          = false
  copy_tags_to_snapshot               = true

  backup_retention_period = 0
  backup_window           = "15:40-16:10"
  maintenance_window      = "wed:13:10-wed:13:40"

  # 운영 DB 보호: 실수로 재생성/삭제 방지 + 삭제 시 스냅샷 정책은 실물(skip)에 맞춤.
  deletion_protection = false
  skip_final_snapshot = true

  lifecycle {
    prevent_destroy = true
    # 비밀번호는 import 시 AWS가 반환하지 않는다. 여기서 관리하지 않고 무시해,
    # apply가 기존 비밀번호를 건드리지 않도록 한다(앱은 계속 .env 비번으로 접속).
    ignore_changes = [password]
  }
}

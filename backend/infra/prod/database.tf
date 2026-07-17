# ─────────────────────────────────────────────────────────────
# 데이터베이스: RDS MySQL (capstone-db)
#
# 퍼블릭 액세스 차단, IAM DB 인증 활성화. 앱은 .env의 고정 비밀번호로 접속하고,
# 로컬 분석 접근은 SSM 포트포워딩 + IAM 토큰을 쓴다(backend/.claude/rules/rds-access.md).
# 데이터는 이관하지 않는다 — 빈 DB로 생성되며, 앱 첫 부팅 시 JPA(DDL_AUTO)가 스키마를,
# seed.region이 기본 지역 데이터를 채운다.
# ─────────────────────────────────────────────────────────────

resource "aws_db_subnet_group" "rds" {
  name        = "capstone-db-subnet-group"
  description = "Subnet group for capstone-db"
  subnet_ids  = data.aws_subnets.default.ids
}

resource "aws_db_instance" "capstone" {
  identifier     = var.db_identifier
  engine         = "mysql"
  engine_version = "8.0.43"
  instance_class = "db.t4g.micro"

  allocated_storage = 20
  storage_type      = "gp2"
  # 새 계정 기본 aws/rds KMS 키로 암호화(고객 관리형 KMS ARN을 쓰지 않는다).
  storage_encrypted = true

  db_name              = "capstone"
  username             = var.db_username
  password             = var.db_password
  db_subnet_group_name = aws_db_subnet_group.rds.name

  vpc_security_group_ids = [aws_security_group.rds.id]
  parameter_group_name   = "default.mysql8.0"
  ca_cert_identifier     = "rds-ca-rsa2048-g1"

  multi_az     = false
  port         = 3306
  network_type = "IPV4"

  iam_database_authentication_enabled = true
  publicly_accessible                 = false
  auto_minor_version_upgrade          = false
  copy_tags_to_snapshot               = true

  backup_retention_period = 0

  deletion_protection = false
  skip_final_snapshot = true
}

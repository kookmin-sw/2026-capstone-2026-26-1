# ─────────────────────────────────────────────────────────────
# 컴퓨트: 배스천 겸 앱 서버 EC2 (t2.micro, Name=capstone)
#
# 호스트 80 → 컨테이너 8080으로 Spring Boot 앱을 직접 서빙하고, SSM으로
# 배스천 역할도 겸한다. 인스턴스 프로파일 ec2Role(iam.tf)을 통해 SSM/CloudWatch/ECR을 사용.
# 공인 IP는 자동할당 대신 Elastic IP(아래)로 고정한다 — 도메인 DNS 전환을 1회로 끝내기 위함.
# ─────────────────────────────────────────────────────────────

# 최신 Ubuntu 22.04 LTS AMI (Canonical). 계정 이관 시 실물 AMI ID 대신 조회로 대체.
data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"] # Canonical

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

resource "aws_instance" "app" {
  ami                         = data.aws_ami.ubuntu.id
  instance_type               = "t2.micro"
  subnet_id                   = tolist(data.aws_subnets.default.ids)[0]
  associate_public_ip_address = false # 자동할당 끔 — 아래 EIP로 고정(주소 1개 유지)
  key_name                    = var.key_name
  iam_instance_profile        = aws_iam_instance_profile.ec2.name
  monitoring                  = true
  vpc_security_group_ids = [
    aws_security_group.ec2_public.id,
    aws_security_group.ec2_to_rds.id,
  ]

  tags = {
    Name = "capstone"
  }

  credit_specification {
    cpu_credits = "standard" # t2 버스터블: 크레딧 소진 시 성능 저하 (unlimited 아님)
  }

  # IMDSv2 강제 (http_tokens=required) — 메타데이터 SSRF 방어.
  metadata_options {
    http_endpoint               = "enabled"
    http_protocol_ipv6          = "disabled"
    http_put_response_hop_limit = 2
    http_tokens                 = "required"
    instance_metadata_tags      = "disabled"
  }

  root_block_device {
    volume_type           = "gp3"
    volume_size           = 20
    delete_on_termination = true
    encrypted             = false
  }
}

# ── Elastic IP: 공인 IP 고정 ─────────────────────────────────
# 자동할당 IP와 비용 동일(주소 1개 기준). EC2 재부팅/재생성에도 IP가 유지되어
# 가비아 NS / Route53 A 레코드를 한 번만 맞추면 된다.
resource "aws_eip" "app" {
  domain = "vpc"

  tags = {
    Name = "capstone"
  }
}

resource "aws_eip_association" "app" {
  instance_id   = aws_instance.app.id
  allocation_id = aws_eip.app.id
}

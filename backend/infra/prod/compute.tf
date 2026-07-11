# ─────────────────────────────────────────────────────────────
# 컴퓨트: 배스천 겸 앱 서버 EC2 (t2.micro, Name=capstone)
#
# 호스트 80 → 컨테이너 8080으로 Spring Boot 앱을 직접 서빙하고, SSM으로
# 배스천 역할도 겸한다. 인스턴스 프로파일 ec2Role(iam.tf)을 통해 SSM/CloudWatch를 사용.
# ─────────────────────────────────────────────────────────────

resource "aws_instance" "app" {
  ami                         = "ami-0dbd539553c3b10c4"
  instance_type               = "t2.micro"
  availability_zone           = "ap-northeast-2a"
  subnet_id                   = "subnet-0dd8da1ad0efc07c0"
  private_ip                  = "172.31.5.23"
  associate_public_ip_address = true
  key_name                    = "capstone-key"
  iam_instance_profile        = "ec2Role"
  monitoring                  = true
  vpc_security_group_ids      = ["sg-08ca3fd6a8a38a082", "sg-094b0ce6ed844dcf1"]

  tags = {
    Name = "capstone"
  }

  credit_specification {
    cpu_credits = "standard" # t2 버스터블: 크레딧 소진 시 성능 저하 (unlimited 아님)
  }

  # IMDSv2 강제 (http_tokens=required) — 메타데이터 SSRF 방어. 실물 그대로.
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

  # 재생성 방지: 운영 인스턴스이므로 실수로 destroy/replace 되지 않게 한다.
  lifecycle {
    prevent_destroy = true
  }
}

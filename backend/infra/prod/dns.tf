# ─────────────────────────────────────────────────────────────
# DNS: Route53 호스팅 영역 + A 레코드
#
# 도메인은 가비아에서 구매했고, 실제 DNS는 Route53 호스팅 영역에서 관리한다.
# 계정 이관 시 새 계정에 호스팅 영역을 새로 만들면 네임서버(NS) 4개가 새로 발급되므로,
# 가비아 콘솔에서 도메인의 네임서버를 이 영역의 NS(출력 route53_name_servers)로 교체해야 한다.
#
# 주의: 기존(구 계정) 호스팅 영역에 A 외 다른 레코드(MX/TXT 등)가 있었다면 여기에 함께 재현한다.
# ─────────────────────────────────────────────────────────────

resource "aws_route53_zone" "main" {
  name = var.domain_name
}

resource "aws_route53_record" "app" {
  zone_id = aws_route53_zone.main.zone_id
  name    = var.app_record_name != "" ? var.app_record_name : var.domain_name
  type    = "A"
  ttl     = 300
  records = [aws_eip.app.public_ip]
}

# nginx + certbot으로 HTTPS 적용하기

## 배경

`passedpath.site` 도메인을 Route53에 위임한 다음 단계로 HTTPS를 적용했다. ALB + ACM 대신
**nginx 리버스 프록시 + Let's Encrypt(certbot)** 를 선택했다 — 이 프로젝트는 EC2 t3.micro
단일 인스턴스라 ALB의 핵심 가치(여러 대상으로 트래픽 분산)를 쓸 수가 없고, ALB는 트래픽
유무와 무관하게 월 $16~20 이상 상시 과금되는 반면 Let's Encrypt 인증서는 무료다. 보안그룹은
이미 443이 열려 있어(`infra/prod/network.tf`의 `ec2_public_https`) Terraform 변경 없이
`docker-compose.prod.yml`과 애플리케이션 레이어만으로 끝나는 작업이다.

## 메모리 예산 재계산

기존 예산(`EC2 응답 불능 문제 해결하기.md` 참고): t3.micro 가용 913MiB 중
`913 − 230(OS+dockerd) − 40(redis) = 643 → app 640m`, 여유 3MiB.

nginx(단일 백엔드 프록시, 캡스톤 규모 트래픽)와 certbot(하루 대부분 `sleep 12h`로 유휴,
갱신 체크 시에만 python 프로세스가 잠깐 뜸)은 가볍지만 스파이크 여유를 감안해 각각
20m/30m으로 잡고, 그만큼을 app에서 뗀다.

| 구성 요소 | mem_limit | 비고 |
|---|---|---|
| OS + dockerd | 230 MiB | 고정, 기존과 동일 |
| redis | 40m | 기존 유지 |
| nginx | 20m | 신규, **실측 전 추정치** |
| certbot | 30m | 신규, 갱신 시 python 프로세스 스파이크 대비 |
| app | 640m → **590m** | 역산: 913−230−40−20−30−3(여유) |

**트레이드오프**: app 실측 피크(728.5MiB)는 이미 640m 캡을 넘어 swap으로 흡수하는 걸
전제로 설계돼 있었다. 590m으로 더 줄이면 swap 의존분이 88.5MiB → 138.5MiB로 늘어난다.
PR #62에서 마련한 2GB swap 안전망 안에서는 감당 가능한 범위지만, 배포 후 `docker stats`나
CloudWatch로 app 컨테이너 재기동(`restart:always`) 빈도가 기존보다 눈에 띄게 잦아지지
않는지 확인이 필요하다. 더 줄여야 하는 상황이 오면 nginx/certbot 캡을 더 깎기보다
t3.small 승급을 검토해야 한다(이미 최소치에 가깝다).

## 최초 인증서 발급 절차

인증서가 없는 상태에서 443 `server` 블록(`ssl_certificate` 참조)이 있는 nginx 설정을
배포하면 **nginx가 그 파일을 찾지 못해 컨테이너 기동 자체가 실패한다.** 그래서 배포를
두 단계로 나눈다.

### Phase A — 80만 쓰는 설정으로 우선 배포

`backend/nginx/conf.d/app.conf`에 이미 반영되어 있다: 80번 포트로 acme-challenge를
서빙하고 나머지는 곧바로 app으로 프록시(리다이렉트 없음, HTTP로 계속 서비스). 이 상태를
`release-server`에 배포하면 nginx가 정상 기동하고 앱은 HTTP로 계속 응답한다.

### 인증서 발급 (EC2에 SSH로 1회 수동 실행)

```bash
cd /home/<EC2_USER>/app

# 1) webroot 경로가 실제로 동작하는지 Let's Encrypt staging 환경으로 검증.
#    --dry-run은 디스크에 아무 인증서도 남기지 않으므로 이후 실제 발급과 충돌하지 않는다.
docker compose -f docker-compose.prod.yml run --rm certbot certonly \
  --webroot -w /var/www/certbot -d passedpath.site \
  --email jeeun03@gmail.com --agree-tos --no-eff-email --dry-run

# 2) 검증 통과 후 실제 발급 (rate limit: 도메인당 주 5회, 아래 "알려진 함정" 참고)
docker compose -f docker-compose.prod.yml run --rm certbot certonly \
  --webroot -w /var/www/certbot -d passedpath.site \
  --email jeeun03@gmail.com --agree-tos --no-eff-email

# 3) 발급 확인
docker exec nginx ls /etc/letsencrypt/live/passedpath.site/
# fullchain.pem, privkey.pem 등이 보이면 성공
```

### Phase C — 443 포함 최종 설정으로 교체

인증서 발급이 끝나면 `backend/nginx/conf.d/app.conf`를 아래 내용으로 교체하고
커밋·배포한다 (인증서는 `certbot-etc` named volume에 이미 영속화되어 있으므로 nginx가
443 블록으로 기동해도 문제없다):

```nginx
server {
    listen 80;
    server_name passedpath.site;

    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    location / {
        return 301 https://$host$request_uri;
    }
}

server {
    listen 443 ssl;
    server_name passedpath.site;

    ssl_certificate     /etc/letsencrypt/live/passedpath.site/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/passedpath.site/privkey.pem;

    location / {
        proxy_pass http://app:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

배포 후 `https://passedpath.site`가 정상 응답하고 `http://passedpath.site`가 301로
리다이렉트되는지 확인한다.

## 자동 갱신과 nginx reload

`docker-compose.prod.yml`의 `certbot` 서비스는 공식 권장 패턴(`while : do certbot renew;
sleep 12h; done`)으로 상시 떠서 만료가 가까운 인증서를 자동 갱신한다. 다만 **nginx는
파일이 바뀌어도 reload 없이는 새 인증서를 읽지 않는다.** `docker.sock`을 certbot
컨테이너에 마운트해 `--deploy-hook`으로 직접 reload시키는 방법도 있지만, 사실상 호스트
root 권한을 컨테이너에 넘겨주는 셈이라 이 프로젝트 규모에는 과하다.

이 저장소는 이미 PR #62의 swap 설정처럼 "1회성 수동 작업 + 런북 문서화" 관례를 쓰고
있으므로, 동일하게 EC2 호스트 crontab에 1회 등록한다(코드화하지 않음):

```bash
# EC2 호스트에서 1회 실행
crontab -e
# 추가: 매일 03:00 reload. 인증서가 그날 갱신됐든 아니든 reload 자체는 무중단이라 안전하다.
0 3 * * * docker exec nginx nginx -s reload >> /var/log/nginx-reload.log 2>&1
```

## 알려진 함정

- **인증서 없이 443 블록 배포 금지**: `ssl_certificate`가 가리키는 파일이 없으면 nginx
  컨테이너가 기동 자체에 실패한다. 반드시 Phase A(80만) → 발급 → Phase C(443 포함) 순서를
  지킨다.
- **Let's Encrypt rate limit**: 동일 도메인 기준 주 5회로 제한된다. 발급 전 항상
  `--dry-run`으로 먼저 검증한다.
- **nginx/certbot mem_limit(20m/30m)은 실측 전 추정치**다. 배포 후 `docker stats`로 실제
  RSS를 확인해 필요하면 재조정한다.

## 향후 개선 여지

- crontab 기반 reload를 systemd timer나 `inotifywait` 기반 이벤트 트리거로 바꾸면 더
  정교해지지만, 현재 규모(수동 부트스트랩 + 런북 문서화 관례)에는 과하다고 판단해 보류.
- HSTS, `ssl_protocols`/`ssl_ciphers` 최신 권장값 등 TLS 설정 강화는 이번 1차 적용 범위
  밖으로 남겨뒀다.
- t3.small로 승급하는 시점이 오면 이 문서의 메모리 예산 표를 다시 산정해야 한다.

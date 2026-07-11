# infra — 길벗 AWS 인프라 (Terraform)

이미 운영 중인 AWS 리소스(EC2·RDS·ECR·VPC·IAM)를 **재생성 없이 무중단으로** Terraform
코드/state에 흡수(import)해 코드로 관리한다. 성공 판정은 `terraform plan`이
`No changes` 를 내는 것 — 즉 코드가 실물과 정확히 일치하는 상태다.

## 디렉터리

```
backend/infra/
  bootstrap/   원격 state 저장소(S3 버킷). 최초 1회 로컬 state로 apply.
  prod/        실제 운영 인프라. bootstrap의 S3를 원격 백엔드로 사용.
```

> 이 인프라(EC2·RDS·ECR·VPC·IAM)는 개념적으로 AWS 계정 전체에 속하지만, 현재 유일한
> 워크로드가 `backend/`이므로 코드 배치는 `backend/infra/`에 둔다.

## 실행 순서

### 1. 자격증명

Terraform이 리소스를 **describe(읽기)** 할 수 있는 자격증명이 필요하다. 현재 로컬 기본
프로파일(`my-computer`)은 SSM+RDS 접속용 최소 권한이라 describe가 막혀 있으므로,
읽기 권한이 있는 별도 프로파일을 사용한다:

```bash
export AWS_PROFILE=<describe 권한이 있는 프로파일>
aws sts get-caller-identity   # 계정 881933021252 확인
```

### 2. state 백엔드 생성 (최초 1회)

```bash
cd backend/infra/bootstrap
terraform init
terraform apply
terraform output      # bucket 이름 확인 → prod/backend.tf 값과 일치해야 함
```

### 3. prod 초기화

> 이미 운영 리소스는 import되어 state에 흡수돼 있다(`terraform plan` → `No changes`).
> 아래는 이 state를 분실했거나 처음부터 재현할 때 따르는 절차다.

```bash
cd ../prod
cp terraform.tfvars.example terraform.tfvars   # 실제 ID로 채운다 (Phase 0 조회)
terraform init                                  # 원격 백엔드 연결
terraform plan                                  # 정상 상태라면 "No changes"
```

새 리소스를 import해야 할 때는 리소스별 `import { to = ...; id = ... }` 블록을 임시로
추가하고 `terraform plan -generate-config-out=generated.tf`로 HCL 초안을 뽑아
concern별 파일(network/compute/database/ecr/iam.tf)로 정리한 뒤 `apply`한다.

## 리소스 조회 치트시트 (Phase 0)

```bash
R=ap-northeast-2
aws ec2 describe-vpcs --region $R
aws ec2 describe-subnets --filters Name=vpc-id,Values=<vpc> --region $R
aws ec2 describe-security-groups --filters Name=vpc-id,Values=<vpc> --region $R
aws ec2 describe-instances --instance-ids i-0d282f307bfe0f7d7 --region $R
aws rds describe-db-instances --db-instance-identifier capstone-db --region $R
aws ecr describe-repositories --region $R
aws iam list-roles ; aws iam list-instance-profiles
```

## 주의

- `terraform.tfvars`, `*.tfstate`, `.terraform/`, `generated.tf` 는 커밋하지 않는다
  (`.gitignore` 참조). `.terraform.lock.hcl` 은 **커밋한다**(provider 버전 고정).
- RDS/EC2에는 `prevent_destroy`, RDS 비밀번호에는 `ignore_changes`를 걸어 운영 리소스를
  실수로 재생성/변경하지 않도록 방어한다.
- 앱 배포 파이프라인(`.github/workflows/deploy.yml`)은 이 작업 범위 밖이다.

## 향후 확장

- CloudWatch 알람·대시보드·로그 (`prod/monitoring.tf`) — 다음 단계.
- CI 자격증명을 GitHub OIDC + 임시 role로 전환.
- IAM 권한 스코프 축소(`Resource:"*"` → 특정 ARN).

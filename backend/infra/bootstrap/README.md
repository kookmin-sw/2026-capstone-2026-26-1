# bootstrap — Terraform State 백엔드

`prod/` 구성이 사용할 **원격 state 저장소**(S3 버킷)를 만든다. 잠금(lock)은 별도
DynamoDB 없이 S3 네이티브 잠금(`prod/backend.tf`의 `use_lockfile = true`)을 사용한다.

## 왜 별도인가 (닭-달걀 문제)

원격 state를 쓰려면 그 state를 담을 S3 버킷이 먼저 존재해야 한다. 그런데 그 버킷을
Terraform으로 만들면, 만드는 시점엔 아직 원격 백엔드가 없다. 그래서 이 구성만
**로컬 state**(backend 블록 없음)로 딱 한 번 apply 하고, 이후 `prod/`가 여기서 만든
버킷을 원격 백엔드로 참조한다.

## 실행 (최초 1회)

```bash
cd backend/infra/bootstrap
terraform init          # provider 다운로드 (자격증명 불필요)
terraform apply         # S3 버킷 생성 (자격증명 필요)
terraform output        # prod/backend.tf에 옮겨 적을 값 확인
```

산출물:
- S3 버킷: `gilbut-tfstate-<계정 ID>` (계정 ID는 apply 시 자동 조회되어 붙는다)
- 이 버킷명을 `prod/backend.tf`의 `bucket` 값에 그대로 옮겨 적는다.

## 주의

- 이 디렉터리의 `terraform.tfstate`(로컬)는 **커밋하지 않는다**(`../.gitignore`가 무시).
- 버킷에는 `prevent_destroy = true`가 걸려 있어 실수로 `destroy` 되지 않는다.
- 로컬 state를 분실해도 버킷 자체는 AWS에 남아 있으므로, 필요하면 재-import
  (`terraform import aws_s3_bucket.tfstate gilbut-tfstate-<계정 ID>`)로 복구한다.
- 필요 권한: `s3:*`(해당 버킷), `sts:GetCallerIdentity`.

# 부팅 힙 버스트 제거 — region seed를 엑셀 파싱에서 import.sql로

운영 서버가 재배포 직후 `OutOfMemoryError`로 30초 간격 크래시 루프에 빠진 사건을
추적하다, 그 방아쇠가 **부팅 시 법정동 사전(region seed) 적재가 순간적으로 힙을 130MiB
가까이 점유하는 것**임을 실측으로 규명하고, 런타임 엑셀 파싱을 정적 `import.sql`로
전환해 제거한 과정을 정리한다.

## 문제 요약

2026-07-17, `mem_limit`을 700m→640m로 재산정한 이미지를 배포한 직후 운영 EC2(t3.micro)가
**30초 간격으로 412회 재시작**하는 크래시 루프에 빠졌다. 컨테이너 로그에는
`Terminating due to java.lang.OutOfMemoryError: Java heap space`가 반복됐고,
`-XX:+ExitOnOutOfMemoryError` + `restart: always` 조합이 죽은 프로세스를 매번 되살리며
루프가 이어졌다.

처음 의심은 "`mem_limit` 축소로 힙 상한이 176MiB→160MiB(640m의 25%)로 줄어든 것"이었다.
맞는 방향이었지만, 그것만으로는 **왜 하필 부팅 중에** 힙이 터지는지가 설명되지 않았다.

## 원인 규명 — 측정이 가설을 뒤집었다

로컬에서 운영과 동일한 조건(`--cpus=1`, `DDL_AUTO=create`, 로컬 MySQL)으로 5회 반복
부팅하며 GC 로그로 힙을 추적했다. 결과는 처음 가설과 달랐다.

`★ 진짜 범인은 insert가 아니라 엑셀 파싱이었다 ─────────`

GC 로그를 보면 스키마 DDL이 끝나고 `Started CapstoneApplication`이 찍힌 뒤까지 힙은
90~100MiB로 잔잔했다. 그런데 그 직후 **region seed(전국 법정동 5,034건) 적재가 시작되자
단 2초 만에 힙 사용량이 100MiB→231MiB로 치솟았다.**

당초 "5,034건 insert가 무겁다"고 생각했지만, 실제 범인은 insert가 아니라 그 앞단이었다.
`RegionSeedExcelReader`가 Apache POI의 `WorkbookFactory.create()`로 **763KB짜리 xlsx를
DOM 객체 모델로 통째로 펼치는 것**이 문제였다. 이 xlsx의 내부 압축을 풀면 워크시트 XML만
약 6MB인데, POI의 객체 모델은 원본 XML의 10~20배 메모리를 쓰는 것으로 알려져 있어
**약 130MiB의 순간 힙 점유**로 이어졌다. 100→231MiB 스파이크와 정확히 들어맞는다.

`─────────────────────────────────────────────────`

즉 이 사건은 두 요인의 곱이었다:

1. **부팅 버스트(원인)** — POI가 xlsx를 DOM으로 파싱하며 힙을 130MiB 순간 점유.
2. **줄어든 힙 천장(방아쇠)** — 640m/25% = 160MiB 힙이 이 버스트를 못 버팀.

버스트는 `mem_limit`이 700m(힙 176MiB)이던 동안엔 아슬아슬하게 버텨졌으나, 640m로 줄자
경계를 넘겨 매 부팅마다 OOM으로 이어졌다.

> 상세 실측 데이터: `oom-repro-evidence/local/2026-07-17-부팅버스트-실험요약.md`,
> `2026-07-17-GC로그-힙피크.txt`

## 왜 import.sql인가

버스트의 원인이 "런타임 xlsx 파싱"이므로, **런타임에 xlsx를 읽지 않게 만들면** 버스트는
사라진다. 데이터를 DB에 넣는 방법으로 세 가지를 검토했다.

| 방식 | 실행 시점 | 판단 |
|---|---|---|
| **`import.sql`** (Hibernate) | 스키마를 새로 만들 때(`create`/`create-drop`)만 | **채택** |
| `data.sql` (Spring) | 매 부팅마다 | 멱등성 필요 + `defer-datasource-initialization` 필요 |
| Flyway | 평생 한 번(이력 테이블 기준) | `ddl-auto=create`와 충돌 → 지금은 부적합 |

현재 개발·운영 모두 `ddl-auto=create`다. `import.sql`은 Hibernate가 스키마를 생성한 직후
자동 실행되므로, **"스키마가 새로 만들어질 때만 시딩한다"**는 요구와 정확히 맞는다.
Flyway는 스키마 소유권을 Hibernate에서 가져오는 도구라 `create`와 공존이 안 돼, 운영을
`validate`로 안정화하는 시점의 후속 과제로 남겼다.

## 해결 — 무엇을 바꿨나

### 1. 정적 import.sql 생성 (현재 동작과 완전 일치)

새로 파싱해 만드는 대신, **현재 코드가 이미 로컬 MySQL에 적재해둔 region 테이블
5,034건을 UTF-8로 덤프**해 SQL로 변환했다. 이렇게 하면 기존 필터·dedup 로직
(법정동코드 10자리 / 시도·시군구·읍면동명 존재 / 동리명·말소일자 없음 / 코드 기준 중복
제거)을 재구현할 필요 없이 **현재 동작과 100% 일치(parity by construction)**가 보장된다.

- 200행씩 묶은 multi-row INSERT 26개 문장, 파일 크기 약 352KB.
- 컬럼·타임스탬프는 기존 `batchInsert`와 동일:
  `(legal_dong_code, sido_name, sigungu_name, legal_dong_name, created_at, updated_at)`,
  `now()` 사용.
- 스크래치 DB에 실제 region DDL로 실행해 5,034건·distinct 코드 5,034·한글 인코딩을
  사전 검증한 뒤 `src/main/resources/import.sql`에 배치.

### 2. 런타임 시딩 코드·POI 의존성 제거

- 삭제: `RegionSeedInitializer`, `RegionSeedBulkInsertService`, `RegionSeedExcelReader`,
  `RegionSeedRow`, `RegionSeedException` (서로만 참조하던 자기완결 그룹).
- `build.gradle`에서 `poi-ooxml` 제거(코드베이스에서 POI는 이 리더 한 곳에서만 쓰였다).
- `application.yml`에서 `seed.region.enabled` 제거.
- `region_seed.xlsx`는 런타임 jar에서 빼되 원본은 `docs/seed-source/`로 이동해 보존
  (재생성 절차는 그곳 README에 문서화).

### 의미론 변화 (의도된 것)

기존 `RegionSeedInitializer`(ApplicationRunner)는 `ddl-auto` 값과 무관하게 **매 부팅마다**
실행돼 upsert했다 — 이게 버스트의 원천이었다. `import.sql`은 **스키마가 새로 생성될 때만**
실행된다. 즉 재시작(`update`/`validate`/`none`)엔 안 돌고, 이미 있는 데이터를 다시 건드리지
않는다.

## 실측 검증 결과

변경 전후를 **동일 조건**(`--cpus=1`, `-Xmx400m`, 로컬 MySQL, `DDL_AUTO=create`, 동일한
GC 로그 계측)으로 부팅해 비교했다.

| 지표 | 변경 전 (POI 파싱) | 변경 후 (import.sql) | 변화 |
|---|---:|---:|---:|
| **힙 used 피크** | 231 MiB | **73 MiB** | **−68%** |
| **cgroup RSS 피크** | 455 MiB | **307.6 MiB** | **−32%** |
| region 적재 건수 | 5,034 | **5,034** | 동일 |
| 부팅 중 OOM | 640m에서 크래시 루프 | **0건** | — |
| `region seed initializer` 로그 | 있음 | **없음** | 제거 확인 |
| `app.jar` 크기 | 136.5 MiB | **118.8 MiB** | **−17.7 MiB** |
| Docker 이미지 크기 | 728 MB | **693 MB** | −35 MB |
| `./gradlew build` (테스트 77개) | — | **전부 통과** | — |

`★ 이 수치가 확정하는 것 ───────────────────────────`

**힙 피크가 231MiB → 73MiB로 떨어졌다.** GC 로그 전 구간에서 힙이 70MiB대를 넘지
않는다 — region seed 구간의 100→231MiB 버스트가 **완전히 사라졌다.**

핵심은 이 73MiB가 **640m의 기본 힙 상한(160MiB) 아래로 넉넉히 들어온다**는 점이다.
크래시 루프의 원인이던 버스트 자체를 없앴으므로, `mem_limit`을 640m 그대로 두고
`MaxRAMPercentage`를 올리지 않아도 부팅이 안전하다. **증상(힙을 키우기)이 아니라
원인(xlsx 런타임 파싱)을 제거한 결과다.**

또한 region은 여전히 5,034건 적재된다 — 이것이 곧 `import.sql`이 Hibernate에 의해 실제로
실행됐다는 직접 증거다(별도 `hibernate.hbm2ddl.import_files` 설정 없이 자동 인식). 테스트의
H2(`MODE=MySQL`, `ddl-auto=create`)에서도 동일하게 실행돼 77개 테스트가 통과했다.

`─────────────────────────────────────────────────`

> 부팅 시간(`Started CapstoneApplication`) 자체는 직접 비교가 어렵다 — 기존에는 seed가
> `Started` 이후 ApplicationRunner에서 돌았고, 지금은 `Started` 이전 스키마 생성 단계에서
> 돌기 때문이다. 그래서 여기서는 부팅 시간이 아니라 힙·RSS 피크를 성과 지표로 삼았다.

## 남은 한계 / 후속

- **운영 `validate` 전환 시**: `import.sql`은 `create`/`create-drop`에서만 실행된다.
  운영을 `validate`로 안정화하면 이 파일은 안 돌므로, 그 시점엔 별도 마이그레이션
  메커니즘(예: Flyway)으로 seed를 관리해야 한다. 현재는 개발·운영 모두 `create`라
  `import.sql`로 충분하다.
- **법정동 갱신 절차**: 행정구역 개편으로 코드가 바뀌면 `import.sql`을 직접 편집하거나
  xlsx에서 재생성한다. 절차는 `docs/seed-source/README.md`에 정리했다.
- **`MaxRAMPercentage` 상향 불필요**: 이번 근본 수정으로 앞서 검토했던 힙 비율 상향
  (`-XX:MaxRAMPercentage=47.0`)은 필요 없어졌다. 그 산정은 "증상 대응"이었고, 이번이
  "원인 대응"이다.

## 참고

- 부팅 버스트 실측 원본: `oom-repro-evidence/local/2026-07-17-부팅버스트-실험요약.md`,
  `2026-07-17-GC로그-힙피크.txt`, `2026-07-17-cgroup-폴링-메모리피크.txt`
- MaxRAMPercentage 산정(대안, 미채택): `oom-repro-evidence/local/2026-07-17-MaxRAMPercentage-산정.md`
- region seed 원본·재생성 절차: `../../seed-source/README.md`
- 상위 OOM 사건 전체 맥락: `EC2 응답 불능 문제 해결하기.md`

# region seed 원본 & 재생성 절차

`region_seed.xlsx`는 법정동 사전(전국 법정동 코드) **원본 데이터**다. 애플리케이션은
런타임에 이 파일을 읽지 않는다 — 이 데이터는 정적 SQL로 변환돼
`src/main/resources/import.sql`에 박혀 있고, Hibernate가 `ddl-auto=create`/`create-drop`으로
스키마를 생성한 직후 자동 실행한다.

## 왜 런타임에서 뺐나

기존에는 부팅 때마다 Apache POI로 이 xlsx(763KB, 내부 XML 약 6MB)를 DOM으로 통째로
파싱하며 힙을 약 130MiB 순간 점유했고, 이것이 2026-07-17 운영 OOM 크래시 루프의 직접
원인이었다. 정적 `import.sql`로 대체해 이 버스트와 POI 의존성(`poi-ooxml`)을 모두 제거했다.
경위·실측: `backend/docs/tech/infra/oom-repro-evidence/local/2026-07-17-*`.

## 법정동 코드가 바뀌면 (import.sql 재생성)

법정동은 행정구역 개편 시 가끔 바뀐다. 그때 `import.sql`을 갱신하는 두 방법:

### 방법 A — import.sql 직접 편집 (소규모 변경 권장)

바뀐 코드가 몇 건이면 `src/main/resources/import.sql`의 해당 INSERT 튜플만 직접
수정/추가한다. 각 문장은 `INSERT INTO region (...) VALUES (...),(...);` 형식의 multi-row
INSERT다(한 문장당 200행).

### 방법 B — xlsx에서 전체 재생성 (대규모 변경)

원본 xlsx를 새 데이터로 교체한 뒤 다시 뽑는다. 현재 코드는 POI를 제거했으므로,
**필터·정제 로직**(법정동코드 10자리 / 시도·시군구·읍면동명 존재 / 동리명·말소일자 없음 /
legal_dong_code 기준 dedup)을 다시 적용해야 한다. 절차:

1. 로컬 MySQL을 띄운다: `docker compose -f docker-compose.local.yml up -d`.
2. xlsx를 필터·dedup해 `region` 테이블에 적재한다(과거 `RegionSeedExcelReader` +
   `RegionSeedBulkInsertService`의 로직 — git 이력에서 참고 가능. 일회성 스크립트로 재구현하거나
   POI를 임시 복원해 사용).
3. 적재된 테이블을 SQL로 덤프해 `import.sql`을 만든다:
   ```sql
   -- 컨테이너 내부에서 UTF-8로 튜플 생성
   SET NAMES utf8mb4;
   SELECT CONCAT("('", legal_dong_code, "','", sido_name, "','",
                 sigungu_name, "','", legal_dong_name, "',now(),now())")
   FROM region ORDER BY region_id;
   ```
4. 결과를 200행씩 묶어 `INSERT INTO region (legal_dong_code, sido_name, sigungu_name,
   legal_dong_name, created_at, updated_at) VALUES ... ;` 형식으로 조립한다.
5. 스크래치 DB에 실행해 행 수·distinct 코드 수·한글 인코딩을 검증한 뒤 교체한다.

## 알려진 제약

- `import.sql`은 `ddl-auto`가 `create`/`create-drop`일 때만 실행된다. 운영을 `validate`로
  전환하면 이 파일은 실행되지 않으므로, 그 시점엔 별도 마이그레이션 메커니즘(예: Flyway)으로
  seed를 관리해야 한다. 현재는 dev·운영 모두 `create`라 import.sql로 충분하다.

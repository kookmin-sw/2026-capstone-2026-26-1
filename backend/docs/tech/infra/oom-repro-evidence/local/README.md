# OOM 재현 실험 — 원시 증거 모음

`EC2 서버 배포 후 10분 만에 다운되는 문제 해결하기.md` §17의 원인 확정을 **직접 증거**로
보강하기 위해, 로컬에서 t2.micro OOM 조건을 재현하며 관측한 원시 지표를 모아둔다.
재현 절차/설계는 `.claude/plans/backend-docs-tech-ec2-sleepy-pelican.md` 참고.

## 파일 목록

| 파일 | 내용 |
|---|---|
| `실험요약.md` | 두 실험 요약 + 종합 결론 |
| `955MB-초과여부-산술판정.md` | 실험 A: 충실 조건 footprint → 955MB 산술 판정(핵심) |
| `NMT-힙논힙분해.txt` | 실험 A: NMT 힙/논힙 분해 + GC.heap_info + VM.flags(1 vCPU, SerialGC, MaxRAM 955) |
| `앱메모리-스냅샷.txt` | 실험 A: memory.peak/current/anon/file 스냅샷 |
| `커널-OOM-killer-로그.txt` | 실험 B: 실제 커널 OOM-killer 단일 이벤트 블록(java kill, swap 0) |
| `2026-07-17-부팅버스트-실험요약.md` | 실험 C: 부팅 버스트(스키마 DDL+region seed) 힙 피크 실측 요약 |
| `2026-07-17-MaxRAMPercentage-산정.md` | 실험 C: 실측 기반 `-XX:MaxRAMPercentage` 산출 산술 |
| `2026-07-17-GC로그-힙피크.txt` | 실험 C: GC 로그 발췌(힙 폭증 구간) |
| `2026-07-17-cgroup-폴링-메모리피크.txt` | 실험 C: cgroup memory.current 폴링 원시 로그(5회) |

## 세 실험의 역할

- **실험 A(cpu1)**: 오염 변수(Docker 엔진 오버헤드·CPU 수·GC 종류)를 배제한 충실 조건에서 앱
  footprint를 재고 955와 산술 대조 → 단발 부팅 855MiB(89%), 넘지 않음.
- **실험 B(400m)**: swap 0에서 OOM-killer의 킬 메커니즘 + restart 루프를 직접 포착. 단
  `CONSTRAINT_MEMCG`(cgroup 스코프)라 운영의 호스트 전역과 스코프는 다름.
- **실험 C(2026-07-17)**: 실험 A/B가 "주어진 천장에 맞는가"를 봤다면, 이건 **부팅 버스트가
  실제로 얼마를 요구하는가**를 재서 `-XX:MaxRAMPercentage`를 산출한다. 2026-07-13(실험 A)의
  idle 스냅샷이 놓쳤던 region seed 벌크 삽입 버스트(힙 100MiB→231MiB, 2초)를 처음 포착했다.

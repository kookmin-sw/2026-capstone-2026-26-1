# CloudWatch 대시보드(`gilbut-oom-observability`) 지표 정리

`backend/infra/prod/monitoring.tf`에 정의된 대시보드가 실제로 어떤 지표를 어디서
가져다 보여주는지 정리한다. 위젯 코드 자체는 `monitoring.tf`를 보면 되지만, "왜
이 지표를 골랐는지·프리티어에 잡히는지·알람에 물려있는지"는 여기서 한눈에 본다.

## 위젯별 지표

| 위젯 | 네임스페이스 | 지표명 | 의미 |
|---|---|---|---|
| Memory & Swap available/used % | `Gilbut/EC2` | `mem_available_percent` | 커널이 실제 회수 가능 여부까지 반영한 여유 메모리 %(`mem_used_percent`보다 신뢰도 높음) |
| | `Gilbut/EC2` | `swap_used_percent` | 스왑 사용률 |
| CPU % & Credit Balance | `AWS/EC2` | `CPUUtilization` | EC2 CPU 사용률(AWS 기본 제공, 무료) |
| | `AWS/EC2` | `CPUCreditBalance` | t3.micro 버스터블 크레딧 잔량(AWS 기본 제공, 무료) |
| java process RSS vs JVM committed | `Gilbut/EC2` | `procstat_memory_rss` | OS가 보는 `app.jar`(자바 프로세스) 전체 RSS |
| | `Gilbut/App` | `app.memory.heap.committed.value` | JVM이 OS로부터 커밋한 힙 크기 |
| | `Gilbut/App` | `app.memory.nonheap.committed.value` | JVM이 OS로부터 커밋한 논힙(Metaspace 등) 크기 |
| Network Traffic (bytes) | `AWS/EC2` | `NetworkIn`, `NetworkOut` | 네트워크 트래픽(AWS 기본 제공, 무료) |
| Network Packets (count) | `AWS/EC2` | `NetworkPacketsIn`, `NetworkPacketsOut` | 네트워크 패킷 수(AWS 기본 제공, 무료) |
| JVM Heap/Non-Heap used vs committed | `Gilbut/App` | `app.memory.heap.used.value` | 힙 실사용량(`MemoryMXBean.getHeapMemoryUsage().getUsed()`) |
| | `Gilbut/App` | `app.memory.heap.committed.value` | 힙 커밋량 |
| | `Gilbut/App` | `app.memory.nonheap.used.value` | 논힙 실사용량 |
| | `Gilbut/App` | `app.memory.nonheap.committed.value` | 논힙 커밋량 |
| GC pause (cumulative since JVM start) | `Gilbut/App` | `app.gc.pause.count.value` | 부팅 이후 누적 GC 횟수(모든 GC 종류 합산) |
| | `Gilbut/App` | `app.gc.pause.time.value` | 부팅 이후 누적 GC 소요시간(ms, 모든 GC 종류 합산) |

## 프리티어(계정+리전 통틀어 10개) 대비 실제 수집 중인 지표

`cpu_usage_active`는 대시보드 어느 위젯에도 안 쓰여서 2026-08-04에 CloudWatch
Agent 설정에서 제거했다(`aws_ssm_parameter.cw_agent_config` + `fetch-config`로
반영). 현재는 위 표의 9개 지표만 수집된다 — 프리티어(10개) 안에 여유 1개가 생겼다.

| 네임스페이스 | 지표 | 대시보드에 쓰임? | 알람 연결 |
|---|---|:---:|---|
| `Gilbut/EC2` | `mem_available_percent` | ✅ | `gilbut-mem-high`, `gilbut-mem-warning` |
| `Gilbut/EC2` | `swap_used_percent` | ✅ | `gilbut-swap-high` |
| `Gilbut/EC2` | `procstat_memory_rss` | ✅ | 없음 |
| `Gilbut/App` | `app.memory.heap.used` | ✅ | 없음 |
| `Gilbut/App` | `app.memory.heap.committed` | ✅ | 없음 |
| `Gilbut/App` | `app.memory.nonheap.used` | ✅ | 없음 |
| `Gilbut/App` | `app.memory.nonheap.committed` | ✅ | 없음 |
| `Gilbut/App` | `app.gc.pause.count` | ✅ | 없음 |
| `Gilbut/App` | `app.gc.pause.time` | ✅ | 없음 |

## `AWS/EC2` 네임스페이스는 왜 무료인가

`AWS/EC2`는 AWS가 EC2 인스턴스에 기본 제공하는 표준 지표 네임스페이스라 **커스텀
지표가 아니다** — 몇 개를 쓰든 계정+리전 통틀어 10개짜리 프리티어 한도와 무관하게
무료다. `Gilbut/EC2`(CloudWatch Agent가 만든 네임스페이스)와 `Gilbut/App`(이 앱이
직접 push하는 네임스페이스)만 프리티어 대상이다.

## 참고

- 지표 이름 뒤에 `.value`가 붙는 건 micrometer-registry-cloudwatch2가 Gauge 타입에
  자동으로 붙이는 접미사다(Timer/Counter는 `.count`/`.sum`/`.max`/`.avg`).
- `app.*` 접두사로 지은 이유, 카디널리티 축소 과정, 데이터가 한동안 안 들어왔던
  원인 규명 과정은 `oom-repro-evidence/production/2026-08-04-운영-힙논힙-실측.md`와
  PR #67~#74 참고.

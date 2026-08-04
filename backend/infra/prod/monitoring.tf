# ─────────────────────────────────────────────────────────────
# 관측(Observability): CloudWatch Agent 설정 + 로그 그룹 + 메트릭 필터 + 대시보드 + 경보
#
# EC2 기본 메트릭(AWS/EC2)에는 메모리·swap이 없다(하이퍼바이저가 게스트 내부를 못 봄).
# 게스트 OS 안에서 도는 CloudWatch Agent가 /proc를 읽어 커스텀 네임스페이스(Gilbut/EC2)로
# push한다. IAM은 ec2Role의 CloudWatchAgentServerPolicy(iam.tf)로 이미 충족 — 신규 권한 없음.
#
# 목적: 세 방어선(swap/mem_limit/JVM) 적용 전 "before"(무방어) 상태의 OOM을 실기기에서 관측.
# ─────────────────────────────────────────────────────────────

# ── 로그 그룹 ────────────────────────────────────────────────
resource "aws_cloudwatch_log_group" "kern" {
  name              = "/gilbut/ec2/kern"
  retention_in_days = 7 # 단기 실험용
}

resource "aws_cloudwatch_log_group" "docker_events" {
  name              = "/gilbut/ec2/docker-events"
  retention_in_days = 7
}

# ── CloudWatch Agent 설정 (SSM Parameter Store) ──────────────
# 이름이 "AmazonCloudWatch-" 로 시작해야 CloudWatchAgentServerPolicy의
# ssm:GetParameter(Resource: parameter/AmazonCloudWatch-*)로 에이전트가 읽을 수 있다.
resource "aws_ssm_parameter" "cw_agent_config" {
  name = "AmazonCloudWatch-gilbut-agent"
  type = "String"

  value = jsonencode({
    agent = {
      # 실험 구간에서 고해상도가 필요하면 10으로 낮춘다(기본 60).
      metrics_collection_interval = 60
      run_as_user                 = "cwagent"
    }
    metrics = {
      namespace = "Gilbut/EC2"
      # $${...} → 리터럴 ${aws:InstanceId} 로 출력(에이전트가 런타임에 치환).
      append_dimensions = {
        InstanceId = "$${aws:InstanceId}"
      }
      metrics_collected = {
        mem = {
          # mem_used_percent는 buff/cache를 전부 "안 쓴 것"으로 계산해 캐시 고갈 상황을
          # 못 잡아낸다(2026-07-14 11:34 OOM-kill 사건 실측으로 확인). mem_available_percent는
          # 커널이 실제 회수 가능 여부까지 반영한 수치라 훨씬 신뢰할 수 있는 조기 경보 지표다.
          # mem_used_percent/mem_available/mem_used/mem_total은 CloudWatch 커스텀 지표
          # 프리티어(계정+리전 통틀어 10개)를 맞추기 위해 제거 — mem_high/mem_warning 알람이
          # 쓰는 mem_available_percent만 남긴다.
          measurement = ["mem_available_percent"]
        }
        swap = {
          # swap_used는 같은 이유로 제거 — swap_high 알람이 쓰는 swap_used_percent만 남긴다.
          measurement = ["swap_used_percent"]
        }
        # cpu_usage_active는 CloudWatch 프리티어(10개)에 잡히면서 대시보드 어느 위젯에도
        # 안 쓰였다(CPU 위젯은 무료인 AWS/EC2 CPUUtilization을 씀) — 2026-08-04 제거.
        procstat = [
          {
            pattern     = "app.jar"
            measurement = ["memory_rss"]
          }
        ]
      }
    }
    logs = {
      logs_collected = {
        files = {
          collect_list = [
            {
              file_path       = "/var/log/kern.log"
              log_group_name  = aws_cloudwatch_log_group.kern.name
              log_stream_name = "{instance_id}"
            },
            {
              file_path       = "/var/log/gilbut/docker-events.log"
              log_group_name  = aws_cloudwatch_log_group.docker_events.name
              log_stream_name = "{instance_id}"
            }
          ]
        }
      }
    }
  })
}

# ── 메트릭 필터: 로그 → 메트릭 ───────────────────────────────
# OOMKillCount/ContainerDieCount 로그 지표 필터는 CloudWatch 커스텀 지표 프리티어
# (계정+리전 통틀어 10개)를 맞추기 위해 제거했다(2026-08-04). 커널 OOM-killer 발화
# ("invoked oom-killer"/"Out of memory: Killed process")와 컨테이너 OOM 사망(exitCode=137)
# 로그 자체는 kern/docker_events 로그 그룹에 계속 수집되므로, 필요시 CloudWatch Logs
# Insights로 수동 조회하거나 로그 지표 필터를 다시 만들면 복구 가능하다. 다만 이 변경으로
# gilbut-oom-kill 알람(Slack 긴급 알림)은 더 이상 발동하지 않는다 — mem_high/mem_warning/
# swap_high 알람은 그대로 유지된다.

# ── 알림 채널: SNS → AWS Chatbot → Slack ─────────────────────
# 같은 계정 내 CloudWatch 알람은 SNS가 기본 부여하는 토픽 정책(aws:SourceOwner 조건)만으로
# Publish가 허용된다 — 교차 계정이 아니므로 별도 aws_sns_topic_policy 불필요.
#
# slack_workspace_id/slack_channel_id는 AWS Chatbot 콘솔에서 Slack 워크스페이스를
# 최초 1회 수동으로 OAuth 인증해야 발급된다(Terraform으로 자동화 불가).
resource "aws_sns_topic" "alerts" {
  name = "gilbut-alerts"
}

resource "aws_chatbot_slack_channel_configuration" "alerts" {
  configuration_name = "gilbut-alerts"
  iam_role_arn       = aws_iam_role.chatbot.arn
  slack_channel_id   = var.slack_channel_id
  slack_team_id      = var.slack_workspace_id
  sns_topic_arns     = [aws_sns_topic.alerts.arn]
}

# ── 경보 (SNS → Slack 알림) ──────────────────────────────────
# gilbut-oom-kill(OOMKillCount 기반) 알람은 지표 프리티어를 맞추기 위해 위의 로그 지표
# 필터와 함께 제거했다 — 커널 OOM-killer 발화에 대한 실시간 Slack 알림은 더 이상 없다.

resource "aws_cloudwatch_metric_alarm" "mem_high" {
  alarm_name          = "gilbut-mem-high"
  alarm_description   = "❗ [긴급] 가용 메모리 5% 미만(mem_used_percent는 buff/cache 고갈을 못 잡아내 조기경보로 부적합함이 2026-07-14 11:34 OOM-kill 사건 실측으로 확인됨 — mem_available_percent로 대체). 실측상 붕괴가 몇 분 안에 벌어져 evaluation_periods=1 유지."
  namespace           = "Gilbut/EC2"
  metric_name         = "mem_available_percent"
  statistic           = "Average"
  period              = 60
  evaluation_periods  = 1
  threshold           = 5
  comparison_operator = "LessThanThreshold"
  treat_missing_data  = "notBreaching"

  dimensions = {
    InstanceId = aws_instance.app.id
  }

  alarm_actions = [aws_sns_topic.alerts.arn]
  ok_actions    = [aws_sns_topic.alerts.arn]
}

resource "aws_cloudwatch_metric_alarm" "mem_warning" {
  alarm_name          = "gilbut-mem-warning"
  alarm_description   = "⚠️ [경고] 가용 메모리 15% 미만 3분 연속. 5% 미만 즉시 발동은 gilbut-mem-high(긴급)가 담당 — 대응 시간을 벌기 위한 조기 경보."
  namespace           = "Gilbut/EC2"
  metric_name         = "mem_available_percent"
  statistic           = "Average"
  period              = 60
  evaluation_periods  = 3
  threshold           = 15
  comparison_operator = "LessThanThreshold"
  treat_missing_data  = "notBreaching"

  dimensions = {
    InstanceId = aws_instance.app.id
  }

  alarm_actions = [aws_sns_topic.alerts.arn]
  ok_actions    = [aws_sns_topic.alerts.arn]
}

resource "aws_cloudwatch_metric_alarm" "swap_high" {
  alarm_name          = "gilbut-swap-high"
  alarm_description   = "❗ [긴급] swap 사용률 50% 이상 2분 연속. vm.swappiness=10 환경에서는 평소 스왑을 거의 안 쓰므로, 유의미한 스왑 사용 자체가 방어선이 가동 중이라는 신호."
  namespace           = "Gilbut/EC2"
  metric_name         = "swap_used_percent"
  statistic           = "Average"
  period              = 60
  evaluation_periods  = 2
  threshold           = 50
  comparison_operator = "GreaterThanOrEqualToThreshold"
  treat_missing_data  = "notBreaching"

  dimensions = {
    InstanceId = aws_instance.app.id
  }

  alarm_actions = [aws_sns_topic.alerts.arn]
  ok_actions    = [aws_sns_topic.alerts.arn]
}

# ── 대시보드 ─────────────────────────────────────────────────
# 커스텀 메트릭은 dimension 취약성을 피하려고 SEARCH 표현식으로 느슨하게 매칭한다.
resource "aws_cloudwatch_dashboard" "oom" {
  dashboard_name = "gilbut-oom-observability"

  dashboard_body = jsonencode({
    widgets = [
      {
        type   = "metric"
        x      = 0
        y      = 0
        width  = 12
        height = 6
        properties = {
          title  = "Memory & Swap available/used %"
          region = var.region
          view   = "timeSeries"
          period = 60
          stat   = "Average"
          yAxis  = { left = { min = 0, max = 100 } }
          metrics = [
            [{ expression = "SEARCH('{Gilbut/EC2,InstanceId} MetricName=\"mem_available_percent\"', 'Average', 60)", label = "mem_available_percent", id = "m3" }],
            [{ expression = "SEARCH('{Gilbut/EC2,InstanceId} MetricName=\"swap_used_percent\"', 'Average', 60)", label = "swap_used_percent", id = "m2" }]
          ]
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 0
        width  = 12
        height = 6
        properties = {
          title  = "CPU % & Credit Balance"
          region = var.region
          view   = "timeSeries"
          period = 60
          metrics = [
            ["AWS/EC2", "CPUUtilization", "InstanceId", aws_instance.app.id, { stat = "Average", label = "CPUUtilization %" }],
            ["AWS/EC2", "CPUCreditBalance", "InstanceId", aws_instance.app.id, { stat = "Average", label = "CPUCreditBalance", yAxis = "right" }]
          ]
        }
      },
      {
        type   = "metric"
        x      = 0
        y      = 6
        width  = 12
        height = 6
        properties = {
          title  = "java process RSS vs JVM committed (bytes)"
          region = var.region
          view   = "timeSeries"
          period = 60
          stat   = "Average"
          metrics = [
            # {Namespace,Dim1,Dim2,...} 스키마 검색은 "정확히 이 dimension들만 가진" 메트릭을 찾는다.
            # procstat_memory_rss는 dimension이 3개(InstanceId·process_name·pattern)라 dimension을
            # 하나도 안 적은 {Gilbut/EC2}(=dimension 0개 스키마)와 매칭되지 않아 늘 빈 결과였다.
            [{ expression = "SEARCH('{Gilbut/EC2,InstanceId,process_name,pattern} MetricName=\"procstat_memory_rss\"', 'Average', 60)", label = "java RSS", id = "r1" }],
            # app.memory.heap/nonheap.committed는 태그 없는 단일 Gauge라 SEARCH 없이 직접 참조.
            # RSS − (heap.committed + nonheap.committed) = 순수 네이티브 오버헤드(스레드 스택 등)다.
            ["Gilbut/App", "app.memory.heap.committed.value", { label = "heap committed" }],
            ["Gilbut/App", "app.memory.nonheap.committed.value", { label = "nonheap committed" }]
          ]
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 6
        width  = 12
        height = 6
        properties = {
          title  = "Network Traffic (bytes)"
          region = var.region
          view   = "timeSeries"
          period = 60
          stat   = "Sum"
          metrics = [
            ["AWS/EC2", "NetworkIn", "InstanceId", aws_instance.app.id, { label = "NetworkIn" }],
            ["AWS/EC2", "NetworkOut", "InstanceId", aws_instance.app.id, { label = "NetworkOut" }]
          ]
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 12
        width  = 12
        height = 6
        properties = {
          title  = "Network Packets (count)"
          region = var.region
          view   = "timeSeries"
          period = 60
          stat   = "Sum"
          metrics = [
            ["AWS/EC2", "NetworkPacketsIn", "InstanceId", aws_instance.app.id, { label = "NetworkPacketsIn" }],
            ["AWS/EC2", "NetworkPacketsOut", "InstanceId", aws_instance.app.id, { label = "NetworkPacketsOut" }]
          ]
        }
      },
      {
        type   = "metric"
        x      = 0
        y      = 18
        width  = 12
        height = 6
        properties = {
          title  = "JVM Heap/Non-Heap used vs committed (bytes)"
          region = var.region
          view   = "timeSeries"
          period = 60
          stat   = "Average"
          metrics = [
            ["Gilbut/App", "app.memory.heap.used.value", { label = "heap used" }],
            ["Gilbut/App", "app.memory.heap.committed.value", { label = "heap committed" }],
            ["Gilbut/App", "app.memory.nonheap.used.value", { label = "nonheap used" }],
            ["Gilbut/App", "app.memory.nonheap.committed.value", { label = "nonheap committed" }]
          ]
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 18
        width  = 12
        height = 6
        properties = {
          title  = "GC pause (cumulative since JVM start)"
          region = var.region
          view   = "timeSeries"
          period = 60
          stat   = "Maximum"
          metrics = [
            ["Gilbut/App", "app.gc.pause.count.value", { label = "GC count" }],
            ["Gilbut/App", "app.gc.pause.time.value", { label = "GC time (ms)", yAxis = "right" }]
          ]
        }
      }
    ]
  })
}

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
          measurement = ["mem_used_percent", "mem_available_percent", "mem_available", "mem_used", "mem_total"]
        }
        swap = {
          measurement = ["swap_used_percent", "swap_used"]
        }
        cpu = {
          measurement = ["usage_active"]
          totalcpu    = true
        }
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
# 커널 OOM-killer 발화. "?" 는 OR(둘 중 하나라도 포함하는 라인 매칭).
resource "aws_cloudwatch_log_metric_filter" "oom_kill" {
  name           = "gilbut-oom-kill"
  log_group_name = aws_cloudwatch_log_group.kern.name
  pattern        = "?\"invoked oom-killer\" ?\"Out of memory: Killed process\""

  metric_transformation {
    name          = "OOMKillCount"
    namespace     = "Gilbut/EC2"
    value         = "1"
    default_value = "0"
  }
}

# docker events 로거가 남기는 "exitCode=137"(SIGKILL, 컨테이너 OOM 사망) 라인.
resource "aws_cloudwatch_log_metric_filter" "container_die" {
  name           = "gilbut-container-die"
  log_group_name = aws_cloudwatch_log_group.docker_events.name
  pattern        = "\"exitCode=137\""

  metric_transformation {
    name          = "ContainerDieCount"
    namespace     = "Gilbut/EC2"
    value         = "1"
    default_value = "0"
  }
}

# ── 경보 (SNS 액션 없이 상태만; 콘솔 Alarms에서 확인) ────────
resource "aws_cloudwatch_metric_alarm" "oom_kill" {
  alarm_name          = "gilbut-oom-kill"
  alarm_description   = "커널 OOM-killer 발화 감지"
  namespace           = "Gilbut/EC2"
  metric_name         = "OOMKillCount"
  statistic           = "Sum"
  period              = 60
  evaluation_periods  = 1
  threshold           = 1
  comparison_operator = "GreaterThanOrEqualToThreshold"
  treat_missing_data  = "notBreaching"
}

resource "aws_cloudwatch_metric_alarm" "mem_high" {
  alarm_name          = "gilbut-mem-high"
  alarm_description   = "가용 메모리 5% 미만(mem_used_percent는 buff/cache 고갈을 못 잡아내 조기경보로 부적합함이 2026-07-14 11:34 OOM-kill 사건 실측으로 확인됨 — mem_available_percent로 대체)"
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
          title  = "Memory & Swap used %"
          region = var.region
          view   = "timeSeries"
          period = 60
          stat   = "Average"
          yAxis  = { left = { min = 0, max = 100 } }
          metrics = [
            [{ expression = "SEARCH('{Gilbut/EC2,InstanceId} MetricName=\"mem_used_percent\"', 'Average', 60)", label = "mem_used_percent", id = "m1" }],
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
          title  = "java process RSS (bytes)"
          region = var.region
          view   = "timeSeries"
          period = 60
          stat   = "Average"
          metrics = [
            [{ expression = "SEARCH('{Gilbut/EC2} MetricName=\"procstat_memory_rss\"', 'Average', 60)", label = "java RSS", id = "r1" }]
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
          title  = "OOM kills & Container deaths (per min)"
          region = var.region
          view   = "timeSeries"
          period = 60
          stat   = "Sum"
          metrics = [
            ["Gilbut/EC2", "OOMKillCount", { label = "OOMKillCount" }],
            ["Gilbut/EC2", "ContainerDieCount", { label = "ContainerDieCount (exit137)" }]
          ]
        }
      },
      {
        type   = "metric"
        x      = 0
        y      = 12
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
      }
    ]
  })
}

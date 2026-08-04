package backend.capstone.global.config;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

/**
 * Spring Boot 3.x는 micrometer-registry-cloudwatch2의 자동 설정을 제공하지 않아
 * (Prometheus/Datadog 등과 달리 spring-boot-actuator-autoconfigure에 cloudwatch 패키지 자체가 없음)
 * CloudWatchMeterRegistry를 수동으로 배선한다.
 *
 * jvm.memory/jvm.gc의 기본 바인더는 메모리 풀(Eden/Survivor/Metaspace 등)·GC 종류별로 태그를
 * 나눠 등록해 CloudWatch 커스텀 지표 개수를 크게 불린다(application.yml에서
 * management.metrics.enable.jvm.memory/jvm.gc=false로 꺼둠). 대신 MemoryMXBean/
 * GarbageCollectorMXBean(JVM이 이미 풀·GC 종류를 합산해주는 API)을 직접 읽어 태그 없는
 * 집계 지표로 대체한다 — CloudWatch 프리티어(계정+리전 통틀어 10개)를 맞추기 위함.
 *
 * 지표 이름을 반드시 "jvm.memory"/"jvm.gc"가 아닌 "app.memory"/"app.gc"로 지어야 한다 —
 * management.metrics.enable.*는 "누가 등록했는지"가 아니라 "이름이 무엇인지"로 걸러내는
 * MeterFilter라서, jvm.memory/jvm.gc를 끄면 이 커스텀 Gauge도 이름이 겹쳐 조용히
 * 거부(DENY)당한다(에러 없이 등록만 안 됨 — 2026-08-04 실제로 이 문제로 데이터가
 * 하나도 안 들어온 적이 있음). application.yml의 management.metrics.enable.app=true와 짝을
 * 이룬다.
 */
@Configuration
@ConditionalOnProperty(name = "management.cloudwatch.metrics.export.enabled", havingValue = "true")
public class CloudWatchMetricsConfig {

  @Bean
  public CloudWatchAsyncClient cloudWatchAsyncClient() {
    return CloudWatchAsyncClient.create();
  }

  @Bean
  public MeterBinder totalMemoryMetrics() {
    MemoryMXBean memoryMxBean = ManagementFactory.getMemoryMXBean();
    return registry -> {
      Gauge.builder("app.memory.heap.used", memoryMxBean,
          bean -> bean.getHeapMemoryUsage().getUsed()).register(registry);
      Gauge.builder("app.memory.heap.committed", memoryMxBean,
          bean -> bean.getHeapMemoryUsage().getCommitted()).register(registry);
      Gauge.builder("app.memory.nonheap.used", memoryMxBean,
          bean -> bean.getNonHeapMemoryUsage().getUsed()).register(registry);
      Gauge.builder("app.memory.nonheap.committed", memoryMxBean,
          bean -> bean.getNonHeapMemoryUsage().getCommitted()).register(registry);
    };
  }

  @Bean
  public MeterBinder totalGcMetrics() {
    List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
    return registry -> {
      Gauge.builder("app.gc.pause.count", gcBeans,
          beans -> beans.stream().mapToLong(GarbageCollectorMXBean::getCollectionCount).sum())
          .register(registry);
      Gauge.builder("app.gc.pause.time", gcBeans,
          beans -> beans.stream().mapToLong(GarbageCollectorMXBean::getCollectionTime).sum())
          .register(registry);
    };
  }

  @Bean
  public CloudWatchMeterRegistry cloudWatchMeterRegistry(CloudWatchAsyncClient cloudWatchAsyncClient) {
    CloudWatchConfig config = new CloudWatchConfig() {
      @Override
      public String get(String key) {
        return null;
      }

      @Override
      public String namespace() {
        return "Gilbut/App";
      }

      @Override
      public Duration step() {
        return Duration.ofMinutes(1);
      }

      @Override
      public int batchSize() {
        return 20;
      }
    };

    return new CloudWatchMeterRegistry(config, Clock.SYSTEM, cloudWatchAsyncClient);
  }

}

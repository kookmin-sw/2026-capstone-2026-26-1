package backend.capstone.global.config;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

/**
 * Spring Boot 3.x는 micrometer-registry-cloudwatch2의 자동 설정을 제공하지 않아
 * (Prometheus/Datadog 등과 달리 spring-boot-actuator-autoconfigure에 cloudwatch 패키지 자체가 없음)
 * CloudWatchMeterRegistry를 수동으로 배선한다.
 *
 * jvm.memory의 기본 바인더(JvmMemoryMetrics)는 메모리 풀(Eden/Survivor/Metaspace 등)별로
 * 태그를 나눠 등록해 CloudWatch 커스텀 지표 개수를 크게 불린다(application.yml에서
 * management.metrics.enable.jvm.memory=false로 꺼둠). 대신 MemoryMXBean(JVM이 이미 풀
 * 단위를 합산해주는 API)을 직접 읽어 힙/논힙 각각 하나씩, 태그 없는 집계 Gauge로 대체한다.
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
      Gauge.builder("jvm.memory.heap.used", memoryMxBean,
          bean -> bean.getHeapMemoryUsage().getUsed()).register(registry);
      Gauge.builder("jvm.memory.heap.committed", memoryMxBean,
          bean -> bean.getHeapMemoryUsage().getCommitted()).register(registry);
      Gauge.builder("jvm.memory.heap.max", memoryMxBean,
          bean -> bean.getHeapMemoryUsage().getMax()).register(registry);
      Gauge.builder("jvm.memory.nonheap.used", memoryMxBean,
          bean -> bean.getNonHeapMemoryUsage().getUsed()).register(registry);
      Gauge.builder("jvm.memory.nonheap.committed", memoryMxBean,
          bean -> bean.getNonHeapMemoryUsage().getCommitted()).register(registry);
      Gauge.builder("jvm.memory.nonheap.max", memoryMxBean,
          bean -> bean.getNonHeapMemoryUsage().getMax()).register(registry);
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

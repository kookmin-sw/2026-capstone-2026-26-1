package backend.capstone.global.config;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

/**
 * Spring Boot 3.x는 micrometer-registry-cloudwatch2의 자동 설정을 제공하지 않아
 * (Prometheus/Datadog 등과 달리 spring-boot-actuator-autoconfigure에 cloudwatch 패키지 자체가 없음)
 * CloudWatchMeterRegistry를 수동으로 배선한다.
 */
@Configuration
@ConditionalOnProperty(name = "management.cloudwatch.metrics.export.enabled", havingValue = "true")
public class CloudWatchMetricsConfig {

  @Bean
  public CloudWatchAsyncClient cloudWatchAsyncClient() {
    return CloudWatchAsyncClient.create();
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

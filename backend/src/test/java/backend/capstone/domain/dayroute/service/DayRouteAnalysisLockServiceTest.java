package backend.capstone.domain.dayroute.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class DayRouteAnalysisLockServiceTest {

    @Test
    void 같은_dayRouteId_작업은_동시에_실행되지_않는다() throws Exception {
        DayRouteAnalysisLockService lockService = new DayRouteAnalysisLockService();
        Long dayRouteId = 7L;

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(2);
        AtomicInteger activeTasks = new AtomicInteger();
        AtomicInteger maxActiveTasks = new AtomicInteger();

        Runnable guardedTask = () -> {
            ready.countDown();
            try {
                start.await(1, TimeUnit.SECONDS);
                lockService.withLock(dayRouteId, () -> {
                    int active = activeTasks.incrementAndGet();
                    maxActiveTasks.accumulateAndGet(active, Math::max);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        activeTasks.decrementAndGet();
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                finished.countDown();
            }
        };

        executorService.submit(guardedTask);
        executorService.submit(guardedTask);

        assertThat(ready.await(1, TimeUnit.SECONDS)).isTrue();
        start.countDown();
        assertThat(finished.await(2, TimeUnit.SECONDS)).isTrue();

        executorService.shutdownNow();

        assertThat(maxActiveTasks.get()).isEqualTo(1);
    }
}

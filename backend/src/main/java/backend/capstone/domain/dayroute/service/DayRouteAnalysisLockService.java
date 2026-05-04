package backend.capstone.domain.dayroute.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Service;

@Service
public class DayRouteAnalysisLockService {

    private final ConcurrentHashMap<Long, ReentrantLock> locks = new ConcurrentHashMap<>();

    public void withLock(Long dayRouteId, Runnable task) {
        ReentrantLock lock = locks.computeIfAbsent(dayRouteId, ignored -> new ReentrantLock());
        lock.lock(); //락 획득
        try {
            task.run(); //분석 순차 실행
        } finally {
            lock.unlock();
            if (!lock.hasQueuedThreads()) {
                locks.remove(dayRouteId, lock); //대기 중인 스레드가 없으면 맵에서 락을 제거
            }
        }
    }
}

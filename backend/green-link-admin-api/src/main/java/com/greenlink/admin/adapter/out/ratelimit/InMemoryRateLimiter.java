package com.greenlink.admin.adapter.out.ratelimit;

import com.greenlink.core.domain.shop.port.RateLimiter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/** 단일 인스턴스용 슬라이딩 윈도 제한기. 다중 인스턴스가 되면 공유 저장소 기반 구현으로 교체해야 한다. */
@Component
public class InMemoryRateLimiter implements RateLimiter {

    private static final Duration MAX_RETENTION = Duration.ofHours(1);

    private final ConcurrentHashMap<String, Deque<Long>> hits = new ConcurrentHashMap<>();

    @Override
    public boolean tryAcquire(String key, int limit, Duration window) {
        long now = System.currentTimeMillis();
        long cutoff = now - window.toMillis();
        Deque<Long> deque = hits.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (deque) {
            while (!deque.isEmpty() && deque.peekFirst() < cutoff) {
                deque.pollFirst();
            }
            if (deque.size() >= limit) {
                return false;
            }
            deque.addLast(now);
            return true;
        }
    }

    @Scheduled(fixedDelay = 600_000)
    void evictStale() {
        long cutoff = System.currentTimeMillis() - MAX_RETENTION.toMillis();
        hits.entrySet().removeIf(e -> {
            synchronized (e.getValue()) {
                return e.getValue().isEmpty() || e.getValue().peekLast() < cutoff;
            }
        });
    }
}

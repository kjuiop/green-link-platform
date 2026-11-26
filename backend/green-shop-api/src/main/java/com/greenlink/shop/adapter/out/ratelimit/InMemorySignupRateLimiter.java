package com.greenlink.shop.adapter.out.ratelimit;

import com.greenlink.shopcore.application.auth.port.SignupRateLimiter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

/** 단일 인스턴스용 슬라이딩 윈도 제한기. 다중 인스턴스가 되면 공유 저장소 기반 구현으로 교체해야 한다. */
@Component
public class InMemorySignupRateLimiter implements SignupRateLimiter {

    private static final Duration WINDOW = Duration.ofHours(1);
    private static final Map<Bucket, Integer> LIMITS = new EnumMap<>(Map.of(Bucket.SIGNUP, 5, Bucket.LOOKUP, 30, Bucket.PASSWORD_SETUP, 10));

    private final ConcurrentHashMap<String, Deque<Long>> hits = new ConcurrentHashMap<>();
    private final LongSupplier nowMillis;

    public InMemorySignupRateLimiter() {
        this(System::currentTimeMillis);
    }

    InMemorySignupRateLimiter(LongSupplier nowMillis) {
        this.nowMillis = nowMillis;
    }

    @Override
    public boolean tryAcquire(Bucket bucket, String clientKey) {
        long now = nowMillis.getAsLong();
        long cutoff = now - WINDOW.toMillis();
        Deque<Long> deque = hits.computeIfAbsent(bucket + ":" + clientKey, k -> new ArrayDeque<>());
        synchronized (deque) {
            while (!deque.isEmpty() && deque.peekFirst() <= cutoff) {
                deque.pollFirst();
            }
            if (deque.size() >= LIMITS.get(bucket)) {
                return false;
            }
            deque.addLast(now);
            return true;
        }
    }

    @Scheduled(fixedDelay = 600_000)
    void evictStale() {
        long cutoff = nowMillis.getAsLong() - WINDOW.toMillis();
        hits.entrySet().removeIf(e -> {
            synchronized (e.getValue()) {
                return e.getValue().isEmpty() || e.getValue().peekLast() <= cutoff;
            }
        });
    }
}

package com.greenlink.shop.adapter.out.ratelimit;

import com.greenlink.shopcore.application.auth.port.SignupRateLimiter.Bucket;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class InMemorySignupRateLimiterTest {

    private final AtomicLong now = new AtomicLong(1_000_000L);
    private final InMemorySignupRateLimiter limiter = new InMemorySignupRateLimiter(now::get);

    @Test
    void 가입은_IP당_1시간에_5회까지_허용하고_6번째는_거부한다() {
        for (int i = 0; i < 5; i++) {
            assertThat(limiter.tryAcquire(Bucket.SIGNUP, "1.1.1.1")).isTrue();
        }
        assertThat(limiter.tryAcquire(Bucket.SIGNUP, "1.1.1.1")).isFalse();
    }

    @Test
    void IP가_다르면_독립적으로_센다() {
        for (int i = 0; i < 5; i++) {
            limiter.tryAcquire(Bucket.SIGNUP, "1.1.1.1");
        }
        assertThat(limiter.tryAcquire(Bucket.SIGNUP, "2.2.2.2")).isTrue();
    }

    @Test
    void 윈도가_지나면_다시_허용한다() {
        for (int i = 0; i < 5; i++) {
            limiter.tryAcquire(Bucket.SIGNUP, "1.1.1.1");
        }
        now.addAndGet(3_600_001L);

        assertThat(limiter.tryAcquire(Bucket.SIGNUP, "1.1.1.1")).isTrue();
    }

    @Test
    void 조회_버킷은_가입_버킷과_별도로_더_넉넉한_한도를_가진다() {
        for (int i = 0; i < 5; i++) {
            limiter.tryAcquire(Bucket.SIGNUP, "1.1.1.1");
        }
        for (int i = 0; i < 30; i++) {
            assertThat(limiter.tryAcquire(Bucket.LOOKUP, "1.1.1.1")).isTrue();
        }
        assertThat(limiter.tryAcquire(Bucket.LOOKUP, "1.1.1.1")).isFalse();
    }

    @Test
    void 오래된_기록은_청소된다() {
        limiter.tryAcquire(Bucket.SIGNUP, "1.1.1.1");
        now.addAndGet(3_600_001L);

        limiter.evictStale();

        for (int i = 0; i < 5; i++) {
            assertThat(limiter.tryAcquire(Bucket.SIGNUP, "1.1.1.1")).isTrue();
        }
    }
}

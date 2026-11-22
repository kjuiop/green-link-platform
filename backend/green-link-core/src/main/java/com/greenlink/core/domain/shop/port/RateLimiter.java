package com.greenlink.core.domain.shop.port;

import java.time.Duration;

public interface RateLimiter {

    /** window 안에서 key당 limit회까지 허용한다. 허용되면 true, 초과하면 false. */
    boolean tryAcquire(String key, int limit, Duration window);
}

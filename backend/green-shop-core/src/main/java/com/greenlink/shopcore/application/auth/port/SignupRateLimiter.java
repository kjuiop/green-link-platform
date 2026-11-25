package com.greenlink.shopcore.application.auth.port;

public interface SignupRateLimiter {

    enum Bucket { SIGNUP, LOOKUP }

    boolean tryAcquire(Bucket bucket, String clientKey);
}

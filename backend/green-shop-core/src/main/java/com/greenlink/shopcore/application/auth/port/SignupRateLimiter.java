package com.greenlink.shopcore.application.auth.port;

public interface SignupRateLimiter {

    enum Bucket { SIGNUP, LOOKUP, PASSWORD_SETUP }

    boolean tryAcquire(Bucket bucket, String clientKey);
}

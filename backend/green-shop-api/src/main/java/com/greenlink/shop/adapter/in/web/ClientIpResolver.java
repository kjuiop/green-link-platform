package com.greenlink.shop.adapter.in.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class ClientIpResolver {

    private final byte[] proxySecret;

    public ClientIpResolver(@Value("${shop.proxy.secret:}") String proxySecret) {
        this.proxySecret = proxySecret.getBytes(StandardCharsets.UTF_8);
    }

    // 프록시(Vercel) 서버의 IP가 아니라 실제 사용자 IP를 쓰기 위해, 공유 시크릿이 맞을 때만 X-Shop-Client-Ip 를 신뢰한다.
    public String resolve(HttpServletRequest http) {
        String proxied = http.getHeader("X-Shop-Client-Ip");
        String secret = http.getHeader("X-Shop-Proxy-Secret");
        if (proxySecret.length > 0 && proxied != null && !proxied.isBlank() && secret != null
                && MessageDigest.isEqual(proxySecret, secret.getBytes(StandardCharsets.UTF_8))) {
            return proxied.trim();
        }
        String forwarded = http.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            String[] hops = forwarded.split(",");
            return hops[hops.length - 1].trim();
        }
        return http.getRemoteAddr();
    }
}

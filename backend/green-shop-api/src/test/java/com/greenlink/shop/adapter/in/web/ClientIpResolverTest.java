package com.greenlink.shop.adapter.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ClientIpResolverTest {

    private MockHttpServletRequest request() {
        MockHttpServletRequest r = new MockHttpServletRequest();
        r.setRemoteAddr("10.0.0.1");
        return r;
    }

    @Test
    void 시크릿이_맞으면_프록시가_전달한_IP를_쓴다() {
        MockHttpServletRequest r = request();
        r.addHeader("X-Shop-Client-Ip", " 9.9.9.9 ");
        r.addHeader("X-Shop-Proxy-Secret", "s3cret");

        assertThat(new ClientIpResolver("s3cret").resolve(r)).isEqualTo("9.9.9.9");
    }

    @Test
    void 시크릿이_틀리면_헤더를_무시하고_XFF_마지막_hop을_쓴다() {
        MockHttpServletRequest r = request();
        r.addHeader("X-Shop-Client-Ip", "9.9.9.9");
        r.addHeader("X-Shop-Proxy-Secret", "wrong");
        r.addHeader("X-Forwarded-For", "6.6.6.6, 7.7.7.7");

        assertThat(new ClientIpResolver("s3cret").resolve(r)).isEqualTo("7.7.7.7");
    }

    @Test
    void 시크릿이_설정되지_않으면_프록시_헤더를_신뢰하지_않는다() {
        MockHttpServletRequest r = request();
        r.addHeader("X-Shop-Client-Ip", "9.9.9.9");
        r.addHeader("X-Shop-Proxy-Secret", "");

        assertThat(new ClientIpResolver("").resolve(r)).isEqualTo("10.0.0.1");
    }
}

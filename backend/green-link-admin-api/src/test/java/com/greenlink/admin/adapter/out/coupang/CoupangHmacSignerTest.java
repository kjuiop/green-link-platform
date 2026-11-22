package com.greenlink.admin.adapter.out.coupang;

import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CoupangHmacSignerTest {

    @Test
    void 서명_헤더_형식과_결정적_결과() {
        ZonedDateTime now = ZonedDateTime.of(2026, 11, 19, 1, 2, 3, 0, ZoneOffset.UTC);

        String a = CoupangHmacSigner.authorization("GET", "/v2/x", "a=1&b=2", "AK", "SK", now);
        String b = CoupangHmacSigner.authorization("GET", "/v2/x", "a=1&b=2", "AK", "SK", now);

        assertThat(a).isEqualTo(b);
        assertThat(a).startsWith("CEA algorithm=HmacSHA256, access-key=AK, signed-date=261119T010203Z, signature=");
        assertThat(a.substring(a.indexOf("signature=") + 10)).matches("[0-9a-f]{64}");
    }

    @Test
    void 쿼리가_다르면_서명도_다르다() {
        ZonedDateTime now = ZonedDateTime.of(2026, 11, 19, 1, 2, 3, 0, ZoneOffset.UTC);

        assertThat(CoupangHmacSigner.authorization("GET", "/v2/x", "a=1", "AK", "SK", now))
                .isNotEqualTo(CoupangHmacSigner.authorization("GET", "/v2/x", "a=2", "AK", "SK", now));
    }

    @Test
    void 한국시간_입력도_UTC로_변환해_서명한다() {
        ZonedDateTime kst = ZonedDateTime.of(2026, 11, 19, 10, 2, 3, 0, java.time.ZoneId.of("Asia/Seoul"));

        assertThat(CoupangHmacSigner.authorization("GET", "/p", "", "AK", "SK", kst))
                .contains("signed-date=261119T010203Z");
    }
}

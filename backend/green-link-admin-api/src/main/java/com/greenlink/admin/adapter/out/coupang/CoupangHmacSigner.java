package com.greenlink.admin.adapter.out.coupang;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;

public final class CoupangHmacSigner {

    private static final DateTimeFormatter SIGNED_DATE = DateTimeFormatter.ofPattern("yyMMdd'T'HHmmss'Z'");

    private CoupangHmacSigner() {}

    /** query는 '?' 없이, 실제 요청 URL에 쓰는 문자열과 동일해야 한다. */
    public static String authorization(String method, String path, String query,
                                       String accessKey, String secretKey, ZonedDateTime now) {
        String signedDate = now.withZoneSameInstant(ZoneOffset.UTC).format(SIGNED_DATE);
        String message = signedDate + method + path + (query == null ? "" : query);
        return "CEA algorithm=HmacSHA256, access-key=" + accessKey
                + ", signed-date=" + signedDate + ", signature=" + hmacHex(secretKey, message);
    }

    private static String hmacHex(String secretKey, String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("HMAC 서명 생성에 실패했습니다.", e);
        }
    }
}

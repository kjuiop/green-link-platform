package com.greenlink.admin.adapter.out.coupang;

import com.greenlink.core.global.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AesGcmSecretCipherTest {

    private static final String KEY = Base64.getEncoder().encodeToString(new byte[32]);

    @Test
    void 암호화_후_복호화하면_원문이_나온다() {
        AesGcmSecretCipher cipher = new AesGcmSecretCipher(KEY);

        String encrypted = cipher.encrypt("my-secret");

        assertThat(encrypted).isNotEqualTo("my-secret");
        assertThat(cipher.decrypt(encrypted)).isEqualTo("my-secret");
    }

    @Test
    void 같은_값도_매번_다른_암호문이_나온다() {
        AesGcmSecretCipher cipher = new AesGcmSecretCipher(KEY);

        assertThat(cipher.encrypt("x")).isNotEqualTo(cipher.encrypt("x"));
    }

    @Test
    void 키가_없으면_사용할_수_없다() {
        AesGcmSecretCipher cipher = new AesGcmSecretCipher("");

        assertThatThrownBy(() -> cipher.encrypt("x")).isInstanceOf(BusinessException.class);
    }

    @Test
    void 키_길이가_잘못되면_기동_단계에서_실패한다() {
        String shortKey = Base64.getEncoder().encodeToString(new byte[16]);

        assertThatThrownBy(() -> new AesGcmSecretCipher(shortKey)).isInstanceOf(IllegalStateException.class);
    }
}

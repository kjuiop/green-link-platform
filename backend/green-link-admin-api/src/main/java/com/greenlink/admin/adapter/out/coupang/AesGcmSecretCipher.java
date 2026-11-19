package com.greenlink.admin.adapter.out.coupang;

import com.greenlink.core.domain.coupang.port.SecretCipher;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.coupang.CoupangErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class AesGcmSecretCipher implements SecretCipher {

    private static final int IV_LENGTH = 12;
    private static final int TAG_BITS = 128;

    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    public AesGcmSecretCipher(@Value("${coupang.credential-key:}") String base64Key) {
        if (base64Key == null || base64Key.isBlank()) {
            this.key = null;
        } else {
            byte[] raw = Base64.getDecoder().decode(base64Key.trim());
            if (raw.length != 32) {
                throw new IllegalStateException("COUPANG_CRED_KEY는 base64로 인코딩된 32바이트 값이어야 합니다.");
            }
            this.key = new SecretKeySpec(raw, "AES");
        }
    }

    @Override
    public String encrypt(String plain) {
        requireKey();
        try {
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(encrypted, 0, out, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalStateException("암호화에 실패했습니다.", e);
        }
    }

    @Override
    public String decrypt(String encrypted) {
        requireKey();
        try {
            byte[] in = Base64.getDecoder().decode(encrypted);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, in, 0, IV_LENGTH));
            return new String(cipher.doFinal(in, IV_LENGTH, in.length - IV_LENGTH), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("복호화에 실패했습니다. 암호화 키가 변경되었는지 확인해 주세요.", e);
        }
    }

    private void requireKey() {
        if (key == null) {
            throw new BusinessException(CoupangErrorCode.CIPHER_NOT_CONFIGURED);
        }
    }
}

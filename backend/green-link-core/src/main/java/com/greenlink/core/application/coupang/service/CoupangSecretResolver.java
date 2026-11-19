package com.greenlink.core.application.coupang.service;

import com.greenlink.core.domain.coupang.model.CoupangCredential;
import com.greenlink.core.domain.coupang.model.CoupangCredentialSecret;
import com.greenlink.core.domain.coupang.port.SecretCipher;
import org.springframework.stereotype.Component;

@Component
public class CoupangSecretResolver {

    private final SecretCipher cipher;

    public CoupangSecretResolver(SecretCipher cipher) {
        this.cipher = cipher;
    }

    public CoupangCredentialSecret resolve(CoupangCredential c) {
        return new CoupangCredentialSecret(c.getVendorCode(),
                cipher.decrypt(c.getAccessKeyEnc()), cipher.decrypt(c.getSecretKeyEnc()));
    }
}

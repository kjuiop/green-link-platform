package com.greenlink.core.domain.coupang.port;

public interface SecretCipher {

    String encrypt(String plain);

    String decrypt(String encrypted);
}

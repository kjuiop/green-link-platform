package com.greenlink.core.domain.coupang.model;

public record CoupangCredentialSecret(String vendorCode, String accessKey, String secretKey) {

    @Override
    public String toString() {
        return "CoupangCredentialSecret[vendorCode=" + vendorCode + ", accessKey=***, secretKey=***]";
    }
}

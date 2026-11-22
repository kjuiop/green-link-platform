package com.greenlink.core.domain.fax.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

public record OrderSheet(
        String orderNumber,
        String orderingCompanyName,
        String orderingCompanyPhone,
        String vendorName,
        String vendorPhone,
        String productSummary,
        long totalAmount,
        String deliveryAddress,
        String deliveryDateTime,
        String customerName,
        String customerPhone,
        String ribbonLeft,
        String ribbonRight,
        String ribbonExtrasJson,
        String eventInfo,
        String vendorMemo
) {

    public String contentHash() {
        List<String> parts = List.of(
                nz(orderNumber), nz(orderingCompanyName), nz(orderingCompanyPhone), nz(vendorName), nz(vendorPhone),
                nz(productSummary), String.valueOf(totalAmount), nz(deliveryAddress), nz(deliveryDateTime),
                nz(customerName), nz(customerPhone), nz(ribbonLeft), nz(ribbonRight), nz(ribbonExtrasJson),
                nz(eventInfo), nz(vendorMemo));
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(String.join("\u001f", parts).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}

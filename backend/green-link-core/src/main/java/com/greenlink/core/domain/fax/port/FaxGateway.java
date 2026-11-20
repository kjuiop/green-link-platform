package com.greenlink.core.domain.fax.port;

public interface FaxGateway {

    record FaxAccount(String apiKey, String corpNum, String userId, String senderNumber, String serviceUrl,
                      String ftpHost, Integer ftpPort, String ftpUsername, String ftpPassword, String ftpDirectory) {}

    record FaxSendResult(boolean success, String sendKey, String failReason) {}

    enum FaxState { PENDING, DELIVERED, FAILED }

    FaxSendResult send(FaxAccount account, String fileName, byte[] pdf, String toNumber, String receiveName, String refKey);

    FaxState getState(FaxAccount account, String sendKey);
}

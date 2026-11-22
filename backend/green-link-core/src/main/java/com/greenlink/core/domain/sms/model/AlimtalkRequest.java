package com.greenlink.core.domain.sms.model;

public record AlimtalkRequest(
        String apiKey,
        String userId,
        String corpNum,
        String senderKey,
        String templateCode,
        String callback,
        String receiver,
        AlimtalkVariables variables) {

    public record AlimtalkVariables(String companyName, String recipientName,
                                    String orderNumber, String placeName, String photoToken) {}
}

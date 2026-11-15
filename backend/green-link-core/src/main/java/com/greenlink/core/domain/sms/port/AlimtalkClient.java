package com.greenlink.core.domain.sms.port;

public interface AlimtalkClient {

    AlimtalkSendResult send(String apiKey, String senderKey, String templateCode,
                            String variable, String callback, String dstaddr);

    record AlimtalkSendResult(boolean success, boolean replaced, String resultCode) {}
}

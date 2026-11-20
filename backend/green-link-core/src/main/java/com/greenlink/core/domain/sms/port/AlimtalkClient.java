package com.greenlink.core.domain.sms.port;

import com.greenlink.core.domain.sms.model.AlimtalkRequest;

public interface AlimtalkClient {

    AlimtalkSendResult send(AlimtalkRequest request);

    record AlimtalkSendResult(boolean success, boolean replaced, String resultCode) {}
}

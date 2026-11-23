package com.greenlink.shop.adapter.in.web.dto;

import com.greenlink.shopcore.application.access.dto.MyAccess;

public record MyAccessResponse(String accessStatus, String rejectReason) {

    public static MyAccessResponse from(MyAccess a) {
        return new MyAccessResponse(a.accessStatus().name(), a.rejectReason());
    }
}

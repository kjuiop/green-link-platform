package com.greenlink.shop.adapter.in.web.dto;

import com.greenlink.shopcore.application.access.dto.MyAccess;

public record MyAccessResponse(String accessStatus, String rejectReason, String requestType, String requestedRole) {

    public static MyAccessResponse from(MyAccess a) {
        return new MyAccessResponse(a.accessStatus().name(), a.rejectReason(),
            a.requestType() == null ? null : a.requestType().name(),
            a.requestedRole() == null ? null : a.requestedRole().name());
    }
}

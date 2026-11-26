package com.greenlink.shop.adapter.in.web.dto;

import com.greenlink.shopcore.application.access.dto.MyAccess;

import java.util.List;

public record MyAccessResponse(String accessStatus, String rejectReason, String requestType, String requestedRole,
                               List<Long> requestedBrandIds) {

    public static MyAccessResponse from(MyAccess a) {
        return new MyAccessResponse(a.accessStatus().name(), a.rejectReason(),
            a.requestType() == null ? null : a.requestType().name(),
            a.requestedRole() == null ? null : a.requestedRole().name(),
            a.requestedBrandIds() == null ? List.of() : a.requestedBrandIds().stream().sorted().toList());
    }
}

package com.greenlink.shop.adapter.in.web.dto;

import com.greenlink.shopcore.application.onurim.dto.OnurimDocumentRequirement;

public record OnurimDocumentRequirementResponse(boolean needsBusinessReg, boolean needsBankBook) {

    public static OnurimDocumentRequirementResponse from(OnurimDocumentRequirement r) {
        return new OnurimDocumentRequirementResponse(r.needsBusinessReg(), r.needsBankBook());
    }
}

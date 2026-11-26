package com.greenlink.admin.adapter.in.web.dto;

import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.onurimapplication.model.OnurimApplicationInfo;
import com.greenlink.core.domain.onurimapplication.model.OnurimApplicationRegion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OnurimApplicationApplyRequest(
    @NotNull Long adminId,
    @NotNull Long companyId,
    @NotNull CompanyType desiredType,
    @NotBlank @Size(max = 30) String companyPhone,
    @NotBlank @Size(max = 255) String address,
    @Size(max = 255) String detailAddress,
    @Size(max = 255) String taxInvoiceEmail,
    @Size(max = 500) String memo,
    @Size(max = 30) String faxNumber,
    @Size(max = 50) String bankName,
    @Size(max = 50) String accountNumber,
    @Size(max = 50) String accountHolder,
    @Valid @Size(max = 50) List<OnurimApplicationRegionRequest> regions
) {
    public OnurimApplicationInfo toInfo() {
        List<OnurimApplicationRegion> mapped = regions == null ? List.of()
            : regions.stream().map(r -> new OnurimApplicationRegion(r.sido(), r.sigungu())).toList();
        return new OnurimApplicationInfo(companyPhone, address, detailAddress, taxInvoiceEmail, memo,
            faxNumber, bankName, accountNumber, accountHolder, mapped);
    }
}

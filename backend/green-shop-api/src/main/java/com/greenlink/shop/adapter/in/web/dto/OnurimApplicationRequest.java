package com.greenlink.shop.adapter.in.web.dto;

import com.greenlink.shopcore.application.onurim.dto.OnurimApplyCommand;
import com.greenlink.shopcore.application.onurim.dto.OnurimCompanyType;
import com.greenlink.shopcore.application.onurim.dto.OnurimDocument;
import com.greenlink.shopcore.application.onurim.dto.OnurimRegion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OnurimApplicationRequest(
    @NotNull OnurimCompanyType desiredType,
    @NotBlank @Size(max = 30) String companyPhone,
    @NotBlank @Size(max = 255) String address,
    @Size(max = 255) String detailAddress,
    @Email @Size(max = 255) String taxInvoiceEmail,
    @Size(max = 500) String memo,
    @Size(max = 30) String faxNumber,
    @Size(max = 50) String bankName,
    @Size(max = 50) String accountNumber,
    @Size(max = 50) String accountHolder,
    @Valid @Size(max = 50) List<RegionItem> regions
) {
    public record RegionItem(
        @NotBlank @Size(max = 30) @Pattern(regexp = "[^,|]*") String sido,
        @Size(max = 30) @Pattern(regexp = "[^,|]*") String sigungu
    ) {}

    public OnurimApplyCommand toCommand(OnurimDocument businessDoc, OnurimDocument bankDoc) {
        List<OnurimRegion> mapped = regions == null ? List.of()
            : regions.stream().map(r -> new OnurimRegion(r.sido(), r.sigungu())).toList();
        return new OnurimApplyCommand(desiredType, companyPhone, address, detailAddress, taxInvoiceEmail, memo,
            faxNumber, bankName, accountNumber, accountHolder, mapped, businessDoc, bankDoc);
    }
}

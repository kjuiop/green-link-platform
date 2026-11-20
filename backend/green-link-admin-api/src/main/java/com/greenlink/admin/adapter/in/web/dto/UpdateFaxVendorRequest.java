package com.greenlink.admin.adapter.in.web.dto;

import jakarta.validation.constraints.Size;

public record UpdateFaxVendorRequest(
        boolean faxEnabled,
        @Size(max = 20) String faxNumber
) {}

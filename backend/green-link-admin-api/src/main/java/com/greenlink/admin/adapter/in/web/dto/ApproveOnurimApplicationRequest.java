package com.greenlink.admin.adapter.in.web.dto;

import com.greenlink.core.domain.company.model.CompanyType;
import jakarta.validation.constraints.NotNull;

public record ApproveOnurimApplicationRequest(@NotNull CompanyType type) {}

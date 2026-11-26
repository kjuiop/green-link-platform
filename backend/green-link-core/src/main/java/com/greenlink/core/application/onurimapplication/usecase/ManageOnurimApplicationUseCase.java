package com.greenlink.core.application.onurimapplication.usecase;

import com.greenlink.core.application.onurimapplication.dto.OnurimApplicationResult;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.onurimapplication.model.OnurimApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ManageOnurimApplicationUseCase {
    Page<OnurimApplicationResult> list(OnurimApplicationStatus status, Pageable pageable);
    OnurimApplicationResult get(Long id);
    OnurimApplicationResult approve(Long id, CompanyType type, Long operatorId);
    OnurimApplicationResult reject(Long id, String reason, Long operatorId);
}

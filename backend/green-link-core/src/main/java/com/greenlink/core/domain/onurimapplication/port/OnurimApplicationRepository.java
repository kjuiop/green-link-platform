package com.greenlink.core.domain.onurimapplication.port;

import com.greenlink.core.domain.onurimapplication.model.OnurimApplication;
import com.greenlink.core.domain.onurimapplication.model.OnurimApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface OnurimApplicationRepository {
    OnurimApplication save(OnurimApplication application);
    Optional<OnurimApplication> findById(Long id);
    Optional<OnurimApplication> findLatestByCompanyId(Long companyId);
    boolean existsPendingByCompanyId(Long companyId);
    Page<OnurimApplication> findAllByStatus(OnurimApplicationStatus status, Pageable pageable);
}

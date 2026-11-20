package com.greenlink.core.application.fax.usecase;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.fax.dto.FaxMessageResult;
import com.greenlink.core.application.fax.dto.FaxVendorResult;
import org.springframework.data.domain.Pageable;

public interface ManageFaxUseCase {

    PageResult<FaxVendorResult> getVendors(String keyword, Pageable pageable);

    FaxVendorResult updateVendor(Long companyId, boolean faxEnabled, String faxNumber);

    PageResult<FaxMessageResult> getMessages(String status, String keyword, Pageable pageable);
}

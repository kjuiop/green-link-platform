package com.greenlink.core.application.coupang.usecase;

import com.greenlink.core.application.coupang.dto.CoupangSyncResult;
import com.greenlink.core.domain.coupang.model.CoupangTriggerType;

public interface CollectCoupangOrdersUseCase {

    CoupangSyncResult collect(Long companyId, CoupangTriggerType trigger);
}

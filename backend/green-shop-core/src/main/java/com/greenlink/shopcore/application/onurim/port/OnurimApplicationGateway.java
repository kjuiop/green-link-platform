package com.greenlink.shopcore.application.onurim.port;

import com.greenlink.shopcore.application.onurim.dto.OnurimApplicationView;
import com.greenlink.shopcore.application.onurim.dto.OnurimApplyCommand;

import java.util.Optional;

public interface OnurimApplicationGateway {

    OnurimApplicationView apply(Long adminId, Long companyId, OnurimApplyCommand command);

    Optional<OnurimApplicationView> findLatest(Long companyId);
}

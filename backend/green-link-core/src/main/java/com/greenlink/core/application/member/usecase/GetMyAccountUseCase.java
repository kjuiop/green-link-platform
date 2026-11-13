package com.greenlink.core.application.member.usecase;

import com.greenlink.core.application.member.dto.CompanyAccountResult;

public interface GetMyAccountUseCase {
    CompanyAccountResult getMyAccount(Long companyId);
}

package com.greenlink.core.application.member.usecase;

import com.greenlink.core.application.member.dto.MemberSummaryResult;

public interface GetMemberSummaryUseCase {
    MemberSummaryResult getMemberSummary(Long companyId);
}

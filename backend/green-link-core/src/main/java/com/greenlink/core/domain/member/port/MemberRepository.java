package com.greenlink.core.domain.member.port;

import com.greenlink.core.domain.member.model.Member;

public interface MemberRepository {
    boolean existsByLoginId(String loginId);
    Member save(Member member);
}

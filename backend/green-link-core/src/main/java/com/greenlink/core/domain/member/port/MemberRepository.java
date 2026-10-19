package com.greenlink.core.domain.member.port;

import com.greenlink.core.domain.member.model.Member;
import com.greenlink.core.domain.member.model.Role;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    boolean existsByLoginId(String loginId);
    Optional<Member> findByLoginId(String loginId);
    Member save(Member member);
    Optional<Member> findById(Long id);
    List<Member> findAll();
    List<Member> findAllByRole(Role role);
    List<Member> findAllByCompanyId(Long companyId);
}

package com.greenlink.admin.adapter.out.persistence.member;

import com.greenlink.core.domain.member.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, Long> {
    boolean existsByLoginId(String loginId);
    List<MemberJpaEntity> findByCompanyId(Long companyId);
    List<MemberJpaEntity> findByRoles_Role(Role role);
}

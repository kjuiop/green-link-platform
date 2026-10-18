package com.greenlink.api.adapter.out.persistence.member;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, Long> {
    boolean existsByLoginId(String loginId);
}

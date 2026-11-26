package com.greenlink.shopcore.application.admin.service;

import com.greenlink.shopcore.domain.brand.port.ShopBrandMemberRepository;
import com.greenlink.shopcore.domain.member.model.ShopAdminMember;
import com.greenlink.shopcore.domain.member.port.ShopAdminMemberRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Component
public class MemberApprovalWriter {

    private final ShopAdminMemberRepository memberRepository;
    private final ShopBrandMemberRepository brandMemberRepository;

    public MemberApprovalWriter(ShopAdminMemberRepository memberRepository,
                                ShopBrandMemberRepository brandMemberRepository) {
        this.memberRepository = memberRepository;
        this.brandMemberRepository = brandMemberRepository;
    }

    @Transactional
    public void save(ShopAdminMember member, Set<Long> brandIds, LocalDateTime now) {
        memberRepository.save(member);
        brandMemberRepository.replaceAssignments(member.getId(), brandIds, now);
    }

    @Transactional
    public Long saveNew(ShopAdminMember member, Set<Long> brandIds, LocalDateTime now) {
        ShopAdminMember saved = memberRepository.save(member);
        brandMemberRepository.replaceAssignments(saved.getId(), brandIds, now);
        return saved.getId();
    }
}

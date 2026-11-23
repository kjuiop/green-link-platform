package com.greenlink.shopcore.application.admin.service;

import com.greenlink.shopcore.application.admin.dto.ShopAdminView;
import com.greenlink.shopcore.application.admin.usecase.ShopAdminManagementUseCase;
import com.greenlink.shopcore.domain.member.model.ShopAdminMember;
import com.greenlink.shopcore.domain.member.model.ShopAdminStatus;
import com.greenlink.shopcore.domain.member.port.ShopAdminMemberRepository;
import com.greenlink.shopcore.global.exception.ShopBusinessException;
import com.greenlink.shopcore.global.exception.ShopErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ShopAdminManagementService implements ShopAdminManagementUseCase {

    private final ShopAdminMemberRepository memberRepository;
    private final Clock clock;

    public ShopAdminManagementService(ShopAdminMemberRepository memberRepository, Clock clock) {
        this.memberRepository = memberRepository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopAdminView> list(ShopAdminStatus status) {
        return memberRepository.findAll(status).stream().map(ShopAdminView::from).toList();
    }

    @Override
    public void approve(Long id, Long actorAdminId) {
        ShopAdminMember member = find(id);
        member.approve(actorAdminId, LocalDateTime.now(clock));
        memberRepository.save(member);
    }

    @Override
    public void reject(Long id, String reason, Long actorAdminId) {
        ShopAdminMember member = find(id);
        member.reject(reason, actorAdminId, LocalDateTime.now(clock));
        memberRepository.save(member);
    }

    private ShopAdminMember find(Long id) {
        return memberRepository.findById(id)
            .orElseThrow(() -> new ShopBusinessException(ShopErrorCode.ADMIN_REQUEST_NOT_FOUND));
    }
}

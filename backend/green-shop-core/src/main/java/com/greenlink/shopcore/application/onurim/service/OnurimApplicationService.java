package com.greenlink.shopcore.application.onurim.service;

import com.greenlink.shopcore.application.common.ManagementActor;
import com.greenlink.shopcore.application.common.ManagementGuard;
import com.greenlink.shopcore.application.onurim.dto.OnurimApplicationView;
import com.greenlink.shopcore.application.onurim.dto.OnurimApplyCommand;
import com.greenlink.shopcore.application.onurim.dto.OnurimDocumentRequirement;
import com.greenlink.shopcore.application.onurim.port.OnurimApplicationGateway;
import com.greenlink.shopcore.application.onurim.usecase.OnurimApplicationUseCase;
import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.global.exception.ShopBusinessException;
import com.greenlink.shopcore.global.exception.ShopErrorCode;
import org.springframework.stereotype.Service;

import java.util.Optional;

// 온우림 호출이 트랜잭션 안에 갇히지 않도록 @Transactional을 두지 않는다.
@Service
public class OnurimApplicationService implements OnurimApplicationUseCase {

    private final OnurimApplicationGateway gateway;
    private final ManagementGuard guard;

    public OnurimApplicationService(OnurimApplicationGateway gateway, ManagementGuard guard) {
        this.gateway = gateway;
        this.guard = guard;
    }

    @Override
    public OnurimApplicationView apply(ManagementActor actor, OnurimApplyCommand command) {
        Long companyId = requirePartnerCompany(actor);
        return gateway.apply(actor.adminId(), companyId, command);
    }

    @Override
    public Optional<OnurimApplicationView> getLatest(ManagementActor actor) {
        return gateway.findLatest(requirePartnerCompany(actor));
    }

    @Override
    public OnurimDocumentRequirement getDocumentRequirement(ManagementActor actor) {
        return gateway.getDocumentRequirement(actor.adminId(), requirePartnerCompany(actor));
    }

    private Long requirePartnerCompany(ManagementActor actor) {
        if (actor.role() != ShopRole.PARTNER_ADMIN) {
            throw new ShopBusinessException(ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
        }
        Long companyId = guard.requireManager(actor);
        if (companyId == null) {
            throw new ShopBusinessException(ShopErrorCode.ACCESS_MANAGE_FORBIDDEN);
        }
        return companyId;
    }
}

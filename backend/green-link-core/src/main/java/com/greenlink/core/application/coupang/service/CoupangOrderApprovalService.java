package com.greenlink.core.application.coupang.service;

import com.greenlink.core.application.coupang.dto.CoupangOrderResult;
import com.greenlink.core.application.coupang.usecase.ProcessCoupangOrderUseCase;
import com.greenlink.core.application.order.dto.CreateOrderCommand;
import com.greenlink.core.application.order.dto.OrderResult;
import com.greenlink.core.domain.coupang.model.CoupangCredential;
import com.greenlink.core.domain.coupang.model.CoupangOrder;
import com.greenlink.core.domain.coupang.model.CoupangOrderSheet;
import com.greenlink.core.domain.coupang.port.CoupangApiException;
import com.greenlink.core.domain.coupang.port.CoupangApiPort;
import com.greenlink.core.domain.coupang.port.CoupangCredentialRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.coupang.CoupangErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.function.LongFunction;

/** 승인 전 쿠팡 실시간 상태를 확인(외부 호출)한 뒤 트랜잭션 서비스에 위임한다. 자체 트랜잭션을 열지 않는다. */
@Service
public class CoupangOrderApprovalService implements ProcessCoupangOrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(CoupangOrderApprovalService.class);
    private static final Set<String> APPROVABLE_COUPANG_STATUS = Set.of("ACCEPT", "INSTRUCT");

    private final CoupangOrderStateService stateService;
    private final CoupangCredentialRepository credentialRepository;
    private final CoupangApiPort apiPort;
    private final CoupangSecretResolver secretResolver;
    private final boolean verifyLive;

    public CoupangOrderApprovalService(CoupangOrderStateService stateService,
                                       CoupangCredentialRepository credentialRepository,
                                       CoupangApiPort apiPort,
                                       CoupangSecretResolver secretResolver,
                                       @Value("${coupang.approve.verify-live:true}") boolean verifyLive) {
        this.stateService = stateService;
        this.credentialRepository = credentialRepository;
        this.apiPort = apiPort;
        this.secretResolver = secretResolver;
        this.verifyLive = verifyLive;
    }

    @Override
    public OrderResult approve(Long callerCompanyId, Long id, LongFunction<CreateOrderCommand> commandFactory) {
        CoupangOrder order = stateService.loadApprovable(callerCompanyId, id);
        if (verifyLive) {
            verifyOnCoupang(order);
        }
        return stateService.approve(callerCompanyId, id, resolveCurrentAdminId(), commandFactory);
    }

    @Override
    public CoupangOrderResult hold(Long callerCompanyId, Long id) {
        return stateService.hold(callerCompanyId, id);
    }

    @Override
    public CoupangOrderResult release(Long callerCompanyId, Long id) {
        return stateService.release(callerCompanyId, id);
    }

    private void verifyOnCoupang(CoupangOrder order) {
        CoupangCredential credential = credentialRepository.findByCompanyId(order.getCompanyId())
                .orElseThrow(() -> new BusinessException(CoupangErrorCode.CREDENTIAL_NOT_FOUND));
        Optional<CoupangOrderSheet> live;
        try {
            live = apiPort.fetchOrderSheet(secretResolver.resolve(credential), order.getOrderId(),
                    order.getShipmentBoxId());
        } catch (CoupangApiException e) {
            log.warn("쿠팡 주문 상태 확인 실패 coupangOrderId={} httpStatus={}", order.getId(), e.getHttpStatus());
            throw new BusinessException(CoupangErrorCode.VERIFY_FAILED);
        }
        if (live.isEmpty()) {
            stateService.markCanceled(order.getId());
            throw new BusinessException(CoupangErrorCode.CANCELED_ON_COUPANG);
        }
        if (!APPROVABLE_COUPANG_STATUS.contains(live.get().status())) {
            throw new BusinessException(CoupangErrorCode.CANCELED_ON_COUPANG);
        }
    }

    private Long resolveCurrentAdminId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        try {
            return Long.parseLong(String.valueOf(auth.getPrincipal()));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

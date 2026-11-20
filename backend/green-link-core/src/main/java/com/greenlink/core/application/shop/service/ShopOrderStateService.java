package com.greenlink.core.application.shop.service;

import com.greenlink.core.application.order.dto.CreateOrderCommand;
import com.greenlink.core.application.order.dto.OrderResult;
import com.greenlink.core.application.order.usecase.CreateOrderUseCase;
import com.greenlink.core.application.shop.dto.ShopOrderResult;
import com.greenlink.core.application.shop.usecase.ProcessShopOrderUseCase;
import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.shop.model.ShopOrder;
import com.greenlink.core.domain.shop.port.ShopOrderRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.shop.ShopErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.function.Function;

/** 쇼핑몰 주문 상태 전이를 트랜잭션 단위로 처리한다. */
@Service
public class ShopOrderStateService implements ProcessShopOrderUseCase {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final ShopOrderRepository orderRepository;
    private final CreateOrderUseCase createOrderUseCase;
    private final CompanyRepository companyRepository;
    private final AdminRepository adminRepository;

    public ShopOrderStateService(ShopOrderRepository orderRepository, CreateOrderUseCase createOrderUseCase,
                                 CompanyRepository companyRepository, AdminRepository adminRepository) {
        this.orderRepository = orderRepository;
        this.createOrderUseCase = createOrderUseCase;
        this.companyRepository = companyRepository;
        this.adminRepository = adminRepository;
    }

    /** 발주 생성이 실패하면 전체가 롤백되어 쇼핑몰 주문은 원래 상태로 남는다. */
    @Override
    @Transactional
    public OrderResult approve(Long callerCompanyId, Long id, Long adminId,
                               Function<Long, CreateOrderCommand> commandFactory) {
        ShopOrder order = lock(callerCompanyId, id);
        if (!order.isApprovable()) {
            throw new BusinessException(ShopErrorCode.NOT_APPROVABLE);
        }
        Company company = companyRepository.findById(order.getCompanyId())
                .orElseThrow(() -> new BusinessException(ShopErrorCode.SHOP_NOT_FOUND));
        boolean canOrder = company.getType() != CompanyType.VENDOR;
        CreateOrderCommand command = commandFactory.apply(canOrder ? company.getId() : null);
        if (!canOrder) {
            ensureOwnVendor(company.getId(), command.vendorId());
        }
        OrderResult created = createOrderUseCase.createOrder(command);
        order.approve(created.id(), adminId, LocalDateTime.now(KST));
        orderRepository.save(order);
        return created;
    }

    @Override
    @Transactional
    public ShopOrderResult hold(Long callerCompanyId, Long id) {
        ShopOrder order = lock(callerCompanyId, id);
        order.hold();
        return ShopOrderResult.from(orderRepository.save(order));
    }

    @Override
    @Transactional
    public ShopOrderResult release(Long callerCompanyId, Long id) {
        ShopOrder order = lock(callerCompanyId, id);
        order.release();
        return ShopOrderResult.from(orderRepository.save(order));
    }

    @Override
    @Transactional
    public ShopOrderResult reject(Long callerCompanyId, Long id, String reason) {
        ShopOrder order = lock(callerCompanyId, id);
        order.reject(reason);
        return ShopOrderResult.from(orderRepository.save(order));
    }

    private void ensureOwnVendor(Long companyId, Long vendorAdminId) {
        boolean own = vendorAdminId != null && adminRepository.findById(vendorAdminId)
                .map(Admin::getCompanyId)
                .filter(companyId::equals)
                .isPresent();
        if (!own) {
            throw new BusinessException(ShopErrorCode.INVALID_VENDOR);
        }
    }

    private ShopOrder lock(Long callerCompanyId, Long id) {
        ShopOrder order = orderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException(ShopErrorCode.ORDER_NOT_FOUND));
        ShopOrderAccess.check(callerCompanyId, order);
        return order;
    }
}

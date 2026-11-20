package com.greenlink.core.application.shop.service;

import com.greenlink.core.application.order.dto.CreateOrderCommand;
import com.greenlink.core.application.order.dto.OrderResult;
import com.greenlink.core.application.order.usecase.CreateOrderUseCase;
import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.shop.model.Shop;
import com.greenlink.core.domain.shop.model.ShopOrder;
import com.greenlink.core.domain.shop.model.ShopOrderItem;
import com.greenlink.core.domain.shop.model.ShopOrderStatus;
import com.greenlink.core.domain.shop.port.ShopOrderRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.shop.ShopErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ShopOrderStateServiceTest {

    static final long COMPANY_ID = 7L;

    @Mock ShopOrderRepository orderRepository;
    @Mock CreateOrderUseCase createOrderUseCase;
    @Mock CompanyRepository companyRepository;
    @Mock AdminRepository adminRepository;

    ShopOrderStateService service;
    ShopOrder order;

    @BeforeEach
    void setUp() {
        service = new ShopOrderStateService(orderRepository, createOrderUseCase, companyRepository, adminRepository);
        Shop shop = Shop.of(1L, COMPANY_ID, "rose", true, "장미꽃집", null, null, "#15803d", null, null, null, null,
                null, null);
        ShopOrder.Contact contact = new ShopOrder.Contact("홍길동", "01012345678", "김철수", "01099998888",
                "서울시", null, "2099-01-01", null, null, null, null, null, null);
        order = ShopOrder.place(shop, "SO-20990101-ABC234", List.of(new ShopOrderItem(1L, "근조3단", 80_000, 1)),
                contact);
        given(orderRepository.findByIdForUpdate(3L)).willReturn(Optional.of(order));
    }

    private Company company(CompanyType type) {
        Company company = mock(Company.class);
        given(company.getType()).willReturn(type);
        return company;
    }

    private CreateOrderCommand commandWithVendor(Long vendorId) {
        CreateOrderCommand command = mock(CreateOrderCommand.class);
        given(command.vendorId()).willReturn(vendorId);
        return command;
    }

    @Test
    void 발주사_승인은_본인_회사를_발주사로_발주를_만든다() {
        Company company = company(CompanyType.ORDERING);
        given(company.getId()).willReturn(COMPANY_ID);
        given(companyRepository.findById(COMPANY_ID)).willReturn(Optional.of(company));
        OrderResult created = mock(OrderResult.class);
        given(created.id()).willReturn(99L);
        given(createOrderUseCase.createOrder(any())).willReturn(created);
        AtomicReference<Long> orderingCompanyId = new AtomicReference<>();

        OrderResult result = service.approve(COMPANY_ID, 3L, 5L, cid -> {
            orderingCompanyId.set(cid);
            return mock(CreateOrderCommand.class);
        });

        assertThat(result).isSameAs(created);
        assertThat(orderingCompanyId.get()).isEqualTo(COMPANY_ID);
        assertThat(order.getStatus()).isEqualTo(ShopOrderStatus.APPROVED);
        assertThat(order.getLinkedOrderId()).isEqualTo(99L);
        assertThat(order.getApprovedBy()).isEqualTo(5L);
        verify(orderRepository).save(order);
    }

    @Test
    void 수주사_승인은_발주사_없이_본인_계정으로_발주한다() {
        Company vendorCompany = company(CompanyType.VENDOR);
        given(vendorCompany.getId()).willReturn(COMPANY_ID);
        given(companyRepository.findById(COMPANY_ID)).willReturn(Optional.of(vendorCompany));
        Admin vendorAdmin = mock(Admin.class);
        given(vendorAdmin.getCompanyId()).willReturn(COMPANY_ID);
        given(adminRepository.findById(11L)).willReturn(Optional.of(vendorAdmin));
        OrderResult created = mock(OrderResult.class);
        given(created.id()).willReturn(100L);
        given(createOrderUseCase.createOrder(any())).willReturn(created);
        AtomicReference<Long> orderingCompanyId = new AtomicReference<>(-1L);

        service.approve(COMPANY_ID, 3L, 11L, cid -> {
            orderingCompanyId.set(cid);
            return commandWithVendor(11L);
        });

        assertThat(orderingCompanyId.get()).isNull();
        assertThat(order.getStatus()).isEqualTo(ShopOrderStatus.APPROVED);
    }

    @Test
    void 수주사_승인에서_다른_회사_수주사를_지정하면_거부한다() {
        Company vendorCompany = company(CompanyType.VENDOR);
        given(vendorCompany.getId()).willReturn(COMPANY_ID);
        given(companyRepository.findById(COMPANY_ID)).willReturn(Optional.of(vendorCompany));
        Admin other = mock(Admin.class);
        given(other.getCompanyId()).willReturn(999L);
        given(adminRepository.findById(12L)).willReturn(Optional.of(other));

        assertThatThrownBy(() -> service.approve(COMPANY_ID, 3L, 11L, cid -> commandWithVendor(12L)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ShopErrorCode.INVALID_VENDOR);

        verify(createOrderUseCase, never()).createOrder(any());
        verify(orderRepository, never()).save(any());
        assertThat(order.getStatus()).isEqualTo(ShopOrderStatus.PENDING);
    }

    @Test
    void 발주_생성이_실패하면_쇼핑몰_주문을_저장하지_않는다() {
        Company company = company(CompanyType.ORDERING);
        given(company.getId()).willReturn(COMPANY_ID);
        given(companyRepository.findById(COMPANY_ID)).willReturn(Optional.of(company));
        given(createOrderUseCase.createOrder(any())).willThrow(new IllegalStateException("boom"));

        assertThatThrownBy(() -> service.approve(COMPANY_ID, 3L, 5L, cid -> mock(CreateOrderCommand.class)))
                .isInstanceOf(IllegalStateException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void 이미_승인된_주문은_발주를_만들지_않는다() {
        order.approve(1L, 1L, java.time.LocalDateTime.now());

        assertThatThrownBy(() -> service.approve(COMPANY_ID, 3L, 5L, cid -> mock(CreateOrderCommand.class)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ShopErrorCode.NOT_APPROVABLE);

        verify(createOrderUseCase, never()).createOrder(any());
    }

    @Test
    void 다른_회사_주문은_처리할_수_없다() {
        assertThatThrownBy(() -> service.hold(999L, 3L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ShopErrorCode.FORBIDDEN_COMPANY);
        assertThatThrownBy(() -> service.reject(999L, 3L, "x")).isInstanceOf(BusinessException.class);
    }

    @Test
    void 슈퍼관리자는_회사_제한_없이_처리한다() {
        given(orderRepository.save(order)).willReturn(order);

        assertThat(service.hold(null, 3L).status()).isEqualTo("HELD");
    }
}

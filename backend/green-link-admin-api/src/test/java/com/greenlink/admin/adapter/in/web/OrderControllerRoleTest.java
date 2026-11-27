package com.greenlink.admin.adapter.in.web;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.order.dto.OrderResult;
import com.greenlink.core.application.order.usecase.CreateOrderUseCase;
import com.greenlink.core.application.order.usecase.DeleteOrderUseCase;
import com.greenlink.core.application.order.usecase.GetOrderUseCase;
import com.greenlink.core.application.order.usecase.GetOrdersUseCase;
import com.greenlink.core.application.order.usecase.UpdateOrderStatusUseCase;
import com.greenlink.core.application.order.usecase.UpdateOrderUseCase;
import com.greenlink.core.application.orderhistory.usecase.GetOrderHistoryUseCase;
import com.greenlink.core.application.orderpriceadjustment.usecase.DecidePriceAdjustmentUseCase;
import com.greenlink.core.application.orderpriceadjustment.usecase.GetPriceAdjustmentsUseCase;
import com.greenlink.core.application.orderpriceadjustment.usecase.RequestPriceAdjustmentUseCase;
import com.greenlink.core.application.sms.usecase.SendDeliveryAlimtalkUseCase;
import com.greenlink.core.application.sms.usecase.SendDeliveryMmsUseCase;
import com.greenlink.core.application.auth.AdminAuthorities;
import com.greenlink.core.domain.admin.model.AdminRole;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.order.model.OrderSearchCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerRoleTest {

    private static final Long ADMIN_ID = 41L;
    private static final Long COMPANY_ID = 7L;

    private GetOrdersUseCase getOrders;
    private GetOrderUseCase getOrder;
    private UpdateOrderUseCase updateOrder;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        getOrders = mock(GetOrdersUseCase.class);
        getOrder = mock(GetOrderUseCase.class);
        updateOrder = mock(UpdateOrderUseCase.class);
        PageResult<OrderResult> empty = new PageResult<>(List.of(), 0, 20, 0, 0, 0L);
        when(getOrders.getOrdersPaged(any(), any())).thenReturn(empty);
        when(getOrders.getVendorOrdersPaged(any(), any())).thenReturn(empty);

        OrderController controller = new OrderController(getOrders, getOrder, mock(CreateOrderUseCase.class),
            updateOrder, mock(UpdateOrderStatusUseCase.class), mock(DeleteOrderUseCase.class),
            mock(GetOrderHistoryUseCase.class), mock(SendDeliveryMmsUseCase.class), mock(SendDeliveryAlimtalkUseCase.class),
            mock(RequestPriceAdjustmentUseCase.class), mock(DecidePriceAdjustmentUseCase.class),
            mock(GetPriceAdjustmentsUseCase.class));
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private static UsernamePasswordAuthenticationToken partner(CompanyType type) {
        var authorities = AdminAuthorities.from(AdminRole.PARTNER_ADMIN, type).stream()
            .map(a -> new SimpleGrantedAuthority("ROLE_" + a)).toList();
        var auth = new UsernamePasswordAuthenticationToken(String.valueOf(ADMIN_ID), null, authorities);
        auth.setDetails(COMPANY_ID);
        return auth;
    }

    private OrderSearchCondition captureOrderingCondition() {
        ArgumentCaptor<OrderSearchCondition> captor = ArgumentCaptor.forClass(OrderSearchCondition.class);
        verify(getOrders).getOrdersPaged(captor.capture(), any(Pageable.class));
        return captor.getValue();
    }

    private OrderSearchCondition captureVendorCondition() {
        ArgumentCaptor<OrderSearchCondition> captor = ArgumentCaptor.forClass(OrderSearchCondition.class);
        verify(getOrders).getVendorOrdersPaged(captor.capture(), any(Pageable.class));
        return captor.getValue();
    }

    @Test
    void 발주사_목록은_회사_기준_발주_조회() throws Exception {
        mvc.perform(get("/api/orders").principal(partner(CompanyType.ORDERING))).andExpect(status().isOk());

        OrderSearchCondition c = captureOrderingCondition();
        assertThat(c.orderingCompanyId()).isEqualTo(COMPANY_ID);
        assertThat(c.vendorId()).isNull();
        verify(getOrders, never()).getVendorOrdersPaged(any(), any());
    }

    @Test
    void 수주사_목록은_본인_기준_수주_조회이고_view_ORDERING을_무시한다() throws Exception {
        mvc.perform(get("/api/orders").param("view", "ORDERING").principal(partner(CompanyType.VENDOR)))
            .andExpect(status().isOk());

        assertThat(captureVendorCondition().vendorId()).isEqualTo(ADMIN_ID);
        verify(getOrders, never()).getOrdersPaged(any(), any());
    }

    @Test
    void 수발주사는_view_ORDERING이면_회사_기준_발주_조회() throws Exception {
        mvc.perform(get("/api/orders").param("view", "ORDERING").principal(partner(CompanyType.BOTH)))
            .andExpect(status().isOk());

        OrderSearchCondition c = captureOrderingCondition();
        assertThat(c.orderingCompanyId()).isEqualTo(COMPANY_ID);
        assertThat(c.vendorId()).isNull();
        verify(getOrders, never()).getVendorOrdersPaged(any(), any());
    }

    @Test
    void 수발주사는_view가_없으면_수주_조회() throws Exception {
        mvc.perform(get("/api/orders").principal(partner(CompanyType.BOTH))).andExpect(status().isOk());

        assertThat(captureVendorCondition().vendorId()).isEqualTo(ADMIN_ID);
        verify(getOrders, never()).getOrdersPaged(any(), any());
    }

    @Test
    void 수발주사는_view_VENDOR이면_수주_조회() throws Exception {
        mvc.perform(get("/api/orders").param("view", "VENDOR").principal(partner(CompanyType.BOTH)))
            .andExpect(status().isOk());

        assertThat(captureVendorCondition().vendorId()).isEqualTo(ADMIN_ID);
    }

    private static final String UPDATE_BODY =
        "{\"vendorId\":1,\"deliveryDate\":\"2026-12-01\",\"deliveryTimeType\":\"ANY\","
        + "\"ordererName\":\"홍길동\",\"ordererPhone\":\"010-1111-2222\","
        + "\"sendPhotoToOrderer\":false,\"sendPhotoToRecipient\":false}";

    @Test
    void 수주사_전용_계정은_주문_수정이_금지된다() throws Exception {
        mvc.perform(put("/api/orders/5").contentType(MediaType.APPLICATION_JSON).content(UPDATE_BODY)
                .principal(partner(CompanyType.VENDOR)))
            .andExpect(status().isForbidden());
        verify(updateOrder, never()).updateOrder(anyLong(), any());
    }

    @Test
    void 수발주사도_다른_회사_주문은_수정할_수_없다() throws Exception {
        when(getOrder.getOrder(5L)).thenReturn(order(ADMIN_ID, 999L));

        mvc.perform(put("/api/orders/5").contentType(MediaType.APPLICATION_JSON).content(UPDATE_BODY)
                .principal(partner(CompanyType.BOTH)))
            .andExpect(status().isForbidden());
        verify(updateOrder, never()).updateOrder(anyLong(), any());
    }

    @Test
    void 수발주사가_수주받은_주문이면_수주사_관점으로_조회한다() throws Exception {
        when(getOrder.getOrder(anyLong(), anyBoolean())).thenReturn(order(ADMIN_ID, 999L));

        mvc.perform(get("/api/orders/5").principal(partner(CompanyType.BOTH)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.vendorAmount").value(5000))
            .andExpect(jsonPath("$.amount").value(7000))
            .andExpect(jsonPath("$.vendorFee").doesNotExist());
    }

    @Test
    void 수발주사가_발주한_주문이면_발주사_관점으로_조회한다() throws Exception {
        when(getOrder.getOrder(anyLong(), anyBoolean())).thenReturn(order(555L, COMPANY_ID));

        mvc.perform(get("/api/orders/5").principal(partner(CompanyType.BOTH)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.vendorAmount").doesNotExist())
            .andExpect(jsonPath("$.amount").value(10000));
    }

    @Test
    void 수주사_전용_계정은_수주사_관점으로_조회한다() throws Exception {
        when(getOrder.getOrder(anyLong(), anyBoolean())).thenReturn(order(ADMIN_ID, 999L));

        mvc.perform(get("/api/orders/5").principal(partner(CompanyType.VENDOR)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.amount").value(7000));
    }

    private static OrderResult order(Long vendorId, Long orderingCompanyId) {
        return new OrderResult(
            5L, "ORD-5", vendorId, null, "수주사", null,
            orderingCompanyId, "발주사", null,
            "수주완료", null, "상품", 1, 10000L,
            5000L, null, 10000L, 3000L, null,
            "2026-12-01", "ANY", null, null, null, null, null,
            null, null, null, null, null, null, null,
            "홍길동", "010-1111-2222", null, null,
            false, false, null, null, null,
            null, null, null, null, null, null, 0,
            false, null, null, false, null, null, null,
            false, null, 0L, null);
    }
}

package com.greenlink.core.application.shop.service;

import com.greenlink.core.application.shop.dto.PlaceShopOrderCommand;
import com.greenlink.core.application.shop.dto.PlacedShopOrderResult;
import com.greenlink.core.application.shop.dto.PublicOrderStatusView;
import com.greenlink.core.domain.shop.model.Shop;
import com.greenlink.core.domain.shop.model.ShopOrder;
import com.greenlink.core.domain.shop.model.ShopOrderItem;
import com.greenlink.core.domain.shop.model.ShopProduct;
import com.greenlink.core.domain.shop.model.ShopProductCategory;
import com.greenlink.core.domain.shop.port.RateLimiter;
import com.greenlink.core.domain.shop.port.ShopOrderRepository;
import com.greenlink.core.domain.shop.port.ShopProductRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.shop.ShopErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PublicShopServiceTest {

    static final String TOMORROW = LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(1).toString();

    @Mock ShopLookup shopLookup;
    @Mock ShopProductRepository productRepository;
    @Mock ShopOrderRepository orderRepository;
    @Mock RateLimiter rateLimiter;

    PublicShopService service;
    Shop shop;

    @BeforeEach
    void setUp() {
        service = new PublicShopService(shopLookup, productRepository, orderRepository, rateLimiter);
        shop = Shop.of(1L, 7L, "rose", true, "장미꽃집", null, null, "#15803d", null, null, null, null, null, null);
        given(shopLookup.findPublished("rose")).willReturn(shop);
    }

    private ShopProduct product(long id, long price) {
        return ShopProduct.of(id, 1L, 7L, ShopProductCategory.FUNERAL, "근조3단", null, price, null, true, 0);
    }

    private PlaceShopOrderCommand command(List<PlaceShopOrderCommand.Line> lines, String deliveryDate,
                                          String honeypot) {
        return new PlaceShopOrderCommand(lines, "홍길동", "010-1234-5678", "김철수", "010-9999-8888", "서울시 중구",
                null, deliveryDate, null, null, null, null, null, null, honeypot);
    }

    private PlaceShopOrderCommand command(List<PlaceShopOrderCommand.Line> lines) {
        return command(lines, TOMORROW, null);
    }

    private void allowRate() {
        given(rateLimiter.tryAcquire(anyString(), anyInt(), any(Duration.class))).willReturn(true);
    }

    @Test
    void 가격은_서버의_상품가로_계산한다() {
        allowRate();
        given(productRepository.findVisibleByIdInAndShopId(any(), org.mockito.ArgumentMatchers.eq(1L)))
                .willReturn(List.of(product(10L, 50_000)));
        given(orderRepository.existsByOrderNo(anyString())).willReturn(false);
        given(orderRepository.save(any())).willAnswer(i -> i.getArgument(0));

        PlacedShopOrderResult result = service.placeOrder("rose",
                command(List.of(new PlaceShopOrderCommand.Line(10L, 2))), "1.1.1.1");

        ArgumentCaptor<ShopOrder> saved = ArgumentCaptor.forClass(ShopOrder.class);
        verify(orderRepository).save(saved.capture());
        assertThat(saved.getValue().getTotalPrice()).isEqualTo(100_000);
        assertThat(saved.getValue().getItems()).containsExactly(new ShopOrderItem(10L, "근조3단", 50_000, 2));
        assertThat(saved.getValue().getOrdererPhone()).isEqualTo("01012345678");
        assertThat(result.orderNo()).startsWith("SO-");
        assertThat(result.status()).isEqualTo("PENDING");
    }

    @Test
    void 같은_상품이_여러_줄이면_수량을_합산한다() {
        allowRate();
        given(productRepository.findVisibleByIdInAndShopId(any(), any())).willReturn(List.of(product(10L, 1_000)));
        given(orderRepository.existsByOrderNo(anyString())).willReturn(false);
        given(orderRepository.save(any())).willAnswer(i -> i.getArgument(0));

        service.placeOrder("rose", command(List.of(new PlaceShopOrderCommand.Line(10L, 1),
                new PlaceShopOrderCommand.Line(10L, 2))), "1.1.1.1");

        ArgumentCaptor<ShopOrder> saved = ArgumentCaptor.forClass(ShopOrder.class);
        verify(orderRepository).save(saved.capture());
        assertThat(saved.getValue().getItems()).hasSize(1);
        assertThat(saved.getValue().getTotalPrice()).isEqualTo(3_000);
    }

    @Test
    void 다른_쇼핑몰_또는_비노출_상품이_섞이면_거부한다() {
        allowRate();
        given(productRepository.findVisibleByIdInAndShopId(any(), any())).willReturn(List.of(product(10L, 1_000)));

        assertThatThrownBy(() -> service.placeOrder("rose", command(List.of(
                new PlaceShopOrderCommand.Line(10L, 1), new PlaceShopOrderCommand.Line(77L, 1))), "1.1.1.1"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ShopErrorCode.INVALID_ORDER_REQUEST);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void 허니팟이_채워지면_저장하지_않고_성공처럼_응답한다() {
        PlacedShopOrderResult result = service.placeOrder("rose",
                command(List.of(new PlaceShopOrderCommand.Line(10L, 1)), TOMORROW, "http://spam"), "1.1.1.1");

        assertThat(result.orderNo()).startsWith("SO-");
        verify(orderRepository, never()).save(any());
        verify(rateLimiter, never()).tryAcquire(anyString(), anyInt(), any());
    }

    @Test
    void 요청_한도를_넘으면_거부한다() {
        given(rateLimiter.tryAcquire(anyString(), anyInt(), any(Duration.class))).willReturn(false);

        assertThatThrownBy(() -> service.placeOrder("rose",
                command(List.of(new PlaceShopOrderCommand.Line(10L, 1))), "1.1.1.1"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ShopErrorCode.TOO_MANY_REQUESTS);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void 지난_배송일과_잘못된_수량은_거부한다() {
        allowRate();
        String yesterday = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1).toString();

        assertThatThrownBy(() -> service.placeOrder("rose",
                command(List.of(new PlaceShopOrderCommand.Line(10L, 1)), yesterday, null), "1.1.1.1"))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.placeOrder("rose",
                command(List.of(new PlaceShopOrderCommand.Line(10L, 100))), "1.1.1.1"))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.placeOrder("rose",
                command(List.of(new PlaceShopOrderCommand.Line(10L, 0))), "1.1.1.1"))
                .isInstanceOf(BusinessException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void 조회는_주문번호와_전화번호가_모두_맞아야_한다() {
        given(rateLimiter.tryAcquire(anyString(), anyInt(), any(Duration.class))).willReturn(true);
        ShopOrder placed = ShopOrder.place(shop, "SO-20990101-ABC234",
                List.of(new ShopOrderItem(10L, "근조3단", 50_000, 1)),
                new ShopOrder.Contact("홍길동", "01012345678", "김철수", "01099998888", "서울시", null,
                        "2099-01-01", null, null, null, null, null, null));
        given(orderRepository.findByShopIdAndOrderNo(1L, "SO-20990101-ABC234")).willReturn(Optional.of(placed));

        PublicOrderStatusView view = service.lookupOrder("rose", " so-20990101-abc234 ", "010-1234-5678", "1.1.1.1");
        assertThat(view.orderNo()).isEqualTo("SO-20990101-ABC234");

        assertThatThrownBy(() -> service.lookupOrder("rose", "SO-20990101-ABC234", "010-0000-0000", "1.1.1.1"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ShopErrorCode.ORDER_LOOKUP_FAILED);
    }

    @Test
    void 조회_한도를_넘으면_거부한다() {
        given(rateLimiter.tryAcquire(anyString(), anyInt(), any(Duration.class))).willReturn(false);

        assertThatThrownBy(() -> service.lookupOrder("rose", "SO-1", "01012345678", "1.1.1.1"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ShopErrorCode.TOO_MANY_REQUESTS);
    }
}

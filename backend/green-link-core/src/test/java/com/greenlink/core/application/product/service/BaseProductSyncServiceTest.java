package com.greenlink.core.application.product.service;

import com.greenlink.core.domain.product.model.BaseProduct;
import com.greenlink.core.domain.product.model.Product;
import com.greenlink.core.domain.product.port.BaseProductRepository;
import com.greenlink.core.domain.product.port.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BaseProductSyncServiceTest {

    @Mock BaseProductRepository baseProductRepository;
    @Mock ProductRepository productRepository;
    @InjectMocks BaseProductSyncService service;

    private BaseProduct base(long id) {
        return BaseProduct.of(id, "상품" + id, 1000L * id, null, null, (int) id);
    }

    @Test
    @SuppressWarnings("unchecked")
    void 아직_복사되지_않은_기본_상품만_복사한다() {
        given(baseProductRepository.findAll()).willReturn(List.of(base(1), base(2), base(3)));
        given(productRepository.findBaseProductIdsByCompanyId(10L)).willReturn(List.of(2L));

        service.syncBaseProducts(10L);

        ArgumentCaptor<List<Product>> captor = ArgumentCaptor.forClass(List.class);
        verify(productRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).extracting(Product::getBaseProductId).containsExactly(1L, 3L);
        assertThat(captor.getValue()).allSatisfy(p -> assertThat(p.getCompanyId()).isEqualTo(10L));
    }

    @Test
    void 이미_모두_복사됐으면_저장하지_않는다() {
        given(baseProductRepository.findAll()).willReturn(List.of(base(1)));
        given(productRepository.findBaseProductIdsByCompanyId(10L)).willReturn(List.of(1L));

        service.syncBaseProducts(10L);

        verify(productRepository, never()).saveAll(anyList());
    }
}

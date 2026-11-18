package com.greenlink.core.domain.product.port;

import com.greenlink.core.domain.product.model.BaseProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BaseProductRepository {
    BaseProduct save(BaseProduct product);
    Optional<BaseProduct> findById(Long id);
    Page<BaseProduct> findAll(Pageable pageable);
    List<BaseProduct> findAll();
    List<BaseProduct> findAllByIdIn(Collection<Long> ids);
    void delete(Long id);
}

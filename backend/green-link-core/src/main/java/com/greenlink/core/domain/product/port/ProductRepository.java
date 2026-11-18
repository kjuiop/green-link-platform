package com.greenlink.core.domain.product.port;

import com.greenlink.core.domain.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);
    List<Product> saveAll(List<Product> products);
    Optional<Product> findByIdAndCompanyId(Long id, Long companyId);
    Page<Product> search(Long companyId, String keyword, boolean enabledOnly, Pageable pageable);
    List<Product> findAllByBaseProductId(Long baseProductId);
    List<Long> findBaseProductIdsByCompanyId(Long companyId);
    void delete(Long id);
    void deleteAllByBaseProductId(Long baseProductId);
}

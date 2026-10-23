package com.greenlink.core.domain.product.port;

import com.greenlink.core.domain.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(Long id);
    Page<Product> findAll(Pageable pageable);
    List<Product> findAll();
    void delete(Long id);
    boolean existsById(Long id);
}

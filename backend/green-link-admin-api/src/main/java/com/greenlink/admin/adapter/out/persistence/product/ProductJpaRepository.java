package com.greenlink.admin.adapter.out.persistence.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, Long> {
    Page<ProductJpaEntity> findByOrderingCompanyId(Long orderingCompanyId, Pageable pageable);
    List<ProductJpaEntity> findByOrderingCompanyIdOrderByNameAsc(Long orderingCompanyId);
}

package com.greenlink.admin.adapter.out.persistence.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, Long> {

    Optional<ProductJpaEntity> findByIdAndCompanyId(Long id, Long companyId);

    List<ProductJpaEntity> findAllByBaseProductId(Long baseProductId);

    @Query("select p from ProductJpaEntity p where p.companyId = :companyId "
            + "and (:keyword is null or p.name like concat('%', :keyword, '%')) "
            + "and (:enabledOnly = false or p.enabled = true)")
    Page<ProductJpaEntity> search(@Param("companyId") Long companyId,
                                  @Param("keyword") String keyword,
                                  @Param("enabledOnly") boolean enabledOnly,
                                  Pageable pageable);

    @Query("select p.baseProductId from ProductJpaEntity p where p.companyId = :companyId and p.baseProductId is not null")
    List<Long> findBaseProductIdsByCompanyId(@Param("companyId") Long companyId);

    @Modifying
    @Query("delete from ProductJpaEntity p where p.baseProductId = :baseProductId")
    void deleteAllByBaseProductId(@Param("baseProductId") Long baseProductId);
}

package com.greenlink.admin.adapter.out.persistence.fax;

import com.greenlink.core.domain.fax.model.FaxStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FaxMessageJpaRepository extends JpaRepository<FaxMessageJpaEntity, Long> {

    boolean existsByOrderIdAndSequence(Long orderId, int sequence);

    Optional<FaxMessageJpaEntity> findFirstByOrderIdAndStatusNotOrderBySequenceDesc(Long orderId, FaxStatus status);

    @Query("SELECT COALESCE(MAX(m.sequence), 0) FROM FaxMessageJpaEntity m WHERE m.orderId = :orderId")
    int findMaxSequence(@Param("orderId") Long orderId);

    List<FaxMessageJpaEntity> findByStatusOrderByIdAsc(FaxStatus status, Pageable pageable);

    @Query("""
            SELECT m FROM FaxMessageJpaEntity m
            WHERE (:status IS NULL OR m.status = :status)
              AND (:keyword IS NULL
                   OR m.orderId IN (SELECT o.id FROM OrderJpaEntity o WHERE o.orderNumber LIKE CONCAT('%', :keyword, '%'))
                   OR m.vendorCompanyId IN (SELECT c.id FROM CompanyJpaEntity c WHERE c.companyName LIKE CONCAT('%', :keyword, '%')))
            ORDER BY m.requestedAt DESC, m.id DESC
            """)
    Page<FaxMessageJpaEntity> search(@Param("status") FaxStatus status, @Param("keyword") String keyword, Pageable pageable);
}

package com.greenlink.admin.adapter.out.persistence.referencephoto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReferencePhotoJpaRepository extends JpaRepository<ReferencePhotoJpaEntity, Long> {

    List<ReferencePhotoJpaEntity> findAllByOrderId(Long orderId);

    @Query("SELECT r.orderId, COUNT(r) FROM ReferencePhotoJpaEntity r WHERE r.orderId IN :orderIds GROUP BY r.orderId")
    List<Object[]> countByOrderIds(@Param("orderIds") List<Long> orderIds);
}

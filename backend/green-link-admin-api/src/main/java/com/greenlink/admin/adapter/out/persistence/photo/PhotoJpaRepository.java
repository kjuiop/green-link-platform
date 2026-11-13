package com.greenlink.admin.adapter.out.persistence.photo;

import com.greenlink.core.domain.photo.model.PhotoType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PhotoJpaRepository extends JpaRepository<PhotoJpaEntity, Long> {

    List<PhotoJpaEntity> findAllByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    boolean existsByOrderIdAndPhotoType(Long orderId, PhotoType photoType);

    List<PhotoJpaEntity> findByOrderIdAndIsSendToCustomerTrue(Long orderId);

    @Query("SELECT DISTINCT p.orderId FROM PhotoJpaEntity p WHERE p.orderId IN :orderIds AND p.photoType = :photoType")
    List<Long> findOrderIdsWithPhotoType(@Param("orderIds") Collection<Long> orderIds, @Param("photoType") PhotoType photoType);

    @Query("SELECT p FROM PhotoJpaEntity p JOIN OrderJpaEntity o ON p.orderId = o.id WHERE o.vendorId = :vendorId ORDER BY p.createdAt DESC")
    List<PhotoJpaEntity> findRecentByVendorId(@Param("vendorId") Long vendorId, Pageable pageable);
}

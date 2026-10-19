package com.greenlink.admin.adapter.out.persistence.photo;

import com.greenlink.core.domain.photo.model.PhotoType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhotoJpaRepository extends JpaRepository<PhotoJpaEntity, Long> {

    List<PhotoJpaEntity> findAllByOrderId(Long orderId);

    boolean existsByOrderIdAndPhotoType(Long orderId, PhotoType photoType);
}

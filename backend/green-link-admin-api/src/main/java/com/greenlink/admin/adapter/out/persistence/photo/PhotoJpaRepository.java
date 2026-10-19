package com.greenlink.admin.adapter.out.persistence.photo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhotoJpaRepository extends JpaRepository<PhotoJpaEntity, Long> {

    List<PhotoJpaEntity> findAllByOrderId(Long orderId);
}

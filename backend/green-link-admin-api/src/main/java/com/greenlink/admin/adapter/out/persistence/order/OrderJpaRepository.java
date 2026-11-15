package com.greenlink.admin.adapter.out.persistence.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long> {

    Optional<OrderJpaEntity> findByPhotoToken(String photoToken);
}

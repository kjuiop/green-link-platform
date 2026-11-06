package com.greenlink.admin.adapter.out.persistence.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface PostJpaRepository extends JpaRepository<PostJpaEntity, Long> {

    @Transactional
    @Modifying
    @Query("UPDATE PostJpaEntity p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query("UPDATE PostJpaEntity p SET p.deletedAt = :now WHERE p.id = :id")
    void softDelete(@Param("id") Long id, @Param("now") LocalDateTime now);
}

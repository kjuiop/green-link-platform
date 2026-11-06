package com.greenlink.admin.adapter.out.persistence.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface PostAttachmentJpaRepository extends JpaRepository<PostAttachmentJpaEntity, Long> {

    List<PostAttachmentJpaEntity> findAllByPostId(Long postId);

    @Query("SELECT DISTINCT a.postId FROM PostAttachmentJpaEntity a WHERE a.postId IN :postIds")
    Set<Long> findDistinctPostIdsByPostIdIn(@Param("postIds") List<Long> postIds);
}

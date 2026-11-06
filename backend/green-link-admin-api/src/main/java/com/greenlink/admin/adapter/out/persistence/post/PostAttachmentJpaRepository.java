package com.greenlink.admin.adapter.out.persistence.post;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostAttachmentJpaRepository extends JpaRepository<PostAttachmentJpaEntity, Long> {

    List<PostAttachmentJpaEntity> findAllByPostId(Long postId);
}

package com.greenlink.admin.adapter.out.persistence.menu;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuJpaRepository extends JpaRepository<MenuJpaEntity, Long> {

    @EntityGraph(attributePaths = "roles")
    @Override
    List<MenuJpaEntity> findAll();

    boolean existsByParentId(Long parentId);
}

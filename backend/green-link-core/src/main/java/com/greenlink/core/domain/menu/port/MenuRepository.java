package com.greenlink.core.domain.menu.port;

import com.greenlink.core.domain.menu.model.Menu;

import java.util.List;
import java.util.Optional;

public interface MenuRepository {
    List<Menu> findAll();
    Optional<Menu> findById(Long id);
    boolean existsByParentId(Long parentId);
    Menu save(Menu menu);
    void delete(Long id);
}

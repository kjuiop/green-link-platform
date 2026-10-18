package com.greenlink.admin.adapter.out.persistence.menu;

import com.greenlink.core.domain.member.model.Role;
import com.greenlink.core.domain.menu.model.Menu;
import com.greenlink.core.domain.menu.port.MenuRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class MenuRepositoryAdapter implements MenuRepository {

    private final MenuJpaRepository menuJpaRepository;

    public MenuRepositoryAdapter(MenuJpaRepository menuJpaRepository) {
        this.menuJpaRepository = menuJpaRepository;
    }

    @Override
    public List<Menu> findAll() {
        return menuJpaRepository.findAll().stream()
            .map(MenuJpaEntity::toDomain)
            .toList();
    }

    @Override
    public Optional<Menu> findById(Long id) {
        return menuJpaRepository.findById(id)
            .map(MenuJpaEntity::toDomain);
    }

    @Override
    public Menu save(Menu menu) {
        MenuJpaEntity entity;
        if (menu.getId() != null) {
            entity = menuJpaRepository.findById(menu.getId())
                .orElse(MenuJpaEntity.from(menu));
            entity.updateFields(menu);
        } else {
            entity = MenuJpaEntity.from(menu);
        }

        entity.getRoles().clear();
        for (Role role : menu.getRoles()) {
            entity.getRoles().add(MenuRoleJpaEntity.of(entity, role));
        }

        return menuJpaRepository.save(entity).toDomain();
    }

    @Override
    public void delete(Long id) {
        menuJpaRepository.deleteById(id);
    }
}

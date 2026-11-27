package com.greenlink.admin.adapter.out.persistence.menu;

import com.greenlink.core.domain.admin.model.AdminRole;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.menu.model.Menu;
import com.greenlink.core.domain.menu.port.MenuRepository;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

        Set<AdminRole> newRoles = new HashSet<>(menu.getRoles());
        Set<AdminRole> existingRoles = entity.getRoles().stream()
            .map(MenuRoleJpaEntity::getRole)
            .collect(Collectors.toSet());
        entity.getRoles().removeIf(r -> !newRoles.contains(r.getRole()));
        for (AdminRole role : newRoles) {
            if (!existingRoles.contains(role)) {
                entity.getRoles().add(MenuRoleJpaEntity.of(entity, role));
            }
        }

        Set<CompanyType> newTypes = new HashSet<>(menu.getCompanyTypes());
        Set<CompanyType> existingTypes = entity.getCompanyTypes().stream()
            .map(MenuCompanyTypeJpaEntity::getCompanyType)
            .collect(Collectors.toSet());
        entity.getCompanyTypes().removeIf(t -> !newTypes.contains(t.getCompanyType()));
        for (CompanyType type : newTypes) {
            if (!existingTypes.contains(type)) {
                entity.getCompanyTypes().add(MenuCompanyTypeJpaEntity.of(entity, type));
            }
        }

        return menuJpaRepository.save(entity).toDomain();
    }

    @Override
    public boolean existsByParentId(Long parentId) {
        return menuJpaRepository.existsByParentId(parentId);
    }

    @Override
    public void delete(Long id) {
        menuJpaRepository.deleteById(id);
    }
}

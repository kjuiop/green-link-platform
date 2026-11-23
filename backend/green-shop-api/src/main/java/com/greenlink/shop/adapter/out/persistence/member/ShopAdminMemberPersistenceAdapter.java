package com.greenlink.shop.adapter.out.persistence.member;

import com.greenlink.shopcore.domain.member.model.ShopAdminMember;
import com.greenlink.shopcore.domain.member.model.ShopAdminStatus;
import com.greenlink.shopcore.domain.member.port.ShopAdminMemberRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class ShopAdminMemberPersistenceAdapter implements ShopAdminMemberRepository {

    private final ShopAdminMemberJpaRepository jpaRepository;

    ShopAdminMemberPersistenceAdapter(ShopAdminMemberJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<ShopAdminMember> findById(Long id) {
        return jpaRepository.findById(id).map(ShopAdminMemberJpaEntity::toDomain);
    }

    @Override
    public Optional<ShopAdminMember> findByOnurimAdminId(Long onurimAdminId) {
        return jpaRepository.findByOnurimAdminId(onurimAdminId).map(ShopAdminMemberJpaEntity::toDomain);
    }

    @Override
    public List<ShopAdminMember> findAll(ShopAdminStatus status) {
        List<ShopAdminMemberJpaEntity> rows = status == null
            ? jpaRepository.findAllByOrderByRequestedAtDesc()
            : jpaRepository.findByStatusOrderByRequestedAtDesc(status);
        return rows.stream().map(ShopAdminMemberJpaEntity::toDomain).toList();
    }

    @Override
    public ShopAdminMember save(ShopAdminMember member) {
        ShopAdminMemberJpaEntity entity = member.getId() == null
            ? ShopAdminMemberJpaEntity.from(member)
            : jpaRepository.findById(member.getId()).map(e -> { e.apply(member); return e; })
                .orElseGet(() -> ShopAdminMemberJpaEntity.from(member));
        return jpaRepository.save(entity).toDomain();
    }
}

package com.greenlink.shop.adapter.out.persistence.member;

import com.greenlink.shopcore.domain.member.model.ShopAdminMember;
import com.greenlink.shopcore.domain.member.model.ShopAdminStatus;
import com.greenlink.shopcore.domain.member.port.ShopAdminMemberRepository;
import com.greenlink.shopcore.global.exception.ShopBusinessException;
import com.greenlink.shopcore.global.exception.ShopErrorCode;
import org.springframework.dao.DataIntegrityViolationException;
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
    public Optional<ShopAdminMember> findByAdminId(Long adminId) {
        return jpaRepository.findByAdminId(adminId).map(ShopAdminMemberJpaEntity::toDomain);
    }

    @Override
    public List<ShopAdminMember> findAll(ShopAdminStatus status) {
        List<ShopAdminMemberJpaEntity> rows = status == null
            ? jpaRepository.findAllByOrderByRequestedAtDesc()
            : jpaRepository.findByStatusOrderByRequestedAtDesc(status);
        return rows.stream().map(ShopAdminMemberJpaEntity::toDomain).toList();
    }

    @Override
    public boolean existsApprovedByCompanyId(Long companyId) {
        return jpaRepository.existsByCompanyIdAndStatus(companyId, ShopAdminStatus.APPROVED);
    }

    @Override
    public ShopAdminMember save(ShopAdminMember member) {
        ShopAdminMemberJpaEntity entity = member.getId() == null
            ? ShopAdminMemberJpaEntity.from(member)
            : jpaRepository.findById(member.getId()).map(e -> { e.apply(member); return e; })
                .orElseGet(() -> ShopAdminMemberJpaEntity.from(member));
        try {
            return jpaRepository.save(entity).toDomain();
        } catch (DataIntegrityViolationException e) {
            throw new ShopBusinessException(ShopErrorCode.ACCESS_ALREADY_REQUESTED);
        }
    }
}

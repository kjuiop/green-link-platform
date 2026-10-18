package com.greenlink.api.adapter.out.persistence.member;

import com.greenlink.api.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.member.model.Member;
import com.greenlink.core.domain.member.model.MemberStatus;
import com.greenlink.core.domain.member.model.Role;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "members")
public class MemberJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(name = "company_id")
    private Long companyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberRoleJpaEntity> roles = new ArrayList<>();

    protected MemberJpaEntity() {}

    public static MemberJpaEntity from(Member member) {
        MemberJpaEntity e = new MemberJpaEntity();
        e.id = member.getId();
        e.loginId = member.getLoginId();
        e.password = member.getPassword();
        e.name = member.getName();
        e.phone = member.getPhone();
        e.companyId = member.getCompanyId();
        e.status = member.getStatus();
        member.getRoles().forEach(role -> e.roles.add(MemberRoleJpaEntity.of(e, role)));
        return e;
    }

    public Member toDomain() {
        List<Role> domainRoles = roles.stream().map(MemberRoleJpaEntity::getRole).toList();
        return Member.of(id, loginId, password, name, phone, companyId, status, domainRoles);
    }
}

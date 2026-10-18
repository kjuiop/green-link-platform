package com.greenlink.admin.adapter.out.persistence.member;

import com.greenlink.core.domain.member.model.Role;
import jakarta.persistence.*;

@Entity
@Table(name = "member_roles",
       uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "role"}))
public class MemberRoleJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberJpaEntity member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    protected MemberRoleJpaEntity() {}

    public static MemberRoleJpaEntity of(MemberJpaEntity member, Role role) {
        MemberRoleJpaEntity e = new MemberRoleJpaEntity();
        e.member = member;
        e.role = role;
        return e;
    }

    public Role getRole() { return role; }
}

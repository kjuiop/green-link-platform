package com.greenlink.shop.adapter.out.persistence.member;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ShopAdminMemberJpaEntityTest {

    @Test
    void 희망_브랜드는_정렬된_콤마_문자열로_저장되고_복원된다() {
        String raw = ShopAdminMemberJpaEntity.join(Set.of(30L, 4L, 12L));

        assertThat(raw).isEqualTo("4,12,30");
        assertThat(ShopAdminMemberJpaEntity.split(raw)).containsExactlyInAnyOrder(4L, 12L, 30L);
    }

    @Test
    void 비어있거나_null이면_NULL로_저장하고_빈_집합으로_복원한다() {
        assertThat(ShopAdminMemberJpaEntity.join(Set.of())).isNull();
        assertThat(ShopAdminMemberJpaEntity.join(null)).isNull();
        assertThat(ShopAdminMemberJpaEntity.split(null)).isEmpty();
        assertThat(ShopAdminMemberJpaEntity.split("  ")).isEmpty();
    }
}

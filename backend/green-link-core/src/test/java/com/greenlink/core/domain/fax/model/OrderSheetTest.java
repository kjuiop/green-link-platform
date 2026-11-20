package com.greenlink.core.domain.fax.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderSheetTest {

    private OrderSheet sheet(String address, String memo) {
        return new OrderSheet("DF-1", "발주사", "02-1", "수주사", "02-2", "장미 × 1", 50000L,
                address, "2026-11-20 14:00까지", "홍길동", "010-1111-2222", "보내는분", "축하합니다", null, null, memo);
    }

    @Test
    void 같은_내용이면_해시가_같다() {
        assertThat(sheet("서울", "메모").contentHash()).isEqualTo(sheet("서울", "메모").contentHash());
    }

    @Test
    void 인쇄되는_내용이_바뀌면_해시가_달라진다() {
        assertThat(sheet("서울", "메모").contentHash()).isNotEqualTo(sheet("부산", "메모").contentHash());
        assertThat(sheet("서울", "메모").contentHash()).isNotEqualTo(sheet("서울", "다른 메모").contentHash());
    }

    @Test
    void 필드_경계가_달라지면_해시가_달라진다() {
        assertThat(sheet("서울 A", "B").contentHash()).isNotEqualTo(sheet("서울", "A B").contentHash());
    }
}

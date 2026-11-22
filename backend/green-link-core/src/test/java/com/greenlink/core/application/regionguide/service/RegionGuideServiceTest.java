package com.greenlink.core.application.regionguide.service;

import com.greenlink.core.application.regionguide.dto.RegionGuideResult;
import com.greenlink.core.application.regionguide.dto.SaveRegionGuideCommand;
import com.greenlink.core.domain.regionguide.model.RegionGuide;
import com.greenlink.core.domain.regionguide.model.RegionGuideCategory;
import com.greenlink.core.domain.regionguide.port.RegionGuideRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.regionguide.RegionGuideErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegionGuideServiceTest {

    @Mock RegionGuideRepository repository;
    @InjectMocks RegionGuideService service;

    private SaveRegionGuideCommand command(String sido, String placeName, RegionGuideCategory category, String content) {
        return new SaveRegionGuideCommand(sido, "  ", placeName, category, content, null, 0);
    }

    private RegionGuide guide(Long id, boolean active) {
        return RegionGuide.of(id, "대전광역시", null, "선샤인웨딩홀", RegionGuideCategory.BAN_ALL,
                "7층은 상품 일체 반입금지", null, 0, active, 1L, "2026-11-22 10:00:00");
    }

    @Test
    void 안내를_등록하면_활성_상태로_저장된다() {
        given(repository.save(any(RegionGuide.class))).willAnswer(inv -> inv.getArgument(0));

        RegionGuideResult result = service.create(
                command("대전광역시", "선샤인웨딩홀", RegionGuideCategory.BAN_ALL, "7층은 상품 일체 반입금지"), 1L);

        assertThat(result.active()).isTrue();
        assertThat(result.sigungu()).isNull();
        assertThat(result.category()).isEqualTo("BAN_ALL");
        assertThat(result.updatedBy()).isEqualTo(1L);
    }

    @Test
    void 허용되지_않은_시도는_등록할_수_없다() {
        assertThatThrownBy(() -> service.create(
                command("강원도", "속초의료원장례식장", RegionGuideCategory.OBJET, "오브제 가능"), 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(RegionGuideErrorCode.REGION_GUIDE_INVALID_SIDO);
        verify(repository, never()).save(any());
    }

    @Test
    void 공통_유의사항이_아니면_장소명이_필요하다() {
        assertThatThrownBy(() -> service.create(
                command("서울특별시", " ", RegionGuideCategory.BAN_ALL, "상품 일체 반입금지"), 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(RegionGuideErrorCode.REGION_GUIDE_INVALID_INPUT);
    }

    @Test
    void 공통_유의사항은_장소명_없이_등록할_수_있다() {
        given(repository.save(any(RegionGuide.class))).willAnswer(inv -> inv.getArgument(0));

        RegionGuideResult result = service.create(
                command("서울특별시", null, RegionGuideCategory.NOTICE, "성당·교회·카페는 발주 전 반입 가능 여부 확인"), 1L);

        assertThat(result.placeName()).isNull();
    }

    @Test
    void 안내를_수정한다() {
        given(repository.findById(1L)).willReturn(Optional.of(guide(1L, true)));
        given(repository.save(any(RegionGuide.class))).willAnswer(inv -> inv.getArgument(0));

        RegionGuideResult result = service.update(1L,
                command("대전광역시", "선샤인웨딩홀", RegionGuideCategory.BAN_WREATH, "7층 화환 반입불가"), 2L);

        assertThat(result.category()).isEqualTo("BAN_WREATH");
        assertThat(result.content()).isEqualTo("7층 화환 반입불가");
        assertThat(result.updatedBy()).isEqualTo(2L);
    }

    @Test
    void 존재하지_않는_안내는_수정할_수_없다() {
        given(repository.findById(9L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(9L,
                command("대전광역시", "선샤인웨딩홀", RegionGuideCategory.BAN_ALL, "내용"), 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(RegionGuideErrorCode.REGION_GUIDE_NOT_FOUND);
    }

    @Test
    void 활성_상태를_변경한다() {
        given(repository.findById(1L)).willReturn(Optional.of(guide(1L, true)));
        given(repository.save(any(RegionGuide.class))).willAnswer(inv -> inv.getArgument(0));

        RegionGuideResult result = service.changeActive(1L, false, 2L);

        assertThat(result.active()).isFalse();
    }

    @Test
    void 안내를_삭제한다() {
        given(repository.findById(1L)).willReturn(Optional.of(guide(1L, true)));

        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void 존재하지_않는_안내는_삭제할_수_없다() {
        given(repository.findById(9L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(9L)).isInstanceOf(BusinessException.class);
        verify(repository, never()).deleteById(any());
    }

    @Test
    void 활성_안내만_조회한다() {
        given(repository.findAllActive()).willReturn(List.of(guide(1L, true)));

        assertThat(service.getActiveGuides()).hasSize(1);
    }
}

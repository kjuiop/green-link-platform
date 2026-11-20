package com.greenlink.admin.adapter.out.fax;

import com.greenlink.core.domain.fax.model.OrderSheet;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PdfBoxOrderSheetRendererTest {

    private final PdfBoxOrderSheetRenderer renderer = new PdfBoxOrderSheetRenderer();

    private OrderSheet sheet(String memo, String extras) {
        return new OrderSheet("DF-20261120-0001", "발주사", "02-111-2222", "수주화원", "031-333-4444",
                "축하화환 3단 × 1", 55000L, "서울시 강남구 101호 / 그랜드볼룸 A홀", "2026-11-21 14:30까지",
                "김영희", "010-9876-5432", "홍길동", "축 결혼", extras, "신랑 측 하객", memo);
    }

    @Test
    void 한글_주문서를_PDF로_만든다() throws Exception {
        byte[] pdf = renderer.renderPdf(sheet("꽃 상태 확인 부탁드립니다", null));

        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
        try (PDDocument doc = Loader.loadPDF(pdf)) {
            assertThat(doc.getNumberOfPages()).isEqualTo(1);
            String text = new PDFTextStripper().getText(doc);
            assertThat(text).contains("주문서", "인수증", "수주화원", "그랜드볼룸", "DF-20261120-0001");
        }
    }

    @Test
    void 글꼴에_없는_문자와_긴_문장이_있어도_실패하지_않는다() throws Exception {
        String longMemo = "아주 긴 요청사항 ".repeat(40) + "\n줄바꿈 😀 이모지 <script>&</script>";
        String extras = "[{\"left\":\"추가 보내는분\",\"right\":\"추가 문구\"}]";

        byte[] pdf = renderer.renderPdf(sheet(longMemo, extras));

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            assertThat(new PDFTextStripper().getText(doc)).contains("추가 문구");
        }
    }
}

package com.greenlink.admin.adapter.out.fax;

import com.greenlink.core.domain.fax.model.OrderSheet;
import com.greenlink.core.domain.fax.port.OrderSheetRenderer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class PdfBoxOrderSheetRenderer implements OrderSheetRenderer {

    private static final String FONT_PATH = "fonts/NanumGothic-Regular.ttf";
    private static final float MARGIN = 40f;
    private static final float TABLE_WIDTH = PDRectangle.A4.getWidth() - MARGIN * 2;
    private static final float LABEL_WIDTH = 90f;
    private static final float PAD = 6f;
    private static final float FONT_SIZE = 12f;
    private static final float TITLE_SIZE = 18f;
    private static final float LEADING = 16f;
    private static final float MIN_ROW = 28f;

    private final JsonMapper objectMapper = JsonMapper.builder().build();

    @Override
    public byte[] renderPdf(OrderSheet sheet) {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDType0Font font;
            try (InputStream in = new ClassPathResource(FONT_PATH).getInputStream()) {
                font = PDType0Font.load(doc, in);
            }
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float y = PDRectangle.A4.getHeight() - MARGIN;
                y = drawSection(cs, font, sheet, "주문서", true, y);
                y -= 24f;
                drawSection(cs, font, sheet, "인수증", false, y);
            }
            doc.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private float drawSection(PDPageContentStream cs, PDType0Font font, OrderSheet s, String title,
                              boolean includeAmountAndMemo, float top) throws IOException {
        float y = top;
        drawText(cs, font, FONT_SIZE, MARGIN, y - 12f, "주문번호 " + nz(s.orderNumber()));
        y -= 18f;

        y = row(cs, font, y, new Cell(title, 0.3f, TITLE_SIZE), new Cell("http://downflower.co.kr", 0.7f, FONT_SIZE));
        y = row(cs, font, y, label("발주화원"), new Cell(nz(s.orderingCompanyName()), 0.4f, FONT_SIZE),
                new Cell("TEL: " + nz(s.orderingCompanyPhone()), 0.35f, FONT_SIZE));
        y = row(cs, font, y, label("수주화원"), new Cell(nz(s.vendorName()), 0.4f, FONT_SIZE),
                new Cell("TEL: " + nz(s.vendorPhone()), 0.35f, FONT_SIZE));
        if (includeAmountAndMemo) {
            y = row(cs, font, y, label("상품명"), new Cell(nz(s.productSummary()), 0.4f, FONT_SIZE),
                    new Cell("발주금액: " + String.format("%,d", s.totalAmount()) + "원", 0.35f, FONT_SIZE));
        } else {
            y = row(cs, font, y, label("상품명"), new Cell(nz(s.productSummary()), 0.75f, FONT_SIZE));
        }
        y = row(cs, font, y, label("배달정보"),
                new Cell("배달주소: " + nz(s.deliveryAddress()) + "\n배달일시: " + nz(s.deliveryDateTime()), 0.75f, FONT_SIZE));
        y = row(cs, font, y, label("받는 고객명"), new Cell(nz(s.customerName()), 0.4f, FONT_SIZE),
                new Cell(blank(s.customerPhone()) ? "" : "HP: " + s.customerPhone(), 0.35f, FONT_SIZE));
        y = row(cs, font, y, label("리본"), new Cell(ribbonText(s), 0.75f, FONT_SIZE));
        if (!blank(s.eventInfo())) {
            y = row(cs, font, y, label("행사정보"), new Cell(s.eventInfo(), 0.75f, FONT_SIZE));
        }
        if (includeAmountAndMemo) {
            y = row(cs, font, y, label("요청사항"), new Cell(nz(s.vendorMemo()), 0.75f, FONT_SIZE));
        }
        return y;
    }

    private String ribbonText(OrderSheet s) {
        List<String> lines = new ArrayList<>();
        List<Map<String, String>> extras = parseExtras(s.ribbonExtrasJson());
        if (extras.isEmpty()) {
            lines.add("경조사어: " + nz(s.ribbonRight()));
            lines.add("보내시는 분: " + nz(s.ribbonLeft()));
        } else {
            lines.add("1. 경조사어: " + nz(s.ribbonRight()) + " / 보내시는 분: " + nz(s.ribbonLeft()));
            int i = 2;
            for (Map<String, String> e : extras) {
                lines.add(i++ + ". 경조사어: " + nz(e.get("right")) + " / 보내시는 분: " + nz(e.get("left")));
            }
        }
        return String.join("\n", lines);
    }

    private List<Map<String, String>> parseExtras(String json) {
        if (blank(json)) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, String>>>() {});
        } catch (JacksonException e) {
            return List.of();
        }
    }

    private float row(PDPageContentStream cs, PDType0Font font, float top, Cell... cells) throws IOException {
        float[] widths = new float[cells.length];
        float fixed = 0f;
        float rest = 0f;
        for (int i = 0; i < cells.length; i++) {
            if (cells[i].fixedWidth > 0f) {
                widths[i] = cells[i].fixedWidth;
                fixed += widths[i];
            } else {
                rest += cells[i].ratio;
            }
        }
        float remaining = TABLE_WIDTH - fixed;
        for (int i = 0; i < cells.length; i++) {
            if (cells[i].fixedWidth <= 0f) widths[i] = remaining * cells[i].ratio / rest;
        }

        List<List<String>> wrapped = new ArrayList<>();
        float height = MIN_ROW;
        for (int i = 0; i < cells.length; i++) {
            List<String> lines = wrap(font, cells[i].size, cells[i].text, widths[i] - PAD * 2);
            wrapped.add(lines);
            height = Math.max(height, lines.size() * (cells[i].size + 4f) + PAD * 2);
        }

        float x = MARGIN;
        for (int i = 0; i < cells.length; i++) {
            cs.setLineWidth(1f);
            cs.addRect(x, top - height, widths[i], height);
            cs.stroke();
            float ty = top - PAD - cells[i].size;
            for (String line : wrapped.get(i)) {
                drawText(cs, font, cells[i].size, x + PAD, ty, line);
                ty -= cells[i].size + 4f;
            }
            x += widths[i];
        }
        return top - height;
    }

    private List<String> wrap(PDType0Font font, float size, String text, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        for (String paragraph : sanitize(font, text).split("\n", -1)) {
            StringBuilder current = new StringBuilder();
            for (int i = 0; i < paragraph.length(); ) {
                int cp = paragraph.codePointAt(i);
                String ch = new String(Character.toChars(cp));
                float w = font.getStringWidth(current + ch) / 1000f * size;
                if (w > maxWidth && current.length() > 0) {
                    lines.add(current.toString());
                    current.setLength(0);
                }
                current.append(ch);
                i += Character.charCount(cp);
            }
            lines.add(current.toString());
        }
        return lines;
    }

    private String sanitize(PDType0Font font, String text) {
        StringBuilder sb = new StringBuilder();
        String normalized = nz(text).replace("\r\n", "\n").replace('\r', '\n').replace('\t', ' ');
        for (int i = 0; i < normalized.length(); ) {
            int cp = normalized.codePointAt(i);
            i += Character.charCount(cp);
            String ch = new String(Character.toChars(cp));
            if (cp == '\n') {
                sb.append(ch);
                continue;
            }
            if (Character.isISOControl(cp)) continue;
            try {
                font.encode(ch);
                sb.append(ch);
            } catch (IllegalArgumentException | IOException e) {
                sb.append('?');
            }
        }
        return sb.toString();
    }

    private void drawText(PDPageContentStream cs, PDType0Font font, float size, float x, float y, String text) throws IOException {
        if (text == null || text.isEmpty()) return;
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    private Cell label(String text) {
        Cell c = new Cell(text, 0f, FONT_SIZE);
        c.fixedWidth = LABEL_WIDTH;
        return c;
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private static final class Cell {
        final String text;
        final float ratio;
        final float size;
        float fixedWidth;

        Cell(String text, float ratio, float size) {
            this.text = text;
            this.ratio = ratio;
            this.size = size;
        }
    }
}

package com.greenlink.admin.application.excel;

import com.greenlink.core.application.settlement.dto.SettlementOrderResult;
import com.greenlink.core.application.settlement.usecase.ExportSettlementExcelUseCase;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class SettlementExcelService implements ExportSettlementExcelUseCase {

    private static final String[] HEADERS = {
            "주문번호", "상대업체", "상품명", "배송지", "주문자", "배송완료일",
            "주문금액", "추가비용", "발주금액", "정산금액"
    };

    @Override
    public byte[] export(List<SettlementOrderResult> orders) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("정산목록");

            CellStyle headerStyle = createHeaderStyle(workbook);
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (SettlementOrderResult o : orders) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(nullToEmpty(o.orderNumber()));
                row.createCell(1).setCellValue(nullToEmpty(o.counterpartCompanyName()));
                row.createCell(2).setCellValue(nullToEmpty(o.productName()));
                row.createCell(3).setCellValue(nullToEmpty(o.placeName()));
                row.createCell(4).setCellValue(nullToEmpty(o.ordererName()));
                row.createCell(5).setCellValue(nullToEmpty(o.deliveredAt()));
                row.createCell(6).setCellValue(o.orderAmount());
                row.createCell(7).setCellValue(o.extraCost() != null ? o.extraCost() : 0L);
                row.createCell(8).setCellValue(o.totalAmount());
                row.createCell(9).setCellValue(o.vendorAmount() != null ? o.vendorAmount() : 0L);
            }

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("엑셀 파일 생성에 실패했습니다.", e);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private String nullToEmpty(String v) {
        return v != null ? v : "";
    }
}

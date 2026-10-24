package com.greenlink.admin.application.excel;

import com.greenlink.core.application.excel.usecase.ExportOrderExcelUseCase;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.model.OrderStatus;
import com.greenlink.core.domain.order.port.OrderRepository;
import com.greenlink.core.domain.receipt.model.ReceiptConfirmation;
import com.greenlink.core.domain.receipt.port.ReceiptConfirmationRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class OrderExcelService implements ExportOrderExcelUseCase {

    private static final String[] HEADERS = {
            "주문번호", "배송요구일", "주문접수일", "주문자", "받는분",
            "수주업체", "상품명", "배송지", "수량", "수주금액",
            "배송현황", "인수자", "관계", "인수일자"
    };

    private final OrderRepository orderRepository;
    private final ReceiptConfirmationRepository receiptRepository;
    private final AdminRepository adminRepository;
    private final CompanyRepository companyRepository;

    public OrderExcelService(OrderRepository orderRepository,
                              ReceiptConfirmationRepository receiptRepository,
                              AdminRepository adminRepository,
                              CompanyRepository companyRepository) {
        this.orderRepository = orderRepository;
        this.receiptRepository = receiptRepository;
        this.adminRepository = adminRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] export(List<Long> orderIds) {
        List<Order> orders = orderRepository.findAllByIds(orderIds);
        Map<Long, String> vendorNameMap = buildVendorNameMap();
        Map<Long, ReceiptConfirmation> receiptMap = receiptRepository.findAllByOrderIds(orderIds)
                .stream().collect(Collectors.toMap(ReceiptConfirmation::getOrderId, r -> r));

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("주문목록");

            CellStyle headerStyle = createHeaderStyle(workbook);
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Order order : orders) {
                Row row = sheet.createRow(rowNum++);
                ReceiptConfirmation receipt = receiptMap.get(order.getId());
                String vendorName = vendorNameMap.getOrDefault(order.getVendorId(), "");
                fillRow(row, order, vendorName, receipt);
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

    private void fillRow(Row row, Order order, String vendorName, ReceiptConfirmation receipt) {
        String fullAddress = order.getAddress() != null ? order.getAddress() : "";
        if (order.getAddressDetail() != null && !order.getAddressDetail().isBlank()) {
            fullAddress = fullAddress + " " + order.getAddressDetail();
        }

        row.createCell(0).setCellValue(nullToEmpty(order.getOrderNumber()));
        row.createCell(1).setCellValue(nullToEmpty(order.getDeliveryDate()));
        row.createCell(2).setCellValue(nullToEmpty(order.getCreatedAt()));
        row.createCell(3).setCellValue(nullToEmpty(order.getOrdererName()));
        row.createCell(4).setCellValue(nullToEmpty(order.getRecipientName()));
        row.createCell(5).setCellValue(vendorName);
        row.createCell(6).setCellValue(nullToEmpty(order.getProductName()));
        row.createCell(7).setCellValue(fullAddress.trim());
        row.createCell(8).setCellValue(order.getQuantity());
        row.createCell(9).setCellValue(order.getAmount());
        row.createCell(10).setCellValue(statusToKorean(order.getStatus()));
        row.createCell(11).setCellValue(receipt != null ? nullToEmpty(receipt.getReceiverName()) : "");
        row.createCell(12).setCellValue(receipt != null ? nullToEmpty(receipt.getReceiverRelation()) : "");
        row.createCell(13).setCellValue(receipt != null && receipt.getConfirmedAt() != null
                ? receipt.getConfirmedAt().toString() : "");
    }

    private Map<Long, String> buildVendorNameMap() {
        List<Admin> vendors = adminRepository.findAllVendors();
        List<Long> companyIds = vendors.stream()
                .map(Admin::getCompanyId)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, String> companyNameById = companyRepository.findAllByIdIn(companyIds).stream()
                .collect(Collectors.toMap(Company::getId, Company::getCompanyName));
        return vendors.stream()
                .filter(a -> a.getCompanyId() != null && companyNameById.containsKey(a.getCompanyId()))
                .collect(Collectors.toMap(Admin::getId, a -> companyNameById.get(a.getCompanyId())));
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

    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }

    private String statusToKorean(OrderStatus status) {
        if (status == null) return "";
        return switch (status) {
            case PENDING -> "발주대기";
            case ACCEPTED -> "수주완료";
            case DELIVERED -> "배송완료";
            case REJECTED -> "수주거절";
            case CANCELLED -> "주문취소";
        };
    }
}

package com.greenlink.admin.adapter.in.web;

import com.greenlink.core.application.excel.usecase.ExportOrderExcelUseCase;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orders/export")
public class OrderExportController {

    private final ExportOrderExcelUseCase exportUseCase;

    public OrderExportController(ExportOrderExcelUseCase exportUseCase) {
        this.exportUseCase = exportUseCase;
    }

    @PostMapping("/excel")
    public ResponseEntity<byte[]> exportExcel(@RequestBody ExcelExportRequest request) {
        byte[] bytes = exportUseCase.export(request.orderIds());
        String filename = "orders_" + LocalDate.now() + ".xlsx";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(bytes);
    }

    record ExcelExportRequest(List<Long> orderIds) {}
}

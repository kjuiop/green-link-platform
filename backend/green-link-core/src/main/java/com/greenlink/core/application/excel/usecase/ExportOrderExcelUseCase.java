package com.greenlink.core.application.excel.usecase;

import java.util.List;

public interface ExportOrderExcelUseCase {
    byte[] export(List<Long> orderIds);
}

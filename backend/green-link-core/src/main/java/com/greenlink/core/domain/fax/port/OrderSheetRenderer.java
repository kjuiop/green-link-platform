package com.greenlink.core.domain.fax.port;

import com.greenlink.core.domain.fax.model.OrderSheet;

public interface OrderSheetRenderer {
    byte[] renderPdf(OrderSheet sheet);
}

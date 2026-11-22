package com.greenlink.core.domain.customerorder.model;

import java.util.List;

public record CustomerOrderSlice(List<CustomerOrder> content, long totalElements) {
}

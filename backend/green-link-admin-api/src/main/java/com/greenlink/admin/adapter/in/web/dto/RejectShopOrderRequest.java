package com.greenlink.admin.adapter.in.web.dto;

import jakarta.validation.constraints.Size;

public record RejectShopOrderRequest(@Size(max = 500) String reason) {}

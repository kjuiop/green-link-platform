package com.greenlink.admin.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record UpdateOrderStatusRequest(@NotBlank String action, List<String> photoUrls) {}

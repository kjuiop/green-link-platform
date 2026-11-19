package com.greenlink.core.application.coupang.dto;

import java.time.LocalDateTime;

public record CoupangSyncResult(LocalDateTime from, LocalDateTime to, int fetchedCount, int insertedCount) {}

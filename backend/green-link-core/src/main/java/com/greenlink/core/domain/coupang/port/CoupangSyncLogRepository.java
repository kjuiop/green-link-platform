package com.greenlink.core.domain.coupang.port;

import com.greenlink.core.domain.coupang.model.CoupangSyncLog;

import java.util.List;

public interface CoupangSyncLogRepository {

    CoupangSyncLog save(CoupangSyncLog log);

    List<CoupangSyncLog> findRecent(Long companyId, int limit);
}

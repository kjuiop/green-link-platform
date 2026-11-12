package com.greenlink.core.domain.settlement.model;

public class Settlement {

    private Long id;
    private Long companyId;
    private SettlementType type;
    private String fromDate;
    private String toDate;
    private String completedAt;
    private Long completedByAdminId;

    private Settlement() {}

    public static Settlement complete(Long companyId, SettlementType type,
                                       String fromDate, String toDate,
                                       String completedAt, Long completedByAdminId) {
        Settlement s = new Settlement();
        s.companyId = companyId;
        s.type = type;
        s.fromDate = fromDate;
        s.toDate = toDate;
        s.completedAt = completedAt;
        s.completedByAdminId = completedByAdminId;
        return s;
    }

    public static Settlement of(Long id, Long companyId, SettlementType type,
                                 String fromDate, String toDate,
                                 String completedAt, Long completedByAdminId) {
        Settlement s = new Settlement();
        s.id = id;
        s.companyId = companyId;
        s.type = type;
        s.fromDate = fromDate;
        s.toDate = toDate;
        s.completedAt = completedAt;
        s.completedByAdminId = completedByAdminId;
        return s;
    }

    public Long getId() { return id; }
    public Long getCompanyId() { return companyId; }
    public SettlementType getType() { return type; }
    public String getFromDate() { return fromDate; }
    public String getToDate() { return toDate; }
    public String getCompletedAt() { return completedAt; }
    public Long getCompletedByAdminId() { return completedByAdminId; }
}

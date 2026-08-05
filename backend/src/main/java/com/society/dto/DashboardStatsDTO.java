package com.society.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardStatsDTO {
    private long totalResidents;
    private long totalStaff;
    private long totalComplaints;
    private long pendingComplaints;
    private long assignedComplaints;
    private long inProgressComplaints;
    private long resolvedComplaints;
    private long totalBills;
    private long paidBills;
    private long pendingBills;
    private long overdueBills;
    private long totalPayments;
}

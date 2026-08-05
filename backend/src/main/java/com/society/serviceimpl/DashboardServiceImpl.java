package com.society.serviceimpl;

import com.society.dto.DashboardStatsDTO;
import com.society.entity.Complaint;
import com.society.entity.MaintenanceBill;
import com.society.repository.*;
import com.society.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final ResidentRepository residentRepository;
    private final StaffRepository staffRepository;
    private final ComplaintRepository complaintRepository;
    private final MaintenanceBillRepository billRepository;
    private final PaymentRepository paymentRepository;

    @Override public DashboardStatsDTO getStats() {
        return DashboardStatsDTO.builder()
            .totalResidents(residentRepository.count())
            .totalStaff(staffRepository.count())
            .totalComplaints(complaintRepository.count())
            .pendingComplaints(complaintRepository.countByStatus(Complaint.Status.PENDING))
            .assignedComplaints(complaintRepository.countByStatus(Complaint.Status.ASSIGNED))
            .inProgressComplaints(complaintRepository.countByStatus(Complaint.Status.IN_PROGRESS))
            .resolvedComplaints(complaintRepository.countByStatus(Complaint.Status.RESOLVED))
            .totalBills(billRepository.count())
            .paidBills(billRepository.countByStatus(MaintenanceBill.BillStatus.PAID))
            .pendingBills(billRepository.countByStatus(MaintenanceBill.BillStatus.PENDING))
            .overdueBills(billRepository.countByStatus(MaintenanceBill.BillStatus.OVERDUE))
            .totalPayments(paymentRepository.count())
            .build();
    }
}

package com.society.serviceimpl;

import com.society.dto.DashboardStatsDTO;
import com.society.entity.Complaint;
import com.society.entity.MaintenanceBill;
import com.society.repository.*;
import com.society.security.SecurityUtils;
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
    private final SecurityUtils securityUtils;

    @Override public DashboardStatsDTO getStats() {
        Long societyId = securityUtils.getCurrentSocietyId();
        return DashboardStatsDTO.builder()
            .totalResidents(residentRepository.countBySociety_SocietyId(societyId))
            .totalStaff(staffRepository.countBySociety_SocietyId(societyId))
            .totalComplaints(complaintRepository.countBySociety_SocietyId(societyId))
            .pendingComplaints(complaintRepository.countByStatusAndSociety_SocietyId(Complaint.Status.PENDING, societyId))
            .assignedComplaints(complaintRepository.countByStatusAndSociety_SocietyId(Complaint.Status.ASSIGNED, societyId))
            .inProgressComplaints(complaintRepository.countByStatusAndSociety_SocietyId(Complaint.Status.IN_PROGRESS, societyId))
            .resolvedComplaints(complaintRepository.countByStatusAndSociety_SocietyId(Complaint.Status.RESOLVED, societyId))
            .totalBills(billRepository.countBySociety_SocietyId(societyId))
            .paidBills(billRepository.countByStatusAndSociety_SocietyId(MaintenanceBill.BillStatus.PAID, societyId))
            .pendingBills(billRepository.countByStatusAndSociety_SocietyId(MaintenanceBill.BillStatus.PENDING, societyId))
            .overdueBills(billRepository.countByStatusAndSociety_SocietyId(MaintenanceBill.BillStatus.OVERDUE, societyId))
            .totalPayments(paymentRepository.countBySociety_SocietyId(societyId))
            .build();
    }
}

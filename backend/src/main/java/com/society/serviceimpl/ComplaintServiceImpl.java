package com.society.serviceimpl;

import com.society.dto.ComplaintDTO;
import com.society.dto.ComplaintStatusUpdateDTO;
import com.society.entity.*;
import com.society.exception.BadRequestException;
import com.society.exception.ResourceNotFoundException;
import com.society.repository.*;
import com.society.security.SecurityUtils;
import com.society.service.ComplaintService;
import com.society.service.NotificationService;
import com.society.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository        complaintRepository;
    private final ComplaintHistoryRepository historyRepository;
    private final ResidentRepository         residentRepository;
    private final StaffRepository            staffRepository;
    private final UserRepository             userRepository;
    private final NotificationService        notificationService;
    private final SecurityUtils              securityUtils;

    @Override
    @Transactional
    public ComplaintDTO createComplaint(ComplaintDTO dto, Long userId) {
        Long societyId = securityUtils.getCurrentSocietyId();
        Resident resident = residentRepository.findByUser_UserIdAndSociety_SocietyId(userId, societyId)
            .orElseThrow(() -> new ResourceNotFoundException("Resident profile not found"));

        Complaint complaint = Complaint.builder()
            .title(dto.getTitle())
            .description(dto.getDescription())
            .status(Complaint.Status.PENDING)
            .resident(resident)
            .society(resident.getSociety())
            .build();
        complaint = complaintRepository.save(complaint);

        // Notify every Society Admin of THIS resident's society (never a
        // hardcoded global user id, which would leak across tenants).
        for (User admin : userRepository.findByRole_RoleNameAndSociety_SocietyId(AppConstants.ROLE_ADMIN, societyId)) {
            notificationService.sendNotification(admin.getUserId(),
                "New complaint raised by " + resident.getUser().getName() + ": " + dto.getTitle(),
                "COMPLAINT");
        }

        return toDTO(complaint);
    }

    @Override
    public Page<ComplaintDTO> getAllComplaints(Pageable pageable) {
        return complaintRepository.findBySociety_SocietyId(securityUtils.getCurrentSocietyId(), pageable).map(this::toDTO);
    }

    @Override
    public Page<ComplaintDTO> getComplaintsByResident(Long residentId, Pageable pageable) {
        return complaintRepository.findByResident_ResidentIdAndSociety_SocietyId(
            residentId, securityUtils.getCurrentSocietyId(), pageable).map(this::toDTO);
    }

    @Override
    public Page<ComplaintDTO> getComplaintsByStaff(Long staffId, Pageable pageable) {
        return complaintRepository.findByAssignedStaff_StaffIdAndSociety_SocietyId(
            staffId, securityUtils.getCurrentSocietyId(), pageable).map(this::toDTO);
    }

    @Override
    public ComplaintDTO getComplaintById(Long id) {
        return toDTO(findById(id));
    }

    @Override
    @Transactional
    public ComplaintDTO updateComplaintStatus(Long id, ComplaintStatusUpdateDTO dto, Long updatedByUserId) {
        Complaint complaint = findById(id);
        Complaint.Status oldStatus = complaint.getStatus();
        Complaint.Status newStatus;
        try {
            newStatus = Complaint.Status.valueOf(dto.getStatus());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + dto.getStatus());
        }

        User updatedBy = userRepository.findById(updatedByUserId).orElse(null);
        ComplaintHistory history = ComplaintHistory.builder()
            .complaint(complaint)
            .oldStatus(oldStatus)
            .newStatus(newStatus)
            .remarks(dto.getRemarks())
            .updatedBy(updatedBy)
            .build();
        historyRepository.save(history);

        complaint.setStatus(newStatus);
        complaint = complaintRepository.save(complaint);

        notificationService.sendNotification(
            complaint.getResident().getUser().getUserId(),
            "Your complaint \"" + complaint.getTitle() + "\" status updated to: " + newStatus,
            "COMPLAINT");

        return toDTO(complaint);
    }

    @Override
    @Transactional
    public ComplaintDTO assignComplaint(Long id, Long staffId, Long adminUserId) {
        Complaint complaint = findById(id);
        Staff staff = staffRepository.findByStaffIdAndSociety_SocietyId(staffId, securityUtils.getCurrentSocietyId())
            .orElseThrow(() -> new ResourceNotFoundException("Staff not found: " + staffId));

        Complaint.Status oldStatus = complaint.getStatus();
        complaint.setAssignedStaff(staff);
        complaint.setStatus(Complaint.Status.ASSIGNED);
        complaint = complaintRepository.save(complaint);

        User admin = userRepository.findById(adminUserId).orElse(null);
        historyRepository.save(ComplaintHistory.builder()
            .complaint(complaint)
            .oldStatus(oldStatus)
            .newStatus(Complaint.Status.ASSIGNED)
            .remarks("Assigned to " + staff.getUser().getName())
            .updatedBy(admin)
            .build());

        notificationService.sendNotification(staff.getUser().getUserId(),
            "New complaint assigned to you: " + complaint.getTitle(), "COMPLAINT");
        notificationService.sendNotification(complaint.getResident().getUser().getUserId(),
            "Your complaint \"" + complaint.getTitle() + "\" has been assigned to " + staff.getUser().getName(),
            "COMPLAINT");

        return toDTO(complaint);
    }

    @Override
    @Transactional
    public void deleteComplaint(Long id) {
        complaintRepository.delete(findById(id));
    }

    // ---------- helpers ----------
    private Complaint findById(Long id) {
        return complaintRepository.findByComplaintIdAndSociety_SocietyId(id, securityUtils.getCurrentSocietyId())
            .orElseThrow(() -> new ResourceNotFoundException("Complaint not found: " + id));
    }

    private ComplaintDTO toDTO(Complaint c) {
        ComplaintDTO dto = new ComplaintDTO();
        dto.setComplaintId(c.getComplaintId());
        dto.setTitle(c.getTitle());
        dto.setDescription(c.getDescription());
        dto.setStatus(c.getStatus().name());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        dto.setResidentId(c.getResident().getResidentId());
        dto.setResidentName(c.getResident().getUser().getName());
        dto.setFlatNo(c.getResident().getFlatNo());
        if (c.getAssignedStaff() != null) {
            dto.setAssignedStaffId(c.getAssignedStaff().getStaffId());
            dto.setAssignedStaffName(c.getAssignedStaff().getUser().getName());
            dto.setDepartment(c.getAssignedStaff().getDepartment());
        }
        return dto;
    }
}

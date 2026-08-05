package com.society.service;
import com.society.dto.ComplaintDTO;
import com.society.dto.ComplaintStatusUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface ComplaintService {
    ComplaintDTO createComplaint(ComplaintDTO dto, Long userId);
    Page<ComplaintDTO> getAllComplaints(Pageable pageable);
    Page<ComplaintDTO> getComplaintsByResident(Long residentId, Pageable pageable);
    Page<ComplaintDTO> getComplaintsByStaff(Long staffId, Pageable pageable);
    ComplaintDTO getComplaintById(Long id);
    ComplaintDTO updateComplaintStatus(Long id, ComplaintStatusUpdateDTO dto, Long updatedByUserId);
    ComplaintDTO assignComplaint(Long id, Long staffId, Long adminUserId);
    void deleteComplaint(Long id);
}

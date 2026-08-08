package com.society.service;
import com.society.dto.StaffDTO;
import java.util.List;
public interface StaffService {
    List<StaffDTO> getAllStaff();
    StaffDTO getStaffById(Long id);
    StaffDTO getStaffByUserId(Long userId);
    StaffDTO updateStaff(Long id, StaffDTO dto);
    void deleteStaff(Long id);

    /** Self-registered staff in the caller's society awaiting approval. */
    List<StaffDTO> getPendingStaff();
    /** Approves a pending staff member, allowing them to log in. */
    StaffDTO approveStaff(Long id);
    /** Rejects (and removes) a pending staff registration. */
    void rejectStaff(Long id);
}

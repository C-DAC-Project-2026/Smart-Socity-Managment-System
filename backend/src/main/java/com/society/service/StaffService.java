package com.society.service;
import com.society.dto.StaffDTO;
import java.util.List;
public interface StaffService {
    List<StaffDTO> getAllStaff();
    StaffDTO getStaffById(Long id);
    StaffDTO getStaffByUserId(Long userId);
    StaffDTO updateStaff(Long id, StaffDTO dto);
    void deleteStaff(Long id);
}

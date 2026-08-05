package com.society.serviceimpl;

import com.society.dto.StaffDTO;
import com.society.entity.Staff;
import com.society.exception.ResourceNotFoundException;
import com.society.repository.StaffRepository;
import com.society.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {
    private final StaffRepository staffRepository;

    @Override public List<StaffDTO> getAllStaff() {
        return staffRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }
    @Override public StaffDTO getStaffById(Long id) { return toDTO(findById(id)); }
    @Override public StaffDTO getStaffByUserId(Long userId) {
        return toDTO(staffRepository.findByUser_UserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found for user: " + userId)));
    }
    @Override @Transactional public StaffDTO updateStaff(Long id, StaffDTO dto) {
        Staff s = findById(id);
        s.setDepartment(dto.getDepartment()); s.setMobile(dto.getMobile());
        return toDTO(staffRepository.save(s));
    }
    @Override @Transactional public void deleteStaff(Long id) { staffRepository.delete(findById(id)); }
    private Staff findById(Long id) { return staffRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Staff not found: " + id)); }
    public StaffDTO toDTO(Staff s) {
        return StaffDTO.builder().staffId(s.getStaffId()).userId(s.getUser().getUserId())
            .name(s.getUser().getName()).email(s.getUser().getEmail())
            .department(s.getDepartment()).mobile(s.getMobile()).build();
    }
}

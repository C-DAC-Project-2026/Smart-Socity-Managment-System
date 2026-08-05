package com.society.service;
import com.society.dto.ResidentDTO;
import java.util.List;
public interface ResidentService {
    List<ResidentDTO> getAllResidents();
    ResidentDTO getResidentById(Long id);
    ResidentDTO getResidentByUserId(Long userId);
    ResidentDTO updateResident(Long id, ResidentDTO dto);
    void deleteResident(Long id);
    List<ResidentDTO> searchResidents(String query);
}

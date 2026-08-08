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

    /** Self-registered residents in the caller's society awaiting approval. */
    List<ResidentDTO> getPendingResidents();
    /** Approves a pending resident, allowing them to log in. */
    ResidentDTO approveResident(Long id);
    /** Rejects (and removes) a pending resident registration. */
    void rejectResident(Long id);
}

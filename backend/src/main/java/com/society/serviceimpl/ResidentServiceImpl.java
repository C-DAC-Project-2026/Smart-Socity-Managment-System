package com.society.serviceimpl;

import com.society.dto.ResidentDTO;
import com.society.entity.Resident;
import com.society.exception.ResourceNotFoundException;
import com.society.repository.ResidentRepository;
import com.society.repository.UserRepository;
import com.society.security.SecurityUtils;
import com.society.service.ResidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResidentServiceImpl implements ResidentService {

    private final ResidentRepository residentRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    @Override
    public List<ResidentDTO> getAllResidents() {
        return residentRepository.findAllBySociety_SocietyId(securityUtils.getCurrentSocietyId())
            .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public ResidentDTO getResidentById(Long id) {
        return toDTO(findById(id));
    }

    @Override
    public ResidentDTO getResidentByUserId(Long userId) {
        Resident resident = residentRepository
            .findByUser_UserIdAndSociety_SocietyId(userId, securityUtils.getCurrentSocietyId())
            .orElseThrow(() -> new ResourceNotFoundException("Resident profile not found for user: " + userId));
        return toDTO(resident);
    }

    @Override
    @Transactional
    public ResidentDTO updateResident(Long id, ResidentDTO dto) {
        Resident resident = findById(id);
        resident.setAddress(dto.getAddress());
        resident.setMobile(dto.getMobile());
        return toDTO(residentRepository.save(resident));
    }

    @Override
    @Transactional
    public void deleteResident(Long id) {
        residentRepository.delete(findById(id));
    }

    @Override
    public List<ResidentDTO> searchResidents(String query) {
        return residentRepository.searchByNameOrFlatNo(securityUtils.getCurrentSocietyId(), query)
            .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<ResidentDTO> getPendingResidents() {
        return residentRepository.findAllBySociety_SocietyIdAndUser_ActiveFalse(securityUtils.getCurrentSocietyId())
            .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ResidentDTO approveResident(Long id) {
        Resident resident = findById(id);
        resident.getUser().setActive(true);
        userRepository.save(resident.getUser());
        return toDTO(resident);
    }

    @Override
    @Transactional
    public void rejectResident(Long id) {
        // Removing the resident row and its linked user together discards the
        // whole self-registration attempt, freeing up the email/flat number
        // for a future (correct) registration.
        Resident resident = findById(id);
        residentRepository.delete(resident);
        userRepository.delete(resident.getUser());
    }

    // ---------- helpers ----------

    /**
     * Looks up a resident by id SCOPED to the caller's society. If the id
     * belongs to a resident in a different society, this behaves exactly
     * like "not found" — it never leaks whether the id exists elsewhere.
     */
    private Resident findById(Long id) {
        return residentRepository.findByResidentIdAndSociety_SocietyId(id, securityUtils.getCurrentSocietyId())
            .orElseThrow(() -> new ResourceNotFoundException("Resident not found with id: " + id));
    }

    public ResidentDTO toDTO(Resident r) {
        return ResidentDTO.builder()
            .residentId(r.getResidentId())
            .userId(r.getUser().getUserId())
            .name(r.getUser().getName())
            .email(r.getUser().getEmail())
            .address(r.getAddress())
            .mobile(r.getMobile())
            .flatNo(r.getFlatNo())
            .active(r.getUser().getActive())
            .build();
    }
}

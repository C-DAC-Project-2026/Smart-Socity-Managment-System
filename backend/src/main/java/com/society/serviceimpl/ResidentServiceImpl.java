package com.society.serviceimpl;

import com.society.dto.ResidentDTO;
import com.society.entity.Resident;
import com.society.exception.ResourceNotFoundException;
import com.society.repository.ResidentRepository;
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

    @Override
    public List<ResidentDTO> getAllResidents() {
        return residentRepository.findAll().stream()
            .map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public ResidentDTO getResidentById(Long id) {
        return toDTO(findById(id));
    }

    @Override
    public ResidentDTO getResidentByUserId(Long userId) {
        Resident resident = residentRepository.findByUser_UserId(userId)
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
        return residentRepository.searchByNameOrFlatNo(query).stream()
            .map(this::toDTO).collect(Collectors.toList());
    }

    // ---------- helpers ----------
    private Resident findById(Long id) {
        return residentRepository.findById(id)
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
            .build();
    }
}

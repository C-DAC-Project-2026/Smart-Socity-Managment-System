package com.society.service;

import com.society.dto.SocietyDTOs.*;
import java.util.List;

public interface SuperAdminService {
    SocietyResponse registerSociety(RegisterSocietyRequest request);
    SocietyResponse activateSociety(Long societyId);
    SocietyResponse suspendSociety(Long societyId);
    SocietyResponse getSociety(Long societyId);
    List<SocietyResponse> getAllSocieties();
    /** Public-safe list of ACTIVE societies, for the registration dropdown. */
    List<SocietyOption> getActiveSocietyOptions();
}

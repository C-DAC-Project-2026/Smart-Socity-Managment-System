package com.society.controller;

import com.society.dto.ApiResponse;
import com.society.dto.SocietyDTOs.*;
import com.society.exception.BadRequestException;
import com.society.service.CaptchaService;
import com.society.service.SuperAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Everything here is reachable WITHOUT authentication (see SecurityConfig).
 * This is deliberately the only place unauthenticated requests can create
 * data — a new Society (PENDING, needs SUPER_ADMIN approval) — and it is
 * guarded by a captcha for the same anti-bot reason as login/forgot-password.
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@Tag(name = "Public", description = "Unauthenticated endpoints: browse societies, self-register a society")
public class PublicController {

    private final SuperAdminService superAdminService;
    private final CaptchaService captchaService;

    @GetMapping("/societies")
    @Operation(summary = "List ACTIVE societies", description = "Powers the 'select your society' dropdown shown to Residents/Staff during registration.")
    public ResponseEntity<ApiResponse<List<SocietyOption>>> getActiveSocieties() {
        return ResponseEntity.ok(ApiResponse.success("Societies fetched", superAdminService.getActiveSocietyOptions()));
    }

    @PostMapping("/societies/register")
    @Operation(summary = "Self-register a new society and its first Society Admin",
               description = "Anyone can submit this (e.g. a housing society management committee). " +
                              "The society is created with status PENDING and cannot be used to log in " +
                              "until a Platform Super Admin reviews and activates it.")
    public ResponseEntity<ApiResponse<SocietyResponse>> registerSociety(@Valid @RequestBody RegisterSocietyRequest request) {
        if (!captchaService.verify(request.getCaptchaId(), request.getCaptchaAnswer())) {
            throw new BadRequestException("Incorrect captcha answer. Please try again.");
        }
        return ResponseEntity.ok(ApiResponse.success(
            "Society registration submitted. You'll be able to log in once a platform admin approves it.",
            superAdminService.registerSociety(request)));
    }
}

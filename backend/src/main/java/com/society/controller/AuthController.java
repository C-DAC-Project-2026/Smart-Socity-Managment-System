package com.society.controller;

import com.society.dto.ApiResponse;
import com.society.dto.AuthDTOs.*;
import com.society.service.AuthService;
import com.society.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login, Register, Captcha and Forgot Password APIs")
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;

    @GetMapping("/captcha")
    @Operation(summary = "Get a new captcha question (call before login/register/forgot-password)")
    public ResponseEntity<ApiResponse<CaptchaResponse>> getCaptcha() {
        return ResponseEntity.ok(ApiResponse.success("Captcha generated", captchaService.generate()));
    }

    @PostMapping("/login")
    @Operation(summary = "User Login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login successful", authService.login(request)));
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Register a RESIDENT or STAFF user into the caller's own society",
               description = "Society Admin only. The new user is always created in the caller's " +
                              "society — the client cannot choose a society.")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.register(request)));
    }

    @PostMapping("/register-public")
    @Operation(summary = "Public self-registration for a RESIDENT or STAFF user",
               description = "Open to anyone. The user picks their society from the public society " +
                              "list (GET /api/public/societies). The new account starts inactive and " +
                              "can only log in once that society's Admin approves it.")
    public ResponseEntity<ApiResponse<String>> registerPublic(@Valid @RequestBody PublicRegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.registerPublic(request)));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Send a password reset link to the given email")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.forgotPassword(request)));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using the token emailed to the user")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.resetPassword(request)));
    }
}

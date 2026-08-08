package com.society.service;
import com.society.dto.AuthDTOs.*;
public interface AuthService {
    LoginResponse login(LoginRequest request);
    String register(RegisterRequest request);
    /** Public self-registration for RESIDENT/STAFF. Account starts inactive, pending admin approval. */
    String registerPublic(PublicRegisterRequest request);
    String forgotPassword(ForgotPasswordRequest request);
    String resetPassword(ResetPasswordRequest request);
}

package com.society.service;
import com.society.dto.AuthDTOs.*;
public interface AuthService {
    LoginResponse login(LoginRequest request);
    String register(RegisterRequest request);
    String forgotPassword(ForgotPasswordRequest request);
    String resetPassword(ResetPasswordRequest request);
}

package az.edu.xalqbank.ms_auth.service;


import az.edu.xalqbank.ms_auth.dto.request.ForgotPasswordRequest;
import az.edu.xalqbank.ms_auth.dto.request.LoginRequest;
import az.edu.xalqbank.ms_auth.dto.request.RefreshRequest;
import az.edu.xalqbank.ms_auth.dto.request.ResetPasswordRequest;
import az.edu.xalqbank.ms_auth.dto.response.LoginResponse;
import az.edu.xalqbank.ms_auth.dto.response.RefreshTokenResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    RefreshTokenResponse refreshToken(RefreshRequest request);

    String forgotPassword(ForgotPasswordRequest request, String authenticatedEmail);

    String resetPassword(ResetPasswordRequest request);
}

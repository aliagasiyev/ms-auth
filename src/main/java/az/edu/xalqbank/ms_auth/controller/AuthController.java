package az.edu.xalqbank.ms_auth.controller;// AuthController

import az.edu.xalqbank.ms_auth.dto.request.ForgotPasswordRequest;
import az.edu.xalqbank.ms_auth.dto.request.LoginRequest;
import az.edu.xalqbank.ms_auth.dto.request.RefreshRequest;
import az.edu.xalqbank.ms_auth.dto.request.ResetPasswordRequest;
import az.edu.xalqbank.ms_auth.dto.response.LoginResponse;
import az.edu.xalqbank.ms_auth.dto.response.RefreshTokenResponse;
import az.edu.xalqbank.ms_auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request, Authentication authentication) {
        String authenticatedEmail = authentication.getName();
        String responseMessage = authService.forgotPassword(request, authenticatedEmail);
        return ResponseEntity.ok(responseMessage);
    }


    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        String responseMessage = authService.resetPassword(request);
        return ResponseEntity.ok(responseMessage);
    }
}

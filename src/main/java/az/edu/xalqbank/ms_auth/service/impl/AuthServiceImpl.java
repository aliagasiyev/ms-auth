package az.edu.xalqbank.ms_auth.service.impl;

import az.edu.xalqbank.ms_auth.dto.request.ForgotPasswordRequest;
import az.edu.xalqbank.ms_auth.dto.request.LoginRequest;
import az.edu.xalqbank.ms_auth.dto.request.RefreshRequest;
import az.edu.xalqbank.ms_auth.dto.request.ResetPasswordRequest;
import az.edu.xalqbank.ms_auth.dto.response.LoginResponse;
import az.edu.xalqbank.ms_auth.dto.response.RefreshTokenResponse;
import az.edu.xalqbank.ms_auth.entity.UserEntity;
import az.edu.xalqbank.ms_auth.exceptions.CustomAccessDeniedException;
import az.edu.xalqbank.ms_auth.exceptions.InvalidRequestException;
import az.edu.xalqbank.ms_auth.exceptions.InvalidTokenException;
import az.edu.xalqbank.ms_auth.exceptions.UserNotFoundException;
import az.edu.xalqbank.ms_auth.repository.UserRepository;
import az.edu.xalqbank.ms_auth.security.JwtTokenProvider;
import az.edu.xalqbank.ms_auth.service.AuthService;
import az.edu.xalqbank.ms_auth.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    private void storeToken(String key, String token) {
        Duration ttl = key.startsWith("access_token") ? Duration.ofMinutes(30) : Duration.ofDays(7);
        redisTemplate.opsForValue().set(key, token, ttl);
    }


    public String getToken(String tokenKey) {
        return (String) redisTemplate.opsForValue().get(tokenKey);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        storeToken("access_token:" + user.getId(), accessToken);
        storeToken("refresh_token:" + user.getId(), refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .message("Login successful")
                .build();
    }

    @Override
    public RefreshTokenResponse refreshToken(RefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String storedRefreshToken = getToken("refresh_token:" + user.getId());
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            redisTemplate.delete("refresh_token:" + user.getId());
            throw new InvalidTokenException("Refresh token not found or expired");
        }

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        String newAccessToken = jwtTokenProvider.generateAccessToken(auth);

        storeToken("access_token:" + user.getId(), newAccessToken);

        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .build();
    }

    @Override
    public String forgotPassword(ForgotPasswordRequest request, String authenticatedEmail) {
        if (authenticatedEmail == null) {
            throw new CustomAccessDeniedException("Authenticated email is null");
        }

        if (!authenticatedEmail.equals(request.getEmail())) {
            throw new CustomAccessDeniedException("You can only reset your own password");
        }

        userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String resetToken = UUID.randomUUID().toString();

        String subject = "Password Reset Request";
        String emailContent = "<p>Hello,</p>"
                + "<p>You requested to reset your password.</p>"
                + "<p>Your reset token: <strong>" + resetToken + "</strong></p>"
                + "<p>If you didn’t request this, ignore this email.</p>";

        emailService.sendEmail(request.getEmail(), subject, emailContent);

        return "Password reset token sent.";
    }

    @Override
    public String resetPassword(ResetPasswordRequest request) {
        if (request.getToken() == null || request.getToken().isEmpty()) {
            throw new InvalidRequestException("Token is missing!");
        }

        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return "Password successfully reset!";
    }
}

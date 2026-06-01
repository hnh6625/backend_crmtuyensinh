package com.company.crm_backend.Auth.application;

import com.company.crm_backend.Auth.application.dto.ChangePasswordRequest;
import com.company.crm_backend.Auth.application.dto.LoginRequest;
import com.company.crm_backend.Auth.application.dto.LoginResponse;
import com.company.crm_backend.User.domain.User;
import com.company.crm_backend.User.domain.UserSession;
import com.company.crm_backend.User.domain.UserStatus;
import com.company.crm_backend.User.infrastructure.UserRepository;
import com.company.crm_backend.User.infrastructure.UserSessionRepository;
import com.company.crm_backend.shared.exception.AppException;
import com.company.crm_backend.shared.exception.ErrorCode;
import com.company.crm_backend.shared.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${app.jwt.access-token-expiry}")
    private long accessExpiry;

    public LoginResponse login(LoginRequest req, HttpServletRequest httpReq) {
        User user = userRepository
                .findByUsernameAndDeletedAtIsNull(req.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        if (user.isAccountLocked())
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);

        if (user.getStatus() == UserStatus.INACTIVE)
            throw new AppException(ErrorCode.ACCOUNT_INACTIVE);

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            user.incrementFailedAttempts();
            userRepository.save(user);
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        user.resetLoginState();
        userRepository.save(user);

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        sessionRepository.revokeAllByUserId(user.getUserId());
        sessionRepository.save(UserSession.builder()
                .userId(user.getUserId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .ipAddress(getClientIp(httpReq))
                .userAgent(httpReq.getHeader("User-Agent"))
                .expiredAt(LocalDateTime.now().plusSeconds(jwtUtil.getRefreshExpiry()))
                .build());

        log.info("User {} ({}) logged in", user.getUsername(),
                user.getRole().getRoleName());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessExpiry)
                .user(LoginResponse.UserInfo.from(user))
                .build();
    }

    public LoginResponse refresh(String refreshToken) {
        UserSession session = sessionRepository
                .findByRefreshTokenAndIsRevokedFalse(refreshToken)
                .orElseThrow(() -> new AppException(ErrorCode.TOKEN_INVALID));

        if (!jwtUtil.isValid(refreshToken)) {
            sessionRepository.delete(session);
            sessionRepository.save(session);
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        User user = userRepository
                .findByUserIdAndDeletedAtIsNull(session.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String newAccess = jwtUtil.generateAccessToken(user);
        String newRefresh = jwtUtil.generateRefreshToken(user);

        session.setAccessToken(newAccess);
        session.setRefreshToken(newRefresh);
        session.setExpiredAt(LocalDateTime.now().plusSeconds(jwtUtil.getRefreshExpiry()));
        sessionRepository.save(session);

        return LoginResponse.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .tokenType("Bearer")
                .expiresIn(accessExpiry)
                .user(LoginResponse.UserInfo.from(user))
                .build();
    }

    public void logout(Long userId) {
        sessionRepository.revokeAllByUserId(userId);
    }

    public void changePassword(Long userId, ChangePasswordRequest req) {
        if (!req.getNewPassword().equals(req.getConfirmPassword()))
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);

        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword()))
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);
        sessionRepository.revokeAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public LoginResponse.UserInfo getMe(Long userId) {
        return userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .map(LoginResponse.UserInfo::from)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private String getClientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) return ip.split(",")[0].trim();
        return req.getRemoteAddr();
    }
}
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
    private final JwtUtil               jwtUtil;

    @Value("${app.jwt.access-token-expiry}")
    private long accessExpiry;

    public LoginResponse login(LoginRequest req, HttpServletRequest httpReq) {
        // 1. Tìm user
        User user = userRepository
                .findByUsernameAndDeletedAtIsNull(req.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        // 2. Kiểm tra tài khoản bị khóa
        if (user.isAccountLocked())
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);

        // 3. Kiểm tra tài khoản INACTIVE
        if (user.getStatus() == UserStatus.INACTIVE)
            throw new AppException(ErrorCode.ACCOUNT_INACTIVE);

        // 4. Kiểm tra password
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            user.incrementFailedAttempts();
            userRepository.save(user);
            log.warn("Failed login for user={}, attempts={}",
                    user.getUsername(), user.getFailedLoginAttempts());
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 5. Login thành công — reset failed attempts
        user.resetLoginState();
        userRepository.save(user);

        // 6. Tạo token
        String accessToken  = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        // 7. Revoke session cũ + lưu session mới
        sessionRepository.revokeAllByUserId(user.getUserId());
        sessionRepository.save(UserSession.builder()
                .userId(user.getUserId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .ipAddress(getClientIp(httpReq))
                .userAgent(httpReq.getHeader("User-Agent"))
                .expiredAt(LocalDateTime.now().plusSeconds(
                        jwtUtil.getRefreshExpiry()))
                .build());

        log.info("User {} logged in from {}", user.getUsername(), getClientIp(httpReq));

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessExpiry)
                .user(LoginResponse.UserInfo.from(user))
                .build();
    }

    // Refresh token
    public LoginResponse refresh(String refreshToken) {
        // 1. Tìm session còn hiệu lực
        UserSession session = sessionRepository
                .findByRefreshTokenAndIsRevokedFalse(refreshToken)
                .orElseThrow(() -> new AppException(ErrorCode.TOKEN_INVALID));

        // 2. Kiểm tra token chưa hết hạn
        if (!jwtUtil.isValid(refreshToken))
            throw new AppException(ErrorCode.TOKEN_EXPIRED);

        // 3. Lấy user
        User user = userRepository
                .findByUserIdAndDeletedAtIsNull(session.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 4. Cấp token mới
        String newAccess  = jwtUtil.generateAccessToken(user);
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

    // Logout
    public void logout(Long userId) {
        sessionRepository.revokeAllByUserId(userId);
        log.info("User {} logged out", userId);
    }

    // Đổi mật khẩu
    public void changePassword(Long userId, ChangePasswordRequest req) {
        // Validate confirm
        if (!req.getNewPassword().equals(req.getConfirmPassword()))
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);

        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword()))
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);

        // Revoke tất cả session — bắt đăng nhập lại
        sessionRepository.revokeAllByUserId(userId);
        log.info("User {} changed password", userId);
    }

    //Lấy thông tin user hiện tại
    @Transactional(readOnly = true)
    public LoginResponse.UserInfo getMe(Long userId) {
        return userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .map(LoginResponse.UserInfo::from)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    // Helpers
    private String getClientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) return ip.split(",")[0].trim();
        return req.getRemoteAddr();
    }
}
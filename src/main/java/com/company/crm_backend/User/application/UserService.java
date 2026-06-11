package com.company.crm_backend.User.application;

import com.company.crm_backend.User.application.dto.*;
import com.company.crm_backend.User.domain.RoleConstants;
import com.company.crm_backend.User.domain.UserStatus;
import com.company.crm_backend.shared.exception.AppException;
import com.company.crm_backend.shared.exception.ErrorCode;
import com.company.crm_backend.User.domain.Role;
import com.company.crm_backend.User.domain.User;
import com.company.crm_backend.User.infrastructure.RoleRepository;
import com.company.crm_backend.User.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository  userRepository;
    private final RoleRepository  roleRepository;
    private final PasswordEncoder passwordEncoder;

    // list
    @Transactional(readOnly = true)
    public Page<UserResponse> getList(UserFilterRequest filter, Pageable pageable) {
        return userRepository
                .findAll(UserSpecification.build(filter), pageable)
                .map(UserResponse::from);
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long userId) {
        return userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    // Dropdown cho assign lead — chỉ CONSULTANT + COLLABORATOR còn hoạt động
    @Transactional(readOnly = true)
    public List<SimpleUserResponse> getActiveConsultants() {
        return userRepository
                .findActiveByRoleNames(List.of(
                        RoleConstants.CONSULTANT_RAW,
                        RoleConstants.COLLABORATOR_RAW)) // thêm _RAW
                .stream()
                .map(SimpleUserResponse::from)
                .toList();
    }

    // Create
    public UserResponse create(CreateUserRequest req) {
        if (userRepository.existsByUsernameAndDeletedAtIsNull(req.getUsername()))
            throw new AppException(ErrorCode.USERNAME_EXISTED);

        if (userRepository.existsByEmailAndDeletedAtIsNull(req.getEmail()))
            throw new AppException(ErrorCode.EMAIL_EXISTED);

        Role role = roleRepository.findById(req.getRoleId())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        // Password mặc định = username, phải đổi lần đầu đăng nhập
        User user = User.builder()
                .username(req.getUsername().trim())
                .email(req.getEmail().trim())
                .fullName(req.getFullName().trim())
                .phone(req.getPhone())
                .password(passwordEncoder.encode(req.getUsername()))
                .role(role)
                .mustChangePassword(true)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);
        log.info("User created: username={}, role={}", user.getUsername(),
                role.getRoleName());
        return UserResponse.from(user);
    }

    // Update
    public UserResponse update(Long userId, UpdateUserRequest req) {
        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Kiểm tra email trùng với user khác
        if (StringUtils.hasText(req.getEmail())
                && !req.getEmail().equals(user.getEmail())
                && userRepository.existsByEmailAndDeletedAtIsNull(req.getEmail()))
            throw new AppException(ErrorCode.EMAIL_EXISTED);

        if (StringUtils.hasText(req.getFullName()))
            user.setFullName(req.getFullName().trim());
        if (StringUtils.hasText(req.getEmail()))
            user.setEmail(req.getEmail().trim());
        if (req.getPhone() != null)
            user.setPhone(req.getPhone());
        if (req.getAvatar() != null)
            user.setAvatar(req.getAvatar());
        if (req.getRoleId() != null) {
            Role role = roleRepository.findById(req.getRoleId())
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
            user.setRole(role);
        }

        return UserResponse.from(userRepository.save(user));
    }

    // Update status
    public UserResponse updateStatus(Long userId, UpdateStatusRequest req) {
        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setStatus(req.getStatus());

        // Khi mở khóa → reset failed attempts
        if (req.getStatus() == UserStatus.ACTIVE) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
        }

        return UserResponse.from(userRepository.save(user));
    }

    // Reset password
    public void resetPassword(Long userId) {
        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Reset về username, bắt đổi lại lần đầu đăng nhập
        user.setPassword(passwordEncoder.encode(user.getUsername()));
        user.setMustChangePassword(true);
        userRepository.save(user);

        log.info("Password reset for user: {}", user.getUsername());
    }

    // Unlock
    public UserResponse unlock(Long userId) {
        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setStatus(UserStatus.ACTIVE);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);

        return UserResponse.from(userRepository.save(user));
    }

    // Soft delete
    public void delete(Long userId) {
        userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userRepository.softDelete(userId, LocalDateTime.now());
        log.info("User {} soft deleted", userId);
    }

    // Danh sách role
    @Transactional(readOnly = true)
    public List<Role> getRoles() {
        return roleRepository.findAll();
    }
}

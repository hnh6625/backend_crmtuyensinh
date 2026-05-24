package com.company.crm_backend.User.infrastructure;

import com.company.crm_backend.User.domain.UserSession;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findByRefreshTokenAndIsRevokedFalse(String refreshToken);

    // Revoke tất cả session khi logout
    @Modifying
    @Query("""
        UPDATE UserSession s SET s.isRevoked = true
        WHERE s.userId = :userId AND s.isRevoked = false
        """)
    void revokeAllByUserId(@Param("userId") Long userId);

    // Revoke session hết hạn — dùng trong scheduled job dọn dẹp
    @Modifying
    @Query("""
        DELETE FROM UserSession s
        WHERE s.expiredAt < :now OR s.isRevoked = true
        """)
    void deleteExpiredOrRevoked(@Param("now") LocalDateTime now);
}
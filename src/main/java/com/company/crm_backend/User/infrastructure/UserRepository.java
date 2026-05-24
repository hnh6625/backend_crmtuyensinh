package com.company.crm_backend.User.infrastructure;

import com.company.crm_backend.User.domain.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {

    Optional<User> findByUsernameAndDeletedAtIsNull(String username);
    Optional<User> findByEmailAndDeletedAtIsNull(String email);
    Optional<User> findByUserIdAndDeletedAtIsNull(Long userId);

    boolean existsByUsernameAndDeletedAtIsNull(String username);
    boolean existsByEmailAndDeletedAtIsNull(String email);

    // Lấy danh sách theo role
    Page<User> findAllByRole_RoleNameAndDeletedAtIsNull(
            String roleName, Pageable pageable);

    // Soft delete
    @Modifying
    @Query("UPDATE User u SET u.deletedAt = :now WHERE u.userId = :userId")
    void softDelete(@Param("userId") Long userId,
                    @Param("now") LocalDateTime now);

    // Reset failed attempts sau khi admin mở khóa
    @Modifying
    @Query("""
        UPDATE User u SET
            u.status              = 'ACTIVE',
            u.failedLoginAttempts = 0,
            u.lockedUntil         = null
        WHERE u.userId = :userId
        """)
    void unlockUser(@Param("userId") Long userId);

    // Tất cả user không bị xóa — cho dropdown assign lead
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL AND u.status = 'ACTIVE'")
    List<User> findAllActiveUsers();
}
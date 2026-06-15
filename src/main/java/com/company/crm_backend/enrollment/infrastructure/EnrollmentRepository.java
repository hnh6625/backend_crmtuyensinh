package com.company.crm_backend.enrollment.infrastructure;

import com.company.crm_backend.enrollment.domain.Enrollment;
import com.company.crm_backend.enrollment.domain.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository
        extends JpaRepository<Enrollment, Long>,
        JpaSpecificationExecutor<Enrollment> {

    // Kiểm tra lead đã nhập học chưa
    boolean existsByLead_LeadId(Long leadId);

    // Lấy enrollment theo lead
    Optional<Enrollment> findByLead_LeadId(Long leadId);

    // Đếm theo trạng thái
    long countByEnrollmentStatus(EnrollmentStatus status);

    @Query("""
            SELECT e FROM Enrollment e
            JOIN FETCH e.lead l
            JOIN FETCH e.major m
            LEFT JOIN FETCH e.enrolledBy u
            WHERE e.enrollmentId = :id
            """)
    Optional<Enrollment> findByIdWithDetails(@Param("id") Long id);

    // Cập nhật trạng thái không load cả entity
    @Modifying
    @Query("""
            UPDATE Enrollment e
            SET e.enrollmentStatus = :status,
                e.convertedAt = CASE
                    WHEN :status = 'CONFIRMED' THEN CURRENT_TIMESTAMP
                    ELSE e.convertedAt END
            WHERE e.enrollmentId = :id
            """)
    int updateStatus(@Param("id") Long id,
                     @Param("status") EnrollmentStatus status);

    // Gán mã sinh viên
    @Modifying
    @Query("""
            UPDATE Enrollment e
            SET e.studentCode = :studentCode
            WHERE e.enrollmentId = :id
            """)
    int assignStudentCode(@Param("id") Long id,
                          @Param("studentCode") String studentCode);

    @Query(value = """
            SELECT
                m.major_code,
                m.major_name,
                COUNT(e.enrollment_id)                  AS total,
                SUM(e.enrollment_status = 'CONFIRMED')  AS confirmed,
                SUM(e.enrollment_status = 'PENDING')    AS pending,
                SUM(e.enrollment_status = 'CANCELLED')  AS cancelled,
                COALESCE(SUM(e.final_fee), 0)           AS total_fee
            FROM enrollments e
            JOIN majors m ON e.major_id = m.major_id
            GROUP BY m.major_id, m.major_code, m.major_name
            ORDER BY total DESC
            """, nativeQuery = true)
    List<Object[]> statsGroupByMajor();
}
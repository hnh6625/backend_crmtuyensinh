package com.company.crm_backend.enrollment.application;

import com.company.crm_backend.User.domain.User;
import com.company.crm_backend.User.infrastructure.UserRepository;
import com.company.crm_backend.enrollment.application.dto.CreateEnrollmentRequest;
import com.company.crm_backend.enrollment.application.dto.EnrollmentFilterRequest;
import com.company.crm_backend.enrollment.application.dto.EnrollmentResponse;
import com.company.crm_backend.enrollment.application.dto.UpdateEnrollmentRequest;
import com.company.crm_backend.enrollment.domain.*;
import com.company.crm_backend.enrollment.infrastructure.*;
import com.company.crm_backend.lead.application.LeadHistoryService;
import com.company.crm_backend.lead.domain.Lead;
import com.company.crm_backend.lead.domain.LeadStatus;
import com.company.crm_backend.lead.infrastructure.LeadRepository;
import com.company.crm_backend.lead.infrastructure.LeadStatusRepository;
import com.company.crm_backend.shared.exception.AppException;
import com.company.crm_backend.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final MajorRepository majorRepository;
    private final LeadRepository leadRepository;
    private final UserRepository userRepository;

    private final LeadStatusRepository leadStatusRepository;
    private final LeadHistoryService historyService;
    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public List<Major> getActiveMajors() {
        return majorRepository.findAllByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public Page<EnrollmentResponse> getList(EnrollmentFilterRequest filter, Pageable pageable) {
        return enrollmentRepository
                .findAll(EnrollmentSpecification.build(filter), pageable)
                .map(EnrollmentResponse::from);
    }

    @Transactional(readOnly = true)
    public EnrollmentResponse getById(Long id) {
        return enrollmentRepository.findByIdWithDetails(id)
                .map(EnrollmentResponse::from)
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public EnrollmentResponse getByLeadId(Long leadId) {
        return enrollmentRepository.findByLead_LeadId(leadId)
                .map(EnrollmentResponse::from)
                .orElse(null);
    }

    // TẠO MỚI NHẬP HỌC (ĐÃ FIX)
    public EnrollmentResponse create(CreateEnrollmentRequest req, Long enrolledByUserId) {
        Lead lead = leadRepository.findByLeadIdAndDeletedAtIsNull(req.getLeadId())
                .orElseThrow(() -> new AppException(ErrorCode.LEAD_NOT_FOUND));

        if (enrollmentRepository.existsByLead_LeadId(req.getLeadId()))
            throw new AppException(ErrorCode.LEAD_ALREADY_ENROLLED);

        Major major = majorRepository.findById(req.getMajorId())
                .orElseThrow(() -> new AppException(ErrorCode.MAJOR_NOT_FOUND));
        User enrolledBy = userRepository.findById(enrolledByUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // ƯU TIÊN LẤY HỌC PHÍ TỪ FRONTEND GỬI LÊN
        BigDecimal tuitionFee;
        if (req.getTuitionFee() != null) {
            tuitionFee = BigDecimal.valueOf(req.getTuitionFee());
        } else if (major.getTuitionFee() != null) {
            tuitionFee = major.getTuitionFee();
        } else {
            tuitionFee = BigDecimal.valueOf(10000000); // Mặc định 10 triệu
        }

        Enrollment enrollment = Enrollment.builder()
                .lead(lead)
                .major(major)
                .tuitionFee(tuitionFee)
                .scholarshipAmount(BigDecimal.ZERO)
                .finalFee(tuitionFee) // Vì đã bỏ học bổng nên Phí cuối = Học phí
                .paymentMethod(req.getPaymentMethod())
                .convertedAt(LocalDateTime.now())
                .note(req.getNote())
                .enrolledBy(enrolledBy)
                .enrollmentStatus(EnrollmentStatus.PENDING)
                .build();

        enrollmentRepository.save(enrollment);

        // Tự động đổi trạng thái Lead
        LeadStatus enrolledStatus = leadStatusRepository.findByStatusName("Đã đăng ký")
                .orElseThrow(() -> new RuntimeException("Chưa có trạng thái 'Đã đăng ký' trong DB"));
        lead.setStatus(enrolledStatus);
        leadRepository.save(lead);

        // Lưu lịch sử
        historyService.record(lead.getLeadId(), "ENROLLMENT_CREATED", null,
                "Chốt nhập học thành công (Khoa/Ngành: " + major.getMajorName() + ")", enrolledByUserId);

        log.info("Enrollment created: leadId={}, major={}", lead.getLeadId(), major.getMajorCode());

        return EnrollmentResponse.from(enrollment);
    }

    // CẬP NHẬT NHẬP HỌC
    public EnrollmentResponse update(Long id, UpdateEnrollmentRequest req) {
        Enrollment e = enrollmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));

        if (e.getEnrollmentStatus() == EnrollmentStatus.CANCELLED
                || e.getEnrollmentStatus() == EnrollmentStatus.COMPLETED)
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION);

        if (req.getMajorId() != null) {
            Major major = majorRepository.findById(req.getMajorId())
                    .orElseThrow(() -> new AppException(ErrorCode.MAJOR_NOT_FOUND));
            e.setMajor(major);
        }

        // Cập nhật học phí từ Frontend
        if (req.getTuitionFee() != null) {
            e.setTuitionFee(BigDecimal.valueOf(req.getTuitionFee()));
        }

        if (StringUtils.hasText(req.getStudentCode())) e.setStudentCode(req.getStudentCode());
        if (req.getNote() != null) e.setNote(req.getNote());

        // Đồng bộ finalFee
        BigDecimal tuition = e.getTuitionFee() != null ? e.getTuitionFee() : BigDecimal.ZERO;
        e.setFinalFee(tuition);

        return EnrollmentResponse.from(enrollmentRepository.save(e));
    }

    // Cập nhật trạng thái
    public EnrollmentResponse updateStatus(Long id, EnrollmentStatus newStatus) {
        Enrollment e = enrollmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));

        if (e.getEnrollmentStatus() == EnrollmentStatus.CANCELLED
                || e.getEnrollmentStatus() == EnrollmentStatus.COMPLETED)
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION);

        enrollmentRepository.updateStatus(id, newStatus);
        e.setEnrollmentStatus(newStatus);

        log.info("Enrollment {} status → {}", id, newStatus);
        return EnrollmentResponse.from(e);
    }

    public List<Department> getActiveDepartments() {
        return departmentRepository.findAllByIsActiveTrue();
    }
}
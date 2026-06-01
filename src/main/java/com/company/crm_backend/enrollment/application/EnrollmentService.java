package com.company.crm_backend.enrollment.application;

import com.company.crm_backend.User.domain.User;
import com.company.crm_backend.User.infrastructure.UserRepository;
import com.company.crm_backend.enrollment.application.dto.CreateEnrollmentRequest;
import com.company.crm_backend.enrollment.application.dto.EnrollmentFilterRequest;
import com.company.crm_backend.enrollment.application.dto.EnrollmentResponse;
import com.company.crm_backend.enrollment.application.dto.UpdateEnrollmentRequest;
import com.company.crm_backend.enrollment.domain.*;
import com.company.crm_backend.enrollment.infrastructure.*;
import com.company.crm_backend.lead.domain.Lead;
import com.company.crm_backend.lead.infrastructure.LeadRepository;
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
    private final CampusRepository campusRepository;
    private final SemesterRepository semesterRepository;
    private final LeadRepository leadRepository;
    private final UserRepository userRepository;

    // Dropdown — ngành, cơ sở, học kỳ
    @Transactional(readOnly = true)
    public List<Major> getActiveMajors() {
        return majorRepository.findAllByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Campus> getActiveCampuses() {
        return campusRepository.findAllByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Semester> getOpenSemesters() {
        return semesterRepository.findAllByStatus(SemesterStatus.OPEN);
    }

    // Danh sách enrollment có filter + phân trang
    @Transactional(readOnly = true)
    public Page<EnrollmentResponse> getList(EnrollmentFilterRequest filter,
                                            Pageable pageable) {
        return enrollmentRepository
                .findAll(EnrollmentSpecification.build(filter), pageable)
                .map(EnrollmentResponse::from);
    }

    // Chi tiết 1 enrollment
    @Transactional(readOnly = true)
    public EnrollmentResponse getById(Long id) {
        return enrollmentRepository.findByIdWithDetails(id)
                .map(EnrollmentResponse::from)
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));
    }

    // 4. Lấy enrollment theo lead
    @Transactional(readOnly = true)
    public EnrollmentResponse getByLeadId(Long leadId) {
        return enrollmentRepository.findByLead_LeadId(leadId)
                .map(EnrollmentResponse::from)
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));
    }

    // 5. Tạo enrollment mới
    public EnrollmentResponse create(CreateEnrollmentRequest req,
                                     Long enrolledByUserId) {
        // Kiểm tra lead tồn tại
        Lead lead = leadRepository.findByLeadIdAndDeletedAtIsNull(req.getLeadId())
                .orElseThrow(() -> new AppException(ErrorCode.LEAD_NOT_FOUND));

        // Kiểm tra lead chưa nhập học
        if (enrollmentRepository.existsByLead_LeadId(req.getLeadId()))
            throw new AppException(ErrorCode.LEAD_ALREADY_ENROLLED);

        // Lấy các danh mục
        Major    major    = majorRepository.findById(req.getMajorId())
                .orElseThrow(() -> new AppException(ErrorCode.MAJOR_NOT_FOUND));
        Campus campus   = campusRepository.findById(req.getCampusId())
                .orElseThrow(() -> new AppException(ErrorCode.CAMPUS_NOT_FOUND));
        Semester semester = semesterRepository.findById(req.getSemesterId())
                .orElseThrow(() -> new AppException(ErrorCode.SEMESTER_NOT_FOUND));
        User enrolledBy   = userRepository.findById(enrolledByUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Tính học phí
        BigDecimal tuitionFee  = major.getTuitionFee() != null
                ? major.getTuitionFee() : BigDecimal.ZERO;
        BigDecimal scholarship = req.getScholarshipAmount() != null
                ? req.getScholarshipAmount() : BigDecimal.ZERO;
        BigDecimal finalFee    = tuitionFee.subtract(scholarship);

        if (finalFee.compareTo(BigDecimal.ZERO) < 0)
            throw new AppException(ErrorCode.SCHOLARSHIP_EXCEEDS_TUITION);

        Enrollment enrollment = Enrollment.builder()
                .lead(lead)
                .major(major)
                .campus(campus)
                .semester(semester)
                .tuitionFee(tuitionFee)
                .scholarshipAmount(scholarship)
                .finalFee(finalFee)
                .conversionSource(req.getConversionSource())
                .convertedAt(LocalDateTime.now())
                .note(req.getNote())
                .enrolledBy(enrolledBy)
                .build();

        enrollmentRepository.save(enrollment);
        log.info("Enrollment created: leadId={}, major={}, semester={}",
                lead.getLeadId(), major.getMajorCode(), semester.getSemesterCode());

        return EnrollmentResponse.from(enrollment);
    }

    // 6. Cập nhật enrollment
    public EnrollmentResponse update(Long id, UpdateEnrollmentRequest req) {
        Enrollment e = enrollmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));

        // Không cho sửa khi đã CANCELLED hoặc COMPLETED
        if (e.getEnrollmentStatus() == EnrollmentStatus.CANCELLED
                || e.getEnrollmentStatus() == EnrollmentStatus.COMPLETED)
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION);

        if (req.getMajorId() != null) {
            Major major = majorRepository.findById(req.getMajorId())
                    .orElseThrow(() -> new AppException(ErrorCode.MAJOR_NOT_FOUND));
            e.setMajor(major);
            e.setTuitionFee(major.getTuitionFee());
        }
        if (req.getCampusId() != null)
            e.setCampus(campusRepository.findById(req.getCampusId())
                    .orElseThrow(() -> new AppException(ErrorCode.CAMPUS_NOT_FOUND)));
        if (req.getSemesterId() != null)
            e.setSemester(semesterRepository.findById(req.getSemesterId())
                    .orElseThrow(() -> new AppException(ErrorCode.SEMESTER_NOT_FOUND)));
        if (req.getScholarshipAmount() != null)
            e.setScholarshipAmount(req.getScholarshipAmount());
        if (StringUtils.hasText(req.getStudentCode()))
            e.setStudentCode(req.getStudentCode());
        if (req.getNote() != null)
            e.setNote(req.getNote());

        // Tính lại finalFee
        BigDecimal tuition     = e.getTuitionFee()        != null ? e.getTuitionFee()        : BigDecimal.ZERO;
        BigDecimal scholarship = e.getScholarshipAmount() != null ? e.getScholarshipAmount() : BigDecimal.ZERO;
        e.setFinalFee(tuition.subtract(scholarship));

        return EnrollmentResponse.from(enrollmentRepository.save(e));
    }

    // 7. Cập nhật trạng thái
    public EnrollmentResponse updateStatus(Long id, EnrollmentStatus newStatus) {
        Enrollment e = enrollmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));

        // Không cho đổi khi đã là trạng thái cuối
        if (e.getEnrollmentStatus() == EnrollmentStatus.CANCELLED
                || e.getEnrollmentStatus() == EnrollmentStatus.COMPLETED)
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION);

        enrollmentRepository.updateStatus(id, newStatus);
        e.setEnrollmentStatus(newStatus);

        log.info("Enrollment {} status → {}", id, newStatus);
        return EnrollmentResponse.from(e);
    }
}
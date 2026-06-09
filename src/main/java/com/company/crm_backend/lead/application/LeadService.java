package com.company.crm_backend.lead.application;

import com.company.crm_backend.User.infrastructure.UserRepository;
import com.company.crm_backend.lead.domain.*;
import com.company.crm_backend.lead.domain.dto.*;
import com.company.crm_backend.lead.infrastructure.*;
import com.company.crm_backend.shared.exception.AppException;
import com.company.crm_backend.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.company.crm_backend.User.domain.User;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LeadService {

    private final LeadRepository leadRepository;
    private final LeadSourceRepository leadSourceRepository;
    private final LeadStatusRepository leadStatusRepository;
    private final LeadTagRepository leadTagRepository;
    private final LeadTagMapRepository leadTagMapRepository;
    private final LeadAssignmentRepository assignmentRepository;
    private final LeadHistoryService historyService;
    private final UserRepository userRepository;

    // Lấy dữ liệu dropdown
    @Transactional(readOnly = true)
    public List<LeadSource> getSources() {
        return leadSourceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<LeadStatus> getStatuses() {
        return leadStatusRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<LeadTag> getTags() {
        return leadTagRepository.findAll();
    }

    // Danh sách lead có filter + phân trang
    @Transactional(readOnly = true)
    public Page<LeadResponse> getList(LeadFilterRequest filter, Pageable pageable) {
        return leadRepository
                .findAll(LeadSpecification.build(filter), pageable)
                .map(lead -> {
                    // Query thêm danh sách tags cho từng lead trong danh sách
                    List<String> tags = leadTagMapRepository
                            .findAllByLead_LeadId(lead.getLeadId())
                            .stream()
                            .map(m -> m.getTag().getTagName())
                            .toList();

                    // Sử dụng hàm from(lead, tags)
                    return LeadResponse.from(lead, tags);
                });
    }

    // Chi tiết 1 lead kèm tags
    @Transactional(readOnly = true)
    public LeadResponse getById(Long leadId) {
        Lead lead = findOrThrow(leadId);
        List<String> tags = leadTagMapRepository
                .findAllByLead_LeadId(leadId)
                .stream()
                .map(m -> m.getTag().getTagName())
                .toList();
        return LeadResponse.from(lead, tags);
    }

    // Tạo lead mới
    public LeadResponse create(CreateLeadRequest req, Long createdBy) {
        // Kiểm tra trùng SĐT
        String normalized = normalizePhone(req.getPhone());
        if (leadRepository.existsByPhoneNormalizedAndDeletedAtIsNull(normalized))
            throw new AppException(ErrorCode.PHONE_DUPLICATE);

        // Lấy FK
        LeadSource source = getSourceById(req.getSourceId());
        LeadStatus status = getStatusById(req.getStatusId());
        User assignedTo = getUserById(req.getAssignedTo());

        Lead lead = Lead.builder()
                .fullName(req.getFullName().trim())
                .phone(req.getPhone().trim())
                .phoneNormalized(normalized)
                .email(req.getEmail())
                .gender(req.getGender())
                .birthDate(parseDate(req.getBirthDate()))
                .schoolName(req.getSchoolName())
                .graduationYear(req.getGraduationYear())
                .address(req.getAddress())
                .province(req.getProvince())
                .note(req.getNote())
                .source(source)
                .status(status)
                .assignedTo(assignedTo)
                .createdBy(createdBy)
                .build();

        leadRepository.save(lead);

        // Gắn tags nếu có

        if (req.getTags() != null && !req.getTags().isEmpty()) {
            saveTags(lead, req.getTags());
        }
        // Ghi lịch sử
        historyService.record(lead.getLeadId(), "CREATE",
                null, lead.getFullName() + " - " + lead.getPhone(), createdBy);

        log.info("Lead created: id={}, phone={}", lead.getLeadId(), lead.getPhone());
        return LeadResponse.from(lead);
    }

    // Cập nhật thông tin lead
    public LeadResponse update(Long leadId, UpdateLeadRequest req, Long changedBy) {
        Lead lead = findOrThrow(leadId);

        String oldStatus = lead.getStatus() != null
                ? lead.getStatus().getStatusName() : null;

        if (StringUtils.hasText(req.getFullName()))
            lead.setFullName(req.getFullName().trim());
        if (req.getEmail() != null) lead.setEmail(req.getEmail());
        if (req.getGender() != null) lead.setGender(req.getGender());
        if (req.getBirthDate() != null) lead.setBirthDate(parseDate(req.getBirthDate()));
        if (req.getSchoolName() != null) lead.setSchoolName(req.getSchoolName());
        if (req.getGraduationYear() != null) lead.setGraduationYear(req.getGraduationYear());
        if (req.getAddress() != null) lead.setAddress(req.getAddress());
        if (req.getProvince() != null) lead.setProvince(req.getProvince());
        if (req.getNote() != null) lead.setNote(req.getNote());

        if (req.getSourceId() != null)
            lead.setSource(getSourceById(req.getSourceId()));

        if (req.getStatusId() != null) {
            LeadStatus newStatus = leadStatusRepository.findById(req.getStatusId())
                    .orElseThrow(() -> new AppException(ErrorCode.LEAD_STATUS_NOT_FOUND));
            lead.setStatus(newStatus);
            historyService.record(leadId, "STATUS_CHANGE",
                    oldStatus, newStatus.getStatusName(), changedBy);
        }

        // Cập nhật tags
        if (req.getTags() != null) {
            leadTagMapRepository.deleteAllByLeadId(leadId);
            if (!req.getTags().isEmpty()) {
                saveTags(lead, req.getTags());
            }
        }
        leadRepository.save(lead);
        return LeadResponse.from(lead);
    }


    // Phân công lead cho nhân viên
    public LeadResponse assign(Long leadId, AssignLeadRequest req, Long assignedBy) {
        Lead lead = findOrThrow(leadId);
        User newUser = userRepository.findById(req.getAssignToUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        User assigner = userRepository.findById(assignedBy)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String oldAssignee = lead.getAssignedTo() != null
                ? lead.getAssignedTo().getFullName() : "Chưa có";

        lead.setAssignedTo(newUser);
        leadRepository.save(lead);

        // Lưu lịch sử phân công
        assignmentRepository.save(LeadAssignment.builder()
                .lead(lead)
                .assignedTo(newUser)
                .assignedBy(assigner)
                .note(req.getNote())
                .build());

        historyService.record(leadId, "ASSIGN",
                oldAssignee, newUser.getFullName(), assignedBy);

        log.info("Lead {} assigned to {}", leadId, newUser.getUsername());
        return LeadResponse.from(lead);
    }

    // Xóa mềm lead
    public void delete(Long leadId, Long deletedBy) {
        findOrThrow(leadId);
        leadRepository.softDelete(leadId, LocalDateTime.now());
        historyService.record(leadId, "DELETE", null, "Đã xóa", deletedBy);
        log.info("Lead {} soft-deleted by {}", leadId, deletedBy);
    }

    // Lịch sử thay đổi của lead
    @Transactional(readOnly = true)
    public List<LeadHistoryResponse> getHistory(Long leadId) {
        findOrThrow(leadId);
        return historyService.getHistory(leadId);
    }

    // Helpers
    private Lead findOrThrow(Long leadId) {
        return leadRepository.findByLeadIdAndDeletedAtIsNull(leadId)
                .orElseThrow(() -> new AppException(ErrorCode.LEAD_NOT_FOUND));
    }

        // Ghi đè hàm saveTags cũ bằng hàm này:
        private void saveTags(Lead lead, List<String> tagNames) {
            for (String name : tagNames) {
                String cleanName = name.trim();
                if (cleanName.isEmpty()) continue;

                // Tìm tag theo tên, nếu chưa có trong DB thì tạo mới luôn
                LeadTag tag = leadTagRepository.findByTagName(cleanName)
                        .orElseGet(() -> {
                            LeadTag newTag = new LeadTag(); // Khởi tạo tùy theo cấu trúc entity của bạn
                            newTag.setTagName(cleanName);
                            return leadTagRepository.save(newTag);
                        });

                // Map tag với lead
                LeadTagMap map = new LeadTagMap(); // Khởi tạo tùy theo entity LeadTagMap
                map.setLead(lead);
                map.setTag(tag);
                leadTagMapRepository.save(map);
            }
        }

    private LeadSource getSourceById(Long id) {
        return id == null ? null : leadSourceRepository.findById(id).orElse(null);
    }

    private LeadStatus getStatusById(Long id) {
        return id == null ? null : leadStatusRepository.findById(id).orElse(null);
    }

    private User getUserById(Long id) {
        return id == null ? null : userRepository.findById(id).orElse(null);
    }

    private String normalizePhone(String phone) {
        if (phone == null) return null;
        String p = phone.trim().replaceAll("\\s+", "");
        return p.startsWith("+84") ? "0" + p.substring(3) : p;
    }

    private LocalDate parseDate(String dateStr) {
        if (!StringUtils.hasText(dateStr)) return null;
        try {
            return LocalDate.parse(dateStr.trim(),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return null;
        }
    }
}
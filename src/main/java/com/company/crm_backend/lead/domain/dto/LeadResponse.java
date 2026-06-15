package com.company.crm_backend.lead.domain.dto;

import com.company.crm_backend.lead.domain.Lead;
import lombok.Builder;
import lombok.Getter;
import org.springframework.cglib.core.Local;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Builder
public class LeadResponse {

    private Long leadId;
    private String fullName;
    private String phone;
    private String phoneNormalized;
    private String email;
    private String gender;
    private LocalDate birthDate;
    private String schoolName;
    private Integer graduationYear;
    private String address;
    private String province;
    private String note;

    // Nguồn
    private Long sourceId;
    private String sourceName;

    // Trạng thái
    private Long statusId;
    private String statusName;

    // Người phụ trách
    private Long assignedToId;
    private String assignedToName;

    // Duplicate
    private BigDecimal duplicateScore;
    private Boolean isDuplicate;

    // Thời gian
    private LocalDateTime lastCalledAt;
    private LocalDateTime nextFollowUpAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Tags
    private List<String> tags;

    public static LeadResponse from(Lead l) {
        return LeadResponse.builder()
                .leadId(l.getLeadId())
                .fullName(l.getFullName())
                .phone(l.getPhone())
                .phoneNormalized(l.getPhoneNormalized())
                .email(l.getEmail())
                .gender(l.getGender() != null ? l.getGender().name() : null)
                .birthDate(l.getBirthDate())
                .schoolName(l.getSchoolName())
                .graduationYear(l.getGraduationYear())
                .address(l.getAddress())
                .province(l.getProvince())
                .note(l.getNote())
                .sourceId(l.getSource() != null ? l.getSource().getSourceId() : null)
                .sourceName(l.getSource() != null ? l.getSource().getSourceName() : null)
                .statusId(l.getStatus() != null ? l.getStatus().getStatusId() : null)
                .statusName(l.getStatus() != null ? l.getStatus().getStatusName() : null)
                .assignedToId(l.getAssignedTo() != null ? l.getAssignedTo().getUserId() : null)
                .assignedToName(l.getAssignedTo() != null ? l.getAssignedTo().getFullName() : null)
                .duplicateScore(l.getDuplicateScore())
                .isDuplicate(l.getIsDuplicate())
                .lastCalledAt(l.getLastCalledAt())
                .nextFollowUpAt(l.getNextFollowUpAt())
                .createdAt(l.getCreatedAt())
                .updatedAt(l.getUpdatedAt())
                .build();
    }

    public static LeadResponse from(Lead l, List<String> tags) {
        LeadResponse res = from(l);
        res.tags = tags;
        return res;
    }
}
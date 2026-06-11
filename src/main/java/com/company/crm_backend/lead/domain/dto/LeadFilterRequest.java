package com.company.crm_backend.lead.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LeadFilterRequest {
    private String keyword;      // tìm tên, SĐT, email
    private Long statusId;     // lọc theo trạng thái
    private Long sourceId;     // lọc theo nguồn
    private Long assignedTo;   // lọc theo người phụ trách
    private String province;     // lọc theo tỉnh thành
    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
    private Long consultantId;
}

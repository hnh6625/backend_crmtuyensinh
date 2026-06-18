package com.company.crm_backend.lead.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeadStatusStatDto {
    private String statusName;
    private Long count;
}
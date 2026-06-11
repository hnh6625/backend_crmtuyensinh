package com.company.crm_backend.lead.domain.dto;

import com.company.crm_backend.lead.domain.Gender;
import lombok.Getter;

import java.util.List;

@Getter
public class UpdateLeadRequest {
    private String fullName;
    private String phone;
    private String email;
    private Gender gender;
    private String birthDate;
    private String schoolName;
    private Integer graduationYear;
    private String address;
    private String province;
    private String note;
    private Long sourceId;
    private Long statusId;
    private List<String> tags;
}

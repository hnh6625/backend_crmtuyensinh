package com.company.crm_backend.importjob.application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeadRowDto {
    private String fullName;
    private String phone;
    private String email;
    private String gender;
    private String birthDate;
    private String schoolName;
    private String graduationYear;
    private String province;
    private String address;
    private String note;
    private String sourceName;
}
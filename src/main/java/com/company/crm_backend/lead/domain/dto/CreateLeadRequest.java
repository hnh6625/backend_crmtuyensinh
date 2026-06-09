package com.company.crm_backend.lead.domain.dto;

import com.company.crm_backend.lead.domain.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class CreateLeadRequest {

    @NotBlank(message = "Họ tên không được trống")
    @Size(max = 150)
    private String fullName;

    @NotBlank(message = "Số điện thoại không được trống")
    @Size(max = 20)
    private String phone;

    @Email(message = "Email không đúng định dạng")
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
    private Long assignedTo;
    private List<String> tags;}
package com.company.crm_backend.User.domain;

public class RoleConstants {
    public static final String MANAGER = "ROLE_MANAGER";
    public static final String CONSULTANT = "ROLE_CONSULTANT";
    public static final String COLLABORATOR = "ROLE_COLLABORATOR";

    // Lấy tên không có chữ ROLE_ để trả về Frontend
    public static final String MANAGER_RAW = "MANAGER";
    public static final String CONSULTANT_RAW = "CONSULTANT";
    public static final String COLLABORATOR_RAW = "COLLABORATOR";
}

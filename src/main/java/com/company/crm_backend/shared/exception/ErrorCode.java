package com.company.crm_backend.shared.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Auth
    UNAUTHENTICATED(401, "Chưa đăng nhập hoặc token hết hạn"),
    FORBIDDEN(403, "Không có quyền thực hiện thao tác này"),
    INVALID_CREDENTIALS(401, "Tên đăng nhập hoặc mật khẩu không đúng"),
    ACCOUNT_LOCKED(423, "Tài khoản đã bị khóa, thử lại sau 30 phút"),
    TOKEN_INVALID(401, "Token không hợp lệ"),
    TOKEN_EXPIRED(401, "Token đã hết hạn"),

    // User
    USER_NOT_FOUND(404, "Không tìm thấy người dùng"),
    USERNAME_EXISTED(409, "Tên đăng nhập đã tồn tại"),
    EMAIL_EXISTED(409, "Email đã tồn tại"),

    // Lead
    LEAD_NOT_FOUND(404, "Không tìm thấy lead"),
    PHONE_DUPLICATE(409, "Số điện thoại đã tồn tại trong hệ thống"),
    LEAD_ALREADY_ENROLLED(409, "Lead này đã được nhập học"),

    // Call
    CALL_ATTEMPT_EXCEEDED(400, "Lead này đã được gọi đủ 3 lần"),

    // Import
    IMPORT_PROCESSING(409, "File đang được xử lý, vui lòng đợi"),
    INVALID_FILE_FORMAT(400, "File không đúng định dạng CSV"),

    // Common
    VALIDATION_ERROR(400, "Dữ liệu không hợp lệ"),
    INTERNAL_ERROR(500, "Lỗi hệ thống, vui lòng thử lại");

    private final int httpStatus;
    private final String message;

}

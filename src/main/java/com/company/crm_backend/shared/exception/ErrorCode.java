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
    ACCOUNT_INACTIVE     (403, "Tài khoản đã bị vô hiệu hóa"),
    USER_NOT_FOUND       (404, "Không tìm thấy người dùng"),
    USERNAME_EXISTED     (409, "Tên đăng nhập đã tồn tại"),
    EMAIL_EXISTED        (409, "Email đã tồn tại"),
    ROLE_NOT_FOUND       (404, "Không tìm thấy role"),
    PASSWORD_NOT_MATCH   (400, "Mật khẩu xác nhận không khớp"),

    // Lead
    LEAD_NOT_FOUND          (404, "Không tìm thấy lead"),
    LEAD_STATUS_NOT_FOUND   (404, "Không tìm thấy trạng thái lead"),
    PHONE_DUPLICATE         (409, "Số điện thoại đã tồn tại trong hệ thống"),

    // Call
    CALL_RESULT_NOT_FOUND   (404, "Không tìm thấy kết quả cuộc gọi"),
    CALL_ATTEMPT_EXCEEDED   (400, "Lead này đã được gọi đủ 3 lần"),
    FOLLOW_UP_NOT_FOUND     (404, "Không tìm thấy lịch hẹn"),
    INVALID_STATUS_TRANSITION(400, "Không thể chuyển trạng thái này"),

    ENROLLMENT_NOT_FOUND       (404, "Không tìm thấy thông tin nhập học"),
    LEAD_ALREADY_ENROLLED      (409, "Lead này đã được nhập học"),
    MAJOR_NOT_FOUND            (404, "Không tìm thấy ngành học"),
    CAMPUS_NOT_FOUND           (404, "Không tìm thấy cơ sở"),
    SEMESTER_NOT_FOUND         (404, "Không tìm thấy học kỳ"),
    SCHOLARSHIP_EXCEEDS_TUITION(400, "Học bổng không được lớn hơn học phí"),

    //import
    IMPORT_NOT_FOUND           (404, "Không tìm thấy import job"),
    INVALID_FILE_FORMAT        (400, "File phải có định dạng .csv"),
    FILE_TOO_LARGE             (400, "File không được vượt quá 50MB"),

    // Common
    VALIDATION_ERROR(400, "Dữ liệu không hợp lệ"),
    INTERNAL_ERROR(500, "Lỗi hệ thống, vui lòng thử lại");

    private final int httpStatus;
    private final String message;

}

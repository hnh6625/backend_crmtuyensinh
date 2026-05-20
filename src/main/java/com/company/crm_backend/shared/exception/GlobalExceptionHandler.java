package com.company.crm_backend.shared.exception;

import com.company.crm_backend.shared.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    // loi do minh tu nem
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException e) {
        ErrorCode err = e.getErrorCode();
        log.warn("AppException: {} - {}", err.name(), err.getMessage());
        return ResponseEntity
                .status(err.getHttpStatus())
                .body(ApiResponse.of(err.getHttpStatus(), err.getMessage(), null));
    }

    // loi validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException e) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(fe ->
                fieldErrors.put(fe.getField(), fe.getDefaultMessage())
        );

        log.warn("Validation failed: {}", fieldErrors);
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.of(400, "Dữ liệu không hợp lệ", fieldErrors));
    }

    // Bat loi variable sai kieu
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException e) {
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.of(400, "Tham số không hợp lệ: " + e.getName(), null));
    }

    // Bat loi khong thay route
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NoResourceFoundException e) {
        return ResponseEntity
                .status(404)
                .body(ApiResponse.of(404, "Không tìm thấy đường dẫn", null));
    }

    // bat cac loi con lai
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception e) {
        log.error("Unhandled exception: ", e);  // log de debug
        return ResponseEntity
                .status(500)
                .body(ApiResponse.of(500, "Lỗi hệ thống, vui lòng thử lại", null));
    }
}

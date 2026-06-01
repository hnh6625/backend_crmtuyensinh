package com.company.crm_backend.importjob.infrastructure.batch;

// Để Spring Batch nhận ra và skip row lỗi
public class ImportValidationException extends Exception {
    public ImportValidationException(String message) {
        super(message);
    }
}
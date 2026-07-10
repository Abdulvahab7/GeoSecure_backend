package com.geosecure.attendance.exception;

import org.springframework.http.HttpStatus;

public class DuplicateScanException extends ApiException {
    public DuplicateScanException(String message) {
        super(HttpStatus.CONFLICT, "DUPLICATE_SCAN", message);
    }
}

package com.geosecure.attendance.exception;

import org.springframework.http.HttpStatus;

public class QrExpiredException extends ApiException {
    public QrExpiredException(String message) {
        super(HttpStatus.GONE, "QR_EXPIRED", message);
    }
}

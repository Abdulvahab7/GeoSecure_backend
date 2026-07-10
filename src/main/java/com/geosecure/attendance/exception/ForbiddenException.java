package com.geosecure.attendance.exception;

import org.springframework.http.HttpStatus;

/** Thrown for ownership/coordinator/mentor rule violations (authenticated but not permitted). */
public class ForbiddenException extends ApiException {
    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, "FORBIDDEN", message);
    }
}

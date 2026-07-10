package com.geosecure.attendance.exception;

import org.springframework.http.HttpStatus;

/** Thrown when a student's scan location falls outside the geofence radius. */
public class OutOfRangeException extends ApiException {
    public OutOfRangeException(String message) {
        super(HttpStatus.BAD_REQUEST, "OUT_OF_RANGE", message);
    }
}

package com.geosecure.attendance.service;

import com.geosecure.attendance.dto.request.GenerateQrRequest;
import com.geosecure.attendance.dto.request.ManualAttendanceRequest;
import com.geosecure.attendance.dto.request.ScanQrRequest;
import com.geosecure.attendance.dto.response.AttendanceRecordResponse;
import com.geosecure.attendance.dto.response.QrSessionResponse;

import java.util.List;
import java.util.Map;

public interface AttendanceService {

    /** Faculty generates a QR session for one of their own timetable slots. */
    QrSessionResponse generateQr(GenerateQrRequest request, Integer facultyUserId);

    /**
     * Student scans a QR code. Validates: session exists & active & not
     * expired, distance is within the configured geofence radius, and the
     * student has not already scanned this session (defense-in-depth: also
     * enforced by the DB unique constraint).
     */
    AttendanceRecordResponse scanQr(ScanQrRequest request, Integer studentUserId);

    /** Faculty manual override (present/absent/late) without a QR scan. */
    AttendanceRecordResponse markManually(ManualAttendanceRequest request, Integer facultyUserId);

    /** Full roster (present + absent) for a session - faculty view. */
    List<Map<String, Object>> sessionRoster(Integer sessionId, Integer facultyUserId);

    /** Explicitly ends a session early (in addition to natural expiry). */
    void endSession(Integer sessionId, Integer facultyUserId);
}

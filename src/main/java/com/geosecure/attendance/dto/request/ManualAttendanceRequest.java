package com.geosecure.attendance.dto.request;

import com.geosecure.attendance.entity.AttendanceRecord;

import jakarta.validation.constraints.NotNull;

/** Faculty override: mark a student present/absent/late without a QR scan. */
public class ManualAttendanceRequest {

    @NotNull(message = "Session is required")
    private Integer sessionId;

    @NotNull(message = "Student is required")
    private Integer studentId;

    @NotNull(message = "Status is required")
    private AttendanceRecord.Status status;

    private String remarks;

    public Integer getSessionId() { return sessionId; }
    public void setSessionId(Integer sessionId) { this.sessionId = sessionId; }
    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    public AttendanceRecord.Status getStatus() { return status; }
    public void setStatus(AttendanceRecord.Status status) { this.status = status; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}

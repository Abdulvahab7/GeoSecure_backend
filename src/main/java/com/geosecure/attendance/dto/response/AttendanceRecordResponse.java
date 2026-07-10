package com.geosecure.attendance.dto.response;

import com.geosecure.attendance.entity.AttendanceRecord;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AttendanceRecordResponse {

    private Integer id;
    private Integer sessionId;
    private Integer studentId;
    private String studentName;
    private String registerNo;
    private BigDecimal distanceMeters;
    private LocalDateTime scannedAt;
    private String status;
    private String verificationMethod;
    private String remarks;

    public static AttendanceRecordResponse from(AttendanceRecord ar) {
        AttendanceRecordResponse r = new AttendanceRecordResponse();
        r.id = ar.getId();
        r.sessionId = ar.getSession().getId();
        r.studentId = ar.getStudent().getId();
        r.studentName = ar.getStudent().getName();
        r.registerNo = ar.getStudent().getRegisterNo();
        r.distanceMeters = ar.getDistanceMeters();
        r.scannedAt = ar.getScannedAt();
        r.status = ar.getStatus().name();
        r.verificationMethod = ar.getVerificationMethod().name();
        r.remarks = ar.getRemarks();
        return r;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getSessionId() { return sessionId; }
    public void setSessionId(Integer sessionId) { this.sessionId = sessionId; }
    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getRegisterNo() { return registerNo; }
    public void setRegisterNo(String registerNo) { this.registerNo = registerNo; }
    public BigDecimal getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(BigDecimal distanceMeters) { this.distanceMeters = distanceMeters; }
    public LocalDateTime getScannedAt() { return scannedAt; }
    public void setScannedAt(LocalDateTime scannedAt) { this.scannedAt = scannedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getVerificationMethod() { return verificationMethod; }
    public void setVerificationMethod(String verificationMethod) { this.verificationMethod = verificationMethod; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}

package com.geosecure.attendance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Maps to: attendance_records table.
 * Individual student attendance marks per session. The
 * (session_id, student_id) unique constraint is the database-level
 * duplicate-scan guard, mirrored here via @UniqueConstraint so
 * Hibernate validates it against the schema (ddl-auto=validate).
 */
@Entity
@Table(
        name = "attendance_records",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_record_session_student",
                columnNames = {"session_id", "student_id"}
        )
)
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private AttendanceSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "student_latitude", nullable = false, precision = 10, scale = 8)
    private BigDecimal studentLatitude;

    @Column(name = "student_longitude", nullable = false, precision = 11, scale = 8)
    private BigDecimal studentLongitude;

    /** Haversine-computed distance in metres, stored for audit/reporting. */
    @Column(name = "distance_meters", nullable = false, precision = 8, scale = 2)
    private BigDecimal distanceMeters;

    @Column(name = "scanned_at", nullable = false)
    private LocalDateTime scannedAt = LocalDateTime.now(ZoneOffset.UTC);

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status = Status.present;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_method")
    private VerificationMethod verificationMethod = VerificationMethod.qr_scan;

    @Column(name = "remarks", length = 255)
    private String remarks;

    public AttendanceRecord() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public AttendanceSession getSession() {
        return session;
    }

    public void setSession(AttendanceSession session) {
        this.session = session;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public BigDecimal getStudentLatitude() {
        return studentLatitude;
    }

    public void setStudentLatitude(BigDecimal studentLatitude) {
        this.studentLatitude = studentLatitude;
    }

    public BigDecimal getStudentLongitude() {
        return studentLongitude;
    }

    public void setStudentLongitude(BigDecimal studentLongitude) {
        this.studentLongitude = studentLongitude;
    }

    public BigDecimal getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(BigDecimal distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public LocalDateTime getScannedAt() {
        return scannedAt;
    }

    public void setScannedAt(LocalDateTime scannedAt) {
        this.scannedAt = scannedAt;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public VerificationMethod getVerificationMethod() {
        return verificationMethod;
    }

    public void setVerificationMethod(VerificationMethod verificationMethod) {
        this.verificationMethod = verificationMethod;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public enum Status {
        present, absent, late
    }

    public enum VerificationMethod {
        qr_scan, manual
    }
}

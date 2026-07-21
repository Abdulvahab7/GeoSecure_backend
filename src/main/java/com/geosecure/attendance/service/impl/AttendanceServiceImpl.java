package com.geosecure.attendance.service.impl;

import com.geosecure.attendance.dto.request.GenerateQrRequest;
import com.geosecure.attendance.dto.request.ManualAttendanceRequest;
import com.geosecure.attendance.dto.request.ScanQrRequest;
import com.geosecure.attendance.dto.response.AttendanceRecordResponse;
import com.geosecure.attendance.dto.response.QrSessionResponse;
import com.geosecure.attendance.entity.AttendanceRecord;
import com.geosecure.attendance.entity.AttendanceSession;
import com.geosecure.attendance.entity.Faculty;
import com.geosecure.attendance.entity.FacultySubject;
import com.geosecure.attendance.entity.Notification;
import com.geosecure.attendance.entity.Student;
import com.geosecure.attendance.entity.Timetable;
import com.geosecure.attendance.exception.BadRequestException;
import com.geosecure.attendance.exception.DuplicateScanException;
import com.geosecure.attendance.exception.ForbiddenException;
import com.geosecure.attendance.exception.OutOfRangeException;
import com.geosecure.attendance.exception.QrExpiredException;
import com.geosecure.attendance.exception.ResourceNotFoundException;
import com.geosecure.attendance.repository.AttendanceRecordRepository;
import com.geosecure.attendance.repository.AttendanceSessionRepository;
import com.geosecure.attendance.repository.StudentRepository;
import com.geosecure.attendance.service.AttendanceService;
import com.geosecure.attendance.service.AuditLogService;
import com.geosecure.attendance.service.FacultyService;
import com.geosecure.attendance.service.NotificationService;
import com.geosecure.attendance.service.TimetableService;
import com.geosecure.attendance.util.HaversineUtil;
import com.geosecure.attendance.util.QrCodeUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Owns the entire QR-generation -> geofenced-scan -> attendance-record
 * lifecycle. Haversine distance and the duplicate-scan guard live ONLY
 * here (see Phase 1 handoff note: the old sp_mark_attendance stored
 * procedure was intentionally removed so there is one source of truth).
 */
@Service
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final StudentRepository studentRepository;
    private final TimetableService timetableService;
    private final FacultyService facultyService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Value("${geosecure.qr.expiry-seconds}")
    private int qrExpirySeconds;

    @Value("${geosecure.geofence.radius-meters}")
    private double geofenceRadiusMeters;

    public AttendanceServiceImpl(AttendanceSessionRepository sessionRepository,
                                  AttendanceRecordRepository recordRepository,
                                  StudentRepository studentRepository,
                                  TimetableService timetableService,
                                  FacultyService facultyService,
                                  NotificationService notificationService,
                                  AuditLogService auditLogService) {
        this.sessionRepository = sessionRepository;
        this.recordRepository = recordRepository;
        this.studentRepository = studentRepository;
        this.timetableService = timetableService;
        this.facultyService = facultyService;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public QrSessionResponse generateQr(GenerateQrRequest request, Integer facultyUserId) {
        Faculty faculty = facultyService.requireByUserId(facultyUserId);

        Timetable timetable = timetableService.requireOwnedByFaculty(request.getTimetableId(), faculty.getId());
        if (timetable.getFacultySubject() == null) {
            throw new BadRequestException(
                    "This timetable slot has no linked faculty-subject assignment; assign one before generating a QR session.");
        }
        FacultySubject facultySubject = timetable.getFacultySubject();

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        List<AttendanceSession> active = sessionRepository.findActiveSessionsForSlotToday(facultySubject.getId(), today, now);
        if (!active.isEmpty()) {
            throw new BadRequestException("An active QR session already exists for this slot today. Wait for it to expire or end it first.");
        }

        AttendanceSession session = new AttendanceSession();
        session.setTimetable(timetable);
        session.setFacultySubject(facultySubject);
        session.setSessionToken(QrCodeUtil.generateSessionToken());
        session.setFacultyLatitude(BigDecimal.valueOf(request.getLatitude()));
        session.setFacultyLongitude(BigDecimal.valueOf(request.getLongitude()));
        session.setStartedAt(now);
        session.setExpiresAt(now.plusSeconds(qrExpirySeconds));
        session.setIsActive(true);
        session.setSessionDate(today);

        session = sessionRepository.save(session);

        QrSessionResponse response = new QrSessionResponse();
        response.setSessionId(session.getId());
        response.setSessionToken(session.getSessionToken());
        response.setQrImageDataUrl(QrCodeUtil.toPngDataUrl(session.getSessionToken(), 300));
        response.setStartedAt(session.getStartedAt());
        response.setExpiresAt(session.getExpiresAt());
        response.setExpirySeconds(qrExpirySeconds);
        response.setSubjectName(facultySubject.getSubject().getName());
        response.setClassName(facultySubject.getClassEntity().getName());
        return response;
    }

    @Override
    @Transactional
    public AttendanceRecordResponse scanQr(ScanQrRequest request, Integer studentUserId) {
        Student student = studentRepository.findByUserIdWithClassAndDepartment(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for this user."));

        AttendanceSession session = sessionRepository.findBySessionTokenWithDetails(request.getSessionToken())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or unknown QR code."));

        if (!Boolean.TRUE.equals(session.getIsActive())) {
            throw new QrExpiredException("This QR session has already ended.");
        }
        if (LocalDateTime.now(ZoneOffset.UTC).isAfter(session.getExpiresAt())) {
            sessionRepository.deactivate(session.getId());
            throw new QrExpiredException("This QR code has expired. Ask your faculty to generate a new one.");
        }

        if (!student.getClassEntity().getId().equals(session.getFacultySubject().getClassEntity().getId())) {
            throw new ForbiddenException("This attendance session is not for your class.");
        }

        if (recordRepository.existsBySession_IdAndStudent_Id(session.getId(), student.getId())) {
            throw new DuplicateScanException("You have already marked attendance for this session.");
        }

        double distance = HaversineUtil.distanceMeters(
                session.getFacultyLatitude().doubleValue(), session.getFacultyLongitude().doubleValue(),
                request.getLatitude(), request.getLongitude());

        if (distance > geofenceRadiusMeters) {
            throw new OutOfRangeException(String.format(
                    "You are %.1fm away from the faculty's location, which is outside the %.0fm geofence radius.",
                    distance, geofenceRadiusMeters));
        }

        AttendanceRecord record = new AttendanceRecord();
        record.setSession(session);
        record.setStudent(student);
        record.setStudentLatitude(BigDecimal.valueOf(request.getLatitude()));
        record.setStudentLongitude(BigDecimal.valueOf(request.getLongitude()));
        record.setDistanceMeters(BigDecimal.valueOf(distance).setScale(2, java.math.RoundingMode.HALF_UP));
        record.setScannedAt(LocalDateTime.now(ZoneOffset.UTC));
        record.setStatus(AttendanceRecord.Status.present);
        record.setVerificationMethod(AttendanceRecord.VerificationMethod.qr_scan);

        try {
            record = recordRepository.save(record);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Defense-in-depth: DB UNIQUE(session_id, student_id) caught a race the pre-check missed.
            throw new DuplicateScanException("You have already marked attendance for this session.");
        }

        sessionRepository.incrementPresentCount(session.getId());

        notificationService.notify(
                student.getUser().getId(),
                "Attendance marked",
                String.format("You were marked present for %s (%s) at %.1fm from the faculty.",
                        session.getFacultySubject().getSubject().getName(),
                        session.getFacultySubject().getClassEntity().getName(),
                        distance),
                Notification.NotificationType.success);

        return AttendanceRecordResponse.from(record);
    }

    @Override
    @Transactional
    public AttendanceRecordResponse markManually(ManualAttendanceRequest request, Integer facultyUserId) {
        Faculty faculty = facultyService.requireByUserId(facultyUserId);

        AttendanceSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + request.getSessionId()));

        if (!session.getFacultySubject().getFaculty().getId().equals(faculty.getId())) {
            throw new ForbiddenException("This attendance session does not belong to you.");
        }

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + request.getStudentId()));

        AttendanceRecord record = recordRepository.findBySession_IdAndStudent_Id(session.getId(), student.getId())
                .orElse(new AttendanceRecord());

        boolean isNew = record.getId() == null;
        AttendanceRecord.Status previousStatus = record.getStatus();
        record.setSession(session);
        record.setStudent(student);
        record.setStatus(request.getStatus());
        record.setVerificationMethod(AttendanceRecord.VerificationMethod.manual);
        record.setRemarks(request.getRemarks());
        if (record.getScannedAt() == null) {
            record.setScannedAt(LocalDateTime.now(ZoneOffset.UTC));
        }
        if (record.getStudentLatitude() == null) {
            record.setStudentLatitude(session.getFacultyLatitude());
            record.setStudentLongitude(session.getFacultyLongitude());
            record.setDistanceMeters(BigDecimal.ZERO);
        }

        record = recordRepository.save(record);

        if (isNew && request.getStatus() == AttendanceRecord.Status.present) {
            sessionRepository.incrementPresentCount(session.getId());
        }

        // Attendance Correction: a non-new record being re-saved with a different
        // status is a correction of a prior mark (manual or QR-scanned), not a fresh
        // mark. Record it in the audit trail and let the student know their status changed.
        if (!isNew && previousStatus != request.getStatus()) {
            auditLogService.record(
                    facultyUserId,
                    "ATTENDANCE_CORRECTION",
                    "attendance_records",
                    record.getId(),
                    previousStatus == null ? "unknown" : previousStatus.name(),
                    request.getStatus().name(),
                    null, null);
            notificationService.notify(
                    student.getUser().getId(),
                    "Attendance corrected",
                    String.format("Your attendance for %s on %s was corrected from %s to %s%s.",
                            session.getFacultySubject().getSubject().getName(),
                            session.getSessionDate(),
                            previousStatus, request.getStatus(),
                            request.getRemarks() != null ? " (" + request.getRemarks() + ")" : ""),
                    Notification.NotificationType.warning);
        }

        return AttendanceRecordResponse.from(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> sessionRoster(Integer sessionId, Integer facultyUserId) {
        Faculty faculty = facultyService.requireByUserId(facultyUserId);
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        if (!session.getFacultySubject().getFaculty().getId().equals(faculty.getId())) {
            throw new ForbiddenException("This attendance session does not belong to you.");
        }

        List<Object[]> rows = recordRepository.sessionAttendanceRoster(sessionId);
        return rows.stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("studentId", row[0]);
            m.put("name", row[1]);
            m.put("registerNo", row[2]);
            m.put("status", row[3]);
            m.put("scannedAt", row[4]);
            m.put("distanceMeters", row[5]);
            m.put("attendanceStatus", row[6]);
            return m;
        }).toList();
    }

    @Override
    @Transactional
    public void endSession(Integer sessionId, Integer facultyUserId) {
        Faculty faculty = facultyService.requireByUserId(facultyUserId);
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        if (!session.getFacultySubject().getFaculty().getId().equals(faculty.getId())) {
            throw new ForbiddenException("This attendance session does not belong to you.");
        }

        sessionRepository.deactivate(sessionId);

        notificationService.notify(
                facultyUserId,
                "Attendance session ended",
                String.format("You ended the %s session for %s. %d student(s) marked present.",
                        session.getFacultySubject().getSubject().getName(),
                        session.getFacultySubject().getClassEntity().getName(),
                        session.getPresentCount() == null ? 0 : session.getPresentCount()),
                Notification.NotificationType.info);
    }
}

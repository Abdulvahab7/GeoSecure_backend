package com.geosecure.attendance.scheduler;

import com.geosecure.attendance.entity.AttendanceRecord;
import com.geosecure.attendance.entity.AttendanceSession;
import com.geosecure.attendance.entity.Notification;
import com.geosecure.attendance.entity.Student;
import com.geosecure.attendance.repository.AttendanceRecordRepository;
import com.geosecure.attendance.repository.AttendanceSessionRepository;
import com.geosecure.attendance.repository.StudentRepository;
import com.geosecure.attendance.service.NotificationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Phase 3 addition — closes the lazy-expiry gap called out in
 * PROJECT_STATE.md / PHASE_2_HANDOFF.md ("Known gaps"): previously an
 * AttendanceSession's is_active flag only ever flipped to false when a
 * student scanned an expired QR or a faculty member called /end. This
 * sweep runs on a fixed rate, finds any session whose expires_at has
 * passed but is still is_active = true, and:
 *   1. Deactivates it (AttendanceSessionRepository.deactivate).
 *   2. Auto-marks every student of that class who never scanned as
 *      status = ABSENT, verification_method = manual, remarks = "Auto-marked
 *      absent - session expired" (Automatic Session Closing + Session
 *      Detection requirements).
 *   3. Notifies the faculty that the session auto-closed and how many
 *      students were marked present vs. auto-absent (Notification
 *      integration related to attendance).
 *
 * Runs independently of AttendanceServiceImpl.endSession()/scanQr(), which
 * remain the fast-path (manual end / lazy check-on-scan) guards; this job
 * is the backstop that guarantees no session is left dangling once its QR
 * window has passed, even if no one ever scans or the faculty forgets to
 * end it.
 */
@Component
public class AttendanceSessionScheduler {

    private static final Logger log = LoggerFactory.getLogger(AttendanceSessionScheduler.class);

    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final StudentRepository studentRepository;
    private final NotificationService notificationService;

    public AttendanceSessionScheduler(AttendanceSessionRepository sessionRepository,
                                       AttendanceRecordRepository recordRepository,
                                       StudentRepository studentRepository,
                                       NotificationService notificationService) {
        this.sessionRepository = sessionRepository;
        this.recordRepository = recordRepository;
        this.studentRepository = studentRepository;
        this.notificationService = notificationService;
    }

    /** Fixed rate is externalized via geosecure.scheduler.sweep-rate-ms (default 15s). */
    @Scheduled(fixedRateString = "${geosecure.scheduler.sweep-rate-ms:15000}")
    @Transactional
    public void sweepExpiredSessions() {
        List<AttendanceSession> expired = sessionRepository.findExpiredButStillActive(LocalDateTime.now());
        if (expired.isEmpty()) {
            return;
        }

        for (AttendanceSession session : expired) {
            try {
                closeAndReconcile(session);
            } catch (Exception ex) {
                // One bad session must not stop the sweep from closing the rest.
                log.error("Failed to auto-close attendance session {}: {}", session.getId(), ex.getMessage(), ex);
            }
        }
    }

    private void closeAndReconcile(AttendanceSession session) {
        List<Integer> unmarkedStudentIds = recordRepository.studentIdsWithoutRecordForSession(session.getId());

        int autoAbsentCount = 0;
        for (Integer studentId : unmarkedStudentIds) {
            Student student = studentRepository.findById(studentId).orElse(null);
            if (student == null) {
                continue;
            }
            AttendanceRecord record = new AttendanceRecord();
            record.setSession(session);
            record.setStudent(student);
            record.setStatus(AttendanceRecord.Status.absent);
            record.setVerificationMethod(AttendanceRecord.VerificationMethod.manual);
            record.setRemarks("Auto-marked absent - QR session expired without a scan");
            record.setScannedAt(LocalDateTime.now());
            record.setStudentLatitude(session.getFacultyLatitude());
            record.setStudentLongitude(session.getFacultyLongitude());
            record.setDistanceMeters(java.math.BigDecimal.ZERO);
            try {
                recordRepository.save(record);
                autoAbsentCount++;
            } catch (org.springframework.dao.DataIntegrityViolationException ignored) {
                // Student scanned in the split second between the sweep query and this save; leave their real record alone.
            }
        }

        sessionRepository.deactivate(session.getId());

        Integer facultyUserId = session.getFacultySubject().getFaculty().getUser().getId();
        String subjectName = session.getFacultySubject().getSubject().getName();
        String className = session.getFacultySubject().getClassEntity().getName();
        notificationService.notify(
                facultyUserId,
                "Attendance session auto-closed",
                String.format("Your %s session for %s auto-closed after the QR window expired. Present: %d, auto-marked absent: %d.",
                        subjectName, className, session.getPresentCount() == null ? 0 : session.getPresentCount(), autoAbsentCount),
                Notification.NotificationType.info);

        log.info("Auto-closed attendance session {} ({} / {}), {} students auto-marked absent",
                session.getId(), subjectName, className, autoAbsentCount);
    }
}

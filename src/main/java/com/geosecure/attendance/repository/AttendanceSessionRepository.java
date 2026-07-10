package com.geosecure.attendance.repository;

import com.geosecure.attendance.entity.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Mirrors the query previously embedded in the original QR session lifecycle logic:
 *   getSessionByToken()  -> findBySessionTokenWithDetails()
 *   expireSession()      -> deactivate()
 *   isSessionValid()     -> resolved in AttendanceService using the fetched entity
 *
 * Also replaces the original faculty dashboard logic generateQR()
 * duplicate-active-session guard:
 *   SELECT id FROM attendance_sessions WHERE faculty_subject_id = ? AND
 *     session_date = CURDATE() AND is_active = TRUE AND expires_at > NOW()
 */
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Integer> {

    @Query("""
           SELECT s FROM AttendanceSession s
           JOIN FETCH s.facultySubject fs
           JOIN FETCH fs.subject
           JOIN FETCH fs.classEntity
           JOIN FETCH fs.faculty
           WHERE s.sessionToken = :token
           """)
    Optional<AttendanceSession> findBySessionTokenWithDetails(@Param("token") String token);

    @Query("""
           SELECT s FROM AttendanceSession s
           WHERE s.facultySubject.id = :facultySubjectId
             AND s.sessionDate = :today
             AND s.isActive = true
             AND s.expiresAt > :now
           """)
    List<AttendanceSession> findActiveSessionsForSlotToday(@Param("facultySubjectId") Integer facultySubjectId,
                                                              @Param("today") LocalDate today,
                                                              @Param("now") LocalDateTime now);

    @Modifying
    @Transactional
    @Query("UPDATE AttendanceSession s SET s.isActive = false WHERE s.id = :sessionId")
    void deactivate(@Param("sessionId") Integer sessionId);

    @Modifying
    @Transactional
    @Query("UPDATE AttendanceSession s SET s.presentCount = s.presentCount + 1 WHERE s.id = :sessionId")
    void incrementPresentCount(@Param("sessionId") Integer sessionId);

    @Query("""
           SELECT s FROM AttendanceSession s
           WHERE s.facultySubject.id = :facultySubjectId
           ORDER BY s.sessionDate DESC
           """)
    List<AttendanceSession> findByFacultySubjectIdOrderByDateDesc(@Param("facultySubjectId") Integer facultySubjectId);

    @Query("""
           SELECT s FROM AttendanceSession s
           JOIN FETCH s.facultySubject fs
           JOIN FETCH fs.subject
           JOIN FETCH fs.classEntity
           WHERE fs.faculty.id = :facultyId
           ORDER BY s.startedAt DESC
           """)
    List<AttendanceSession> findRecentByFacultyId(@Param("facultyId") Integer facultyId);

    long countByFacultySubject_Faculty_IdAndSessionDate(Integer facultyId, LocalDate date);

    long countBySessionDate(LocalDate date);


    long count();

    /**
     * Phase 3: used by AttendanceSessionScheduler's sweep job to find sessions whose
     * expiry has passed but are still flagged is_active = true (lazy-expiry gap
     * called out in PROJECT_STATE.md / PHASE_2_HANDOFF.md as a deliberately deferred item).
     */
    @Query("""
           SELECT s FROM AttendanceSession s
           JOIN FETCH s.facultySubject fs
           JOIN FETCH fs.subject
           JOIN FETCH fs.classEntity
           JOIN FETCH fs.faculty f
           JOIN FETCH f.user
           WHERE s.isActive = true AND s.expiresAt <= :now
           """)
    List<AttendanceSession> findExpiredButStillActive(@Param("now") LocalDateTime now);

    /** Used by ReportService.getAnalytics() — last 7 days session/present counts grouped by date */
    @Query("""
           SELECT s.sessionDate AS sessionDate, COUNT(s) AS sessions, COALESCE(SUM(s.presentCount), 0) AS present
           FROM AttendanceSession s
           WHERE s.sessionDate >= :sinceDate
           GROUP BY s.sessionDate
           ORDER BY s.sessionDate ASC
           """)
    List<Object[]> findDailySessionStatsSince(@Param("sinceDate") LocalDate sinceDate);
}

package com.geosecure.attendance.repository;

import com.geosecure.attendance.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Mirrors the query previously embedded in the original attendance-calculation logic.
 *
 * markAttendance() duplicate check:
 *   SELECT id FROM attendance_records WHERE session_id = ? AND student_id = ?
 *   -> findBySession_IdAndStudent_Id()  (also enforced by DB UNIQUE constraint as a second guard)
 *
 * getStudentAttendanceSummary() -> studentSubjectSummary()  (native, mirrors original GROUP BY query)
 * getStudentRecentActivity()    -> studentRecentActivity()
 * getSessionAttendance()        -> sessionAttendanceRoster()  (LEFT JOIN students, includes absentees)
 * getSubjectAttendanceReport()  -> subjectReportStudents() + AttendanceSessionRepository for sessions[]
 * getDefaulterList()            -> defaulterList()
 */
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Integer> {

    Optional<AttendanceRecord> findBySession_IdAndStudent_Id(Integer sessionId, Integer studentId);

    boolean existsBySession_IdAndStudent_Id(Integer sessionId, Integer studentId);

    long countBySession_Id(Integer sessionId);

    long countByStudent_Id(Integer studentId);

    /**
     * Per-subject attendance % for one student.
     * Mirrors attendanceService.js getStudentAttendanceSummary().
     * Columns: subject_id, subject_code, subject_name, subject_type, total_sessions, present_count, percentage
     */
    @Query(value = """
           SELECT
             sub.id          AS subject_id,
             sub.code        AS subject_code,
             sub.name        AS subject_name,
             sub.subject_type AS subject_type,
             COUNT(ase.id)   AS total_sessions,
             COUNT(ar.id)    AS present_count,
             ROUND(COUNT(ar.id) * 100.0 / NULLIF(COUNT(ase.id),0), 2) AS percentage
           FROM students s
           JOIN classes c            ON c.id  = s.class_id
           JOIN faculty_subjects fs  ON fs.class_id = c.id
           JOIN subjects sub         ON sub.id = fs.subject_id
           JOIN attendance_sessions ase ON ase.faculty_subject_id = fs.id
           LEFT JOIN attendance_records ar ON ar.session_id = ase.id AND ar.student_id = s.id
           WHERE s.id = :studentId
           GROUP BY sub.id, sub.code, sub.name, sub.subject_type
           ORDER BY sub.name
           """, nativeQuery = true)
    List<Object[]> studentSubjectSummary(@Param("studentId") Integer studentId);

    /**
     * Last N attendance events for a student.
     * Mirrors attendanceService.js getStudentRecentActivity().
     * Columns: id, status, scanned_at, distance_meters, subject_code, subject_name, session_date
     */
    @Query(value = """
           SELECT ar.id, ar.status, ar.scanned_at, ar.distance_meters,
                  sub.code AS subject_code, sub.name AS subject_name, ase.session_date
           FROM attendance_records ar
           JOIN attendance_sessions ase ON ase.id = ar.session_id
           JOIN faculty_subjects fs     ON fs.id  = ase.faculty_subject_id
           JOIN subjects sub            ON sub.id = fs.subject_id
           WHERE ar.student_id = :studentId
           ORDER BY ar.scanned_at DESC
           LIMIT :limit
           """, nativeQuery = true)
    List<Object[]> studentRecentActivity(@Param("studentId") Integer studentId, @Param("limit") int limit);

    /**
     * Per-session-subject history for one student (drill-down page).
     * Mirrors studentController.js getSubjectDetail().
     */
    @Query(value = """
           SELECT ase.session_date, ase.started_at,
                  CASE WHEN ar.id IS NOT NULL THEN 'present' ELSE 'absent' END AS status,
                  ar.distance_meters, ar.scanned_at
           FROM attendance_sessions ase
           JOIN faculty_subjects fs ON fs.id = ase.faculty_subject_id
           JOIN classes c           ON c.id = fs.class_id
           JOIN students s          ON s.class_id = c.id AND s.id = :studentId
           LEFT JOIN attendance_records ar ON ar.session_id = ase.id AND ar.student_id = s.id
           WHERE fs.subject_id = :subjectId
           ORDER BY ase.session_date DESC
           """, nativeQuery = true)
    List<Object[]> studentSubjectDetail(@Param("studentId") Integer studentId, @Param("subjectId") Integer subjectId);

    /**
     * Full roster (present + absent) for one session.
     * Mirrors attendanceService.js getSessionAttendance().
     * Columns: id, name, register_no, status, scanned_at, distance_meters, attendance_status
     */
    @Query(value = """
           SELECT s.id, s.name, s.register_no,
                  ar.status, ar.scanned_at, ar.distance_meters,
                  CASE WHEN ar.id IS NOT NULL THEN 'present' ELSE 'absent' END AS attendance_status
           FROM students s
           JOIN classes c ON c.id = s.class_id
           JOIN attendance_sessions ase ON ase.id = :sessionId
           JOIN faculty_subjects fs ON fs.id = ase.faculty_subject_id AND fs.class_id = c.id
           LEFT JOIN attendance_records ar ON ar.session_id = ase.id AND ar.student_id = s.id
           ORDER BY s.register_no
           """, nativeQuery = true)
    List<Object[]> sessionAttendanceRoster(@Param("sessionId") Integer sessionId);

    /**
     * Per-student attendance % for a whole faculty_subject (subject report).
     * Mirrors attendanceService.js getSubjectAttendanceReport() students[] portion.
     */
    @Query(value = """
           SELECT s.id, s.name, s.register_no,
                  COUNT(ar.id)  AS present,
                  COUNT(ase.id) AS total,
                  ROUND(COUNT(ar.id)*100.0/NULLIF(COUNT(ase.id),0),2) AS percentage
           FROM attendance_sessions ase
           JOIN faculty_subjects fs ON fs.id = ase.faculty_subject_id
           JOIN classes c           ON c.id  = fs.class_id
           JOIN students s          ON s.class_id = c.id
           LEFT JOIN attendance_records ar ON ar.session_id = ase.id AND ar.student_id = s.id
           WHERE ase.faculty_subject_id = :facultySubjectId
           GROUP BY s.id, s.name, s.register_no
           ORDER BY s.register_no
           """, nativeQuery = true)
    List<Object[]> subjectReportStudents(@Param("facultySubjectId") Integer facultySubjectId);

    /**
     * Students below attendance threshold for a class.
     * Mirrors attendanceService.js getDefaulterList().
     */
    @Query(value = """
           SELECT s.id, s.name, s.register_no,
                  sub.code AS subject_code, sub.name AS subject_name,
                  COUNT(ase.id)  AS total_sessions,
                  COUNT(ar.id)   AS present_count,
                  ROUND(COUNT(ar.id)*100.0/NULLIF(COUNT(ase.id),0),2) AS percentage
           FROM students s
           JOIN classes c            ON c.id  = s.class_id
           JOIN faculty_subjects fs  ON fs.class_id = c.id
           JOIN subjects sub         ON sub.id = fs.subject_id
           JOIN attendance_sessions ase ON ase.faculty_subject_id = fs.id
           LEFT JOIN attendance_records ar ON ar.session_id = ase.id AND ar.student_id = s.id
           WHERE s.class_id = :classId
           GROUP BY s.id, s.name, s.register_no, sub.id, sub.code, sub.name
           HAVING percentage < :threshold OR percentage IS NULL
           ORDER BY percentage ASC
           """, nativeQuery = true)
    List<Object[]> defaulterList(@Param("classId") Integer classId, @Param("threshold") double threshold);

    /**
     * Full class attendance matrix with optional subject/date filters.
     * Mirrors reportController.js getClassReport().
     */
    @Query(value = """
           SELECT s.id, s.name, s.register_no,
                  sub.code AS subject_code, sub.name AS subject_name,
                  COUNT(ase.id)  AS total_sessions,
                  COUNT(ar.id)   AS present_count,
                  ROUND(COUNT(ar.id)*100.0/NULLIF(COUNT(ase.id),0),2) AS percentage
           FROM students s
           JOIN classes c            ON c.id = s.class_id
           JOIN faculty_subjects fs   ON fs.class_id = c.id
           JOIN subjects sub          ON sub.id = fs.subject_id
           JOIN attendance_sessions ase ON ase.faculty_subject_id = fs.id
           LEFT JOIN attendance_records ar ON ar.session_id = ase.id AND ar.student_id = s.id
           WHERE s.class_id = :classId
             AND (:subjectId IS NULL OR fs.subject_id = :subjectId)
             AND (:startDate IS NULL OR ase.session_date >= :startDate)
             AND (:endDate   IS NULL OR ase.session_date <= :endDate)
           GROUP BY s.id, s.name, s.register_no, sub.id, sub.code, sub.name
           ORDER BY s.register_no, sub.code
           """, nativeQuery = true)
    List<Object[]> classReportMatrix(@Param("classId") Integer classId,
                                       @Param("subjectId") Integer subjectId,
                                       @Param("startDate") java.time.LocalDate startDate,
                                       @Param("endDate") java.time.LocalDate endDate);

    /**
     * Department-wise average attendance percentage.
     * Mirrors reportController.js getAnalytics() deptStats portion.
     */
    @Query(value = """
           SELECT d.name AS department, ROUND(AVG(ar_sub.pct), 2) AS avg_attendance
           FROM departments d
           JOIN classes c    ON c.department_id = d.id
           JOIN students s   ON s.class_id = c.id
           JOIN (
             SELECT s2.id AS sid,
                    ROUND(COUNT(ar.id)*100.0/NULLIF(COUNT(ase.id),0),2) AS pct
             FROM students s2
             JOIN classes c2           ON c2.id = s2.class_id
             JOIN faculty_subjects fs  ON fs.class_id = c2.id
             JOIN attendance_sessions ase ON ase.faculty_subject_id = fs.id
             LEFT JOIN attendance_records ar ON ar.session_id = ase.id AND ar.student_id = s2.id
             GROUP BY s2.id
           ) ar_sub ON ar_sub.sid = s.id
           GROUP BY d.id, d.name
           ORDER BY avg_attendance DESC
           """, nativeQuery = true)
    List<Object[]> departmentWiseAverageAttendance();

    /**
     * Phase 3: month-wise attendance % per student for a class (monthly report).
     * Columns: student_id, name, register_no, month (YYYY-MM), total_sessions, present_count, percentage
     */
    @Query(value = """
           SELECT s.id, s.name, s.register_no,
                  DATE_FORMAT(ase.session_date, '%Y-%m') AS month,
                  COUNT(ase.id)  AS total_sessions,
                  COUNT(ar.id)   AS present_count,
                  ROUND(COUNT(ar.id)*100.0/NULLIF(COUNT(ase.id),0),2) AS percentage
           FROM students s
           JOIN classes c            ON c.id  = s.class_id
           JOIN faculty_subjects fs  ON fs.class_id = c.id
           JOIN attendance_sessions ase ON ase.faculty_subject_id = fs.id
           LEFT JOIN attendance_records ar ON ar.session_id = ase.id AND ar.student_id = s.id
           WHERE s.class_id = :classId
             AND (:year IS NULL OR YEAR(ase.session_date) = :year)
             AND (:month IS NULL OR MONTH(ase.session_date) = :month)
           GROUP BY s.id, s.name, s.register_no, month
           ORDER BY month DESC, s.register_no
           """, nativeQuery = true)
    List<Object[]> monthlyClassReport(@Param("classId") Integer classId,
                                       @Param("year") Integer year,
                                       @Param("month") Integer month);

    /**
     * Phase 3: one student's month-wise attendance history (all subjects combined per month).
     */
    @Query(value = """
           SELECT DATE_FORMAT(ase.session_date, '%Y-%m') AS month,
                  COUNT(ase.id)  AS total_sessions,
                  COUNT(ar.id)   AS present_count,
                  ROUND(COUNT(ar.id)*100.0/NULLIF(COUNT(ase.id),0),2) AS percentage
           FROM attendance_sessions ase
           JOIN faculty_subjects fs ON fs.id = ase.faculty_subject_id
           JOIN classes c           ON c.id = fs.class_id
           JOIN students s          ON s.class_id = c.id AND s.id = :studentId
           LEFT JOIN attendance_records ar ON ar.session_id = ase.id AND ar.student_id = s.id
           GROUP BY month
           ORDER BY month DESC
           """, nativeQuery = true)
    List<Object[]> studentMonthlyReport(@Param("studentId") Integer studentId);

    /**
     * Phase 3: students of a session's class who have NOT yet been given any
     * attendance_records row for that session — used by the auto-close sweep
     * job to mark them absent once the QR window has expired.
     */
    @Query(value = """
           SELECT s.id
           FROM students s
           JOIN attendance_sessions ase ON ase.id = :sessionId
           JOIN faculty_subjects fs ON fs.id = ase.faculty_subject_id AND fs.class_id = s.class_id
           LEFT JOIN attendance_records ar ON ar.session_id = ase.id AND ar.student_id = s.id
           WHERE ar.id IS NULL
           """, nativeQuery = true)
    List<Integer> studentIdsWithoutRecordForSession(@Param("sessionId") Integer sessionId);

    /** Today's global stats — sessions, total present, unique students who scanned. */
    @Query(value = """
           SELECT COUNT(DISTINCT ase.id) AS sessions,
                  COALESCE(SUM(ase.present_count), 0) AS total_present,
                  COUNT(DISTINCT ar.student_id) AS unique_students
           FROM attendance_sessions ase
           LEFT JOIN attendance_records ar ON ar.session_id = ase.id
           WHERE ase.session_date = CURDATE()
           """, nativeQuery = true)
    List<Object[]> todayGlobalStats();

    @Query(value = """
           SELECT s.id AS studentId, s.name, s.register_no AS registerNo,
                  sub.code AS subjectCode, sub.name AS subjectName,
                  CASE WHEN ar.id IS NOT NULL THEN ar.status ELSE 'absent' END AS status,
                  ar.scanned_at AS scannedAt
           FROM students s
           JOIN classes c ON c.id = s.class_id
           JOIN faculty_subjects fs ON fs.class_id = c.id
           JOIN subjects sub ON sub.id = fs.subject_id
           JOIN attendance_sessions ase ON ase.faculty_subject_id = fs.id
           LEFT JOIN attendance_records ar ON ar.session_id = ase.id AND ar.student_id = s.id
           WHERE c.id = :classId AND ase.session_date = :date
           ORDER BY s.register_no, sub.code
           """, nativeQuery = true)
    List<Object[]> dailyClassReport(@Param("classId") Integer classId, @Param("date") java.time.LocalDate date);

    @Query(value = """
           SELECT s.id AS studentId, s.name, s.register_no AS registerNo,
                  sub.code AS subjectCode, sub.name AS subjectName,
                  COUNT(ase.id) AS totalSessions,
                  COUNT(CASE WHEN ar.status = 'present' OR ar.status = 'late' THEN ar.id END) AS presentCount,
                  ROUND(COUNT(CASE WHEN ar.status = 'present' OR ar.status = 'late' THEN ar.id END)*100.0/NULLIF(COUNT(ase.id),0),2) AS percentage
           FROM students s
           JOIN classes c ON c.id = s.class_id
           JOIN faculty_subjects fs ON fs.class_id = c.id
           JOIN subjects sub ON sub.id = fs.subject_id
           JOIN attendance_sessions ase ON ase.faculty_subject_id = fs.id
           LEFT JOIN attendance_records ar ON ar.session_id = ase.id AND ar.student_id = s.id
           WHERE c.id = :classId
             AND (:startDate IS NULL OR ase.session_date >= :startDate)
             AND (:endDate IS NULL OR ase.session_date <= :endDate)
           GROUP BY s.id, s.name, s.register_no, sub.id, sub.code, sub.name
           ORDER BY s.register_no, sub.code
           """, nativeQuery = true)
    List<Object[]> semesterReport(@Param("classId") Integer classId,
                                  @Param("startDate") java.time.LocalDate startDate,
                                  @Param("endDate") java.time.LocalDate endDate);

    @Query(value = """
           SELECT fs.id AS assignmentId, sub.code AS subjectCode, sub.name AS subjectName,
                  c.name AS className, c.section AS classSection,
                  COUNT(DISTINCT ase.id) AS totalSessions,
                  COUNT(CASE WHEN ar.status = 'present' OR ar.status = 'late' THEN ar.id END) AS presentCount,
                  (COUNT(DISTINCT ase.id) * (SELECT COUNT(st.id) FROM students st WHERE st.class_id = c.id)) AS totalEligibleSlots,
                  ROUND(COUNT(CASE WHEN ar.status = 'present' OR ar.status = 'late' THEN ar.id END) * 100.0 /
                        NULLIF(COUNT(DISTINCT ase.id) * (SELECT COUNT(st.id) FROM students st WHERE st.class_id = c.id), 0), 2) AS percentage
           FROM faculty_subjects fs
           JOIN subjects sub ON sub.id = fs.subject_id
           JOIN classes c ON c.id = fs.class_id
           LEFT JOIN attendance_sessions ase ON ase.faculty_subject_id = fs.id
           LEFT JOIN attendance_records ar ON ar.session_id = ase.id
           WHERE fs.faculty_id = :facultyId
           GROUP BY fs.id, sub.id, sub.code, sub.name, c.id, c.name, c.section
           ORDER BY sub.name, c.name
           """, nativeQuery = true)
    List<Object[]> facultyReport(@Param("facultyId") Integer facultyId);
}


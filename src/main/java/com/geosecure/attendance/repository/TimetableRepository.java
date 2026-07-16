package com.geosecure.attendance.repository;

import com.geosecure.attendance.entity.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Replaces raw SQL in:
 *  - facultyController.js getDashboard()  (today's schedule by day_of_week)
 *  - facultyController.js getTimetable()  (full weekly schedule, FIELD() ordering)
 *  - facultyController.js generateQR()    (slot ownership verification)
 *  - adminController.js getTimetable()    (optional class_id filter)
 */
public interface TimetableRepository extends JpaRepository<Timetable, Integer> {

    @Query("""
           SELECT t FROM Timetable t
           JOIN FETCH t.faculty f
           JOIN FETCH t.subject
           JOIN FETCH t.classEntity
           LEFT JOIN FETCH t.facultySubject
           WHERE f.id = :facultyId AND t.dayOfWeek = :day
           ORDER BY t.sessionNumber ASC
           """)
    List<Timetable> findTodayScheduleForFaculty(@Param("facultyId") Integer facultyId,
                                                  @Param("day") Timetable.DayOfWeek day);

    /**
     * Note: ordering by day-of-week (Mon..Sat) is done in the service layer via
     * Comparator on DayOfWeek.ordinal() — the enum is declared in that exact
     * order, so this avoids fragile JPQL enum CASE expressions. This query only
     * fetches and pre-orders by period number within whatever day grouping the
     * service applies.
     */
    @Query("""
           SELECT t FROM Timetable t
           JOIN FETCH t.faculty f
           JOIN FETCH t.subject
           JOIN FETCH t.classEntity
           LEFT JOIN FETCH t.facultySubject
           WHERE f.id = :facultyId
           ORDER BY t.sessionNumber ASC
           """)
    List<Timetable> findFullWeekForFaculty(@Param("facultyId") Integer facultyId);

    /** Ownership verification used by POST /faculty/generate-qr */
    @Query("""
           SELECT t FROM Timetable t
           JOIN FETCH t.faculty f
           JOIN FETCH t.subject
           JOIN FETCH t.classEntity
           LEFT JOIN FETCH t.facultySubject
           WHERE t.id = :timetableId AND f.id = :facultyId
           """)
    Optional<Timetable> findByIdAndFacultyId(@Param("timetableId") Integer timetableId,
                                               @Param("facultyId") Integer facultyId);

    @Query("""
           SELECT t FROM Timetable t
           JOIN FETCH t.faculty f
           JOIN FETCH t.subject
           JOIN FETCH t.classEntity c
           LEFT JOIN FETCH t.facultySubject
           WHERE (:classId IS NULL OR c.id = :classId)
           """)
    List<Timetable> findAllForAdmin(@Param("classId") Integer classId);

    boolean existsByClassEntity_IdAndDayOfWeekAndSessionNumber(
            Integer classId, Timetable.DayOfWeek day, Integer sessionNumber);

    /** Class conflict check, excluding a given slot (used on update). */
    boolean existsByClassEntity_IdAndDayOfWeekAndSessionNumberAndIdNot(
            Integer classId, Timetable.DayOfWeek day, Integer sessionNumber, Integer excludeId);

    /** Faculty conflict: same faculty already teaching another class in this slot. */
    boolean existsByFaculty_IdAndDayOfWeekAndSessionNumber(
            Integer facultyId, Timetable.DayOfWeek day, Integer sessionNumber);

    boolean existsByFaculty_IdAndDayOfWeekAndSessionNumberAndIdNot(
            Integer facultyId, Timetable.DayOfWeek day, Integer sessionNumber, Integer excludeId);

    /** Room conflict: same room already booked in this slot (only checked when a room is provided). */
    boolean existsByRoomNumberAndDayOfWeekAndSessionNumber(
            String roomNumber, Timetable.DayOfWeek day, Integer sessionNumber);

    boolean existsByRoomNumberAndDayOfWeekAndSessionNumberAndIdNot(
            String roomNumber, Timetable.DayOfWeek day, Integer sessionNumber, Integer excludeId);
}

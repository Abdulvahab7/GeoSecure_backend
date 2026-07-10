package com.geosecure.attendance.repository;

import com.geosecure.attendance.entity.FacultySubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Mirrors the query previously embedded in the original faculty dashboard logic getSubjects():
 *   SELECT fs.id AS faculty_subject_id, sub.*, c.*
 *   FROM faculty_subjects fs JOIN subjects sub ... JOIN classes c ...
 *   WHERE fs.faculty_id = ? ORDER BY sub.name
 *
 * Also replaces the ownership check used in getAttendanceReport():
 *   SELECT id FROM faculty_subjects WHERE id = ? AND faculty_id = ?
 */
public interface FacultySubjectRepository extends JpaRepository<FacultySubject, Integer> {

    @Query("""
           SELECT fs FROM FacultySubject fs
           JOIN FETCH fs.subject s
           JOIN FETCH fs.classEntity c
           WHERE fs.faculty.id = :facultyId
           ORDER BY s.name ASC
           """)
    List<FacultySubject> findByFacultyIdWithSubjectAndClass(@Param("facultyId") Integer facultyId);

    /** Ownership check: does this faculty_subject_id belong to this faculty? */
    Optional<FacultySubject> findByIdAndFaculty_Id(Integer id, Integer facultyId);

    boolean existsByFaculty_IdAndSubject_IdAndClassEntity_IdAndAcademicYear(
            Integer facultyId, Integer subjectId, Integer classId, String academicYear);
}

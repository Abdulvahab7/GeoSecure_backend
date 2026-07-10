package com.geosecure.attendance.repository;

import com.geosecure.attendance.entity.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Mirrors the query previously embedded in the original admin reporting logic getClasses():
 *   SELECT c.*, d.name AS department_name,
 *          (SELECT COUNT(*) FROM students s WHERE s.class_id = c.id) AS student_count
 *   FROM classes c JOIN departments d ON d.id = c.department_id
 *   ORDER BY d.name, c.semester, c.section
 */
public interface ClassRepository extends JpaRepository<ClassEntity, Integer> {

    @Query("""
           SELECT c FROM ClassEntity c
           JOIN FETCH c.department d
           ORDER BY d.name ASC, c.semester ASC, c.section ASC
           """)
    List<ClassEntity> findAllWithDepartmentOrdered();

    @Query("SELECT COUNT(s) FROM Student s WHERE s.classEntity.id = :classId")
    long countStudentsInClass(Integer classId);

    @Query("""
           SELECT c FROM ClassEntity c
           JOIN FETCH c.department
           LEFT JOIN FETCH c.coordinatorFaculty
           WHERE c.coordinatorFaculty.id = :facultyId
           ORDER BY c.semester ASC, c.section ASC
           """)
    List<ClassEntity> findCoordinatedByFacultyId(@Param("facultyId") Integer facultyId);

    boolean existsByIdAndCoordinatorFaculty_Id(Integer classId, Integer facultyId);
}

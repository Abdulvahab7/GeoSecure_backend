package com.geosecure.attendance.repository;

import com.geosecure.attendance.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Mirrors the query previously embedded in the original student profile logic getStudentProfile():
 *   SELECT s.*, c.name AS class_name, c.semester, c.section, d.name AS department_name
 *   FROM students s JOIN classes c ON c.id = s.class_id JOIN departments d ON d.id = c.department_id
 *   WHERE s.user_id = ?
 */
public interface StudentRepository extends JpaRepository<Student, Integer> {

    @Query("""
           SELECT s FROM Student s
           JOIN FETCH s.classEntity c
           JOIN FETCH c.department d
           WHERE s.user.id = :userId
           """)
    Optional<Student> findByUserIdWithClassAndDepartment(@Param("userId") Integer userId);

    Optional<Student> findByRegisterNo(String registerNo);

    boolean existsByRegisterNo(String registerNo);

    List<Student> findByClassEntity_Id(Integer classId);

    @Query("""
           SELECT s FROM Student s
           JOIN FETCH s.classEntity c
           JOIN FETCH c.department
           WHERE s.mentorFaculty.id = :facultyId
           ORDER BY c.name ASC, s.name ASC
           """)
    List<Student> findByMentorFacultyIdWithClassAndDepartment(@Param("facultyId") Integer facultyId);

    boolean existsByClassEntity_IdAndMentorFaculty_Id(Integer classId, Integer facultyId);
}

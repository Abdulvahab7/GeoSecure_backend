package com.geosecure.attendance.repository;

import com.geosecure.attendance.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Mirrors the query previously embedded in the original admin reporting logic getSubjects():
 *   SELECT sub.*, d.name AS department_name FROM subjects sub
 *   JOIN departments d ON d.id = sub.department_id ORDER BY sub.code
 */
public interface SubjectRepository extends JpaRepository<Subject, Integer> {

    Optional<Subject> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT s FROM Subject s JOIN FETCH s.department ORDER BY s.code ASC")
    List<Subject> findAllWithDepartmentOrderByCode();
}

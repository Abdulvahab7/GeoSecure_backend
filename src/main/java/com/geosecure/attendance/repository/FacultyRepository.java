package com.geosecure.attendance.repository;

import com.geosecure.attendance.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Mirrors the query previously embedded in the original faculty dashboard logic getFacultyProfile():
 *   SELECT * FROM faculty WHERE user_id = ?
 * and the original reporting logic getFacultyByUserId().
 */
public interface FacultyRepository extends JpaRepository<Faculty, Integer> {

    Optional<Faculty> findByUser_Id(Integer userId);

    Optional<Faculty> findByEmployeeId(String employeeId);

    boolean existsByEmployeeId(String employeeId);

    @Query("SELECT f FROM Faculty f JOIN FETCH f.department WHERE f.user.id = :userId")
    Optional<Faculty> findByUserIdWithDepartment(@Param("userId") Integer userId);

    @Query("SELECT f FROM Faculty f JOIN FETCH f.department ORDER BY f.name ASC")
    List<Faculty> findAllWithDepartment();
}
